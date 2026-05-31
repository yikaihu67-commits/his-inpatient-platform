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

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.SurgeryOperation;
import com.yueshan.backend.dto.InpatientFeeCreateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.SurgeryActionRequest;
import com.yueshan.backend.dto.SurgeryCreateRequest;
import com.yueshan.backend.dto.SurgeryQueryRequest;
import com.yueshan.backend.dto.SurgeryUpdateRequest;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.InpatientFeeMapper;
import com.yueshan.backend.mapper.SurgeryOperationMapper;
import com.yueshan.backend.service.InpatientFeeService;
import com.yueshan.backend.service.OperationLogService;
import com.yueshan.backend.service.SurgeryOperationService;

@Service
public class SurgeryOperationServiceImpl implements SurgeryOperationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> STATUSES = Set.of("APPLIED", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "BILLED", "CANCELLED");

    private final SurgeryOperationMapper mapper;
    private final AdmissionMapper admissionMapper;
    private final InpatientFeeMapper feeMapper;
    private final InpatientFeeService feeService;
    private final OperationLogService logService;

    public SurgeryOperationServiceImpl(SurgeryOperationMapper mapper, AdmissionMapper admissionMapper,
            InpatientFeeMapper feeMapper, InpatientFeeService feeService, OperationLogService logService) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
        this.feeMapper = feeMapper;
        this.feeService = feeService;
        this.logService = logService;
    }

    @Override
    public PageResponse<SurgeryOperation> findPage(SurgeryQueryRequest query) {
        SurgeryQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<SurgeryOperation> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<SurgeryOperation> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    @Transactional
    public SurgeryOperation create(SurgeryCreateRequest request) {
        try {
            SurgeryOperation item = toOperation(request, null);
            validateAdmission(item.getAdmissionId(), item.getPatientId(), true);
            mapper.insert(item);
            SurgeryOperation created = mapper.findById(item.getId());
            logSuccess("CREATE", created);
            return created;
        } catch (RuntimeException ex) {
            logFailure("CREATE", null, request == null ? null : request.getSurgeryNo(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public Optional<SurgeryOperation> update(Long id, SurgeryUpdateRequest request) {
        try {
            SurgeryOperation existing = mapper.findById(id);
            if (existing == null) {
                return Optional.empty();
            }
            if (!Set.of("APPLIED", "SCHEDULED").contains(existing.getStatus())) {
                throw new BusinessException("只有已申请或已安排手术允许修改");
            }
            SurgeryOperation item = toOperation(request, existing);
            item.setId(id);
            validateAdmission(item.getAdmissionId(), item.getPatientId(), true);
            mapper.update(item);
            SurgeryOperation updated = mapper.findById(id);
            logSuccess("UPDATE", updated);
            return Optional.ofNullable(updated);
        } catch (RuntimeException ex) {
            logFailure("UPDATE", id, request == null ? null : request.getSurgeryNo(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        SurgeryOperation existing = mapper.findById(id);
        try {
            if (existing == null) {
                return false;
            }
            if (!"APPLIED".equals(existing.getStatus())) {
                throw new BusinessException("只有已申请手术允许删除");
            }
            boolean deleted = mapper.logicDeleteById(id) > 0;
            if (deleted) {
                logSuccess("DELETE", existing);
            }
            return deleted;
        } catch (RuntimeException ex) {
            logFailure("DELETE", id, existing == null ? null : existing.getSurgeryNo(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public SurgeryOperation schedule(Long id, SurgeryActionRequest request) {
        try {
            SurgeryOperation existing = requireOperation(id);
            requireStatus(existing, "APPLIED", "只有已申请手术可以安排");
            LocalDateTime plannedTime = request == null || request.getPlannedTime() == null ? LocalDateTime.now() : request.getPlannedTime();
            mapper.updateStatus(id, "SCHEDULED", plannedTime, null, null, text(request == null ? null : request.getOperatingRoom()),
                    text(request == null ? null : request.getPrimaryDoctorName()),
                    text(request == null ? null : request.getAssistantDoctorName()),
                    text(request == null ? null : request.getAnesthesiaMethod()),
                    text(request == null ? null : request.getAnesthesiologistName()),
                    moneyOrNull(request == null ? null : request.getSurgeryFee()),
                    text(request == null ? null : request.getRemark()));
            SurgeryOperation updated = mapper.findById(id);
            logSuccess("OTHER", updated);
            return updated;
        } catch (RuntimeException ex) {
            logFailure("OTHER", id, null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public SurgeryOperation start(Long id, SurgeryActionRequest request) {
        try {
            SurgeryOperation existing = requireOperation(id);
            requireStatus(existing, "SCHEDULED", "只有已安排手术可以开始");
            LocalDateTime startTime = request == null || request.getActualStartTime() == null ? LocalDateTime.now() : request.getActualStartTime();
            mapper.updateStatus(id, "IN_PROGRESS", null, startTime, null, null, null, null, null, null, null, text(request == null ? null : request.getRemark()));
            SurgeryOperation updated = mapper.findById(id);
            logSuccess("EXECUTE", updated);
            return updated;
        } catch (RuntimeException ex) {
            logFailure("EXECUTE", id, null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public SurgeryOperation complete(Long id, SurgeryActionRequest request) {
        try {
            SurgeryOperation existing = requireOperation(id);
            requireStatus(existing, "IN_PROGRESS", "只有手术中记录可以完成");
            LocalDateTime endTime = request == null || request.getActualEndTime() == null ? LocalDateTime.now() : request.getActualEndTime();
            mapper.updateStatus(id, "COMPLETED", null, null, endTime, null, null, null, null, null,
                    moneyOrNull(request == null ? null : request.getSurgeryFee()), text(request == null ? null : request.getRemark()));
            SurgeryOperation updated = mapper.findById(id);
            logSuccess("EXECUTE", updated);
            return updated;
        } catch (RuntimeException ex) {
            logFailure("EXECUTE", id, null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public SurgeryOperation cancel(Long id, SurgeryActionRequest request) {
        try {
            SurgeryOperation existing = requireOperation(id);
            if (!Set.of("APPLIED", "SCHEDULED").contains(existing.getStatus())) {
                throw new BusinessException("只有已申请或已安排手术可以取消");
            }
            mapper.updateStatus(id, "CANCELLED", null, null, null, null, null, null, null, null, null, text(request == null ? null : request.getRemark()));
            SurgeryOperation updated = mapper.findById(id);
            logSuccess("CANCEL", updated);
            return updated;
        } catch (RuntimeException ex) {
            logFailure("CANCEL", id, null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public SurgeryOperation bill(Long id, SurgeryActionRequest request) {
        try {
            SurgeryOperation existing = requireOperation(id);
            if ("BILLED".equals(existing.getStatus()) || feeMapper.findBySource("SURGERY", id) != null) {
                throw new BusinessException("该手术已生成费用，不能重复计费");
            }
            requireStatus(existing, "COMPLETED", "只有已完成手术可以生成费用");
            BigDecimal surgeryFee = money(request == null || request.getSurgeryFee() == null ? existing.getSurgeryFee() : request.getSurgeryFee());
            if (surgeryFee.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("手术费用必须大于0");
            }
            InpatientFeeCreateRequest fee = new InpatientFeeCreateRequest();
            fee.setAdmissionId(existing.getAdmissionId());
            fee.setPatientId(existing.getPatientId());
            fee.setSourceType("SURGERY");
            fee.setSourceId(existing.getId());
            fee.setItemCode(existing.getSurgeryNo());
            fee.setItemName(existing.getSurgeryName());
            fee.setItemCategory("SURGERY");
            fee.setQuantity(BigDecimal.ONE);
            fee.setUnit("台");
            fee.setUnitPrice(surgeryFee);
            fee.setFeeTime(LocalDateTime.now());
            fee.setRemark("手术完成自动计费");
            feeService.create(fee);
            mapper.updateStatus(id, "BILLED", null, null, null, null, null, null, null, null, surgeryFee, text(request == null ? null : request.getRemark()));
            SurgeryOperation updated = mapper.findById(id);
            logSuccess("SETTLE", updated);
            return updated;
        } catch (RuntimeException ex) {
            logFailure("SETTLE", id, null, ex);
            throw ex;
        }
    }

    private SurgeryOperation toOperation(SurgeryCreateRequest request, SurgeryOperation existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        SurgeryOperation item = new SurgeryOperation();
        item.setSurgeryNo(resolveSurgeryNo(request.getSurgeryNo(), existing));
        item.setPatientId(request.getPatientId());
        item.setAdmissionId(request.getAdmissionId());
        item.setSurgeryName(required(request.getSurgeryName(), "手术名称不能为空"));
        item.setPreoperativeDiagnosis(text(request.getPreoperativeDiagnosis()));
        item.setSurgeryLevel(text(request.getSurgeryLevel()));
        item.setSurgeryType(text(request.getSurgeryType()));
        item.setOperatingRoom(text(request.getOperatingRoom()));
        item.setPlannedTime(request.getPlannedTime());
        item.setPrimaryDoctorName(text(request.getPrimaryDoctorName()));
        item.setAssistantDoctorName(text(request.getAssistantDoctorName()));
        item.setAnesthesiaMethod(text(request.getAnesthesiaMethod()));
        item.setAnesthesiologistName(text(request.getAnesthesiologistName()));
        item.setSurgeryFee(money(request.getSurgeryFee()));
        item.setStatus(normalizeStatus(request.getStatus(), existing == null ? "APPLIED" : existing.getStatus()));
        if (existing == null && !"APPLIED".equals(item.getStatus())) {
            throw new BusinessException("新手术申请只能创建为 APPLIED 状态");
        }
        item.setRemark(text(request.getRemark()));
        item.setDeleted(false);
        return item;
    }

    private void validateAdmission(Long admissionId, Long patientId, boolean requireInHospital) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!admission.getPatientId().equals(patientId)) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
        if (requireInHospital && !"IN_HOSPITAL".equals(admission.getStatus())) {
            throw new BusinessException("只有在院患者可以申请或修改手术");
        }
    }

    private SurgeryOperation requireOperation(Long id) {
        SurgeryOperation item = mapper.findById(id);
        if (item == null) {
            throw new BusinessException("手术记录不存在");
        }
        return item;
    }

    private void requireStatus(SurgeryOperation item, String status, String message) {
        if (!status.equals(item.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private SurgeryQueryRequest normalizeQuery(SurgeryQueryRequest query) {
        SurgeryQueryRequest q = new SurgeryQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setPatientId(query.getPatientId());
        q.setAdmissionId(query.getAdmissionId());
        q.setSurgeryNo(text(query.getSurgeryNo()));
        q.setSurgeryName(text(query.getSurgeryName()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String resolveSurgeryNo(String surgeryNo, SurgeryOperation existing) {
        String value = text(surgeryNo);
        if (value == null) {
            if (existing != null) {
                return existing.getSurgeryNo();
            }
            return generateSurgeryNo();
        }
        boolean duplicated = existing == null ? mapper.countBySurgeryNo(value) > 0
                : mapper.countBySurgeryNoExcludeId(value, existing.getId()) > 0;
        if (duplicated) {
            throw new BusinessException("手术编号已存在");
        }
        return value;
    }

    private String generateSurgeryNo() {
        for (int i = 0; i < 5; i++) {
            String no = "SS" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countBySurgeryNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("手术编号生成失败，请重试");
    }

    private String normalizeStatus(String status, String defaultStatus) {
        String value = text(status);
        value = value == null ? defaultStatus : value.toUpperCase();
        if (!STATUSES.contains(value)) {
            throw new BusinessException("手术状态不正确");
        }
        return value;
    }

    private String normalizeStatusOrNull(String status) {
        String value = text(status);
        return value == null ? null : normalizeStatus(value, null);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal moneyOrNull(BigDecimal value) {
        return value == null ? null : money(value);
    }

    private void logSuccess(String operationType, SurgeryOperation item) {
        if (item != null) {
            logService.record("手术管理", operationType, item.getId(), item.getSurgeryNo(), "system", "SUCCESS", null);
        }
    }

    private void logFailure(String operationType, Long businessId, String businessNo, RuntimeException ex) {
        logService.record("手术管理", operationType, businessId, businessNo, "system", "FAILED", ex.getMessage());
    }

    private String required(String value, String message) {
        String text = text(value);
        if (text == null) {
            throw new BusinessException(message);
        }
        return text;
    }

    private String text(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
