package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.Bed;
import com.yueshan.backend.dto.BedAssignRequest;
import com.yueshan.backend.dto.BedCreateRequest;
import com.yueshan.backend.dto.BedQueryRequest;
import com.yueshan.backend.dto.BedUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface BedService {

    PageResponse<Bed> findPage(BedQueryRequest query);

    Optional<Bed> findById(Long id);

    Bed create(BedCreateRequest request);

    Optional<Bed> update(Long id, BedUpdateRequest request);

    boolean deleteById(Long id);

    Bed assign(Long bedId, BedAssignRequest request);

    Bed release(Long bedId);
}
