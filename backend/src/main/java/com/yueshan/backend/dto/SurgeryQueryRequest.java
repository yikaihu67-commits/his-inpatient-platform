package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SurgeryQueryRequest {
    @Min(value = 1, message = "page必须大于0")
    private Integer page = 1;
    @Min(value = 1, message = "size必须大于0")
    @Max(value = 200, message = "size不能超过200")
    private Integer pageSize = 10;
    private Long patientId;
    private Long admissionId;
    private String surgeryNo;
    private String surgeryName;
    private String status;
}
