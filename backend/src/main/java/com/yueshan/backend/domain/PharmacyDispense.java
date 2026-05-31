package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PharmacyDispense {
    private Long id;
    private String dispenseNo;
    private Long orderId;
    private String orderNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private Long drugId;
    private String drugCode;
    private String drugName;
    private Integer quantity;
    private String pharmacistName;
    private LocalDateTime dispenseTime;
    private LocalDateTime returnTime;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
