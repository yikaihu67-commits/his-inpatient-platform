package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.OperationLog;
import com.yueshan.backend.dto.OperationLogQueryRequest;
import com.yueshan.backend.dto.PageResponse;

public interface OperationLogService {
    PageResponse<OperationLog> findPage(OperationLogQueryRequest query);
    Optional<OperationLog> findById(Long id);
    boolean deleteById(Long id);
    void record(String moduleName, String operationType, Long businessId, String businessNo, String operatorName,
                String resultStatus, String errorMessage);
}
