package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalOrderQueryRequest {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;

    private Long admissionId;

    private Long patientId;

    @Size(max = 30, message = "医嘱类型不能超过30个字符")
    private String orderType;

    @Size(max = 30, message = "医嘱分类不能超过30个字符")
    private String orderCategory;

    @Size(max = 30, message = "医嘱状态不能超过30个字符")
    private String status;

    @Size(max = 100, message = "医嘱项目名称不能超过100个字符")
    private String itemName;
}
