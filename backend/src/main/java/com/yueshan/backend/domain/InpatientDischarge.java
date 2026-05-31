package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InpatientDischarge {
    private Long id;
    private String dischargeNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private BigDecimal totalFeeAmount;
    private BigDecimal totalDepositAmount;
    private BigDecimal totalRefundAmount;
    private BigDecimal depositBalance;
    private BigDecimal unpaidAmount;
    private BigDecimal actualPayment;
    private LocalDateTime dischargeTime;
    private String operatorName;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
