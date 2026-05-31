package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MedicalRecord {
    private Long id;
    private String recordNo;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private String recordType;
    private String title;
    private String content;
    private String doctorName;
    private LocalDateTime recordTime;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
