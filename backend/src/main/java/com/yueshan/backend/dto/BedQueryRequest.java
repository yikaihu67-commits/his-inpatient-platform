package com.yueshan.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BedQueryRequest {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;

    @Size(max = 32, message = "床号不能超过32个字符")
    private String bedNo;

    @Size(max = 50, message = "病区不能超过50个字符")
    private String wardName;

    @Size(max = 32, message = "房间号不能超过32个字符")
    private String roomNo;

    @Size(max = 30, message = "床位状态不能超过30个字符")
    private String status;
}
