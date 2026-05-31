package com.yueshan.backend.dto.selfservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SelfServiceBillDetailResponse {
    private String itemName;
    private String itemCategory;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private String sourceType;
    private LocalDateTime feeTime;
    private String status;
    private Boolean settled;
}
