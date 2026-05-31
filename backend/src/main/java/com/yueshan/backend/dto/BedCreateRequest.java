package com.yueshan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BedCreateRequest {

    @NotBlank(message = "床号不能为空")
    @Size(max = 32, message = "床号不能超过32个字符")
    private String bedNo;

    @NotBlank(message = "病区不能为空")
    @Size(max = 50, message = "病区不能超过50个字符")
    private String wardName;

    @Size(max = 32, message = "房间号不能超过32个字符")
    private String roomNo;

    @Size(max = 30, message = "床位类型不能超过30个字符")
    private String bedType;

    @Size(max = 30, message = "床位状态不能超过30个字符")
    private String status;
}
