package com.yueshan.backend.dto.selfservice;

import lombok.Data;

@Data
public class PatientSelfServiceVerifyRequest {
    private Long patientId;
    private Long admissionId;
    private String admissionNo;
    private String idCardLast4;
    private String phone;
    private String patientName;
}
