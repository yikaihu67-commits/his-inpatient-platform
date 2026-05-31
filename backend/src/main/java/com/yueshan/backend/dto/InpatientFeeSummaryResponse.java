package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record InpatientFeeSummaryResponse(
        Long admissionId,
        Long patientId,
        BigDecimal totalFeeAmount,
        BigDecimal unsettledAmount,
        BigDecimal settledAmount,
        BigDecimal cancelledAmount,
        List<FeeCategorySummary> categorySummary) {
}
