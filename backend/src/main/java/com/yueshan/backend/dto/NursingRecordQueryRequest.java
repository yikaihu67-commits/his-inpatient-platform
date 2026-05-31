package com.yueshan.backend.dto;

import lombok.Data;

@Data
public class NursingRecordQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Long orderId;
    private Long admissionId;
    private Long patientId;
    private String nursingType;
    private String status;
    private String nurseName;
}
