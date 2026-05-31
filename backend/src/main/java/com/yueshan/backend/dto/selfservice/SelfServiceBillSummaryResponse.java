package com.yueshan.backend.dto.selfservice;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class SelfServiceBillSummaryResponse {
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal settledAmount = BigDecimal.ZERO;
    private BigDecimal unsettledAmount = BigDecimal.ZERO;
    private BigDecimal drugAmount = BigDecimal.ZERO;
    private BigDecimal orderAmount = BigDecimal.ZERO;
    private BigDecimal nursingAmount = BigDecimal.ZERO;
    private BigDecimal surgeryAmount = BigDecimal.ZERO;
    private BigDecimal examAmount = BigDecimal.ZERO;
    private BigDecimal labAmount = BigDecimal.ZERO;
    private BigDecimal otherAmount = BigDecimal.ZERO;
    private Map<String, BigDecimal> categoryAmounts = new LinkedHashMap<>();
}
