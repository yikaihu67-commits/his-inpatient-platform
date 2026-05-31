package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.Drug;

public record DrugResponse(Long id, String drugCode, String drugName, String specification, String unit,
        BigDecimal price, Integer stockQuantity, Integer stockLowerLimit, String status, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static DrugResponse from(Drug drug) {
        return new DrugResponse(drug.getId(), drug.getDrugCode(), drug.getDrugName(), drug.getSpecification(),
                drug.getUnit(), drug.getPrice(), drug.getStockQuantity(), drug.getStockLowerLimit(),
                drug.getStatus(), drug.getDeleted(),
                drug.getCreatedAt(), drug.getUpdatedAt());
    }
}
