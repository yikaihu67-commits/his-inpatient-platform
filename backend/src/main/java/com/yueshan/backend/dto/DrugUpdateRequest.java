package com.yueshan.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DrugUpdateRequest {
    @NotBlank(message = "药品编码不能为空")
    @Size(max = 32, message = "药品编码不能超过32个字符")
    private String drugCode;
    @NotBlank(message = "药品名称不能为空")
    @Size(max = 100, message = "药品名称不能超过100个字符")
    private String drugName;
    @Size(max = 100, message = "规格不能超过100个字符")
    private String specification;
    @Size(max = 20, message = "单位不能超过20个字符")
    private String unit;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stockQuantity;
    @Min(value = 0, message = "库存下限不能小于0")
    private Integer stockLowerLimit;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
}
