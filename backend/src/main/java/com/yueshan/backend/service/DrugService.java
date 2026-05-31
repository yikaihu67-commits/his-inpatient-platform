package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.dto.DrugCreateRequest;
import com.yueshan.backend.dto.DrugQueryRequest;
import com.yueshan.backend.dto.DrugUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface DrugService {
    PageResponse<Drug> findPage(DrugQueryRequest query);
    Optional<Drug> findById(Long id);
    Drug create(DrugCreateRequest request);
    Optional<Drug> update(Long id, DrugUpdateRequest request);
    boolean deleteById(Long id);
}
