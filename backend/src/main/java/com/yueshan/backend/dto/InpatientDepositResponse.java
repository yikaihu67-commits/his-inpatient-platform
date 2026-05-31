package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.InpatientDeposit;

public record InpatientDepositResponse(Long id, String depositNo, Long admissionId, Long patientId, String patientName,
        BigDecimal amount, String paymentMethod, String transactionType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime transactionTime,
        String operatorName, String status, String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static InpatientDepositResponse from(InpatientDeposit deposit) {
        return new InpatientDepositResponse(deposit.getId(), deposit.getDepositNo(), deposit.getAdmissionId(),
                deposit.getPatientId(), deposit.getPatientName(), deposit.getAmount(), deposit.getPaymentMethod(),
                deposit.getTransactionType(), deposit.getTransactionTime(), deposit.getOperatorName(), deposit.getStatus(),
                deposit.getRemark(), deposit.getDeleted(), deposit.getCreatedAt(), deposit.getUpdatedAt());
    }
}
