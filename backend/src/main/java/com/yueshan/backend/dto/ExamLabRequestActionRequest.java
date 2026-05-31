package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamLabRequestActionRequest {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executedTime;
    @Size(max = 50, message = "执行科室不能超过50个字符")
    private String executionDepartment;
    @Size(max = 50, message = "执行人不能超过50个字符")
    private String executorName;
    @Size(max = 50, message = "报告医生不能超过50个字符")
    private String reportDoctorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;
    @Size(max = 255, message = "结果摘要不能超过255个字符")
    private String resultSummary;
    private String resultDetail;
    private Boolean abnormalFlag;
    @DecimalMin(value = "0.00", message = "单价不能小于0")
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
