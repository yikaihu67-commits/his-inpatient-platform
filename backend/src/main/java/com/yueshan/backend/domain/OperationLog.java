package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OperationLog {
    private Long id;
    private String moduleName;
    private String operationType;
    private Long businessId;
    private String businessNo;
    private String operatorName;
    private String requestMethod;
    private String requestUri;
    private String requestParams;
    private String resultStatus;
    private String errorMessage;
    private LocalDateTime operationTime;
    private LocalDateTime createdAt;
}
