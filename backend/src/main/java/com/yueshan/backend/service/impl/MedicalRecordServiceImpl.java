package com.yueshan.backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.MedicalRecord;
import com.yueshan.backend.dto.MedicalRecordActionRequest;
import com.yueshan.backend.dto.MedicalRecordCreateRequest;
import com.yueshan.backend.dto.MedicalRecordQueryRequest;
import com.yueshan.backend.dto.MedicalRecordUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.MedicalRecordMapper;
import com.yueshan.backend.service.MedicalRecordService;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> RECORD_TYPES = Set.of("ADMISSION_RECORD", "FIRST_COURSE_RECORD",
            "DAILY_COURSE_RECORD", "SUPERIOR_ROUND_RECORD", "DISCHARGE_RECORD", "NURSING_RECORD",
            "PROGRESS_NOTE", "OPERATION_RECORD", "DISCHARGE_SUMMARY", "CONSULTATION_RECORD", "OTHER");
    private static final Set<String> STATUSES = Set.of("DRAFT", "SUBMITTED", "REVIEWED", "ARCHIVED", "CANCELLED");

    private final MedicalRecordMapper mapper;
    private final AdmissionMapper admissionMapper;

    public MedicalRecordServiceImpl(MedicalRecordMapper mapper, AdmissionMapper admissionMapper) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
    }

    @Transactional
    public MedicalRecord create(MedicalRecordCreateRequest request) {
        MedicalRecord record = toRecord(request, null);
        validateAdmission(record.getAdmissionId(), record.getPatientId());
        mapper.insert(record);
        return mapper.findById(record.getId());
    }

    public PageResponse<MedicalRecord> findPage(MedicalRecordQueryRequest query) {
        MedicalRecordQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<MedicalRecord> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<MedicalRecord> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<MedicalRecord> update(Long id, MedicalRecordUpdateRequest request) {
        MedicalRecord existing = mapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }
        requireStatus(existing, "DRAFT", "只有草稿病历允许修改");
        MedicalRecord record = toRecord(request, existing);
        record.setId(id);
        validateAdmission(record.getAdmissionId(), record.getPatientId());
        mapper.update(record);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public boolean deleteById(Long id) {
        MedicalRecord existing = mapper.findById(id);
        if (existing == null) {
            return false;
        }
        requireStatus(existing, "DRAFT", "只有草稿病历允许删除");
        return mapper.logicDeleteById(id) > 0;
    }

    @Transactional
    public MedicalRecord submit(Long id, MedicalRecordActionRequest request) {
        MedicalRecord existing = requireRecord(id);
        requireStatus(existing, "DRAFT", "只有草稿病历可以提交");
        mapper.updateStatus(id, "SUBMITTED", remark(request));
        return mapper.findById(id);
    }

    @Transactional
    public MedicalRecord archive(Long id, MedicalRecordActionRequest request) {
        MedicalRecord existing = requireRecord(id);
        if (!Set.of("SUBMITTED", "REVIEWED").contains(existing.getStatus())) {
            throw new BusinessException("只有已提交或已审核病历可以归档");
        }
        mapper.updateStatus(id, "ARCHIVED", remark(request));
        return mapper.findById(id);
    }

    @Transactional
    public MedicalRecord review(Long id, MedicalRecordActionRequest request) {
        MedicalRecord existing = requireRecord(id);
        requireStatus(existing, "SUBMITTED", "只有已提交病历可以审核");
        mapper.updateStatus(id, "REVIEWED", remark(request));
        return mapper.findById(id);
    }

    @Transactional
    public MedicalRecord cancel(Long id, MedicalRecordActionRequest request) {
        MedicalRecord existing = requireRecord(id);
        requireStatus(existing, "SUBMITTED", "只有已提交病历可以作废");
        mapper.updateStatus(id, "CANCELLED", remark(request));
        return mapper.findById(id);
    }

    private MedicalRecord toRecord(MedicalRecordCreateRequest request, MedicalRecord existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        MedicalRecord record = new MedicalRecord();
        record.setRecordNo(resolveRecordNo(request.getRecordNo(), existing));
        record.setAdmissionId(request.getAdmissionId());
        record.setPatientId(request.getPatientId());
        record.setRecordType(normalizeRecordType(request.getRecordType()));
        record.setTitle(requireText(request.getTitle(), "标题不能为空"));
        record.setContent(requireText(request.getContent(), "内容不能为空"));
        record.setDoctorName(requireText(request.getDoctorName(), "记录医生不能为空"));
        record.setRecordTime(request.getRecordTime() == null ? LocalDateTime.now() : request.getRecordTime());
        record.setStatus(normalizeStatus(request.getStatus()));
        if (!"DRAFT".equals(record.getStatus())) {
            throw new BusinessException("新病历只能创建为草稿状态");
        }
        record.setRemark(trimToNull(request.getRemark()));
        record.setDeleted(false);
        return record;
    }

    private void validateAdmission(Long admissionId, Long patientId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!admission.getPatientId().equals(patientId)) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
    }

    private MedicalRecord requireRecord(Long id) {
        MedicalRecord record = mapper.findById(id);
        if (record == null) {
            throw new BusinessException("病历记录不存在");
        }
        return record;
    }

    private void requireStatus(MedicalRecord record, String status, String message) {
        if (!status.equals(record.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private MedicalRecordQueryRequest normalizeQuery(MedicalRecordQueryRequest query) {
        MedicalRecordQueryRequest q = new MedicalRecordQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setRecordType(normalizeRecordTypeOrNull(query.getRecordType()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        q.setDoctorName(trimToNull(query.getDoctorName()));
        q.setTitle(trimToNull(query.getTitle()));
        return q;
    }

    private String resolveRecordNo(String recordNo, MedicalRecord existing) {
        String value = trimToNull(recordNo);
        if (value == null) {
            if (existing != null) {
                return existing.getRecordNo();
            }
            return generateRecordNo();
        }
        boolean duplicated = existing == null
                ? mapper.countByRecordNo(value) > 0
                : mapper.countByRecordNoExcludeId(value, existing.getId()) > 0;
        if (duplicated) {
            throw new BusinessException("病历编号已存在");
        }
        return value;
    }

    private String generateRecordNo() {
        for (int i = 0; i < 5; i++) {
            String no = "BL" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByRecordNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("病历编号生成失败，请重试");
    }

    private String normalizeRecordType(String value) {
        String normalized = requireText(value, "记录类型不能为空").toUpperCase();
        if (!RECORD_TYPES.contains(normalized)) {
            throw new BusinessException("记录类型不正确");
        }
        return normalized;
    }

    private String normalizeRecordTypeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeRecordType(normalized);
    }

    private String normalizeStatus(String value) {
        String normalized = trimToNull(value);
        normalized = normalized == null ? "DRAFT" : normalized.toUpperCase();
        if (!STATUSES.contains(normalized)) {
            throw new BusinessException("病历状态不正确");
        }
        return normalized;
    }

    private String normalizeStatusOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeStatus(normalized);
    }

    private String remark(MedicalRecordActionRequest request) {
        return request == null ? null : trimToNull(request.getRemark());
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
