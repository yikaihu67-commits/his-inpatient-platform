package com.yueshan.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BedAssignRequest {

    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
}
