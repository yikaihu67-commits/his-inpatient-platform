package com.yueshan.backend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Department;
import com.yueshan.backend.dto.DepartmentCreateRequest;
import com.yueshan.backend.dto.DepartmentQueryRequest;
import com.yueshan.backend.dto.DepartmentUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.DepartmentMapper;
import com.yueshan.backend.service.DepartmentService;
import com.yueshan.backend.service.OperationLogService;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static final Set<String> DEPT_TYPES = Set.of("OUTPATIENT", "INPATIENT", "MEDICAL_TECH", "PHARMACY", "ADMIN", "OTHER");
    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");
    private final DepartmentMapper mapper;
    private final OperationLogService logService;

    public DepartmentServiceImpl(DepartmentMapper mapper, OperationLogService logService) {
        this.mapper = mapper;
        this.logService = logService;
    }

    @Transactional
    public Department create(DepartmentCreateRequest request) {
        try {
            Department department = toDepartment(request);
            if (mapper.countByDeptCode(department.getDeptCode()) > 0) {
                throw new BusinessException("科室编码已存在");
            }
            validateParent(department.getParentId(), null);
            mapper.insert(department);
            Department saved = mapper.findById(department.getId());
            logSuccess("CREATE", saved.getId(), saved.getDeptCode());
            return saved;
        } catch (RuntimeException ex) {
            logFailed("CREATE", null, null, ex);
            throw ex;
        }
    }

    public PageResponse<Department> findPage(DepartmentQueryRequest query) {
        DepartmentQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<Department> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<Department> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<Department> update(Long id, DepartmentUpdateRequest request) {
        try {
            Department current = mapper.findById(id);
            if (current == null) {
                return Optional.empty();
            }
            Department department = toDepartment(request);
            department.setId(id);
            if (mapper.countByDeptCodeExcludeId(department.getDeptCode(), id) > 0) {
                throw new BusinessException("科室编码已存在");
            }
            validateParent(department.getParentId(), id);
            mapper.update(department);
            Department saved = mapper.findById(id);
            logSuccess("UPDATE", id, saved.getDeptCode());
            return Optional.of(saved);
        } catch (RuntimeException ex) {
            logFailed("UPDATE", id, null, ex);
            throw ex;
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        try {
            Department current = mapper.findById(id);
            if (current == null) {
                return false;
            }
            boolean deleted = mapper.logicDeleteById(id) > 0;
            if (deleted) {
                logSuccess("DELETE", id, current.getDeptCode());
            }
            return deleted;
        } catch (RuntimeException ex) {
            logFailed("DELETE", id, null, ex);
            throw ex;
        }
    }

    @Transactional
    public Optional<Department> enable(Long id) {
        return changeStatus(id, "ENABLED", "ENABLE");
    }

    @Transactional
    public Optional<Department> disable(Long id) {
        return changeStatus(id, "DISABLED", "DISABLE");
    }

    private Optional<Department> changeStatus(Long id, String status, String operationType) {
        try {
            Department current = mapper.findById(id);
            if (current == null) {
                return Optional.empty();
            }
            mapper.updateStatus(id, status);
            Department saved = mapper.findById(id);
            logSuccess(operationType, id, saved.getDeptCode());
            return Optional.of(saved);
        } catch (RuntimeException ex) {
            logFailed(operationType, id, null, ex);
            throw ex;
        }
    }

    private Department toDepartment(DepartmentCreateRequest request) {
        Department department = new Department();
        department.setDeptCode(requireText(request.getDeptCode(), "科室编码不能为空"));
        department.setDeptName(requireText(request.getDeptName(), "科室名称不能为空"));
        department.setDeptType(normalizeDeptType(request.getDeptType()));
        department.setParentId(request.getParentId());
        department.setStatus(normalizeStatus(request.getStatus()));
        department.setRemark(trimToNull(request.getRemark()));
        department.setDeleted(false);
        return department;
    }

    private void validateParent(Long parentId, Long excludeId) {
        if (parentId == null) {
            return;
        }
        if (excludeId != null && parentId.equals(excludeId)) {
            throw new BusinessException("上级科室不能是自身");
        }
        Department parent = mapper.findById(parentId);
        if (parent == null) {
            throw new BusinessException("上级科室不存在");
        }
    }

    private DepartmentQueryRequest normalizeQuery(DepartmentQueryRequest query) {
        DepartmentQueryRequest q = new DepartmentQueryRequest();
        if (query == null) return q;
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setDeptCode(trimToNull(query.getDeptCode()));
        q.setDeptName(trimToNull(query.getDeptName()));
        q.setDeptType(normalizeDeptTypeOrNull(query.getDeptType()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String normalizeDeptType(String value) {
        String normalized = requireText(value, "科室类型不能为空").toUpperCase();
        if (!DEPT_TYPES.contains(normalized)) throw new BusinessException("科室类型不正确");
        return normalized;
    }
    private String normalizeDeptTypeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeDeptType(normalized);
    }
    private String normalizeStatus(String value) {
        String normalized = trimToNull(value);
        normalized = normalized == null ? "ENABLED" : normalized.toUpperCase();
        if (!STATUSES.contains(normalized)) throw new BusinessException("状态只能是 ENABLED、DISABLED");
        return normalized;
    }
    private String normalizeStatusOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeStatus(normalized);
    }
    private void logSuccess(String operationType, Long id, String no) {
        logService.record("科室管理", operationType, id, no, "system", "SUCCESS", null);
    }
    private void logFailed(String operationType, Long id, String no, RuntimeException ex) {
        logService.record("科室管理", operationType, id, no, "system", "FAILED", ex.getMessage());
    }
    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) throw new BusinessException(message);
        return trimmed;
    }
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
