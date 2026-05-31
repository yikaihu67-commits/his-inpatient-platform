package com.yueshan.backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.domain.MedicalOrder;
import com.yueshan.backend.domain.PharmacyDispense;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PharmacyDispenseActionRequest;
import com.yueshan.backend.dto.PharmacyDispenseCreateRequest;
import com.yueshan.backend.dto.PharmacyDispenseQueryRequest;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.DrugMapper;
import com.yueshan.backend.mapper.MedicalOrderMapper;
import com.yueshan.backend.mapper.PharmacyDispenseMapper;
import com.yueshan.backend.service.InpatientFeeService;
import com.yueshan.backend.service.PharmacyDispenseService;

@Service
public class PharmacyDispenseServiceImpl implements PharmacyDispenseService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> STATUSES = Set.of("CREATED", "DISPENSED", "RETURNED", "CANCELLED");
    private final PharmacyDispenseMapper mapper;
    private final DrugMapper drugMapper;
    private final MedicalOrderMapper orderMapper;
    private final InpatientFeeService feeService;

    public PharmacyDispenseServiceImpl(PharmacyDispenseMapper mapper, DrugMapper drugMapper, MedicalOrderMapper orderMapper,
                                       InpatientFeeService feeService) {
        this.mapper = mapper;
        this.drugMapper = drugMapper;
        this.orderMapper = orderMapper;
        this.feeService = feeService;
    }

    public PageResponse<PharmacyDispense> findPage(PharmacyDispenseQueryRequest query) {
        PharmacyDispenseQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<PharmacyDispense> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<PharmacyDispense> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public PharmacyDispense create(PharmacyDispenseCreateRequest request) {
        MedicalOrder order = requireDrugOrder(request.getOrderId());
        Drug drug = requireDrug(request.getDrugId());
        if (!"ENABLED".equals(drug.getStatus())) {
            throw new BusinessException("药品已停用，不能发药");
        }
        if (mapper.countActiveByOrderId(order.getId()) > 0) {
            throw new BusinessException("该医嘱已创建发药单或已发药，不能重复发药");
        }
        PharmacyDispense dispense = new PharmacyDispense();
        dispense.setDispenseNo(generateDispenseNo());
        dispense.setOrderId(order.getId());
        dispense.setAdmissionId(order.getAdmissionId());
        dispense.setPatientId(order.getPatientId());
        dispense.setDrugId(drug.getId());
        dispense.setQuantity(request.getQuantity());
        dispense.setPharmacistName(trimToNull(request.getPharmacistName()));
        dispense.setStatus("CREATED");
        dispense.setRemark(trimToNull(request.getRemark()));
        dispense.setDeleted(false);
        mapper.insert(dispense);
        return mapper.findById(dispense.getId());
    }

    @Transactional
    public PharmacyDispense dispense(Long id, PharmacyDispenseActionRequest request) {
        PharmacyDispense dispense = requireDispense(id);
        requireStatus(dispense, "CREATED", "已发药不能重复发药");
        requireDrugOrder(dispense.getOrderId());
        if (drugMapper.decreaseStock(dispense.getDrugId(), dispense.getQuantity()) == 0) {
            throw new BusinessException("库存不足，不能发药");
        }
        dispense.setStatus("DISPENSED");
        dispense.setDispenseTime(LocalDateTime.now());
        dispense.setReturnTime(null);
        dispense.setPharmacistName(pharmacist(request, dispense.getPharmacistName()));
        dispense.setRemark(remark(request, dispense.getRemark()));
        mapper.updateStatus(dispense);
        PharmacyDispense saved = mapper.findById(id);
        feeService.createDrugDispenseFee(saved, requireDrug(saved.getDrugId()));
        return saved;
    }

    @Transactional
    public PharmacyDispense returnDrug(Long id, PharmacyDispenseActionRequest request) {
        PharmacyDispense dispense = requireDispense(id);
        requireStatus(dispense, "DISPENSED", "只有已发药单可以退药");
        drugMapper.increaseStock(dispense.getDrugId(), dispense.getQuantity());
        dispense.setStatus("RETURNED");
        dispense.setReturnTime(LocalDateTime.now());
        dispense.setPharmacistName(pharmacist(request, dispense.getPharmacistName()));
        dispense.setRemark(remark(request, dispense.getRemark()));
        mapper.updateStatus(dispense);
        PharmacyDispense saved = mapper.findById(id);
        feeService.createDrugReturnFee(saved, requireDrug(saved.getDrugId()));
        return saved;
    }

    @Transactional
    public PharmacyDispense cancel(Long id, PharmacyDispenseActionRequest request) {
        PharmacyDispense dispense = requireDispense(id);
        requireStatus(dispense, "CREATED", "只有未发药单可以取消");
        dispense.setStatus("CANCELLED");
        dispense.setPharmacistName(pharmacist(request, dispense.getPharmacistName()));
        dispense.setRemark(remark(request, dispense.getRemark()));
        mapper.updateStatus(dispense);
        return mapper.findById(id);
    }

    private MedicalOrder requireDrugOrder(Long orderId) {
        MedicalOrder order = orderMapper.findById(orderId);
        if (order == null) throw new BusinessException("医嘱不存在");
        if (!"DRUG".equals(order.getOrderCategory())) throw new BusinessException("只有 DRUG 类型医嘱才能创建发药单");
        if (!"CHECKED".equals(order.getStatus())) {
            if ("EXECUTED".equals(order.getStatus())) throw new BusinessException("已执行或已发药的医嘱不能重复发药");
            if (Set.of("STOPPED", "CANCELLED").contains(order.getStatus())) throw new BusinessException("STOPPED、CANCELLED 医嘱不能继续发药");
            throw new BusinessException("只有已核对 CHECKED 的药品医嘱才能发药");
        }
        return order;
    }
    private Drug requireDrug(Long drugId) {
        Drug drug = drugMapper.findById(drugId);
        if (drug == null) throw new BusinessException("药品不存在");
        return drug;
    }
    private PharmacyDispense requireDispense(Long id) {
        PharmacyDispense dispense = mapper.findById(id);
        if (dispense == null) throw new BusinessException("发药单不存在");
        return dispense;
    }
    private void requireStatus(PharmacyDispense dispense, String status, String message) {
        if (!status.equals(dispense.getStatus())) throw new BusinessException(message);
    }
    private PharmacyDispenseQueryRequest normalizeQuery(PharmacyDispenseQueryRequest query) {
        PharmacyDispenseQueryRequest q = new PharmacyDispenseQueryRequest();
        if (query == null) return q;
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setOrderId(query.getOrderId());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setDrugId(query.getDrugId());
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }
    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        if (value == null) return null;
        value = value.toUpperCase();
        if (!STATUSES.contains(value)) throw new BusinessException("发药状态不正确");
        return value;
    }
    private String generateDispenseNo() {
        for (int i = 0; i < 5; i++) {
            String no = "FY" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByDispenseNo(no) == 0) return no;
        }
        throw new BusinessException("发药单号生成失败，请重试");
    }
    private String pharmacist(PharmacyDispenseActionRequest request, String fallback) {
        String value = request == null ? null : trimToNull(request.getPharmacistName());
        return value == null ? fallback : value;
    }
    private String remark(PharmacyDispenseActionRequest request, String fallback) {
        String value = request == null ? null : trimToNull(request.getRemark());
        return value == null ? fallback : value;
    }
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
