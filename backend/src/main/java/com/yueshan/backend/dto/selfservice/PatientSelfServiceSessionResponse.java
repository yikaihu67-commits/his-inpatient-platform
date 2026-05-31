package com.yueshan.backend.dto.selfservice;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PatientSelfServiceSessionResponse {
    private Long patientId;
    private Long admissionId;
    private String patientNo;
    private String patientName;
    private String gender;
    private String maskedPhone;
    private String admissionNo;
    private String departmentName;
    private String wardName;
    private LocalDateTime admissionTime;
    private String admissionStatus;
}
