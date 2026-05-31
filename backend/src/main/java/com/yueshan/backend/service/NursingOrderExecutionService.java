package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.NursingOrderExecution;
import com.yueshan.backend.dto.NursingExecutionActionRequest;
import com.yueshan.backend.dto.NursingExecutionCreateRequest;
import com.yueshan.backend.dto.NursingExecutionQueryRequest;
import com.yueshan.backend.dto.NursingExecutionUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface NursingOrderExecutionService {
    PageResponse<NursingOrderExecution> findPage(NursingExecutionQueryRequest query);
    Optional<NursingOrderExecution> findById(Long id);
    NursingOrderExecution create(NursingExecutionCreateRequest request);
    Optional<NursingOrderExecution> update(Long id, NursingExecutionUpdateRequest request);
    boolean deleteById(Long id);
    NursingOrderExecution execute(Long id, NursingExecutionActionRequest request);
    NursingOrderExecution fail(Long id, NursingExecutionActionRequest request);
    NursingOrderExecution cancel(Long id, NursingExecutionActionRequest request);
}
