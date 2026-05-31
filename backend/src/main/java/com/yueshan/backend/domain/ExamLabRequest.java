package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ExamLabRequest {
    private Long id;
    private String requestNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private String requestType;
    private String itemCode;
    private String itemName;
    private String requestContent;
    private String itemCategory;
    private String doctorName;
    private LocalDateTime requestTime;
    private LocalDateTime scheduledTime;
    private String executionDepartment;
    private String executorName;
    private LocalDateTime executedTime;
    private String reportDoctorName;
    private LocalDateTime reportTime;
    private String resultSummary;
    private String resultDetail;
    private Boolean abnormalFlag;
    private String status;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private Boolean billed;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
