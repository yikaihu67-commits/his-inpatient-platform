package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.Department;
import com.yueshan.backend.dto.DepartmentCreateRequest;
import com.yueshan.backend.dto.DepartmentQueryRequest;
import com.yueshan.backend.dto.DepartmentUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface DepartmentService {
    Department create(DepartmentCreateRequest request);
    PageResponse<Department> findPage(DepartmentQueryRequest query);
    Optional<Department> findById(Long id);
    Optional<Department> update(Long id, DepartmentUpdateRequest request);
    boolean deleteById(Long id);
    Optional<Department> enable(Long id);
    Optional<Department> disable(Long id);
}
