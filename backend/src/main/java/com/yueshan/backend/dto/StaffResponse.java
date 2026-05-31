package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.Staff;

public record StaffResponse(Long id, String staffNo, String staffName, String gender, String phone,
        Long departmentId, String departmentName, String roleType, String title, String status,
        String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static StaffResponse from(Staff staff) {
        return new StaffResponse(staff.getId(), staff.getStaffNo(), staff.getStaffName(), staff.getGender(),
                staff.getPhone(), staff.getDepartmentId(), staff.getDepartmentName(), staff.getRoleType(),
                staff.getTitle(), staff.getStatus(), staff.getRemark(), staff.getDeleted(),
                staff.getCreatedAt(), staff.getUpdatedAt());
    }
}
