package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamLabRequestQueryRequest {
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;
    private Long admissionId;
    private Long patientId;
    @Size(max = 20, message = "申请类型不能超过20个字符")
    private String requestType;
    @Size(max = 30, message = "项目类别不能超过30个字符")
    private String itemCategory;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String itemName;
}
