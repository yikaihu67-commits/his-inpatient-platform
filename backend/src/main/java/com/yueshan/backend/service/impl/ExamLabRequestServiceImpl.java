package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.ExamLabRequest;
import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.ExamLabRequestActionRequest;
import com.yueshan.backend.dto.ExamLabRequestCreateRequest;
import com.yueshan.backend.dto.ExamLabRequestQueryRequest;
import com.yueshan.backend.dto.ExamLabRequestUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.ExamLabRequestMapper;
import com.yueshan.backend.service.ExamLabRequestService;

@Service
public class ExamLabRequestServiceImpl implements ExamLabRequestService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> REQUEST_TYPES = Set.of("EXAM", "LAB");
    private static final Set<String> ITEM_CATEGORIES = Set.of("IMAGING", "ULTRASOUND", "ECG", "BLOOD", "URINE", "BIOCHEMISTRY", "OTHER");
    private static final Set<String> STATUSES = Set.of("DRAFT", "REQUESTED", "SUBMITTED", "SCHEDULED", "COMPLETED", "REPORTED", "BILLED", "CANCELLED");

    private final ExamLabRequestMapper mapper;
    private final AdmissionMapper admissionMapper;
    private final JdbcTemplate jdbcTemplate;

    public ExamLabRequestServiceImpl(ExamLabRequestMapper mapper, AdmissionMapper admissionMapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public ExamLabRequest create(ExamLabRequestCreateRequest request) {
        ExamLabRequest item = toRequest(request, null);
        validateAdmission(item.getAdmissionId(), item.getPatientId());
        mapper.insert(item);
        return mapper.findById(item.getId());
    }

    @Override
    public PageResponse<ExamLabRequest> findPage(ExamLabRequestQueryRequest query) {
        ExamLabRequestQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<ExamLabRequest> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<ExamLabRequest> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    @Transactional
    public Optional<ExamLabRequest> update(Long id, ExamLabRequestUpdateRequest request) {
        ExamLabRequest existing = mapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (!Set.of("DRAFT", "REQUESTED").contains(existing.getStatus())) {
            throw new BusinessException("只有草稿或已申请的检查检验允许修改");
        }
        ExamLabRequest item = toRequest(request, existing);
        item.setId(id);
        mapper.update(item);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        ExamLabRequest existing = mapper.findById(id);
        if (existing == null) {
            return false;
        }
        if (!Set.of("DRAFT", "REQUESTED").contains(existing.getStatus())) {
            throw new BusinessException("只有草稿或已申请的检查检验允许删除");
        }
        return mapper.logicDeleteById(id) > 0;
    }

    @Override
    @Transactional
    public ExamLabRequest submit(Long id, ExamLabRequestActionRequest request) {
        ExamLabRequest existing = requireRequest(id);
        if (!Set.of("DRAFT", "REQUESTED").contains(existing.getStatus())) {
            throw new BusinessException("只有草稿或已申请检查检验可以提交");
        }
        return updateStatus(id, "REQUESTED", request);
    }

    @Override
    @Transactional
    public ExamLabRequest schedule(Long id, ExamLabRequestActionRequest request) {
        ExamLabRequest existing = requireRequest(id);
        if (!Set.of("REQUESTED", "SUBMITTED").contains(existing.getStatus())) {
            throw new BusinessException("只有已申请检查检验可以安排");
        }
        return updateStatus(id, "SCHEDULED", request);
    }

    @Override
    @Transactional
    public ExamLabRequest complete(Long id, ExamLabRequestActionRequest request) {
        ExamLabRequest existing = requireRequest(id);
        if (!Set.of("REQUESTED", "SUBMITTED", "SCHEDULED").contains(existing.getStatus())) {
            throw new BusinessException("只有已申请或已安排检查检验可以完成");
        }
        return updateStatus(id, "COMPLETED", request);
    }

    @Override
    @Transactional
    public ExamLabRequest report(Long id, ExamLabRequestActionRequest request) {
        ExamLabRequest existing = requireRequest(id);
        if (!"COMPLETED".equals(existing.getStatus())) {
            throw new BusinessException("未完成检查检验不能出报告");
        }
        return updateStatus(id, "REPORTED", request);
    }

    @Override
    @Transactional
    public ExamLabRequest bill(Long id, ExamLabRequestActionRequest request) {
        ExamLabRequest existing = requireRequest(id);
        if ("CANCELLED".equals(existing.getStatus())) {
            throw new BusinessException("已取消申请不能计费");
        }
        if (Boolean.TRUE.equals(existing.getBilled()) || "BILLED".equals(existing.getStatus())) {
            throw new BusinessException("该检查检验已计费，不能重复计费");
        }
        if (!Set.of("COMPLETED", "REPORTED").contains(existing.getStatus())) {
            throw new BusinessException("检查检验完成或出报告后才能计费");
        }
        Integer feeCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM inpatient_fee
                WHERE deleted = false AND source_type = 'EXAM_LAB' AND source_id = ?
                """, Integer.class, id);
        if (feeCount != null && feeCount > 0) {
            throw new BusinessException("该检查检验已生成费用，不能重复计费");
        }
        BigDecimal quantity = money(request == null || request.getQuantity() == null ? existing.getQuantity() : request.getQuantity(), BigDecimal.ONE);
        BigDecimal unitPrice = money(request == null || request.getUnitPrice() == null ? existing.getUnitPrice() : request.getUnitPrice(), BigDecimal.ZERO);
        BigDecimal amount = money(unitPrice.multiply(quantity), BigDecimal.ZERO);
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'EXAM_LAB', ?, ?, ?, ?, ?, '次', ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """,
                "FEE-EL" + LocalDateTime.now().format(FORMATTER),
                existing.getAdmissionId(), existing.getPatientId(), existing.getId(), existing.getItemCode(), existing.getItemName(),
                existing.getRequestType(), quantity, unitPrice, amount, remark(request));
        mapper.markBilled(id);
        return requireRequest(id);
    }

    @Override
    @Transactional
    public ExamLabRequest cancel(Long id, ExamLabRequestActionRequest request) {
        ExamLabRequest existing = requireRequest(id);
        if (Set.of("COMPLETED", "REPORTED", "BILLED", "CANCELLED").contains(existing.getStatus())) {
            throw new BusinessException("已完成、已报告、已计费或已取消申请不能取消");
        }
        return updateStatus(id, "CANCELLED", request);
    }

    private ExamLabRequest updateStatus(Long id, String status, ExamLabRequestActionRequest request) {
        mapper.updateStatus(id, status,
                request == null ? null : request.getScheduledTime(),
                text(request == null ? null : request.getExecutionDepartment()),
                text(request == null ? null : request.getExecutorName()),
                request == null || request.getExecutedTime() == null ? ("COMPLETED".equals(status) ? LocalDateTime.now() : null) : request.getExecutedTime(),
                text(request == null ? null : request.getReportDoctorName()),
                request == null || request.getReportTime() == null ? ("REPORTED".equals(status) ? LocalDateTime.now() : null) : request.getReportTime(),
                text(request == null ? null : request.getResultSummary()),
                text(request == null ? null : request.getResultDetail()),
                request == null ? null : request.getAbnormalFlag(),
                remark(request));
        return requireRequest(id);
    }

    private ExamLabRequest toRequest(ExamLabRequestCreateRequest request, ExamLabRequest existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        ExamLabRequest item = new ExamLabRequest();
        item.setRequestNo(resolveRequestNo(request.getRequestNo(), existing));
        item.setAdmissionId(request.getAdmissionId());
        item.setPatientId(request.getPatientId());
        item.setRequestType(normalizeRequestType(request.getRequestType()));
        item.setItemCode(text(request.getItemCode()));
        item.setItemName(requireText(request.getItemName(), "项目名称不能为空"));
        item.setRequestContent(firstNotBlank(request.getRequestContent(), request.getItemName()));
        item.setItemCategory(normalizeItemCategory(request.getItemCategory()));
        item.setDoctorName(requireText(request.getDoctorName(), "申请医生不能为空"));
        item.setRequestTime(request.getRequestTime() == null ? LocalDateTime.now() : request.getRequestTime());
        item.setScheduledTime(request.getScheduledTime());
        item.setExecutionDepartment(text(request.getExecutionDepartment()));
        item.setStatus(normalizeStatus(request.getStatus(), existing == null ? "REQUESTED" : existing.getStatus()));
        item.setUnitPrice(money(request.getUnitPrice(), BigDecimal.ZERO));
        item.setQuantity(money(request.getQuantity(), BigDecimal.ONE));
        item.setTotalAmount(item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP));
        item.setBilled(existing != null && Boolean.TRUE.equals(existing.getBilled()));
        item.setAbnormalFlag(false);
        item.setRemark(text(request.getRemark()));
        item.setDeleted(false);
        return item;
    }

    private void validateAdmission(Long admissionId, Long patientId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!"IN_HOSPITAL".equals(admission.getStatus())) {
            throw new BusinessException("只有在院患者可以创建检查检验申请");
        }
        if (!admission.getPatientId().equals(patientId)) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
    }

    private ExamLabRequest requireRequest(Long id) {
        ExamLabRequest item = mapper.findById(id);
        if (item == null) {
            throw new BusinessException("检查检验申请不存在");
        }
        return item;
    }

    private ExamLabRequestQueryRequest normalizeQuery(ExamLabRequestQueryRequest query) {
        ExamLabRequestQueryRequest q = new ExamLabRequestQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setRequestType(normalizeRequestTypeOrNull(query.getRequestType()));
        q.setItemCategory(normalizeItemCategoryOrNull(query.getItemCategory()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        q.setItemName(text(query.getItemName()));
        return q;
    }

    private String resolveRequestNo(String requestNo, ExamLabRequest existing) {
        String value = text(requestNo);
        if (value == null) {
            if (existing != null) {
                return existing.getRequestNo();
            }
            return generateRequestNo();
        }
        boolean duplicated = existing == null
                ? mapper.countByRequestNo(value) > 0
                : mapper.countByRequestNoExcludeId(value, existing.getId()) > 0;
        if (duplicated) {
            throw new BusinessException("申请单号已存在");
        }
        return value;
    }

    private String generateRequestNo() {
        for (int i = 0; i < 5; i++) {
            String no = "JY" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByRequestNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("申请单号生成失败，请重试");
    }

    private String normalizeRequestType(String value) {
        String normalized = requireText(value, "申请类型不能为空").toUpperCase();
        if (!REQUEST_TYPES.contains(normalized)) {
            throw new BusinessException("申请类型只能是 EXAM、LAB");
        }
        return normalized;
    }

    private String normalizeRequestTypeOrNull(String value) {
        String normalized = text(value);
        return normalized == null ? null : normalizeRequestType(normalized);
    }

    private String normalizeItemCategory(String value) {
        String normalized = requireText(value, "项目类别不能为空").toUpperCase();
        if (!ITEM_CATEGORIES.contains(normalized)) {
            throw new BusinessException("项目类别不正确");
        }
        return normalized;
    }

    private String normalizeItemCategoryOrNull(String value) {
        String normalized = text(value);
        return normalized == null ? null : normalizeItemCategory(normalized);
    }

    private String normalizeStatus(String value, String fallback) {
        String normalized = text(value);
        normalized = normalized == null ? fallback : normalized.toUpperCase();
        if (!STATUSES.contains(normalized)) {
            throw new BusinessException("申请状态不正确");
        }
        return normalized;
    }

    private String normalizeStatusOrNull(String value) {
        String normalized = text(value);
        return normalized == null ? null : normalizeStatus(normalized, "REQUESTED");
    }

    private String remark(ExamLabRequestActionRequest request) {
        return request == null ? null : text(request.getRemark());
    }

    private String requireText(String value, String message) {
        String trimmed = text(value);
        if (trimmed == null) {
            throw new BusinessException(message);
        }
        return trimmed;
    }

    private String firstNotBlank(String value, String fallback) {
        String trimmed = text(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String text(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal money(BigDecimal value, BigDecimal fallback) {
        return (value == null ? fallback : value).setScale(2, RoundingMode.HALF_UP);
    }
}
