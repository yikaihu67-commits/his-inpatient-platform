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
import com.yueshan.backend.domain.MedicalOrder;
import com.yueshan.backend.dto.MedicalOrderActionRequest;
import com.yueshan.backend.dto.MedicalOrderCreateRequest;
import com.yueshan.backend.dto.MedicalOrderQueryRequest;
import com.yueshan.backend.dto.MedicalOrderUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.MedicalOrderMapper;
import com.yueshan.backend.service.MedicalOrderService;

@Service
public class MedicalOrderServiceImpl implements MedicalOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String DEFAULT_STATUS = "DRAFT";
    private static final Set<String> ORDER_TYPES = Set.of("LONG_TERM", "TEMPORARY");
    private static final Set<String> ORDER_CATEGORIES = Set.of("DRUG", "EXAM", "LAB", "NURSING", "TREATMENT", "CARE", "DIET", "OTHER");
    private static final Set<String> ORDER_STATUSES = Set.of("DRAFT", "SUBMITTED", "CHECKED", "EXECUTED", "BILLED", "STOPPED", "CANCELLED");

    private final MedicalOrderMapper medicalOrderMapper;
    private final AdmissionMapper admissionMapper;
    private final JdbcTemplate jdbcTemplate;

    public MedicalOrderServiceImpl(MedicalOrderMapper medicalOrderMapper, AdmissionMapper admissionMapper, JdbcTemplate jdbcTemplate) {
        this.medicalOrderMapper = medicalOrderMapper;
        this.admissionMapper = admissionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResponse<MedicalOrder> findPage(MedicalOrderQueryRequest query) {
        MedicalOrderQueryRequest normalizedQuery = normalizeQuery(query);
        int page = normalizedQuery.getPage();
        int pageSize = normalizedQuery.getPageSize();
        int offset = (page - 1) * pageSize;
        long total = medicalOrderMapper.countPage(normalizedQuery);
        List<MedicalOrder> records = total == 0 ? List.of() : medicalOrderMapper.findPage(normalizedQuery, offset, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<MedicalOrder> findById(Long id) {
        return Optional.ofNullable(medicalOrderMapper.findById(id));
    }

    @Override
    @Transactional
    public MedicalOrder create(MedicalOrderCreateRequest request) {
        MedicalOrder order = toOrder(request);
        if (isBlank(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }
        validateAdmission(order.getAdmissionId(), order.getPatientId());
        validateOrderNoUnique(order, null);
        medicalOrderMapper.insert(order);
        MedicalOrder created = order.getId() == null ? null : medicalOrderMapper.findById(order.getId());
        if (created == null) {
            created = medicalOrderMapper.findByOrderNo(order.getOrderNo());
        }
        if (created == null) {
            throw new BusinessException("医嘱创建失败，请重试");
        }
        return created;
    }

    @Override
    @Transactional
    public Optional<MedicalOrder> update(Long id, MedicalOrderUpdateRequest request) {
        MedicalOrder existing = medicalOrderMapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("非草稿医嘱不允许修改");
        }
        MedicalOrder order = toOrder(request, existing);
        order.setId(id);
        validateAdmission(order.getAdmissionId(), order.getPatientId());
        validateOrderNoUnique(order, id);
        medicalOrderMapper.update(order);
        return Optional.ofNullable(medicalOrderMapper.findById(id));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        MedicalOrder existing = medicalOrderMapper.findById(id);
        if (existing == null) {
            return false;
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿医嘱允许删除");
        }
        return medicalOrderMapper.logicDeleteById(id) > 0;
    }

    @Override
    @Transactional
    public MedicalOrder submit(Long id, MedicalOrderActionRequest request) {
        MedicalOrder existing = requireOrder(id);
        requireStatus(existing, "DRAFT", "只有草稿医嘱可以提交");
        return changeStatus(id, "SUBMITTED", request);
    }

    @Override
    @Transactional
    public MedicalOrder check(Long id, MedicalOrderActionRequest request) {
        MedicalOrder existing = requireOrder(id);
        requireStatus(existing, "SUBMITTED", "只有已提交医嘱可以核对");
        return changeStatus(id, "CHECKED", request);
    }

    @Override
    @Transactional
    public MedicalOrder execute(Long id, MedicalOrderActionRequest request) {
        MedicalOrder existing = requireOrder(id);
        if (!Set.of("SUBMITTED", "CHECKED").contains(existing.getStatus())) {
            throw new BusinessException("只有已提交或已核对医嘱可以执行");
        }
        return changeStatus(id, "EXECUTED", request);
    }

    @Override
    @Transactional
    public MedicalOrder bill(Long id, MedicalOrderActionRequest request) {
        MedicalOrder existing = requireOrder(id);
        requireStatus(existing, "EXECUTED", "只有已执行医嘱可以生成费用");
        if (Boolean.TRUE.equals(existing.getBilled())) {
            throw new BusinessException("该医嘱已计费，不能重复计费");
        }
        Integer feeCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM inpatient_fee
                WHERE deleted = false
                  AND source_type = 'ORDER'
                  AND source_id = ?
                """, Integer.class, id);
        if (feeCount != null && feeCount > 0) {
            throw new BusinessException("该医嘱已生成费用，不能重复计费");
        }

        BigDecimal quantity = money(existing.getQuantity(), BigDecimal.ONE);
        BigDecimal unitPrice = money(existing.getUnitPrice(), BigDecimal.ZERO);
        BigDecimal totalAmount = money(existing.getTotalAmount(), unitPrice.multiply(quantity));
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'ORDER', ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """,
                generateFeeNo("FEE-ORDER"),
                existing.getAdmissionId(),
                existing.getPatientId(),
                existing.getId(),
                existing.getOrderNo(),
                existing.getItemName(),
                normalizeFeeCategory(existing.getOrderCategory()),
                quantity,
                existing.getDosageUnit(),
                unitPrice,
                totalAmount,
                request == null ? "order billing" : trimToNull(request.getRemark()));
        medicalOrderMapper.markBilled(id, request == null ? null : trimToNull(request.getRemark()));
        return requireOrder(id);
    }

    @Override
    @Transactional
    public MedicalOrder stop(Long id, MedicalOrderActionRequest request) {
        MedicalOrder existing = requireOrder(id);
        if (!Set.of("SUBMITTED", "CHECKED", "EXECUTED").contains(existing.getStatus())) {
            throw new BusinessException("只有已提交、已核对或已执行医嘱可以停止");
        }
        return changeStatus(id, "STOPPED", request);
    }

    @Override
    @Transactional
    public MedicalOrder cancel(Long id, MedicalOrderActionRequest request) {
        MedicalOrder existing = requireOrder(id);
        if (!Set.of("DRAFT", "SUBMITTED").contains(existing.getStatus())) {
            throw new BusinessException("只有草稿或已提交医嘱可以作废");
        }
        return changeStatus(id, "CANCELLED", request);
    }

    private MedicalOrder changeStatus(Long id, String status, MedicalOrderActionRequest request) {
        String nurseName = request == null ? null : trimToNull(firstNotBlank(request.getNurseName(), request.getExecutorName()));
        String remark = request == null ? null : trimToNull(request.getRemark());
        medicalOrderMapper.updateStatus(id, status, nurseName, remark);
        return requireOrder(id);
    }

    private MedicalOrder requireOrder(Long id) {
        MedicalOrder order = medicalOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException("医嘱不存在");
        }
        return order;
    }

    private void requireStatus(MedicalOrder order, String requiredStatus, String message) {
        if (!requiredStatus.equals(order.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private void validateAdmission(Long admissionId, Long patientId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!"IN_HOSPITAL".equals(admission.getStatus())) {
            throw new BusinessException("只有在院患者可以开立医嘱");
        }
        if (!admission.getPatientId().equals(patientId)) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
    }

    private void validateOrderNoUnique(MedicalOrder order, Long excludeId) {
        int count = excludeId == null
                ? medicalOrderMapper.countByOrderNo(order.getOrderNo())
                : medicalOrderMapper.countByOrderNoExcludeId(order.getOrderNo(), excludeId);
        if (count > 0) {
            throw new BusinessException("医嘱编号已存在");
        }
    }

    private MedicalOrder toOrder(MedicalOrderCreateRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        MedicalOrder order = new MedicalOrder();
        order.setOrderNo(trimToNull(request.getOrderNo()));
        order.setAdmissionId(request.getAdmissionId());
        order.setPatientId(request.getPatientId());
        order.setOrderType(normalizeOrderType(request.getOrderType()));
        order.setOrderCategory(normalizeOrderCategory(request.getOrderCategory()));
        order.setOrderContent(firstNotBlank(request.getOrderContent(), request.getItemName()));
        order.setItemName(trimToNull(request.getItemName()));
        order.setDosage(trimToNull(request.getDosage()));
        order.setDosageUnit(trimToNull(request.getDosageUnit()));
        order.setFrequency(trimToNull(request.getFrequency()));
        order.setRoute(trimToNull(request.getRoute()));
        order.setStartTime(request.getStartTime() == null ? LocalDateTime.now() : request.getStartTime());
        order.setEndTime(request.getEndTime());
        order.setDoctorName(trimToNull(request.getDoctorName()));
        order.setExecutionDepartment(trimToNull(request.getExecutionDepartment()));
        order.setExecutorName(trimToNull(request.getExecutorName()));
        order.setNurseName(trimToNull(request.getNurseName()));
        order.setStatus(normalizeStatus(request.getStatus(), DEFAULT_STATUS));
        if (!"DRAFT".equals(order.getStatus())) {
            throw new BusinessException("新医嘱只能创建为草稿状态");
        }
        order.setUnitPrice(money(request.getUnitPrice(), BigDecimal.ZERO));
        order.setQuantity(money(request.getQuantity(), BigDecimal.ONE));
        order.setTotalAmount(order.getUnitPrice().multiply(order.getQuantity()).setScale(2, RoundingMode.HALF_UP));
        order.setBilled(false);
        order.setRemark(trimToNull(request.getRemark()));
        order.setDeleted(false);
        return order;
    }

    private MedicalOrder toOrder(MedicalOrderUpdateRequest request, MedicalOrder existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        MedicalOrder order = new MedicalOrder();
        order.setOrderNo(firstNotBlank(request.getOrderNo(), existing.getOrderNo()));
        order.setAdmissionId(request.getAdmissionId());
        order.setPatientId(request.getPatientId());
        order.setOrderType(normalizeOrderType(request.getOrderType()));
        order.setOrderCategory(normalizeOrderCategory(request.getOrderCategory()));
        order.setOrderContent(firstNotBlank(request.getOrderContent(), existing.getOrderContent()));
        order.setItemName(trimToNull(request.getItemName()));
        order.setDosage(trimToNull(request.getDosage()));
        order.setDosageUnit(trimToNull(request.getDosageUnit()));
        order.setFrequency(trimToNull(request.getFrequency()));
        order.setRoute(trimToNull(request.getRoute()));
        order.setStartTime(request.getStartTime() == null ? existing.getStartTime() : request.getStartTime());
        order.setEndTime(request.getEndTime());
        order.setDoctorName(trimToNull(request.getDoctorName()));
        order.setExecutionDepartment(trimToNull(request.getExecutionDepartment()));
        order.setExecutorName(trimToNull(request.getExecutorName()));
        order.setNurseName(trimToNull(request.getNurseName()));
        order.setUnitPrice(money(request.getUnitPrice(), BigDecimal.ZERO));
        order.setQuantity(money(request.getQuantity(), BigDecimal.ONE));
        order.setTotalAmount(order.getUnitPrice().multiply(order.getQuantity()).setScale(2, RoundingMode.HALF_UP));
        order.setRemark(trimToNull(request.getRemark()));
        return order;
    }

    private MedicalOrderQueryRequest normalizeQuery(MedicalOrderQueryRequest query) {
        MedicalOrderQueryRequest normalized = new MedicalOrderQueryRequest();
        if (query == null) {
            return normalized;
        }
        normalized.setPage(query.getPage() == null ? 1 : query.getPage());
        normalized.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        normalized.setAdmissionId(query.getAdmissionId());
        normalized.setPatientId(query.getPatientId());
        normalized.setOrderType(normalizeOrderTypeOrNull(query.getOrderType()));
        normalized.setOrderCategory(normalizeOrderCategoryOrNull(query.getOrderCategory()));
        normalized.setStatus(normalizeStatusOrNull(query.getStatus()));
        normalized.setItemName(trimToNull(query.getItemName()));
        return normalized;
    }

    private String generateOrderNo() {
        for (int i = 0; i < 5; i++) {
            String orderNo = "YZ" + LocalDateTime.now().format(ORDER_NO_FORMATTER);
            if (medicalOrderMapper.countByOrderNo(orderNo) == 0) {
                return orderNo;
            }
        }
        throw new BusinessException("医嘱编号生成失败，请重试");
    }

    private String normalizeOrderType(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException("医嘱长临类型不能为空");
        }
        normalized = normalized.toUpperCase();
        if (!ORDER_TYPES.contains(normalized)) {
            throw new BusinessException("医嘱长临类型只能是 LONG_TERM、TEMPORARY");
        }
        return normalized;
    }

    private String normalizeOrderTypeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeOrderType(normalized);
    }

    private String normalizeOrderCategory(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException("医嘱类型不能为空");
        }
        normalized = normalized.toUpperCase();
        if (!ORDER_CATEGORIES.contains(normalized)) {
            throw new BusinessException("医嘱类型只能是 DRUG、EXAM、LAB、NURSING、TREATMENT、CARE、DIET、OTHER");
        }
        return normalized;
    }

    private String normalizeOrderCategoryOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeOrderCategory(normalized);
    }

    private String normalizeStatus(String value, String defaultStatus) {
        String normalized = trimToNull(value);
        normalized = normalized == null ? defaultStatus : normalized.toUpperCase();
        if (!ORDER_STATUSES.contains(normalized)) {
            throw new BusinessException("医嘱状态只能是 DRAFT、SUBMITTED、CHECKED、EXECUTED、BILLED、STOPPED、CANCELLED");
        }
        return normalized;
    }

    private String normalizeStatusOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeStatus(normalized, DEFAULT_STATUS);
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

    private BigDecimal money(BigDecimal value, BigDecimal fallback) {
        return (value == null ? fallback : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeFeeCategory(String category) {
        if ("CARE".equals(category)) {
            return "NURSING";
        }
        return category == null ? "OTHER" : category;
    }

    private String generateFeeNo(String prefix) {
        return prefix + LocalDateTime.now().format(ORDER_NO_FORMATTER);
    }
}
