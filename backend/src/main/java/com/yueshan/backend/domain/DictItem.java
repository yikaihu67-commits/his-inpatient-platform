package com.yueshan.backend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DictItem {
    private Long id;
    private String dictType;
    private String dictCode;
    private String dictName;
    private Integer sortOrder;
    private String status;
    private String remark;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
