package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OperationLogQueryRequest {
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;
    @Size(max = 100, message = "模块名称不能超过100个字符")
    private String moduleName;
    @Size(max = 30, message = "操作类型不能超过30个字符")
    private String operationType;
    @Size(max = 50, message = "操作人不能超过50个字符")
    private String operatorName;
    private Long businessId;
    @Size(max = 30, message = "结果状态不能超过30个字符")
    private String resultStatus;
}
