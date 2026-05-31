package com.yueshan.backend.dto;

import java.math.BigDecimal;

public record InpatientAccountSummaryResponse(
        Long admissionId,
        Long patientId,
        BigDecimal totalFeeAmount,
        BigDecimal totalDepositAmount,
        BigDecimal totalRefundAmount,
        BigDecimal depositBalance,
        BigDecimal unpaidAmount,
        BigDecimal availableBalance) {
}
