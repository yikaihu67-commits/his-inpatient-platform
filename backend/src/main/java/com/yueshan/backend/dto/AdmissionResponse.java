package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.InpatientAdmission;

public record AdmissionResponse(
        Long id,
        String admissionNo,
        Long patientId,
        String patientNo,
        String patientName,
        String departmentName,
        String wardName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime admissionTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dischargeTime,
        String admissionDiagnosis,
        String chargeType,
        String nursingLevel,
        String doctorName,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt) {

    public static AdmissionResponse from(InpatientAdmission admission) {
        return new AdmissionResponse(
                admission.getId(),
                admission.getAdmissionNo(),
                admission.getPatientId(),
                admission.getPatientNo(),
                admission.getPatientName(),
                admission.getDepartmentName(),
                admission.getWardName(),
                admission.getAdmissionTime(),
                admission.getDischargeTime(),
                admission.getAdmissionDiagnosis(),
                admission.getChargeType(),
                admission.getNursingLevel(),
                admission.getDoctorName(),
                admission.getStatus(),
                admission.getCreatedAt(),
                admission.getUpdatedAt());
    }
}
