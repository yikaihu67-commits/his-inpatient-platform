package com.yueshan.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalOrderActionRequest {

    @Size(max = 50, message = "执行护士不能超过50个字符")
    private String nurseName;

    @Size(max = 50, message = "执行人不能超过50个字符")
    private String executorName;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
