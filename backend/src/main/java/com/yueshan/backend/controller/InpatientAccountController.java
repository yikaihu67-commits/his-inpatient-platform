package com.yueshan.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.dto.InpatientAccountSummaryResponse;
import com.yueshan.backend.service.InpatientAccountService;

@RestController
@RequestMapping(value = "/api/inpatient-accounts", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class InpatientAccountController {
    private final InpatientAccountService service;

    public InpatientAccountController(InpatientAccountService service) {
        this.service = service;
    }

    @GetMapping("/{admissionId}/summary")
    public ApiResponse<InpatientAccountSummaryResponse> summary(@PathVariable Long admissionId) {
        return ApiResponse.success(service.summary(admissionId));
    }
}
