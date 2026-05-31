package com.yueshan.backend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.Bed;
import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.BedAssignRequest;
import com.yueshan.backend.dto.BedCreateRequest;
import com.yueshan.backend.dto.BedQueryRequest;
import com.yueshan.backend.dto.BedUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.BedMapper;
import com.yueshan.backend.service.BedService;

@Service
public class BedServiceImpl implements BedService {

    private static final String DEFAULT_STATUS = "EMPTY";
    private static final Set<String> ALLOWED_STATUSES = Set.of("EMPTY", "AVAILABLE", "OCCUPIED", "RESERVED", "DISABLED");
    private static final Set<String> VALID_ADMISSION_STATUSES = Set.of("REGISTERED", "IN_HOSPITAL");

    private final BedMapper bedMapper;
    private final AdmissionMapper admissionMapper;

    public BedServiceImpl(BedMapper bedMapper, AdmissionMapper admissionMapper) {
        this.bedMapper = bedMapper;
        this.admissionMapper = admissionMapper;
    }

    @Override
    public PageResponse<Bed> findPage(BedQueryRequest query) {
        BedQueryRequest normalizedQuery = normalizeQuery(query);
        int page = normalizedQuery.getPage();
        int pageSize = normalizedQuery.getPageSize();
        int offset = (page - 1) * pageSize;

        long total = bedMapper.countPage(normalizedQuery);
        List<Bed> records = total == 0 ? List.of() : bedMapper.findPage(normalizedQuery, offset, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    @Override
    public Optional<Bed> findById(Long id) {
        return Optional.ofNullable(bedMapper.findById(id));
    }

    @Override
    @Transactional
    public Bed create(BedCreateRequest request) {
        Bed bed = toBed(request);
        validateBedNoUnique(bed, null);
        validateStatusForManualSave(bed);

        bedMapper.insert(bed);
        Bed created = bedMapper.findById(bed.getId());
        if (created == null) {
            throw new BusinessException("床位创建失败，请重试");
        }
        return created;
    }

    @Override
    @Transactional
    public Optional<Bed> update(Long id, BedUpdateRequest request) {
        Bed existing = bedMapper.findById(id);
        if (existing == null) {
            return Optional.empty();
        }

        Bed bed = toBed(request, existing);
        bed.setId(id);
        validateBedNoUnique(bed, id);
        validateStatusForManualSave(bed);
        validateCurrentAdmissionByStatus(bed);

        bedMapper.update(bed);
        return Optional.ofNullable(bedMapper.findById(id));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Bed existing = bedMapper.findById(id);
        if (existing == null) {
            return false;
        }
        if ("OCCUPIED".equals(existing.getStatus())) {
            throw new BusinessException("床位已占用，不能删除");
        }
        return bedMapper.logicDeleteById(id) > 0;
    }

    @Override
    @Transactional
    public Bed assign(Long bedId, BedAssignRequest request) {
        if (request == null || request.getAdmissionId() == null) {
            throw new BusinessException("住院记录ID不能为空");
        }

        Bed bed = bedMapper.findById(bedId);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }
        if (!Set.of("EMPTY", "AVAILABLE").contains(bed.getStatus())) {
            throw new BusinessException("床位不是空床，不能分配");
        }

        InpatientAdmission admission = admissionMapper.findById(request.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!VALID_ADMISSION_STATUSES.contains(admission.getStatus())) {
            throw new BusinessException("患者没有有效住院记录，不能分配床位");
        }
        if (bedMapper.countOccupiedByAdmissionId(request.getAdmissionId()) > 0) {
            throw new BusinessException("该住院记录已占用床位，不能重复分配");
        }

        if (bedMapper.assign(bedId, request.getAdmissionId()) == 0) {
            throw new BusinessException("床位分配失败，请刷新后重试");
        }
        admissionMapper.markInHospital(request.getAdmissionId());
        return bedMapper.findById(bedId);
    }

    @Override
    @Transactional
    public Bed release(Long bedId) {
        Bed bed = bedMapper.findById(bedId);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }
        if (!"OCCUPIED".equals(bed.getStatus())) {
            throw new BusinessException("床位未占用，不能释放");
        }
        if (bed.getCurrentAdmissionId() == null) {
            throw new BusinessException("床位当前没有关联住院记录，不能释放");
        }
        if (bedMapper.release(bedId) == 0) {
            throw new BusinessException("床位释放失败，请刷新后重试");
        }
        return bedMapper.findById(bedId);
    }

