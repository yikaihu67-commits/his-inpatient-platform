package com.yueshan.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PharmacyDispenseCreateRequest {
    @NotNull(message = "医嘱ID不能为空")
    private Long orderId;
    @NotNull(message = "药品ID不能为空")
    private Long drugId;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
    @Size(max = 50, message = "药师姓名不能超过50个字符")
    private String pharmacistName;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
