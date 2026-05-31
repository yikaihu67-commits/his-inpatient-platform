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
public class SurgeryCreateRequest {
    @Size(max = 32, message = "手术编号不能超过32个字符")
    private String surgeryNo;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @NotNull(message = "入院记录ID不能为空")
    private Long admissionId;
    @NotBlank(message = "手术名称不能为空")
    @Size(max = 100, message = "手术名称不能超过100个字符")
    private String surgeryName;
    @Size(max = 255, message = "术前诊断不能超过255个字符")
    private String preoperativeDiagnosis;
    @Size(max = 30, message = "手术级别不能超过30个字符")
    private String surgeryLevel;
    @Size(max = 30, message = "手术类型不能超过30个字符")
    private String surgeryType;
    @Size(max = 50, message = "手术室不能超过50个字符")
    private String operatingRoom;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedTime;
    @Size(max = 50, message = "主刀医生不能超过50个字符")
    private String primaryDoctorName;
    @Size(max = 100, message = "助手医生不能超过100个字符")
    private String assistantDoctorName;
    @Size(max = 50, message = "麻醉方式不能超过50个字符")
    private String anesthesiaMethod;
    @Size(max = 50, message = "麻醉医生不能超过50个字符")
    private String anesthesiologistName;
    @DecimalMin(value = "0.00", message = "手术费用不能小于0")
    private BigDecimal surgeryFee;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
