package com.yueshan.backend.service;

import java.util.List;
import java.util.Optional;

import com.yueshan.backend.domain.DictItem;
import com.yueshan.backend.dto.DictItemCreateRequest;
import com.yueshan.backend.dto.DictItemQueryRequest;
import com.yueshan.backend.dto.DictItemUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface DictItemService {
    DictItem create(DictItemCreateRequest request);
    PageResponse<DictItem> findPage(DictItemQueryRequest query);
    Optional<DictItem> findById(Long id);
    Optional<DictItem> update(Long id, DictItemUpdateRequest request);
    boolean deleteById(Long id);
    List<DictItem> findEnabledByType(String dictType);
}
