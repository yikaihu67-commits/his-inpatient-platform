package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InpatientFeeCreateRequest {
    @Size(max = 32, message = "费用编号不能超过32个字符")
    private String feeNo;
    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @Size(max = 30, message = "费用来源类型不能超过30个字符")
    private String sourceType;
    private Long sourceId;
    @Size(max = 50, message = "项目编码不能超过50个字符")
    private String itemCode;
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String itemName;
    @NotBlank(message = "项目类别不能为空")
    @Size(max = 30, message = "项目类别不能超过30个字符")
    private String itemCategory;
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    private BigDecimal quantity;
    @Size(max = 20, message = "单位不能超过20个字符")
    private String unit;
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.00", message = "单价不能小于0")
    private BigDecimal unitPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feeTime;
    @Size(max = 30, message = "费用状态不能超过30个字符")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
