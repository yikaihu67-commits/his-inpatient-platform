package com.yueshan.backend.dto;

import java.math.BigDecimal;

public record FeeCategorySummary(String itemCategory, BigDecimal amount) {
}
