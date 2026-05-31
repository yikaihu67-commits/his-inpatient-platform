package com.yueshan.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceSessionResponse;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceVerifyRequest;
import com.yueshan.backend.dto.selfservice.SelfServiceAdmissionInfoResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillDetailResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillSummaryResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceDischargeResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceExamLabResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceSurgeryResponse;
import com.yueshan.backend.service.PatientSelfService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/patient-self-service", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
@RequiredArgsConstructor
public class PatientSelfServiceController {

    private final PatientSelfService service;

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PatientSelfServiceSessionResponse> verify(@RequestBody PatientSelfServiceVerifyRequest request) {
        return ApiResponse.success("患者身份验证成功", service.verify(request));
    }

    @GetMapping("/admission-info")
    public ApiResponse<SelfServiceAdmissionInfoResponse> admissionInfo(@RequestParam Long patientId,
                                                                       @RequestParam Long admissionId) {
        return ApiResponse.success(service.admissionInfo(patientId, admissionId));
    }

    @GetMapping("/bill-summary")
    public ApiResponse<SelfServiceBillSummaryResponse> billSummary(@RequestParam Long patientId,
                                                                   @RequestParam Long admissionId) {
        return ApiResponse.success(service.billSummary(patientId, admissionId));
    }

    @GetMapping("/bill-details")
    public ApiResponse<List<SelfServiceBillDetailResponse>> billDetails(
            @RequestParam Long patientId,
            @RequestParam Long admissionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(service.billDetails(patientId, admissionId, startDate, endDate, itemCategory, status));
    }

    @GetMapping("/exam-labs")
    public ApiResponse<List<SelfServiceExamLabResponse>> examLabs(@RequestParam Long patientId,
                                                                  @RequestParam Long admissionId) {
        return ApiResponse.success(service.examLabs(patientId, admissionId));
    }

    @GetMapping("/surgeries")
    public ApiResponse<List<SelfServiceSurgeryResponse>> surgeries(@RequestParam Long patientId,
                                                                   @RequestParam Long admissionId) {
        return ApiResponse.success(service.surgeries(patientId, admissionId));
    }

    @GetMapping("/discharge")
    public ApiResponse<SelfServiceDischargeResponse> discharge(@RequestParam Long patientId,
                                                               @RequestParam Long admissionId) {
        return ApiResponse.success(service.discharge(patientId, admissionId));
    }
}
