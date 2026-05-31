package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.InpatientDischarge;

public record DischargeResponse(
        Long id,
        String dischargeNo,
        Long admissionId,
        Long patientId,
        String patientName,
        BigDecimal totalFeeAmount,
        BigDecimal totalDepositAmount,
        BigDecimal totalRefundAmount,
        BigDecimal depositBalance,
        BigDecimal unpaidAmount,
        BigDecimal actualPayment,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dischargeTime,
        String operatorName,
        String status,
        String remark,
        Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static DischargeResponse from(InpatientDischarge discharge) {
        return new DischargeResponse(discharge.getId(), discharge.getDischargeNo(), discharge.getAdmissionId(),
                discharge.getPatientId(), discharge.getPatientName(), discharge.getTotalFeeAmount(),
                discharge.getTotalDepositAmount(), discharge.getTotalRefundAmount(), discharge.getDepositBalance(),
                discharge.getUnpaidAmount(), discharge.getActualPayment(), discharge.getDischargeTime(),
                discharge.getOperatorName(), discharge.getStatus(), discharge.getRemark(), discharge.getDeleted(),
                discharge.getCreatedAt(), discharge.getUpdatedAt());
    }
}
