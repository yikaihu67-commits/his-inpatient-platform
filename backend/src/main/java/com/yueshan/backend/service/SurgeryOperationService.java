package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.SurgeryOperation;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.SurgeryActionRequest;
import com.yueshan.backend.dto.SurgeryCreateRequest;
import com.yueshan.backend.dto.SurgeryQueryRequest;
import com.yueshan.backend.dto.SurgeryUpdateRequest;

public interface SurgeryOperationService {
    PageResponse<SurgeryOperation> findPage(SurgeryQueryRequest query);
    Optional<SurgeryOperation> findById(Long id);
    SurgeryOperation create(SurgeryCreateRequest request);
    Optional<SurgeryOperation> update(Long id, SurgeryUpdateRequest request);
    boolean deleteById(Long id);
    SurgeryOperation schedule(Long id, SurgeryActionRequest request);
    SurgeryOperation start(Long id, SurgeryActionRequest request);
    SurgeryOperation complete(Long id, SurgeryActionRequest request);
    SurgeryOperation cancel(Long id, SurgeryActionRequest request);
    SurgeryOperation bill(Long id, SurgeryActionRequest request);
}
