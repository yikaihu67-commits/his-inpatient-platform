package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InpatientFee {
    private Long id;
    private String feeNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private String sourceType;
    private Long sourceId;
    private String itemCode;
    private String itemName;
    private String itemCategory;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDateTime feeTime;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
