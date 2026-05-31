package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.Staff;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.StaffCreateRequest;
import com.yueshan.backend.dto.StaffQueryRequest;
import com.yueshan.backend.dto.StaffUpdateRequest;

public interface StaffService {
    Staff create(StaffCreateRequest request);
    PageResponse<Staff> findPage(StaffQueryRequest query);
    Optional<Staff> findById(Long id);
    Optional<Staff> update(Long id, StaffUpdateRequest request);
    boolean deleteById(Long id);
    Optional<Staff> enable(Long id);
    Optional<Staff> disable(Long id);
}
