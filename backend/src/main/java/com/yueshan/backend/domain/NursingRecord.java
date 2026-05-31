package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NursingRecord {
    private Long id;
    private String recordNo;
    private Long orderId;
    private Long admissionId;
    private String admissionNo;
    private Long patientId;
    private String patientName;
    private String nursingType;
    private String nursingContent;
    private BigDecimal temperature;
    private Integer pulse;
    private Integer respiration;
    private String bloodPressure;
    private BigDecimal intakeAmount;
    private BigDecimal outputAmount;
    private String nursingLevel;
    private String nurseName;
    private LocalDateTime recordTime;
    private String status;
    private Boolean billable;
    private BigDecimal nursingFee;
    private Boolean billed;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
