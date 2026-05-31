package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentQueryRequest {
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;
    @Size(max = 32, message = "科室编码不能超过32个字符")
    private String deptCode;
    @Size(max = 100, message = "科室名称不能超过100个字符")
    private String deptName;
    @Size(max = 30, message = "科室类型不能超过30个字符")
    private String deptType;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
}
