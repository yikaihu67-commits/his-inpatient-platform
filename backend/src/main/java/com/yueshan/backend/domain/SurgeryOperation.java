package com.yueshan.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SurgeryOperation {
    private Long id;
    private String surgeryNo;
    private Long patientId;
    private String patientName;
    private Long admissionId;
    private String admissionNo;
    private String surgeryName;
    private String preoperativeDiagnosis;
    private String surgeryLevel;
    private String surgeryType;
    private String operatingRoom;
    private LocalDateTime plannedTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String primaryDoctorName;
    private String assistantDoctorName;
    private String anesthesiaMethod;
    private String anesthesiologistName;
    private String status;
    private BigDecimal surgeryFee;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
