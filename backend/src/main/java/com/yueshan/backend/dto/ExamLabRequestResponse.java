package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.ExamLabRequest;

public record ExamLabRequestResponse(
        Long id,
        String requestNo,
        Long admissionId,
        Long patientId,
        String patientName,
        String requestType,
        String itemCode,
        String itemName,
        String requestContent,
        String itemCategory,
        String doctorName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime requestTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime scheduledTime,
        String executionDepartment,
        String executorName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime executedTime,
        String reportDoctorName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime reportTime,
        String resultSummary,
        String resultDetail,
        Boolean abnormalFlag,
        String status,
        BigDecimal unitPrice,
        BigDecimal quantity,
        BigDecimal totalAmount,
        Boolean billed,
        String remark,
        Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static ExamLabRequestResponse from(ExamLabRequest request) {
        return new ExamLabRequestResponse(
                request.getId(), request.getRequestNo(), request.getAdmissionId(), request.getPatientId(),
                request.getPatientName(), request.getRequestType(), request.getItemCode(), request.getItemName(),
                request.getRequestContent(), request.getItemCategory(), request.getDoctorName(), request.getRequestTime(),
                request.getScheduledTime(), request.getExecutionDepartment(), request.getExecutorName(), request.getExecutedTime(),
                request.getReportDoctorName(), request.getReportTime(), request.getResultSummary(), request.getResultDetail(),
                request.getAbnormalFlag(), request.getStatus(), request.getUnitPrice(), request.getQuantity(), request.getTotalAmount(),
                request.getBilled(), request.getRemark(), request.getDeleted(), request.getCreatedAt(), request.getUpdatedAt());
    }
}
