package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InpatientDeposit {
    private Long id;
    private String depositNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionType;
    private LocalDateTime transactionTime;
    private String operatorName;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
