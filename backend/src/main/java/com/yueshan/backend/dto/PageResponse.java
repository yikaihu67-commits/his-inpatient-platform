package com.yueshan.backend.dto;

import java.util.List;

public record PageResponse<T>(
        long total,
        int page,
        int pageSize,
        List<T> records) {
}
