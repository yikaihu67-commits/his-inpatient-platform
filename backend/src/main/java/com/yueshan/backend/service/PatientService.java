package com.yueshan.backend.service;

import java.util.List;
import java.util.Optional;

import com.yueshan.backend.domain.Patient;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PatientCreateRequest;
import com.yueshan.backend.dto.PatientQueryRequest;
import com.yueshan.backend.dto.PatientUpdateRequest;

public interface PatientService {

    List<Patient> findAll();

    PageResponse<Patient> findPage(PatientQueryRequest query);

    Optional<Patient> findById(Long id);

    Patient create(PatientCreateRequest request);

    Optional<Patient> update(Long id, PatientUpdateRequest request);

    boolean deleteById(Long id);
}
