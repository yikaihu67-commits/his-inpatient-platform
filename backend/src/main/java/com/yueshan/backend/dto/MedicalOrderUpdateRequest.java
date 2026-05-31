package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalOrderUpdateRequest {

    @Size(max = 32, message = "医嘱编号不能超过32个字符")
    private String orderNo;

    @NotNull(message = "住院记录ID不能为空")
    private Long admissionId;

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotBlank(message = "医嘱类型不能为空")
    @Size(max = 30, message = "医嘱类型不能超过30个字符")
    private String orderType;

    @NotBlank(message = "医嘱分类不能为空")
    @Size(max = 30, message = "医嘱分类不能超过30个字符")
    private String orderCategory;

    @Size(max = 500, message = "医嘱内容不能超过500个字符")
    private String orderContent;

    @NotBlank(message = "医嘱项目名称不能为空")
    @Size(max = 100, message = "医嘱项目名称不能超过100个字符")
    private String itemName;

    @Size(max = 50, message = "剂量不能超过50个字符")
    private String dosage;

    @Size(max = 20, message = "剂量单位不能超过20个字符")
    private String dosageUnit;

    @Size(max = 50, message = "频次不能超过50个字符")
    private String frequency;

    @Size(max = 50, message = "用法不能超过50个字符")
    private String route;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "开立医生不能为空")
    @Size(max = 50, message = "开立医生不能超过50个字符")
    private String doctorName;

    @Size(max = 50, message = "执行科室不能超过50个字符")
    private String executionDepartment;

    @Size(max = 50, message = "执行人不能超过50个字符")
    private String executorName;

    @Size(max = 50, message = "执行护士不能超过50个字符")
    private String nurseName;

    private BigDecimal unitPrice;

    private BigDecimal quantity;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
