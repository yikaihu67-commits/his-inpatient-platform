package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.NursingOrderExecution;

public record NursingExecutionResponse(
        Long id, String executionNo, Long orderId, String orderNo, Long admissionId, Long patientId,
        String patientName, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime scheduledTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime executedTime, String nurseName,
        String status, String result, String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static NursingExecutionResponse from(NursingOrderExecution execution) {
        return new NursingExecutionResponse(execution.getId(), execution.getExecutionNo(), execution.getOrderId(),
                execution.getOrderNo(), execution.getAdmissionId(), execution.getPatientId(), execution.getPatientName(),
                execution.getScheduledTime(), execution.getExecutedTime(), execution.getNurseName(), execution.getStatus(),
                execution.getResult(), execution.getRemark(), execution.getDeleted(), execution.getCreatedAt(), execution.getUpdatedAt());
    }
}
