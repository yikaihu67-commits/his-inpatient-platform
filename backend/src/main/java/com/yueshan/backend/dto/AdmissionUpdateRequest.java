package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdmissionUpdateRequest {

    @Size(max = 32, message = "住院号不能超过32个字符")
    private String admissionNo;

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotBlank(message = "入院科室不能为空")
    @Size(max = 50, message = "入院科室不能超过50个字符")
    private String departmentName;

    @Size(max = 50, message = "入院病区不能超过50个字符")
    private String wardName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admissionTime;

    @Size(max = 255, message = "入院诊断不能超过255个字符")
    private String admissionDiagnosis;

    @Size(max = 30, message = "费别不能超过30个字符")
    private String chargeType;

    @Size(max = 30, message = "护理级别不能超过30个字符")
    private String nursingLevel;

    @Size(max = 50, message = "经管医生不能超过50个字符")
    private String doctorName;

    @Size(max = 30, message = "入院状态不能超过30个字符")
    private String status;
}
