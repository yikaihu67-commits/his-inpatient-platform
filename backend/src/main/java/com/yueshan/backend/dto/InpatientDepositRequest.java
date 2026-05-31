package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InpatientDepositRequest {
    @Size(max = 32, message = "预交金编号不能超过32个字符")
    private String depositNo;
    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
    @Size(max = 30, message = "支付方式不能超过30个字符")
    private String paymentMethod;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;
    @Size(max = 50, message = "操作员不能超过50个字符")
    private String operatorName;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
