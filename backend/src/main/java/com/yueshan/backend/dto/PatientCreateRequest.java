package com.yueshan.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientCreateRequest {

    @Size(max = 32, message = "患者编号不能超过32个字符")
    private String patientNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String name;

    @Size(max = 10, message = "性别不能超过10个字符")
    private String gender;

    @Size(max = 32, message = "身份证号不能超过32个字符")
    private String idCard;

    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;

    private LocalDate birthDate;

    @Size(max = 255, message = "地址不能超过255个字符")
    private String address;

    @Size(max = 20, message = "状态不能超过20个字符")
    private String status;
}
