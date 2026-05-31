package com.yueshan.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalRecordActionRequest {
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
