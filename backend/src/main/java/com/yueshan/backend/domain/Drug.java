package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Drug {
    private Long id;
    private String drugCode;
    private String drugName;
    private String specification;
    private String unit;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer stockLowerLimit;
    private String status;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
