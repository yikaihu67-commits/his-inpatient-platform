package com.yueshan.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.Patient;

public record PatientResponse(
        Long id,
        String patientNo,
        String name,
        String gender,
        String idCard,
        String phone,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        String address,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getPatientNo(),
                patient.getName(),
                patient.getGender(),
                patient.getIdCard(),
                patient.getPhone(),
                patient.getBirthDate(),
                patient.getAddress(),
                patient.getStatus(),
                patient.getCreatedAt(),
                patient.getUpdatedAt());
    }
}
