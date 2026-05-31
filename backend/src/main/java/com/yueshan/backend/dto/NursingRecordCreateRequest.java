package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NursingRecordCreateRequest {
    @Size(max = 32, message = "护理记录编号不能超过32个字符")
    private String recordNo;
    private Long orderId;
    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @NotBlank(message = "护理类型不能为空")
    private String nursingType;
    @NotBlank(message = "护理内容不能为空")
    @Size(max = 500, message = "护理内容不能超过500个字符")
    private String nursingContent;
    private BigDecimal temperature;
    private Integer pulse;
    private Integer respiration;
    @Size(max = 30, message = "血压不能超过30个字符")
    private String bloodPressure;
    private BigDecimal intakeAmount;
    private BigDecimal outputAmount;
    @Size(max = 30, message = "护理等级不能超过30个字符")
    private String nursingLevel;
    @Size(max = 50, message = "护士姓名不能超过50个字符")
    private String nurseName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
    private String status;
    private Boolean billable;
    private BigDecimal nursingFee;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
