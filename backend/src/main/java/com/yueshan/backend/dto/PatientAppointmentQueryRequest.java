package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PatientAppointmentQueryRequest {
    @Min(1)
    private Integer page = 1;
    @Min(1)
    @Max(200)
    private Integer pageSize = 10;
    private Long patientId;
    private Long admissionId;
    private String appointmentType;
    private String status;
}
