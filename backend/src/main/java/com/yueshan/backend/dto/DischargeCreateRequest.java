package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DischargeCreateRequest {
    @Size(max = 32, message = "出院结算编号不能超过32个字符")
    private String dischargeNo;
    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;
    @Size(max = 50, message = "结算操作员不能超过50个字符")
    private String operatorName;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
