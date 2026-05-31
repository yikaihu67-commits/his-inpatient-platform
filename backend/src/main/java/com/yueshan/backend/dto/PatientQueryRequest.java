package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientQueryRequest {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;

    @Size(max = 32, message = "患者编号不能超过32个字符")
    private String patientNo;

    @Size(max = 50, message = "姓名不能超过50个字符")
    private String name;

    @Size(max = 32, message = "身份证号不能超过32个字符")
    private String idCard;

    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;
}
