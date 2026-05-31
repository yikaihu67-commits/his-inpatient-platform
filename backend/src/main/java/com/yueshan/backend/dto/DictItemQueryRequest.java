package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictItemQueryRequest {
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;
    @Size(max = 50, message = "字典类型不能超过50个字符")
    private String dictType;
    @Size(max = 50, message = "字典编码不能超过50个字符")
    private String dictCode;
    @Size(max = 100, message = "字典名称不能超过100个字符")
    private String dictName;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
}
