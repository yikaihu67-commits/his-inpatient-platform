package com.yueshan.backend.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PatientAppointment {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long admissionId;
    private String admissionNo;
    private String appointmentType;
    private String appointmentItem;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String contactPhone;
    private String remark;
    private String status;
    private LocalDateTime cancelTime;
    private LocalDateTime completeTime;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
