package com.yueshan.backend.dto.selfservice;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SelfServiceVerifyRow {
    private Long patientId;
    private String patientNo;
    private String patientName;
    private String gender;
    private String phone;
    private LocalDate birthDate;
    private Long admissionId;
    private String admissionNo;
    private String departmentName;
    private String wardName;
    private LocalDateTime admissionTime;
    private LocalDateTime dischargeTime;
    private String doctorName;
    private String admissionStatus;
}
