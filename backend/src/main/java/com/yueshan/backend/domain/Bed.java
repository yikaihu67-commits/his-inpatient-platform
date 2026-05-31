package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Bed {

    private Long id;

    private String bedNo;

    private String wardName;

    private String roomNo;

    private String bedType;

    private String status;

    private Long currentAdmissionId;

    private Boolean deleted;

    private String admissionNo;

    private String patientName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
