package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.MedicalOrder;

public record MedicalOrderResponse(
        Long id,
        String orderNo,
        Long admissionId,
        String admissionNo,
        Long patientId,
        String patientName,
        String orderType,
        String orderCategory,
        String orderContent,
        String itemName,
        String dosage,
        String dosageUnit,
        String frequency,
        String route,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endTime,
        String doctorName,
        String executionDepartment,
        String executorName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime executedTime,
        String nurseName,
        String status,
        BigDecimal unitPrice,
        BigDecimal quantity,
        BigDecimal totalAmount,
        Boolean billed,
        String remark,
        Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt) {

    public static MedicalOrderResponse from(MedicalOrder order) {
        return new MedicalOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getAdmissionId(),
                order.getAdmissionNo(),
                order.getPatientId(),
                order.getPatientName(),
                order.getOrderType(),
                order.getOrderCategory(),
                order.getOrderContent(),
                order.getItemName(),
                order.getDosage(),
                order.getDosageUnit(),
                order.getFrequency(),
                order.getRoute(),
                order.getStartTime(),
                order.getEndTime(),
                order.getDoctorName(),
                order.getExecutionDepartment(),
                order.getExecutorName(),
                order.getExecutedTime(),
                order.getNurseName(),
                order.getStatus(),
                order.getUnitPrice(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getBilled(),
                order.getRemark(),
                order.getDeleted(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
