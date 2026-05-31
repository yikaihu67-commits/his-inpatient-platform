package com.yueshan.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NursingExecutionActionRequest {
    @Size(max = 50, message = "护士姓名不能超过50个字符")
    private String nurseName;
    @Size(max = 255, message = "执行结果不能超过255个字符")
    private String result;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
