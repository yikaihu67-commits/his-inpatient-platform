package com.yueshan.backend.dto.selfservice;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SelfServiceExamLabResponse {
    private String itemName;
    private String requestType;
    private LocalDateTime requestTime;
    private LocalDateTime executedTime;
    private LocalDateTime reportTime;
    private String status;
    private String resultSummary;
    private String resultDetail;
    private Boolean abnormalFlag;
    private Boolean billed;
}
