package com.yueshan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentCreateRequest {
    @NotBlank(message = "科室编码不能为空")
    @Size(max = 32, message = "科室编码不能超过32个字符")
    private String deptCode;
    @NotBlank(message = "科室名称不能为空")
    @Size(max = 100, message = "科室名称不能超过100个字符")
    private String deptName;
    @NotBlank(message = "科室类型不能为空")
    @Size(max = 30, message = "科室类型不能超过30个字符")
    private String deptType;
    private Long parentId;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
