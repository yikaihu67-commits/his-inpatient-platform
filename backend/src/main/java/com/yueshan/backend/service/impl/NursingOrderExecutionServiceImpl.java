package com.yueshan.backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.MedicalOrder;
import com.yueshan.backend.domain.NursingOrderExecution;
import com.yueshan.backend.dto.NursingExecutionActionRequest;
import com.yueshan.backend.dto.NursingExecutionCreateRequest;
import com.yueshan.backend.dto.NursingExecutionQueryRequest;
import com.yueshan.backend.dto.NursingExecutionUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.MedicalOrderMapper;
import com.yueshan.backend.mapper.NursingOrderExecutionMapper;
import com.yueshan.backend.service.NursingOrderExecutionService;

@Service
public class NursingOrderExecutionServiceImpl implements NursingOrderExecutionService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> ALLOWED_QUERY_STATUS = Set.of("PENDING", "EXECUTED", "FAILED", "CANCELLED");
    private final NursingOrderExecutionMapper mapper;
    private final MedicalOrderMapper orderMapper;
    private final AdmissionMapper admissionMapper;
    private final JdbcTemplate jdbcTemplate;

    public NursingOrderExecutionServiceImpl(NursingOrderExecutionMapper mapper, MedicalOrderMapper orderMapper,
            AdmissionMapper admissionMapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.orderMapper = orderMapper;
        this.admissionMapper = admissionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<NursingOrderExecution> findPage(NursingExecutionQueryRequest query) {
        NursingExecutionQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<NursingOrderExecution> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<NursingOrderExecution> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public NursingOrderExecution create(NursingExecutionCreateRequest request) {
        MedicalOrder order = requireExecutableOrder(request.getOrderId());
        NursingOrderExecution execution = new NursingOrderExecution();
        execution.setExecutionNo(generateExecutionNo());
        execution.setOrderId(order.getId());
        execution.setAdmissionId(order.getAdmissionId());
        execution.setPatientId(order.getPatientId());
        execution.setScheduledTime(request.getScheduledTime() == null ? LocalDateTime.now() : request.getScheduledTime());
        execution.setNurseName(trimToNull(request.getNurseName()));
        execution.setStatus("PENDING");
        execution.setRemark(trimToNull(request.getRemark()));
        execution.setDeleted(false);
        mapper.insert(execution);
        return mapper.findById(execution.getId());
    }

    @Transactional
    public Optional<NursingOrderExecution> update(Long id, NursingExecutionUpdateRequest request) {
        NursingOrderExecution existing = mapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (!"PENDING".equals(existing.getStatus())) {
            throw new BusinessException("只有待执行记录允许修改");
        }
        existing.setScheduledTime(request.getScheduledTime() == null ? existing.getScheduledTime() : request.getScheduledTime());
        existing.setNurseName(trimToNull(request.getNurseName()));
        existing.setResult(trimToNull(request.getResult()));
        existing.setRemark(trimToNull(request.getRemark()));
        mapper.update(existing);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public boolean deleteById(Long id) {
        NursingOrderExecution existing = mapper.findById(id);
        if (existing == null) {
            return false;
        }
        if (!"PENDING".equals(existing.getStatus())) {
            throw new BusinessException("只有待执行记录允许删除");
        }
        return mapper.logicDeleteById(id) > 0;
    }

    @Transactional
    public NursingOrderExecution execute(Long id, NursingExecutionActionRequest request) {
        NursingOrderExecution existing = requireExecution(id);
        requirePending(existing, "已执行的护士执行记录不能重复执行");
        requireExecutableOrder(existing.getOrderId());
        mapper.updateStatus(id, "EXECUTED", LocalDateTime.now(), nurse(request), result(request, "执行成功"), remark(request));
        orderMapper.updateStatus(existing.getOrderId(), "EXECUTED", nurse(request), remark(request));
        insertNursingRecord(existing, request);
        return mapper.findById(id);
    }

    @Transactional
    public NursingOrderExecution fail(Long id, NursingExecutionActionRequest request) {
        NursingOrderExecution existing = requireExecution(id);
        requirePending(existing, "只有待执行记录可以标记失败");
        requireExecutableOrder(existing.getOrderId());
        mapper.updateStatus(id, "FAILED", LocalDateTime.now(), nurse(request), result(request, "执行失败"), remark(request));
        return mapper.findById(id);
    }

    @Transactional
    public NursingOrderExecution cancel(Long id, NursingExecutionActionRequest request) {
        NursingOrderExecution existing = requireExecution(id);
        requirePending(existing, "只有待执行记录可以取消执行");
        mapper.updateStatus(id, "CANCELLED", null, nurse(request), result(request, null), remark(request));
        return mapper.findById(id);
    }

    private MedicalOrder requireExecutableOrder(Long orderId) {
        MedicalOrder order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("医嘱不存在");
        }
        if (!Set.of("SUBMITTED", "CHECKED").contains(order.getStatus())) {
            if (Set.of("STOPPED", "CANCELLED").contains(order.getStatus())) {
                throw new BusinessException("已停止或已作废医嘱不能继续执行");
            }
            if ("EXECUTED".equals(order.getStatus()) || "BILLED".equals(order.getStatus())) {
                throw new BusinessException("已执行或已计费医嘱不能重复执行");
            }
            throw new BusinessException("只有已提交或已核对医嘱可以创建或执行护理记录");
        }
        var admission = admissionMapper.findById(order.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!"IN_HOSPITAL".equals(admission.getStatus())) {
            throw new BusinessException("已出院或非在院患者不能再执行医嘱");
        }
        return order;
    }

    private NursingOrderExecution requireExecution(Long id) {
        NursingOrderExecution execution = mapper.findById(id);
        if (execution == null) {
            throw new BusinessException("护士执行记录不存在");
        }
        return execution;
    }

    private void requirePending(NursingOrderExecution execution, String message) {
        if (!"PENDING".equals(execution.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private NursingExecutionQueryRequest normalizeQuery(NursingExecutionQueryRequest query) {
        NursingExecutionQueryRequest q = new NursingExecutionQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setOrderId(query.getOrderId());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        if (value == null) return null;
        value = value.toUpperCase();
        if (!ALLOWED_QUERY_STATUS.contains(value)) throw new BusinessException("执行状态不正确");
        return value;
    }

    private String generateExecutionNo() {
        for (int i = 0; i < 5; i++) {
            String no = "ZX" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByExecutionNo(no) == 0) return no;
        }
        throw new BusinessException("执行编号生成失败，请重试");
    }

    private void insertNursingRecord(NursingOrderExecution execution, NursingExecutionActionRequest request) {
        MedicalOrder order = orderMapper.findById(execution.getOrderId());
        if (order == null) {
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO nursing_record (
                  record_no, order_id, admission_id, patient_id, nursing_type, nursing_content,
                  nurse_name, record_time, status, billable, nursing_fee, billed, remark,
                  deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'ORDER_EXECUTION', ?, ?, NOW(), 'EXECUTED', false, 0, false, ?, false, NOW(), NOW())
                """,
                "HL" + LocalDateTime.now().format(FORMATTER),
                order.getId(),
                order.getAdmissionId(),
                order.getPatientId(),
                order.getItemName(),
                nurse(request),
                remark(request));
    }

    private String nurse(NursingExecutionActionRequest request) { return request == null ? null : trimToNull(request.getNurseName()); }
    private String result(NursingExecutionActionRequest request, String fallback) {
        String result = request == null ? null : trimToNull(request.getResult());
        return result == null ? fallback : result;
    }
    private String remark(NursingExecutionActionRequest request) { return request == null ? null : trimToNull(request.getRemark()); }
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
