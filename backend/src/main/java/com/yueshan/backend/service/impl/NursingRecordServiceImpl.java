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

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.NursingRecord;
import com.yueshan.backend.dto.NursingRecordActionRequest;
import com.yueshan.backend.dto.NursingRecordCreateRequest;
import com.yueshan.backend.dto.NursingRecordQueryRequest;
import com.yueshan.backend.dto.NursingRecordUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.NursingRecordMapper;
import com.yueshan.backend.service.NursingRecordService;

@Service
public class NursingRecordServiceImpl implements NursingRecordService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> TYPES = Set.of("VITAL_SIGN", "DAILY_CARE", "ORDER_EXECUTION", "WOUND_CARE", "INFUSION", "OTHER");
    private static final Set<String> STATUSES = Set.of("RECORDED", "EXECUTED", "REVIEWED", "BILLED", "CANCELLED");

    private final NursingRecordMapper mapper;
    private final AdmissionMapper admissionMapper;
    private final JdbcTemplate jdbcTemplate;

    public NursingRecordServiceImpl(NursingRecordMapper mapper, AdmissionMapper admissionMapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResponse<NursingRecord> findPage(NursingRecordQueryRequest query) {
        NursingRecordQueryRequest q = normalizeQuery(query);
        int page = q.getPage() == null ? 1 : q.getPage();
        int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        long total = mapper.countPage(q);
        List<NursingRecord> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<NursingRecord> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    @Transactional
    public NursingRecord create(NursingRecordCreateRequest request) {
        NursingRecord item = toRecord(request, null);
        if (isBlank(item.getRecordNo())) {
            item.setRecordNo(generateRecordNo());
        }
        validateAdmission(item.getAdmissionId(), item.getPatientId());
        if (mapper.countByRecordNo(item.getRecordNo()) > 0) {
            throw new BusinessException("护理记录编号已存在");
        }
        mapper.insert(item);
        return requireRecord(item.getId());
    }

    @Override
    @Transactional
    public Optional<NursingRecord> update(Long id, NursingRecordUpdateRequest request) {
        NursingRecord existing = mapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (Set.of("BILLED", "CANCELLED").contains(existing.getStatus())) {
            throw new BusinessException("已计费或已取消护理记录不能修改");
        }
        NursingRecord item = toRecord(request, existing);
        item.setId(id);
        mapper.update(item);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    @Transactional
    public NursingRecord execute(Long id, NursingRecordActionRequest request) {
        NursingRecord existing = requireRecord(id);
        if (!Set.of("RECORDED", "REVIEWED").contains(existing.getStatus())) {
            throw new BusinessException("只有已记录或已查看护理记录可以执行");
        }
        mapper.updateStatus(id, "EXECUTED", nurse(request), remark(request));
        return requireRecord(id);
    }

    @Override
    @Transactional
    public NursingRecord review(Long id, NursingRecordActionRequest request) {
        NursingRecord existing = requireRecord(id);
        if ("CANCELLED".equals(existing.getStatus())) {
            throw new BusinessException("已取消护理记录不能查看确认");
        }
        mapper.updateStatus(id, "REVIEWED", nurse(request), remark(request));
        return requireRecord(id);
    }

    @Override
    @Transactional
    public NursingRecord cancel(Long id, NursingRecordActionRequest request) {
        NursingRecord existing = requireRecord(id);
        if ("BILLED".equals(existing.getStatus())) {
            throw new BusinessException("已计费护理记录不能取消");
        }
        if ("CANCELLED".equals(existing.getStatus())) {
            throw new BusinessException("护理记录已取消");
        }
        mapper.updateStatus(id, "CANCELLED", nurse(request), remark(request));
        return requireRecord(id);
    }

    @Override
    @Transactional
    public NursingRecord bill(Long id, NursingRecordActionRequest request) {
        NursingRecord existing = requireRecord(id);
        if ("CANCELLED".equals(existing.getStatus())) {
            throw new BusinessException("已取消护理记录不能计费");
        }
        if (Boolean.TRUE.equals(existing.getBilled()) || "BILLED".equals(existing.getStatus())) {
            throw new BusinessException("护理记录已计费，不能重复计费");
        }
        if (!Boolean.TRUE.equals(existing.getBillable()) || money(existing.getNursingFee(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("该护理记录未产生费用");
        }
        Integer feeCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM inpatient_fee
                WHERE deleted = false
                  AND source_type = 'NURSING'
                  AND source_id = ?
                """, Integer.class, id);
        if (feeCount != null && feeCount > 0) {
            throw new BusinessException("该护理记录已生成费用，不能重复计费");
        }
        BigDecimal amount = money(existing.getNursingFee(), BigDecimal.ZERO);
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'NURSING', ?, ?, ?, 'NURSING', 1, '次', ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """,
                generateFeeNo(),
                existing.getAdmissionId(),
                existing.getPatientId(),
                existing.getId(),
                existing.getRecordNo(),
                existing.getNursingContent(),
                amount,
                amount,
                remark(request));
        mapper.markBilled(id, remark(request));
        return requireRecord(id);
    }

    private NursingRecord toRecord(NursingRecordCreateRequest request, NursingRecord existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        NursingRecord item = new NursingRecord();
        item.setRecordNo(existing == null ? trimToNull(request.getRecordNo()) : firstNotBlank(request.getRecordNo(), existing.getRecordNo()));
        item.setOrderId(request.getOrderId());
        item.setAdmissionId(request.getAdmissionId());
        item.setPatientId(request.getPatientId());
        item.setNursingType(normalizeType(request.getNursingType()));
        item.setNursingContent(trimToNull(request.getNursingContent()));
        item.setTemperature(request.getTemperature());
        item.setPulse(request.getPulse());
        item.setRespiration(request.getRespiration());
        item.setBloodPressure(trimToNull(request.getBloodPressure()));
        item.setIntakeAmount(request.getIntakeAmount());
        item.setOutputAmount(request.getOutputAmount());
        item.setNursingLevel(trimToNull(request.getNursingLevel()));
        item.setNurseName(trimToNull(request.getNurseName()));
        item.setRecordTime(request.getRecordTime() == null ? LocalDateTime.now() : request.getRecordTime());
        item.setStatus(existing == null ? normalizeStatus(request.getStatus(), "RECORDED") : existing.getStatus());
        item.setBillable(Boolean.TRUE.equals(request.getBillable()));
        item.setNursingFee(money(request.getNursingFee(), BigDecimal.ZERO));
        item.setBilled(existing != null && Boolean.TRUE.equals(existing.getBilled()));
        item.setRemark(trimToNull(request.getRemark()));
        item.setDeleted(false);
        return item;
    }

    private NursingRecordQueryRequest normalizeQuery(NursingRecordQueryRequest query) {
        NursingRecordQueryRequest q = new NursingRecordQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setOrderId(query.getOrderId());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setNursingType(normalizeTypeOrNull(query.getNursingType()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        q.setNurseName(trimToNull(query.getNurseName()));
        return q;
    }

    private void validateAdmission(Long admissionId, Long patientId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!admission.getPatientId().equals(patientId)) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
        if (!"IN_HOSPITAL".equals(admission.getStatus())) {
            throw new BusinessException("只有在院患者可以新增护理记录");
        }
    }

    private NursingRecord requireRecord(Long id) {
        NursingRecord item = mapper.findById(id);
        if (item == null) {
            throw new BusinessException("护理记录不存在");
        }
        return item;
    }

    private String normalizeType(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException("护理类型不能为空");
        }
        normalized = normalized.toUpperCase();
        if (!TYPES.contains(normalized)) {
            throw new BusinessException("护理类型只能是 VITAL_SIGN、DAILY_CARE、ORDER_EXECUTION、WOUND_CARE、INFUSION、OTHER");
        }
        return normalized;
    }

    private String normalizeTypeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeType(normalized);
    }

    private String normalizeStatus(String value, String fallback) {
        String normalized = trimToNull(value);
        normalized = normalized == null ? fallback : normalized.toUpperCase();
        if (!STATUSES.contains(normalized)) {
            throw new BusinessException("护理状态只能是 RECORDED、EXECUTED、REVIEWED、BILLED、CANCELLED");
        }
        return normalized;
    }

    private String normalizeStatusOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeStatus(normalized, "RECORDED");
    }

    private String generateRecordNo() {
        for (int i = 0; i < 5; i++) {
            String no = "HL" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByRecordNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("护理记录编号生成失败，请重试");
    }

    private String generateFeeNo() {
        return "FEE-NURSING" + LocalDateTime.now().format(FORMATTER);
    }

    private BigDecimal money(BigDecimal value, BigDecimal fallback) {
        return (value == null ? fallback : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String nurse(NursingRecordActionRequest request) {
        return request == null ? null : trimToNull(request.getNurseName());
    }

    private String remark(NursingRecordActionRequest request) {
        return request == null ? null : trimToNull(request.getRemark());
    }

    private String firstNotBlank(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
