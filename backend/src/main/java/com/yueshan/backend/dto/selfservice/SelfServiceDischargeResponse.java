package com.yueshan.backend.dto.selfservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SelfServiceDischargeResponse {
    private String status;
    private String dischargeDiagnosis;
    private String dischargeSummary;
    private String dischargeOrder;
    private String takeHomeDrugInstruction;
    private BigDecimal unpaidAmount = BigDecimal.ZERO;
    private Boolean canSettle = false;
    private LocalDateTime dischargeTime;
}
