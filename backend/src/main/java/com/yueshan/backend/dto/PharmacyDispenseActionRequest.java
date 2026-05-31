package com.yueshan.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PharmacyDispenseActionRequest {
    @Size(max = 50, message = "药师姓名不能超过50个字符")
    private String pharmacistName;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
