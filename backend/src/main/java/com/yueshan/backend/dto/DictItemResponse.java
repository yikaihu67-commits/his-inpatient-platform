package com.yueshan.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yueshan.backend.domain.DictItem;

public record DictItemResponse(Long id, String dictType, String dictCode, String dictName, Integer sortOrder,
        String status, String remark, Boolean deleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    public static DictItemResponse from(DictItem item) {
        return new DictItemResponse(item.getId(), item.getDictType(), item.getDictCode(), item.getDictName(),
                item.getSortOrder(), item.getStatus(), item.getRemark(), item.getDeleted(),
                item.getCreatedAt(), item.getUpdatedAt());
    }
}
