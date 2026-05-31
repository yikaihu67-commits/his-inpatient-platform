package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdmissionQueryRequest {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;

    @Size(max = 32, message = "住院号不能超过32个字符")
    private String admissionNo;

    private Long patientId;

    @Size(max = 50, message = "患者姓名不能超过50个字符")
    private String patientName;

    @Size(max = 50, message = "入院科室不能超过50个字符")
    private String departmentName;

    @Size(max = 50, message = "经管医生不能超过50个字符")
    private String doctorName;

    @Size(max = 30, message = "入院状态不能超过30个字符")
    private String status;
}