    private Bed toBed(BedCreateRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }

        Bed bed = new Bed();
        bed.setBedNo(trimToNull(request.getBedNo()));
        bed.setWardName(trimToNull(request.getWardName()));
        bed.setRoomNo(trimToNull(request.getRoomNo()));
        bed.setBedType(trimToNull(request.getBedType()));
        bed.setStatus(normalizeStatus(request.getStatus(), DEFAULT_STATUS));
        bed.setCurrentAdmissionId(null);
        bed.setDeleted(false);
        return bed;
    }

    private Bed toBed(BedUpdateRequest request, Bed existing) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }

        Bed bed = new Bed();
        bed.setBedNo(trimToNull(request.getBedNo()));
        bed.setWardName(trimToNull(request.getWardName()));
        bed.setRoomNo(trimToNull(request.getRoomNo()));
        bed.setBedType(trimToNull(request.getBedType()));
        bed.setStatus(normalizeStatus(request.getStatus(), existing.getStatus()));
        bed.setCurrentAdmissionId("OCCUPIED".equals(bed.getStatus()) ? existing.getCurrentAdmissionId() : null);
        bed.setDeleted(false);
        return bed;
    }

    private BedQueryRequest normalizeQuery(BedQueryRequest query) {
        BedQueryRequest normalized = new BedQueryRequest();
        if (query == null) {
            return normalized;
        }

        normalized.setPage(query.getPage() == null ? 1 : query.getPage());
        normalized.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        normalized.setBedNo(trimToNull(query.getBedNo()));
        normalized.setWardName(trimToNull(query.getWardName()));
        normalized.setRoomNo(trimToNull(query.getRoomNo()));
        normalized.setStatus(normalizeStatusOrNull(query.getStatus()));
        return normalized;
    }

    private void validateBedNoUnique(Bed bed, Long excludeId) {
        int count = excludeId == null
                ? bedMapper.countByBedNoAndWardName(bed.getBedNo(), bed.getWardName())
                : bedMapper.countByBedNoAndWardNameExcludeId(bed.getBedNo(), bed.getWardName(), excludeId);
        if (count > 0) {
            throw new BusinessException("同一病区床号已存在");
        }
    }

    private void validateStatusForManualSave(Bed bed) {
        if ("OCCUPIED".equals(bed.getStatus()) && bed.getCurrentAdmissionId() == null) {
            throw new BusinessException("不能手工设置床位为占用，请使用分配床位接口");
        }
    }

    private void validateCurrentAdmissionByStatus(Bed bed) {
        if (!"OCCUPIED".equals(bed.getStatus())) {
            bed.setCurrentAdmissionId(null);
        }
    }

    private String normalizeStatus(String status, String defaultStatus) {
        String value = trimToNull(status);
        value = value == null ? defaultStatus : value.toUpperCase();
        if (!ALLOWED_STATUSES.contains(value)) {
            throw new BusinessException("床位状态只能是 EMPTY、AVAILABLE、OCCUPIED、RESERVED、DISABLED");
        }
        return value;
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        if (value == null) {
            return null;
        }
        value = value.toUpperCase();
        if (!ALLOWED_STATUSES.contains(value)) {
            throw new BusinessException("床位状态只能是 EMPTY、AVAILABLE、OCCUPIED、RESERVED、DISABLED");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
