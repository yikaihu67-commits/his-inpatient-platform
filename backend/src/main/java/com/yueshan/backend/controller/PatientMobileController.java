package com.yueshan.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.domain.PatientAppointment;
import com.yueshan.backend.dto.PatientAppointmentActionRequest;
import com.yueshan.backend.dto.PatientAppointmentCreateRequest;
import com.yueshan.backend.dto.PatientAppointmentQueryRequest;
import com.yueshan.backend.dto.mobile.PatientMobileHomeResponse;
import com.yueshan.backend.dto.mobile.PatientMobileLoginResponse;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceSessionResponse;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceVerifyRequest;
import com.yueshan.backend.dto.selfservice.SelfServiceBillDetailResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillSummaryResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceDischargeResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceExamLabResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceSurgeryResponse;
import com.yueshan.backend.service.PatientAppointmentService;
import com.yueshan.backend.service.PatientSelfService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/patient-mobile", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
@RequiredArgsConstructor
public class PatientMobileController {
    private final PatientSelfService selfService;
    private final PatientAppointmentService appointmentService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PatientMobileLoginResponse> login(@RequestBody PatientSelfServiceVerifyRequest request) {
        PatientSelfServiceSessionResponse patient = selfService.verify(request);
        return ApiResponse.success("患者绑定成功", new PatientMobileLoginResponse(UUID.randomUUID().toString(), patient));
    }

    @GetMapping("/home")
    public ApiResponse<PatientMobileHomeResponse> home(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return ApiResponse.success(new PatientMobileHomeResponse(
                selfService.admissionInfo(patientId, admissionId),
                selfService.billSummary(patientId, admissionId),
                selfService.billDetails(patientId, admissionId, null, null, null, null).stream().limit(5).toList()));
    }

    @GetMapping("/profile")
    public ApiResponse<PatientMobileHomeResponse> profile(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return home(patientId, admissionId);
    }

    @GetMapping("/bill-summary")
    public ApiResponse<SelfServiceBillSummaryResponse> billSummary(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return ApiResponse.success(selfService.billSummary(patientId, admissionId));
    }

    @GetMapping("/bill-details")
    public ApiResponse<List<SelfServiceBillDetailResponse>> billDetails(
            @RequestParam Long patientId,
            @RequestParam Long admissionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(selfService.billDetails(patientId, admissionId, startDate, endDate, itemCategory, status));
    }

    @GetMapping("/appointments")
    public ApiResponse<List<PatientAppointment>> appointments(@RequestParam Long patientId,
                                                              @RequestParam(required = false) Long admissionId) {
        PatientAppointmentQueryRequest query = new PatientAppointmentQueryRequest();
        query.setPatientId(patientId);
        query.setAdmissionId(admissionId);
        query.setPage(1);
        query.setPageSize(100);
        return ApiResponse.success(appointmentService.findPage(query).records());
    }

    @PostMapping(value = "/appointments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PatientAppointment> createAppointment(@Valid @RequestBody PatientAppointmentCreateRequest request) {
        return ApiResponse.success("预约提交成功", appointmentService.create(request));
    }

    @PostMapping("/appointments/cancel")
    public ApiResponse<PatientAppointment> cancelAppointment(@RequestParam Long appointmentId,
                                                             @RequestBody(required = false) PatientAppointmentActionRequest request) {
        return ApiResponse.success("预约取消成功", appointmentService.cancel(appointmentId, request == null ? new PatientAppointmentActionRequest() : request));
    }

    @GetMapping("/reports")
    public ApiResponse<List<SelfServiceExamLabResponse>> reports(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return ApiResponse.success(selfService.examLabs(patientId, admissionId));
    }

    @GetMapping("/exam-lab")
    public ApiResponse<List<SelfServiceExamLabResponse>> examLab(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return reports(patientId, admissionId);
    }

    @GetMapping("/surgeries")
    public ApiResponse<List<SelfServiceSurgeryResponse>> surgeries(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return ApiResponse.success(selfService.surgeries(patientId, admissionId));
    }

    @GetMapping("/surgery")
    public ApiResponse<List<SelfServiceSurgeryResponse>> surgery(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return surgeries(patientId, admissionId);
    }

    @GetMapping("/discharge")
    public ApiResponse<SelfServiceDischargeResponse> discharge(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return ApiResponse.success(selfService.discharge(patientId, admissionId));
    }
}
