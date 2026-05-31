package com.yueshan.backend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.DictItem;
import com.yueshan.backend.dto.DictItemCreateRequest;
import com.yueshan.backend.dto.DictItemQueryRequest;
import com.yueshan.backend.dto.DictItemUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.DictItemMapper;
import com.yueshan.backend.service.DictItemService;
import com.yueshan.backend.service.OperationLogService;

@Service
public class DictItemServiceImpl implements DictItemService {
    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");
    private final DictItemMapper mapper;
    private final OperationLogService logService;

    public DictItemServiceImpl(DictItemMapper mapper, OperationLogService logService) {
        this.mapper = mapper;
        this.logService = logService;
    }

    @Transactional
    public DictItem create(DictItemCreateRequest request) {
        try {
            DictItem item = toItem(request);
            if (mapper.countByTypeAndCode(item.getDictType(), item.getDictCode()) > 0) {
                throw new BusinessException("字典类型和编码已存在");
            }
            mapper.insert(item);
            DictItem saved = mapper.findById(item.getId());
            logSuccess("CREATE", saved.getId(), saved.getDictType() + ":" + saved.getDictCode());
            return saved;
        } catch (RuntimeException ex) {
            logFailed("CREATE", null, null, ex);
            throw ex;
        }
    }

    public PageResponse<DictItem> findPage(DictItemQueryRequest query) {
        DictItemQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<DictItem> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<DictItem> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<DictItem> update(Long id, DictItemUpdateRequest request) {
        try {
            DictItem current = mapper.findById(id);
            if (current == null) return Optional.empty();
            DictItem item = toItem(request);
            item.setId(id);
            if (mapper.countByTypeAndCodeExcludeId(item.getDictType(), item.getDictCode(), id) > 0) {
                throw new BusinessException("字典类型和编码已存在");
            }
            mapper.update(item);
            DictItem saved = mapper.findById(id);
            logSuccess("UPDATE", id, saved.getDictType() + ":" + saved.getDictCode());
            return Optional.of(saved);
        } catch (RuntimeException ex) {
            logFailed("UPDATE", id, null, ex);
            throw ex;
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        try {
            DictItem current = mapper.findById(id);
            if (current == null) return false;
            boolean deleted = mapper.logicDeleteById(id) > 0;
            if (deleted) logSuccess("DELETE", id, current.getDictType() + ":" + current.getDictCode());
            return deleted;
        } catch (RuntimeException ex) {
            logFailed("DELETE", id, null, ex);
            throw ex;
        }
    }

    public List<DictItem> findEnabledByType(String dictType) {
        String value = requireText(dictType, "字典类型不能为空");
        return mapper.findEnabledByType(value);
    }

    private DictItem toItem(DictItemCreateRequest request) {
        DictItem item = new DictItem();
        item.setDictType(requireText(request.getDictType(), "字典类型不能为空"));
        item.setDictCode(requireText(request.getDictCode(), "字典编码不能为空"));
        item.setDictName(requireText(request.getDictName(), "字典名称不能为空"));
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setStatus(normalizeStatus(request.getStatus()));
        item.setRemark(trimToNull(request.getRemark()));
        item.setDeleted(false);
        return item;
    }

    private DictItemQueryRequest normalizeQuery(DictItemQueryRequest query) {
        DictItemQueryRequest q = new DictItemQueryRequest();
        if (query == null) return q;
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setDictType(trimToNull(query.getDictType()));
        q.setDictCode(trimToNull(query.getDictCode()));
        q.setDictName(trimToNull(query.getDictName()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
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
        logService.record("基础字典", operationType, id, no, "system", "SUCCESS", null);
    }
    private void logFailed(String operationType, Long id, String no, RuntimeException ex) {
        logService.record("基础字典", operationType, id, no, "system", "FAILED", ex.getMessage());
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
