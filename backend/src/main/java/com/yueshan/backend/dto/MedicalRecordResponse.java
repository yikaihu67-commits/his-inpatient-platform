package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.MedicalRecord;

public record MedicalRecordResponse(
        Long id,
        String recordNo,
        Long admissionId,
        Long patientId,
        String patientName,
        String recordType,
        String title,
        String content,
        String doctorName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime recordTime,
        String status,
        String remark,
        Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static MedicalRecordResponse from(MedicalRecord record) {
        return new MedicalRecordResponse(record.getId(), record.getRecordNo(), record.getAdmissionId(),
                record.getPatientId(), record.getPatientName(), record.getRecordType(), record.getTitle(),
                record.getContent(), record.getDoctorName(), record.getRecordTime(), record.getStatus(),
                record.getRemark(), record.getDeleted(), record.getCreatedAt(), record.getUpdatedAt());
    }
}
