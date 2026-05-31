package com.yueshan.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yueshan.backend.domain.OperationLog;
import com.yueshan.backend.dto.OperationLogQueryRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.OperationLogMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class OperationLogServiceImpl implements com.yueshan.backend.service.OperationLogService {
    private static final Set<String> OPERATION_TYPES = Set.of("CREATE", "UPDATE", "DELETE", "QUERY", "SUBMIT",
            "CHECK", "EXECUTE", "CANCEL", "SETTLE", "ENABLE", "DISABLE", "OTHER");
    private static final Set<String> RESULT_STATUSES = Set.of("SUCCESS", "FAILED");

    private final OperationLogMapper mapper;

    public OperationLogServiceImpl(OperationLogMapper mapper) {
        this.mapper = mapper;
    }

    public PageResponse<OperationLog> findPage(OperationLogQueryRequest query) {
        OperationLogQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<OperationLog> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<OperationLog> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String moduleName, String operationType, Long businessId, String businessNo, String operatorName,
                       String resultStatus, String errorMessage) {
        try {
            OperationLog log = new OperationLog();
            log.setModuleName(trimToDefault(moduleName, "UNKNOWN"));
            log.setOperationType(normalizeOperationType(operationType));
            log.setBusinessId(businessId);
            log.setBusinessNo(trimToNull(businessNo));
            log.setOperatorName(trimToDefault(operatorName, "system"));
            fillRequest(log);
            log.setResultStatus(normalizeResultStatus(resultStatus));
            log.setErrorMessage(limit(trimToNull(errorMessage), 1000));
            log.setOperationTime(LocalDateTime.now());
            mapper.insert(log);
        } catch (Exception ignored) {
            // Audit failures must never affect business requests.
        }
    }

    private OperationLogQueryRequest normalizeQuery(OperationLogQueryRequest query) {
        OperationLogQueryRequest q = new OperationLogQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setModuleName(trimToNull(query.getModuleName()));
        q.setOperationType(normalizeOperationTypeOrNull(query.getOperationType()));
        q.setOperatorName(trimToNull(query.getOperatorName()));
        q.setBusinessId(query.getBusinessId());
        q.setResultStatus(normalizeResultStatusOrNull(query.getResultStatus()));
        return q;
    }

    private void fillRequest(OperationLog log) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return;
        }
        HttpServletRequest request = attrs.getRequest();
        log.setRequestMethod(request.getMethod());
        log.setRequestUri(request.getRequestURI());
        log.setRequestParams(limit(request.getQueryString(), 2000));
    }

    private String normalizeOperationType(String value) {
        String normalized = trimToNull(value);
        normalized = normalized == null ? "OTHER" : normalized.toUpperCase();
        return OPERATION_TYPES.contains(normalized) ? normalized : "OTHER";
    }

    private String normalizeOperationTypeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeOperationType(normalized);
    }

    private String normalizeResultStatus(String value) {
        String normalized = trimToNull(value);
        normalized = normalized == null ? "SUCCESS" : normalized.toUpperCase();
        return RESULT_STATUSES.contains(normalized) ? normalized : "FAILED";
    }

    private String normalizeResultStatusOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalizeResultStatus(normalized);
    }

    private String trimToDefault(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
