package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.InpatientFee;
import com.yueshan.backend.domain.PharmacyDispense;
import com.yueshan.backend.dto.InpatientFeeCreateRequest;
import com.yueshan.backend.dto.InpatientFeeQueryRequest;
import com.yueshan.backend.dto.InpatientFeeSummaryResponse;
import com.yueshan.backend.dto.InpatientFeeUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.InpatientFeeMapper;
import com.yueshan.backend.service.InpatientFeeService;

@Service
public class InpatientFeeServiceImpl implements InpatientFeeService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> SOURCE_TYPES = Set.of("ORDER", "DRUG_DISPENSE", "DRUG_RETURN", "SURGERY", "BED", "MANUAL", "OTHER");
    private static final Set<String> ITEM_CATEGORIES = Set.of("DRUG", "EXAM", "LAB", "TREATMENT", "SURGERY", "BED", "NURSING", "OTHER");
    private static final Set<String> STATUSES = Set.of("UNSETTLED", "SETTLED", "CANCELLED");

    private final InpatientFeeMapper mapper;
    private final AdmissionMapper admissionMapper;

    public InpatientFeeServiceImpl(InpatientFeeMapper mapper, AdmissionMapper admissionMapper) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
    }

    public PageResponse<InpatientFee> findPage(InpatientFeeQueryRequest query) {
        InpatientFeeQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<InpatientFee> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<InpatientFee> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public InpatientFee create(InpatientFeeCreateRequest request) {
        InpatientFee fee = toFee(request, null);
        mapper.insert(fee);
        return mapper.findById(fee.getId());
    }

    @Transactional
    public Optional<InpatientFee> update(Long id, InpatientFeeUpdateRequest request) {
        InpatientFee current = mapper.findById(id);
        if (current == null) {
            return Optional.empty();
        }
        if ("CANCELLED".equals(current.getStatus())) {
            throw new BusinessException("已取消费用不能修改");
        }
        InpatientFee fee = toFee(request, current);
        fee.setId(id);
        mapper.update(fee);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public boolean deleteById(Long id) {
        InpatientFee current = mapper.findById(id);
        if (current == null) {
            return false;
        }
        if ("SETTLED".equals(current.getStatus())) {
            throw new BusinessException("已结算费用不能删除");
        }
        return mapper.logicDeleteById(id) > 0;
    }

    @Transactional
    public Optional<InpatientFee> cancel(Long id) {
        InpatientFee current = mapper.findById(id);
        if (current == null) {
            return Optional.empty();
        }
        if ("CANCELLED".equals(current.getStatus())) {
            throw new BusinessException("费用已取消，不能重复取消");
        }
        mapper.cancel(id);
        return Optional.ofNullable(mapper.findById(id));
    }

    public InpatientFeeSummaryResponse summary(Long admissionId) {
        InpatientAdmission admission = requireAdmission(admissionId);
        return new InpatientFeeSummaryResponse(
                admissionId,
                admission.getPatientId(),
                money(mapper.sumAllByAdmission(admissionId)),
                money(mapper.sumByAdmissionAndStatus(admissionId, "UNSETTLED")),
                money(mapper.sumByAdmissionAndStatus(admissionId, "SETTLED")),
                money(mapper.sumByAdmissionAndStatus(admissionId, "CANCELLED")),
                mapper.sumCategoryByAdmission(admissionId).stream()
                        .map(item -> new com.yueshan.backend.dto.FeeCategorySummary(item.itemCategory(), money(item.amount())))
                        .toList());
    }

    @Transactional
    public void createDrugDispenseFee(PharmacyDispense dispense, Drug drug) {
        if (dispense == null || drug == null || dispense.getId() == null) {
            return;
        }
        if (mapper.findBySource("DRUG_DISPENSE", dispense.getId()) != null) {
            return;
        }
        InpatientFee fee = new InpatientFee();
        fee.setFeeNo(generateFeeNo());
        fee.setAdmissionId(dispense.getAdmissionId());
        fee.setPatientId(dispense.getPatientId());
        fee.setSourceType("DRUG_DISPENSE");
        fee.setSourceId(dispense.getId());
        fee.setItemCode(drug.getDrugCode());
        fee.setItemName(drug.getDrugName());
        fee.setItemCategory("DRUG");
        fee.setQuantity(money(BigDecimal.valueOf(dispense.getQuantity())));
        fee.setUnit(trimToNull(drug.getUnit()));
        fee.setUnitPrice(money(drug.getPrice()));
        fee.setTotalAmount(calculateTotal(fee.getQuantity(), fee.getUnitPrice()));
        fee.setFeeTime(LocalDateTime.now());
        fee.setStatus("UNSETTLED");
        fee.setRemark("药房发药自动计费");
        fee.setDeleted(false);
        mapper.insert(fee);
    }

    @Transactional
    public void createDrugReturnFee(PharmacyDispense dispense, Drug drug) {
        if (dispense == null || drug == null || dispense.getId() == null) {
            return;
        }
        if (mapper.findBySource("DRUG_RETURN", dispense.getId()) != null) {
            return;
        }
        BigDecimal quantity = money(BigDecimal.valueOf(dispense.getQuantity()).negate());
        BigDecimal unitPrice = money(drug.getPrice());
        InpatientFee fee = new InpatientFee();
        fee.setFeeNo(generateFeeNo());
        fee.setAdmissionId(dispense.getAdmissionId());
        fee.setPatientId(dispense.getPatientId());
        fee.setSourceType("DRUG_RETURN");
        fee.setSourceId(dispense.getId());
        fee.setItemCode(drug.getDrugCode());
        fee.setItemName("退药冲正-" + drug.getDrugName());
        fee.setItemCategory("DRUG");
        fee.setQuantity(quantity);
        fee.setUnit(trimToNull(drug.getUnit()));
        fee.setUnitPrice(unitPrice);
        fee.setTotalAmount(calculateTotal(quantity, unitPrice));
        fee.setFeeTime(LocalDateTime.now());
        fee.setStatus("UNSETTLED");
        fee.setRemark("药房退药自动冲正");
        fee.setDeleted(false);
        mapper.insert(fee);
    }

    private InpatientFee toFee(InpatientFeeCreateRequest request, InpatientFee current) {
        InpatientAdmission admission = requireAdmission(request.getAdmissionId());
        if (!admission.getPatientId().equals(request.getPatientId())) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
        BigDecimal quantity = money(request.getQuantity());
        BigDecimal unitPrice = money(request.getUnitPrice());
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("数量必须大于0");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("单价不能小于0");
        }

        InpatientFee fee = new InpatientFee();
        fee.setFeeNo(resolveFeeNo(request.getFeeNo(), current));
        fee.setAdmissionId(request.getAdmissionId());
        fee.setPatientId(request.getPatientId());
        fee.setSourceType(normalizeSourceType(request.getSourceType()));
        fee.setSourceId(request.getSourceId());
        fee.setItemCode(trimToNull(request.getItemCode()));
        fee.setItemName(requireText(request.getItemName(), "项目名称不能为空"));
        fee.setItemCategory(normalizeItemCategory(request.getItemCategory()));
        fee.setQuantity(quantity);
        fee.setUnit(trimToNull(request.getUnit()));
        fee.setUnitPrice(unitPrice);
        fee.setTotalAmount(calculateTotal(quantity, unitPrice));
        fee.setFeeTime(request.getFeeTime() == null ? LocalDateTime.now() : request.getFeeTime());
        fee.setStatus(normalizeStatus(request.getStatus()));
        fee.setRemark(trimToNull(request.getRemark()));
        fee.setDeleted(false);
        return fee;
    }

    private InpatientAdmission requireAdmission(Long admissionId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        return admission;
    }

    private InpatientFeeQueryRequest normalizeQuery(InpatientFeeQueryRequest query) {
        InpatientFeeQueryRequest q = new InpatientFeeQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setItemName(trimToNull(query.getItemName()));
        q.setItemCategory(normalizeItemCategoryOrNull(query.getItemCategory()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String resolveFeeNo(String feeNo, InpatientFee current) {
        String value = trimToNull(feeNo);
        if (value == null) {
            if (current != null) {
                return current.getFeeNo();
            }
            return generateFeeNo();
        }
        boolean duplicated = current == null
                ? mapper.countByFeeNo(value) > 0
                : mapper.countByFeeNoExcludeId(value, current.getId()) > 0;
        if (duplicated) {
            throw new BusinessException("费用编号已存在");
        }
        return value;
    }

    private String generateFeeNo() {
        for (int i = 0; i < 5; i++) {
            String no = "FEE" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByFeeNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("费用编号生成失败，请重试");
    }

    private BigDecimal calculateTotal(BigDecimal quantity, BigDecimal unitPrice) {
        return money(quantity.multiply(unitPrice));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeSourceType(String sourceType) {
        String value = trimToNull(sourceType);
        value = value == null ? "MANUAL" : value.toUpperCase();
        if (!SOURCE_TYPES.contains(value)) {
            throw new BusinessException("费用来源类型不正确");
        }
        return value;
    }

    private String normalizeItemCategory(String itemCategory) {
        String value = requireText(itemCategory, "项目类别不能为空").toUpperCase();
        if (!ITEM_CATEGORIES.contains(value)) {
            throw new BusinessException("项目类别不正确");
        }
        return value;
    }

    private String normalizeItemCategoryOrNull(String itemCategory) {
        String value = trimToNull(itemCategory);
        return value == null ? null : normalizeItemCategory(value);
    }

    private String normalizeStatus(String status) {
        String value = trimToNull(status);
        value = value == null ? "UNSETTLED" : value.toUpperCase();
        if (!STATUSES.contains(value)) {
            throw new BusinessException("费用状态不正确");
        }
        return value;
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        return value == null ? null : normalizeStatus(value);
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(message);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
