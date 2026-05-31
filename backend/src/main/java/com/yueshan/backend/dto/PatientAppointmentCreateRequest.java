package com.yueshan.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatientAppointmentCreateRequest {
    @NotNull(message = "患者不能为空")
    private Long patientId;
    private Long admissionId;
    @NotBlank(message = "预约类型不能为空")
    private String appointmentType;
    @NotBlank(message = "预约项目不能为空")
    private String appointmentItem;
    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;
    @NotBlank(message = "预约时间段不能为空")
    private String timeSlot;
    private String contactPhone;
    private String remark;
}
