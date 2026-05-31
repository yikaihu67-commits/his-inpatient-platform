package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.MedicalRecord;
import com.yueshan.backend.dto.MedicalRecordActionRequest;
import com.yueshan.backend.dto.MedicalRecordCreateRequest;
import com.yueshan.backend.dto.MedicalRecordQueryRequest;
import com.yueshan.backend.dto.MedicalRecordUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface MedicalRecordService {
    MedicalRecord create(MedicalRecordCreateRequest request);
    PageResponse<MedicalRecord> findPage(MedicalRecordQueryRequest query);
    Optional<MedicalRecord> findById(Long id);
    Optional<MedicalRecord> update(Long id, MedicalRecordUpdateRequest request);
    boolean deleteById(Long id);
    MedicalRecord submit(Long id, MedicalRecordActionRequest request);
    MedicalRecord review(Long id, MedicalRecordActionRequest request);
    MedicalRecord archive(Long id, MedicalRecordActionRequest request);
    MedicalRecord cancel(Long id, MedicalRecordActionRequest request);
}
