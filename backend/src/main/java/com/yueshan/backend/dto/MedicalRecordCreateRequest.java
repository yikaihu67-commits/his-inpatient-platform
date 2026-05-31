package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalRecordCreateRequest {
    @Size(max = 32, message = "病历编号不能超过32个字符")
    private String recordNo;
    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @NotBlank(message = "记录类型不能为空")
    @Size(max = 40, message = "记录类型不能超过40个字符")
    private String recordType;
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    @NotBlank(message = "记录医生不能为空")
    @Size(max = 50, message = "记录医生不能超过50个字符")
    private String doctorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
