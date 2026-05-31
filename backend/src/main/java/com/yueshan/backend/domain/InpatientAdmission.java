package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InpatientAdmission {

    private Long id;

    private String admissionNo;

    private Long patientId;

    private String patientNo;

    private String patientName;

    private String departmentName;

    private String wardName;

    private LocalDateTime admissionTime;

    private LocalDateTime dischargeTime;

    private String admissionDiagnosis;

    private String chargeType;

    private String nursingLevel;

    private String doctorName;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
