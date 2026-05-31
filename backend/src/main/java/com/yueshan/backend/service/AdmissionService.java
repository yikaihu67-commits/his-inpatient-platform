package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.AdmissionCreateRequest;
import com.yueshan.backend.dto.AdmissionQueryRequest;
import com.yueshan.backend.dto.AdmissionUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface AdmissionService {

    PageResponse<InpatientAdmission> findPage(AdmissionQueryRequest query);

    Optional<InpatientAdmission> findById(Long id);

    InpatientAdmission create(AdmissionCreateRequest request);

    Optional<InpatientAdmission> update(Long id, AdmissionUpdateRequest request);

    boolean deleteById(Long id);

    Optional<InpatientAdmission> admit(Long id);

    Optional<InpatientAdmission> cancel(Long id);
}
