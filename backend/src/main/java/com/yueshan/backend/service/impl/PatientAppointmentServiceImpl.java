package com.yueshan.backend.service.impl;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.PatientAppointment;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PatientAppointmentActionRequest;
import com.yueshan.backend.dto.PatientAppointmentCreateRequest;
import com.yueshan.backend.dto.PatientAppointmentQueryRequest;
import com.yueshan.backend.dto.PatientAppointmentUpdateRequest;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.PatientAppointmentMapper;
import com.yueshan.backend.mapper.PatientMapper;
import com.yueshan.backend.service.PatientAppointmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientAppointmentServiceImpl implements PatientAppointmentService {
    private static final Set<String> TYPES = Set.of("EXAM_APPOINTMENT", "LAB_APPOINTMENT", "DISCHARGE_SETTLEMENT", "FOLLOW_UP", "OTHER");

    private final PatientAppointmentMapper mapper;
    private final PatientMapper patientMapper;
    private final AdmissionMapper admissionMapper;

    @Override
    public PageResponse<PatientAppointment> findPage(PatientAppointmentQueryRequest query) {
        int page = query.getPage() == null ? 1 : Math.max(query.getPage(), 1);
        int pageSize = query.getPageSize() == null ? 10 : Math.min(Math.max(query.getPageSize(), 1), 200);
        query.setPage(page);
        query.setPageSize(pageSize);
        long total = mapper.countPage(query);
        return new PageResponse<>(total, page, pageSize, total == 0 ? java.util.List.of() : mapper.findPage(query, (page - 1) * pageSize, pageSize));
    }

    @Override
    public Optional<PatientAppointment> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    public PatientAppointment create(PatientAppointmentCreateRequest request) {
        validatePatientAndAdmission(request.getPatientId(), request.getAdmissionId());
        PatientAppointment item = new PatientAppointment();
        copy(item, request);
        item.setAppointmentType(normalizeType(request.getAppointmentType()));
        item.setStatus("REQUESTED");
        mapper.insert(item);
        return mapper.findById(item.getId());
    }

    @Override
    public Optional<PatientAppointment> update(Long id, PatientAppointmentUpdateRequest request) {
        PatientAppointment current = require(id);
        if (!Set.of("REQUESTED", "CONFIRMED").contains(current.getStatus())) {
            throw new BusinessException("只有待确认或已确认预约可以修改");
        }
        validatePatientAndAdmission(request.getPatientId(), request.getAdmissionId());
        copy(current, request);
        current.setAppointmentType(normalizeType(request.getAppointmentType()));
        mapper.update(current);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    public boolean deleteById(Long id) {
        PatientAppointment current = require(id);
        if (!"REQUESTED".equals(current.getStatus())) {
            throw new BusinessException("只有待确认预约可以删除");
        }
        return mapper.logicDeleteById(id) > 0;
    }

    @Override
    public PatientAppointment confirm(Long id, PatientAppointmentActionRequest request) {
        PatientAppointment current = require(id);
        if (!"REQUESTED".equals(current.getStatus())) {
            throw new BusinessException("只有待确认预约可以确认");
        }
        mapper.confirm(id);
        return mapper.findById(id);
    }

    @Override
    public PatientAppointment complete(Long id, PatientAppointmentActionRequest request) {
        PatientAppointment current = require(id);
        if ("COMPLETED".equals(current.getStatus())) throw new BusinessException("预约已完成，不能重复完成");
        if ("CANCELLED".equals(current.getStatus())) throw new BusinessException("已取消预约不能完成");
        mapper.complete(id);
        return mapper.findById(id);
    }

    @Override
    public PatientAppointment cancel(Long id, PatientAppointmentActionRequest request) {
        PatientAppointment current = require(id);
        if ("COMPLETED".equals(current.getStatus())) throw new BusinessException("已完成预约不能取消");
        if ("CANCELLED".equals(current.getStatus())) throw new BusinessException("预约已取消，不能重复取消");
        mapper.cancel(id);
        return mapper.findById(id);
    }

    private PatientAppointment require(Long id) {
        PatientAppointment item = mapper.findById(id);
        if (item == null) throw new BusinessException("预约不存在");
        return item;
    }

    private void validatePatientAndAdmission(Long patientId, Long admissionId) {
        if (patientId == null || patientMapper.findById(patientId) == null) {
            throw new BusinessException("患者不存在");
        }
        if (admissionId != null) {
            InpatientAdmission admission = admissionMapper.findById(admissionId);
            if (admission == null || !patientId.equals(admission.getPatientId())) {
                throw new BusinessException("住院记录与患者不匹配");
            }
        }
    }

    private void copy(PatientAppointment item, PatientAppointmentCreateRequest request) {
        item.setPatientId(request.getPatientId());
        item.setAdmissionId(request.getAdmissionId());
        item.setAppointmentItem(request.getAppointmentItem());
        item.setAppointmentDate(request.getAppointmentDate());
        item.setTimeSlot(request.getTimeSlot());
        item.setContactPhone(request.getContactPhone());
        item.setRemark(request.getRemark());
    }

    private String normalizeType(String type) {
        String value = type == null ? "" : type.trim().toUpperCase();
        if (!TYPES.contains(value)) {
            throw new BusinessException("预约类型不正确");
        }
        return value;
    }
}
