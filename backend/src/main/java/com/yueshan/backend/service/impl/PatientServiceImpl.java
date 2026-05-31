package com.yueshan.backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Patient;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PatientCreateRequest;
import com.yueshan.backend.dto.PatientQueryRequest;
import com.yueshan.backend.dto.PatientUpdateRequest;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.PatientMapper;
import com.yueshan.backend.service.PatientService;

@Service
public class PatientServiceImpl implements PatientService {

    private static final DateTimeFormatter PATIENT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String DEFAULT_STATUS = "ACTIVE";

    private final PatientMapper patientMapper;

    public PatientServiceImpl(PatientMapper patientMapper) {
        this.patientMapper = patientMapper;
    }

    @Override
    public List<Patient> findAll() {
        return patientMapper.findAll();
    }

    @Override
    public PageResponse<Patient> findPage(PatientQueryRequest query) {
        PatientQueryRequest normalizedQuery = normalizeQuery(query);
        int page = normalizedQuery.getPage();
        int pageSize = normalizedQuery.getPageSize();
        int offset = (page - 1) * pageSize;

        long total = patientMapper.countPage(normalizedQuery);
        List<Patient> records = total == 0
                ? List.of()
                : patientMapper.findPage(normalizedQuery, offset, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<Patient> findById(Long id) {
        return Optional.ofNullable(patientMapper.findById(id));
    }

    @Override
    @Transactional
    public Patient create(PatientCreateRequest request) {
        Patient patient = toPatient(request);
        if (isBlank(patient.getPatientNo())) {
            patient.setPatientNo(generatePatientNo());
        }
        validateUniqueForCreate(patient);

        patientMapper.insert(patient);
        Patient created = patient.getId() == null ? null : patientMapper.findById(patient.getId());
        if (created == null) {
            created = patientMapper.findByPatientNo(patient.getPatientNo());
        }
        if (created == null) {
            throw new BusinessException("患者创建失败，请重试");
        }
        return created;
    }

    @Override
    @Transactional
    public Optional<Patient> update(Long id, PatientUpdateRequest request) {
        Patient existing = patientMapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }

        Patient patient = toPatient(request, existing);
        patient.setId(id);
        validateUniqueForUpdate(patient);

        patientMapper.update(patient);
        return Optional.ofNullable(patientMapper.findById(id));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return patientMapper.logicDeleteById(id) > 0;
    }

    private Patient toPatient(PatientCreateRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }

        Patient patient = new Patient();
        patient.setPatientNo(trimToNull(request.getPatientNo()));
        patient.setName(trimToNull(request.getName()));
        patient.setGender(normalizeGender(request.getGender()));
        patient.setIdCard(trimToNull(request.getIdCard()));
        patient.setPhone(trimToNull(request.getPhone()));
        patient.setBirthDate(request.getBirthDate());
        patient.setAddress(trimToNull(request.getAddress()));
        patient.setStatus(normalizeStatus(request.getStatus()));
        return patient;
    }

    private Patient toPatient(PatientUpdateRequest request, Patient existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }

        Patient patient = new Patient();
        patient.setPatientNo(firstNotBlank(request.getPatientNo(), existing.getPatientNo()));
        patient.setName(trimToNull(request.getName()));
        patient.setGender(normalizeGender(request.getGender()));
        patient.setIdCard(trimToNull(request.getIdCard()));
        patient.setPhone(trimToNull(request.getPhone()));
        patient.setBirthDate(request.getBirthDate());
        patient.setAddress(trimToNull(request.getAddress()));
        patient.setStatus(firstNotBlank(normalizeStatusOrNull(request.getStatus()), existing.getStatus()));
        return patient;
    }

    private PatientQueryRequest normalizeQuery(PatientQueryRequest query) {
        PatientQueryRequest normalized = new PatientQueryRequest();
        if (query == null) {
            return normalized;
        }

        normalized.setPage(query.getPage() == null ? 1 : query.getPage());
        normalized.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        normalized.setPatientNo(trimToNull(query.getPatientNo()));
        normalized.setName(trimToNull(query.getName()));
        normalized.setIdCard(trimToNull(query.getIdCard()));
        normalized.setPhone(trimToNull(query.getPhone()));
        return normalized;
    }

    private void validateUniqueForCreate(Patient patient) {
        if (patientMapper.countByPatientNo(patient.getPatientNo()) > 0) {
            throw new BusinessException("患者编号已存在");
        }
        if (!isBlank(patient.getIdCard()) && patientMapper.countByIdCard(patient.getIdCard()) > 0) {
            throw new BusinessException("身份证号已存在");
        }
        if (!isBlank(patient.getPhone()) && patientMapper.countByPhone(patient.getPhone()) > 0) {
            throw new BusinessException("手机号已存在");
        }
    }

    private void validateUniqueForUpdate(Patient patient) {
        if (patientMapper.countByPatientNoExcludeId(patient.getPatientNo(), patient.getId()) > 0) {
            throw new BusinessException("患者编号已存在");
        }
        if (!isBlank(patient.getIdCard()) && patientMapper.countByIdCardExcludeId(patient.getIdCard(), patient.getId()) > 0) {
            throw new BusinessException("身份证号已存在");
        }
        if (!isBlank(patient.getPhone()) && patientMapper.countByPhoneExcludeId(patient.getPhone(), patient.getId()) > 0) {
            throw new BusinessException("手机号已存在");
        }
    }

    private String generatePatientNo() {
        for (int i = 0; i < 5; i++) {
            String patientNo = "P" + LocalDateTime.now().format(PATIENT_NO_FORMATTER);
            if (patientMapper.countByPatientNo(patientNo) == 0) {
                return patientNo;
            }
        }
        throw new BusinessException("患者编号生成失败，请重试");
    }

    private String normalizeGender(String gender) {
        String value = trimToNull(gender);
        if (value == null) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "MALE", "M", "男" -> "MALE";
            case "FEMALE", "F", "女" -> "FEMALE";
            default -> throw new BusinessException("性别只能是 MALE、FEMALE、男或女");
        };
    }

    private String normalizeStatus(String status) {
        String value = trimToNull(status);
        return value == null ? DEFAULT_STATUS : value.toUpperCase();
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        return value == null ? null : value.toUpperCase();
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
