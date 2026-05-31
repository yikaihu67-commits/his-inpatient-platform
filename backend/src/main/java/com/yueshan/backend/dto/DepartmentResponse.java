package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.Department;

public record DepartmentResponse(Long id, String deptCode, String deptName, String deptType, Long parentId,
        String status, String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getDeptCode(), department.getDeptName(),
                department.getDeptType(), department.getParentId(), department.getStatus(), department.getRemark(),
                department.getDeleted(), department.getCreatedAt(), department.getUpdatedAt());
    }
}
