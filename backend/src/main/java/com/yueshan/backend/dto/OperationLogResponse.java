package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.OperationLog;

public record OperationLogResponse(Long id, String moduleName, String operationType, Long businessId,
        String businessNo, String operatorName, String requestMethod, String requestUri, String requestParams,
        String resultStatus, String errorMessage,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime operationTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
    public static OperationLogResponse from(OperationLog log) {
        return new OperationLogResponse(log.getId(), log.getModuleName(), log.getOperationType(), log.getBusinessId(),
                log.getBusinessNo(), log.getOperatorName(), log.getRequestMethod(), log.getRequestUri(),
                log.getRequestParams(), log.getResultStatus(), log.getErrorMessage(),
                log.getOperationTime(), log.getCreatedAt());
    }
}
