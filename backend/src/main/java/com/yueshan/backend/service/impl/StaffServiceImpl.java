package com.yueshan.backend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Department;
import com.yueshan.backend.domain.Staff;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.StaffCreateRequest;
import com.yueshan.backend.dto.StaffQueryRequest;
import com.yueshan.backend.dto.StaffUpdateRequest;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.DepartmentMapper;
import com.yueshan.backend.mapper.StaffMapper;
import com.yueshan.backend.service.OperationLogService;
import com.yueshan.backend.service.StaffService;

@Service
public class StaffServiceImpl implements StaffService {
    private static final Set<String> ROLE_TYPES = Set.of("DOCTOR", "NURSE", "PHARMACIST", "TECHNICIAN", "CASHIER", "ADMIN", "OTHER");
    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");
    private final StaffMapper mapper;
    private final DepartmentMapper departmentMapper;
    private final OperationLogService logService;

    public StaffServiceImpl(StaffMapper mapper, DepartmentMapper departmentMapper, OperationLogService logService) {
        this.mapper = mapper;
        this.departmentMapper = departmentMapper;
        this.logService = logService;
    }

    @Transactional
    public Staff create(StaffCreateRequest request) {
        try {
            Staff staff = toStaff(request);
            if (mapper.countByStaffNo(staff.getStaffNo()) > 0) throw new BusinessException("工号已存在");
            validateDepartment(staff.getDepartmentId(), staff.getStatus());
            mapper.insert(staff);
            Staff saved = mapper.findById(staff.getId());
            logSuccess("CREATE", saved.getId(), saved.getStaffNo());
            return saved;
        } catch (RuntimeException ex) {
            logFailed("CREATE", null, null, ex);
            throw ex;
        }
    }

    public PageResponse<Staff> findPage(StaffQueryRequest query) {
        StaffQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<Staff> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<Staff> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<Staff> update(Long id, StaffUpdateRequest request) {
        try {
            Staff current = mapper.findById(id);
            if (current == null) return Optional.empty();
            Staff staff = toStaff(request);
            staff.setId(id);
            if (mapper.countByStaffNoExcludeId(staff.getStaffNo(), id) > 0) throw new BusinessException("工号已存在");
            validateDepartment(staff.getDepartmentId(), staff.getStatus());
            mapper.update(staff);
            Staff saved = mapper.findById(id);
            logSuccess("UPDATE", id, saved.getStaffNo());
            return Optional.of(saved);
        } catch (RuntimeException ex) {
            logFailed("UPDATE", id, null, ex);
            throw ex;
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        try {
            Staff current = mapper.findById(id);
            if (current == null) return false;
            boolean deleted = mapper.logicDeleteById(id) > 0;
            if (deleted) logSuccess("DELETE", id, current.getStaffNo());
            return deleted;
        } catch (RuntimeException ex) {
            logFailed("DELETE", id, null, ex);
            throw ex;
        }
    }

    @Transactional
    public Optional<Staff> enable(Long id) {
        return changeStatus(id, "ENABLED", "ENABLE");
    }

    @Transactional
    public Optional<Staff> disable(Long id) {
        return changeStatus(id, "DISABLED", "DISABLE");
    }

    private Optional<Staff> changeStatus(Long id, String status, String operationType) {
        try {
            Staff current = mapper.findById(id);
            if (current == null) return Optional.empty();
            if ("ENABLED".equals(status)) validateDepartment(current.getDepartmentId(), "ENABLED");
            mapper.updateStatus(id, status);
            Staff saved = mapper.findById(id);
            logSuccess(operationType, id, saved.getStaffNo());
            return Optional.of(saved);
        } catch (RuntimeException ex) {
            logFailed(operationType, id, null, ex);
            throw ex;
        }
    }

    private Staff toStaff(StaffCreateRequest request) {
        Staff staff = new Staff();
        staff.setStaffNo(requireText(request.getStaffNo(), "工号不能为空"));
        staff.setStaffName(requireText(request.getStaffName(), "姓名不能为空"));
        staff.setGender(trimToNull(request.getGender()));
        staff.setPhone(trimToNull(request.getPhone()));
        staff.setDepartmentId(request.getDepartmentId());
        staff.setRoleType(normalizeRoleType(request.getRoleType()));
        staff.setTitle(trimToNull(request.getTitle()));
        staff.setStatus(normalizeStatus(request.getStatus()));
        staff.setRemark(trimToNull(request.getRemark()));
        staff.setDeleted(false);
        return staff;
    }

    private void validateDepartment(Long departmentId, String staffStatus) {
        Department department = departmentMapper.findById(departmentId);
        if (department == null) throw new BusinessException("所属科室不存在");
        if ("ENABLED".equals(staffStatus) && !"ENABLED".equals(department.getStatus())) {
            throw new BusinessException("已停用科室下不能新增或启用人员");
        }
    }

    private StaffQueryRequest normalizeQuery(StaffQueryRequest query) {
        StaffQueryRequest q = new StaffQueryRequest();
        if (query == null) return q;
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setStaffNo(trimToNull(query.getStaffNo()));
        q.setStaffName(trimToNull(query.getStaffName()));
        q.setDepartmentId(query.getDepartmentId());
        q.setRoleType(normalizeRoleTypeOrNull(query.getRoleType()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String normalizeRoleType(String value) {
        String normalized = requireText(value, "角色类型不能为空").toUpperCase();
        if (!ROLE_TYPES.contains(normalized)) throw new BusinessException("角色类型不正确");
        return normalized;
    }
    private String normalizeRoleTypeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeRoleType(normalized);
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
        logService.record("人员管理", operationType, id, no, "system", "SUCCESS", null);
    }
    private void logFailed(String operationType, Long id, String no, RuntimeException ex) {
        logService.record("人员管理", operationType, id, no, "system", "FAILED", ex.getMessage());
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
