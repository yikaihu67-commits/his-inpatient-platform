package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.PatientAppointment;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PatientAppointmentActionRequest;
import com.yueshan.backend.dto.PatientAppointmentCreateRequest;
import com.yueshan.backend.dto.PatientAppointmentQueryRequest;
import com.yueshan.backend.dto.PatientAppointmentUpdateRequest;

public interface PatientAppointmentService {
    PageResponse<PatientAppointment> findPage(PatientAppointmentQueryRequest query);
    Optional<PatientAppointment> findById(Long id);
    PatientAppointment create(PatientAppointmentCreateRequest request);
    Optional<PatientAppointment> update(Long id, PatientAppointmentUpdateRequest request);
    boolean deleteById(Long id);
    PatientAppointment confirm(Long id, PatientAppointmentActionRequest request);
    PatientAppointment complete(Long id, PatientAppointmentActionRequest request);
    PatientAppointment cancel(Long id, PatientAppointmentActionRequest request);
}
