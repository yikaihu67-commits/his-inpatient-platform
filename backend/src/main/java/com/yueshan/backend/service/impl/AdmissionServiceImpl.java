package com.yueshan.backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.AdmissionCreateRequest;
import com.yueshan.backend.dto.AdmissionQueryRequest;
import com.yueshan.backend.dto.AdmissionUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.BedMapper;
import com.yueshan.backend.mapper.InpatientFeeMapper;
import com.yueshan.backend.mapper.MedicalOrderMapper;
import com.yueshan.backend.mapper.PatientMapper;
import com.yueshan.backend.service.AdmissionService;

@Service
public class AdmissionServiceImpl implements AdmissionService {

    private static final DateTimeFormatter ADMISSION_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String DEFAULT_STATUS = "REGISTERED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "DRAFT",
            "REGISTERED",
            "IN_HOSPITAL",
            "DISCHARGED",
            "CANCELLED",
            "DELETED");
    private static final Set<String> ADMITTABLE_STATUSES = Set.of("DRAFT", "REGISTERED");
    private static final Set<String> CANCELABLE_STATUSES = Set.of("DRAFT", "REGISTERED");

    private final AdmissionMapper admissionMapper;
    private final PatientMapper patientMapper;
    private final BedMapper bedMapper;
    private final MedicalOrderMapper medicalOrderMapper;
    private final InpatientFeeMapper inpatientFeeMapper;

    public AdmissionServiceImpl(AdmissionMapper admissionMapper, PatientMapper patientMapper,
                                BedMapper bedMapper, MedicalOrderMapper medicalOrderMapper,
                                InpatientFeeMapper inpatientFeeMapper) {
        this.admissionMapper = admissionMapper;
        this.patientMapper = patientMapper;
        this.bedMapper = bedMapper;
        this.medicalOrderMapper = medicalOrderMapper;
        this.inpatientFeeMapper = inpatientFeeMapper;
    }

    @Override
    public PageResponse<InpatientAdmission> findPage(AdmissionQueryRequest query) {
        AdmissionQueryRequest normalizedQuery = normalizeQuery(query);
        int page = normalizedQuery.getPage();
        int pageSize = normalizedQuery.getPageSize();
        int offset = (page - 1) * pageSize;

        long total = admissionMapper.countPage(normalizedQuery);
        List<InpatientAdmission> records = total == 0
                ? List.of()
                : admissionMapper.findPage(normalizedQuery, offset, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<InpatientAdmission> findById(Long id) {
        return Optional.ofNullable(admissionMapper.findById(id));
    }

    @Override
    @Transactional
    public InpatientAdmission create(AdmissionCreateRequest request) {
        InpatientAdmission admission = toAdmission(request);
        if (isBlank(admission.getAdmissionNo())) {
            admission.setAdmissionNo(generateAdmissionNo());
        }

        validateBeforeSave(admission, null);
        admissionMapper.insert(admission);

        InpatientAdmission created = admission.getId() == null ? null : admissionMapper.findById(admission.getId());
        if (created == null) {
            created = admissionMapper.findByAdmissionNo(admission.getAdmissionNo());
        }
        if (created == null) {
            throw new BusinessException("入院登记创建失败，请重试");
        }
        return created;
    }

    @Override
    @Transactional
    public Optional<InpatientAdmission> update(Long id, AdmissionUpdateRequest request) {
        InpatientAdmission existing = admissionMapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }

        InpatientAdmission admission = toAdmission(request, existing);
        admission.setId(id);
        validateBeforeSave(admission, id);
        admissionMapper.update(admission);
        return Optional.ofNullable(admissionMapper.findById(id));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return admissionMapper.logicDeleteById(id) > 0;
    }

    @Override
    @Transactional
    public Optional<InpatientAdmission> admit(Long id) {
        InpatientAdmission admission = admissionMapper.findById(id);
        if (admission == null) {
            return Optional.empty();
        }
        if (!ADMITTABLE_STATUSES.contains(admission.getStatus())) {
            throw new BusinessException("只有草稿或已登记入院记录可以办理入院");
        }
        if (patientMapper.findById(admission.getPatientId()) == null) {
            throw new BusinessException("患者不存在，不能办理入院");
        }
        if (admissionMapper.countOpenByPatientIdExcludeId(admission.getPatientId(), id) > 0) {
            throw new BusinessException("该患者已存在在院住院记录");
        }
        if (admissionMapper.markInHospital(id) == 0) {
            throw new BusinessException("办理入院失败，请刷新后重试");
        }
        return Optional.ofNullable(admissionMapper.findById(id));
    }

    @Override
    @Transactional
    public Optional<InpatientAdmission> cancel(Long id) {
        InpatientAdmission admission = admissionMapper.findById(id);
        if (admission == null) {
            return Optional.empty();
        }
        if (!CANCELABLE_STATUSES.contains(admission.getStatus())) {
            throw new BusinessException("只有草稿或已登记入院记录可以取消");
        }
        if (bedMapper.countOccupiedByAdmissionId(id) > 0) {
            throw new BusinessException("该入院记录已分配床位，不能取消");
        }
        if (medicalOrderMapper.countByAdmissionId(id) > 0) {
            throw new BusinessException("该入院记录已有医嘱，不能取消");
        }
        if (inpatientFeeMapper.countActiveByAdmissionId(id) > 0) {
            throw new BusinessException("该入院记录已有费用，不能取消");
        }
        if (admissionMapper.cancel(id) == 0) {
            throw new BusinessException("取消入院失败，请刷新后重试");
        }
        return Optional.ofNullable(admissionMapper.findById(id));
    }

    private void validateBeforeSave(InpatientAdmission admission, Long excludeId) {
        if (patientMapper.findById(admission.getPatientId()) == null) {
            throw new BusinessException("患者不存在，不能办理入院登记");
        }

        if (excludeId == null) {
            if (admissionMapper.countByAdmissionNo(admission.getAdmissionNo()) > 0) {
                throw new BusinessException("住院号已存在");
            }
            if ("IN_HOSPITAL".equals(admission.getStatus()) && admissionMapper.countOpenByPatientId(admission.getPatientId()) > 0) {
                throw new BusinessException("该患者已存在在院住院记录");
            }
            return;
        }

        if (admissionMapper.countByAdmissionNoExcludeId(admission.getAdmissionNo(), excludeId) > 0) {
            throw new BusinessException("住院号已存在");
        }
        if ("IN_HOSPITAL".equals(admission.getStatus())
                && admissionMapper.countOpenByPatientIdExcludeId(admission.getPatientId(), excludeId) > 0) {
            throw new BusinessException("该患者已存在在院住院记录");
        }
    }

    private InpatientAdmission toAdmission(AdmissionCreateRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }

        InpatientAdmission admission = new InpatientAdmission();
        admission.setAdmissionNo(trimToNull(request.getAdmissionNo()));
        admission.setPatientId(request.getPatientId());
        admission.setDepartmentName(trimToNull(request.getDepartmentName()));
        admission.setWardName(trimToNull(request.getWardName()));
        admission.setAdmissionTime(request.getAdmissionTime() == null ? LocalDateTime.now() : request.getAdmissionTime());
        admission.setDischargeTime(null);
        admission.setAdmissionDiagnosis(trimToNull(request.getAdmissionDiagnosis()));
        admission.setChargeType(trimToNull(request.getChargeType()));
        admission.setNursingLevel(trimToNull(request.getNursingLevel()));
        admission.setDoctorName(trimToNull(request.getDoctorName()));
        admission.setStatus(normalizeStatus(request.getStatus(), DEFAULT_STATUS));
        return admission;
    }

    private InpatientAdmission toAdmission(AdmissionUpdateRequest request, InpatientAdmission existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }

        InpatientAdmission admission = new InpatientAdmission();
        admission.setAdmissionNo(firstNotBlank(request.getAdmissionNo(), existing.getAdmissionNo()));
        admission.setPatientId(request.getPatientId());
        admission.setDepartmentName(trimToNull(request.getDepartmentName()));
        admission.setWardName(trimToNull(request.getWardName()));
        admission.setAdmissionTime(request.getAdmissionTime() == null ? existing.getAdmissionTime() : request.getAdmissionTime());
        admission.setDischargeTime(existing.getDischargeTime());
        admission.setAdmissionDiagnosis(trimToNull(request.getAdmissionDiagnosis()));
        admission.setChargeType(trimToNull(request.getChargeType()));
        admission.setNursingLevel(trimToNull(request.getNursingLevel()));
        admission.setDoctorName(trimToNull(request.getDoctorName()));
        admission.setStatus(normalizeStatus(request.getStatus(), existing.getStatus()));
        return admission;
    }

    private AdmissionQueryRequest normalizeQuery(AdmissionQueryRequest query) {
        AdmissionQueryRequest normalized = new AdmissionQueryRequest();
        if (query == null) {
            return normalized;
        }

        normalized.setPage(query.getPage() == null ? 1 : query.getPage());
        normalized.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        normalized.setAdmissionNo(trimToNull(query.getAdmissionNo()));
        normalized.setPatientId(query.getPatientId());
        normalized.setPatientName(trimToNull(query.getPatientName()));
        normalized.setDepartmentName(trimToNull(query.getDepartmentName()));
        normalized.setDoctorName(trimToNull(query.getDoctorName()));
        normalized.setStatus(normalizeStatusOrNull(query.getStatus()));
        return normalized;
    }

    private String generateAdmissionNo() {
        for (int i = 0; i < 5; i++) {
            String admissionNo = "ZY" + LocalDateTime.now().format(ADMISSION_NO_FORMATTER);
            if (admissionMapper.countByAdmissionNo(admissionNo) == 0) {
                return admissionNo;
            }
        }
        throw new BusinessException("住院号生成失败，请重试");
    }

    private String normalizeStatus(String status, String defaultStatus) {
        String value = trimToNull(status);
        value = value == null ? defaultStatus : value.toUpperCase();
        if (!ALLOWED_STATUSES.contains(value)) {
            throw new BusinessException("入院状态只能是 DRAFT、REGISTERED、IN_HOSPITAL、DISCHARGED、CANCELLED、DELETED");
        }
        return value;
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        if (value == null) {
            return null;
        }
        value = value.toUpperCase();
        if (!ALLOWED_STATUSES.contains(value)) {
            throw new BusinessException("入院状态只能是 DRAFT、REGISTERED、IN_HOSPITAL、DISCHARGED、CANCELLED、DELETED");
        }
        return value;
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
