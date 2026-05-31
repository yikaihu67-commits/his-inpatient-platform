package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PharmacyDispenseQueryRequest {
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;
    private Long orderId;
    private Long admissionId;
    private Long patientId;
    private Long drugId;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
}
