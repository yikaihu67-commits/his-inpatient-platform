package com.yueshan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictItemCreateRequest {
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 50, message = "字典类型不能超过50个字符")
    private String dictType;
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 50, message = "字典编码不能超过50个字符")
    private String dictCode;
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称不能超过100个字符")
    private String dictName;
    private Integer sortOrder;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
