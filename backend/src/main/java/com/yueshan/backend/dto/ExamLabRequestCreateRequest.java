package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamLabRequestCreateRequest {
    @Size(max = 32, message = "申请单号不能超过32个字符")
    private String requestNo;
    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @NotBlank(message = "申请类型不能为空")
    private String requestType;
    @Size(max = 50, message = "项目编码不能超过50个字符")
    private String itemCode;
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String itemName;
    @Size(max = 500, message = "申请内容不能超过500个字符")
    private String requestContent;
    @NotBlank(message = "项目类别不能为空")
    private String itemCategory;
    @NotBlank(message = "申请医生不能为空")
    private String doctorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;
    private String executionDepartment;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private String status;
    private String remark;
}
