package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.SurgeryOperation;

public record SurgeryResponse(
        Long id,
        String surgeryNo,
        Long patientId,
        String patientName,
        Long admissionId,
        String admissionNo,
        String surgeryName,
        String preoperativeDiagnosis,
        String surgeryLevel,
        String surgeryType,
        String operatingRoom,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime plannedTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime actualStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime actualEndTime,
        String primaryDoctorName,
        String assistantDoctorName,
        String anesthesiaMethod,
        String anesthesiologistName,
        String status,
        BigDecimal surgeryFee,
        String remark,
        Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static SurgeryResponse from(SurgeryOperation item) {
        return new SurgeryResponse(item.getId(), item.getSurgeryNo(), item.getPatientId(), item.getPatientName(),
                item.getAdmissionId(), item.getAdmissionNo(), item.getSurgeryName(), item.getPreoperativeDiagnosis(),
                item.getSurgeryLevel(), item.getSurgeryType(), item.getOperatingRoom(), item.getPlannedTime(),
                item.getActualStartTime(), item.getActualEndTime(), item.getPrimaryDoctorName(),
                item.getAssistantDoctorName(), item.getAnesthesiaMethod(), item.getAnesthesiologistName(),
                item.getStatus(), item.getSurgeryFee(), item.getRemark(), item.getDeleted(),
                item.getCreatedAt(), item.getUpdatedAt());
    }
}
