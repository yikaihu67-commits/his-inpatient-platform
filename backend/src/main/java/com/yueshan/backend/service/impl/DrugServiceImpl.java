package com.yueshan.backend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.dto.DrugCreateRequest;
import com.yueshan.backend.dto.DrugQueryRequest;
import com.yueshan.backend.dto.DrugUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.DrugMapper;
import com.yueshan.backend.service.DrugService;

@Service
public class DrugServiceImpl implements DrugService {
    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");
    private final DrugMapper mapper;

    public DrugServiceImpl(DrugMapper mapper) {
        this.mapper = mapper;
    }

    public PageResponse<Drug> findPage(DrugQueryRequest query) {
        DrugQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<Drug> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<Drug> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Drug create(DrugCreateRequest request) {
        Drug drug = toDrug(request);
        if (mapper.countByDrugCode(drug.getDrugCode()) > 0) {
            throw new BusinessException("药品编码已存在");
        }
        mapper.insert(drug);
        return mapper.findById(drug.getId());
    }

    @Transactional
    public Optional<Drug> update(Long id, DrugUpdateRequest request) {
        if (mapper.findById(id) == null) {
            return Optional.empty();
        }
        Drug drug = toDrug(request);
        drug.setId(id);
        if (mapper.countByDrugCodeExcludeId(drug.getDrugCode(), id) > 0) {
            throw new BusinessException("药品编码已存在");
        }
        mapper.update(drug);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public boolean deleteById(Long id) {
        return mapper.logicDeleteById(id) > 0;
    }

    private Drug toDrug(DrugCreateRequest request) {
        Drug drug = new Drug();
        drug.setDrugCode(trimToNull(request.getDrugCode()));
        drug.setDrugName(trimToNull(request.getDrugName()));
        drug.setSpecification(trimToNull(request.getSpecification()));
        drug.setUnit(trimToNull(request.getUnit()));
        drug.setPrice(request.getPrice());
        drug.setStockQuantity(request.getStockQuantity());
        drug.setStockLowerLimit(request.getStockLowerLimit() == null ? 0 : request.getStockLowerLimit());
        drug.setStatus(normalizeStatus(request.getStatus()));
        drug.setDeleted(false);
        return drug;
    }

    private Drug toDrug(DrugUpdateRequest request) {
        Drug drug = new Drug();
        drug.setDrugCode(trimToNull(request.getDrugCode()));
        drug.setDrugName(trimToNull(request.getDrugName()));
        drug.setSpecification(trimToNull(request.getSpecification()));
        drug.setUnit(trimToNull(request.getUnit()));
        drug.setPrice(request.getPrice());
        drug.setStockQuantity(request.getStockQuantity());
        drug.setStockLowerLimit(request.getStockLowerLimit() == null ? 0 : request.getStockLowerLimit());
        drug.setStatus(normalizeStatus(request.getStatus()));
        drug.setDeleted(false);
        return drug;
    }

    private DrugQueryRequest normalizeQuery(DrugQueryRequest query) {
        DrugQueryRequest q = new DrugQueryRequest();
        if (query == null) return q;
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setDrugCode(trimToNull(query.getDrugCode()));
        q.setDrugName(trimToNull(query.getDrugName()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String normalizeStatus(String status) {
        String value = trimToNull(status);
        value = value == null ? "ENABLED" : value.toUpperCase();
        if (!STATUSES.contains(value)) throw new BusinessException("药品状态只能是 ENABLED、DISABLED");
        return value;
    }
    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        return value == null ? null : normalizeStatus(value);
    }
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
