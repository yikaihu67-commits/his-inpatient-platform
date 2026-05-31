package com.yueshan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffCreateRequest {
    @NotBlank(message = "工号不能为空")
    @Size(max = 32, message = "工号不能超过32个字符")
    private String staffNo;
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String staffName;
    @Size(max = 10, message = "性别不能超过10个字符")
    private String gender;
    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;
    @NotNull(message = "所属科室不能为空")
    private Long departmentId;
    @NotBlank(message = "角色类型不能为空")
    @Size(max = 30, message = "角色类型不能超过30个字符")
    private String roleType;
    @Size(max = 50, message = "职称不能超过50个字符")
    private String title;
    @Size(max = 30, message = "状态不能超过30个字符")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
