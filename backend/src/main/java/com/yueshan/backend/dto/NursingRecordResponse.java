package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.NursingRecord;

public record NursingRecordResponse(
        Long id,
        String recordNo,
        Long orderId,
        Long admissionId,
        String admissionNo,
        Long patientId,
        String patientName,
        String nursingType,
        String nursingContent,
        BigDecimal temperature,
        Integer pulse,
        Integer respiration,
        String bloodPressure,
        BigDecimal intakeAmount,
        BigDecimal outputAmount,
        String nursingLevel,
        String nurseName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime recordTime,
        String status,
        Boolean billable,
        BigDecimal nursingFee,
        Boolean billed,
        String remark,
        Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt) {
    public static NursingRecordResponse from(NursingRecord item) {
        return new NursingRecordResponse(
                item.getId(), item.getRecordNo(), item.getOrderId(), item.getAdmissionId(), item.getAdmissionNo(),
                item.getPatientId(), item.getPatientName(), item.getNursingType(), item.getNursingContent(),
                item.getTemperature(), item.getPulse(), item.getRespiration(), item.getBloodPressure(),
                item.getIntakeAmount(), item.getOutputAmount(), item.getNursingLevel(), item.getNurseName(),
                item.getRecordTime(), item.getStatus(), item.getBillable(), item.getNursingFee(), item.getBilled(),
                item.getRemark(), item.getDeleted(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
