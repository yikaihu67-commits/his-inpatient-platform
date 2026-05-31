package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DischargeSettleRequest {
    @DecimalMin(value = "0.00", message = "实际支付金额不能小于0")
    private BigDecimal actualPayment;
    @Size(max = 30, message = "支付方式不能超过30个字符")
    private String paymentMethod;
    @Size(max = 50, message = "结算操作员不能超过50个字符")
    private String operatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
