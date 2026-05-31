package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SurgeryActionRequest {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;
    @Size(max = 50, message = "手术室不能超过50个字符")
    private String operatingRoom;
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
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
