package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Staff {
    private Long id;
    private String staffNo;
    private String staffName;
    private String gender;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String roleType;
    private String title;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
