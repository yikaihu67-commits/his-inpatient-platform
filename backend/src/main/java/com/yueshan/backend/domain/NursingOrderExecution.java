package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NursingOrderExecution {
    private Long id;
    private String executionNo;
    private Long orderId;
    private String orderNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private LocalDateTime scheduledTime;
    private LocalDateTime executedTime;
    private String nurseName;
    private String status;
    private String result;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
