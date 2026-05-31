package com.yueshan.backend.dto;

import java.math.BigDecimal;

public record InpatientDepositSummaryResponse(
        Long admissionId,
        Long patientId,
        BigDecimal totalPaidAmount,
        BigDecimal totalRefundAmount,
        BigDecimal balance) {
}
