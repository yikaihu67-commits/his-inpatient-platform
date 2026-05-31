package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.PharmacyDispense;

public record PharmacyDispenseResponse(Long id, String dispenseNo, Long orderId, String orderNo,
        Long admissionId, Long patientId, String patientName, Long drugId, String drugCode, String drugName,
        Integer quantity, String pharmacistName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dispenseTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime returnTime,
        String status, String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static PharmacyDispenseResponse from(PharmacyDispense dispense) {
        return new PharmacyDispenseResponse(dispense.getId(), dispense.getDispenseNo(), dispense.getOrderId(),
                dispense.getOrderNo(), dispense.getAdmissionId(), dispense.getPatientId(), dispense.getPatientName(),
                dispense.getDrugId(), dispense.getDrugCode(), dispense.getDrugName(), dispense.getQuantity(),
                dispense.getPharmacistName(), dispense.getDispenseTime(), dispense.getReturnTime(), dispense.getStatus(),
                dispense.getRemark(), dispense.getDeleted(), dispense.getCreatedAt(), dispense.getUpdatedAt());
    }
}
