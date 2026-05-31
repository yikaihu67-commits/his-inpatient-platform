package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.Bed;

public record BedResponse(
        Long id,
        String bedNo,
        String wardName,
        String roomNo,
        String bedType,
        String status,
        Long currentAdmissionId,
        Boolean deleted,
        String admissionNo,
        String patientName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt) {

    public static BedResponse from(Bed bed) {
        return new BedResponse(
                bed.getId(),
                bed.getBedNo(),
                bed.getWardName(),
                bed.getRoomNo(),
                bed.getBedType(),
                bed.getStatus(),
                bed.getCurrentAdmissionId(),
                bed.getDeleted(),
                bed.getAdmissionNo(),
                bed.getPatientName(),
                bed.getCreatedAt(),
                bed.getUpdatedAt());
    }
}
