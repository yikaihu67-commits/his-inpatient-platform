package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Department {
    private Long id;
    private String deptCode;
    private String deptName;
    private String deptType;
    private Long parentId;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
