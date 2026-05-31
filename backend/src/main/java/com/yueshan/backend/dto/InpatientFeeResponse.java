package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.InpatientFee;

public record InpatientFeeResponse(Long id, String feeNo, Long admissionId, Long patientId, String patientName,
        String sourceType, Long sourceId, String itemCode, String itemName, String itemCategory,
        BigDecimal quantity, String unit, BigDecimal unitPrice, BigDecimal totalAmount,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime feeTime,
        String status, String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static InpatientFeeResponse from(InpatientFee fee) {
        return new InpatientFeeResponse(fee.getId(), fee.getFeeNo(), fee.getAdmissionId(), fee.getPatientId(),
                fee.getPatientName(), fee.getSourceType(), fee.getSourceId(), fee.getItemCode(), fee.getItemName(),
                fee.getItemCategory(), fee.getQuantity(), fee.getUnit(), fee.getUnitPrice(), fee.getTotalAmount(),
                fee.getFeeTime(), fee.getStatus(), fee.getRemark(), fee.getDeleted(), fee.getCreatedAt(), fee.getUpdatedAt());
    }
}
