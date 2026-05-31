package com.yueshan.backend.dto.selfservice;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SelfServiceAdmissionInfoResponse {
    private String patientName;
    private String gender;
    private Integer age;
    private String maskedPhone;
    private String departmentName;
    private String wardName;
    private String bedNo;
    private String roomNo;
    private LocalDateTime admissionTime;
    private String doctorName;
    private String admissionStatus;
    private String dischargeStatus;
}
