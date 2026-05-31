package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MedicalOrder {

    private Long id;

    private String orderNo;

    private Long admissionId;

    private String admissionNo;

    private Long patientId;

    private String patientName;

    private String orderType;

    private String orderCategory;

    private String orderContent;

    private String itemName;

    private String dosage;

    private String dosageUnit;

    private String frequency;

    private String route;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String doctorName;

    private String executionDepartment;

    private String executorName;

    private LocalDateTime executedTime;

    private String nurseName;

    private String status;

    private BigDecimal unitPrice;

    private BigDecimal quantity;

    private BigDecimal totalAmount;

    private Boolean billed;

    private String remark;

    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
