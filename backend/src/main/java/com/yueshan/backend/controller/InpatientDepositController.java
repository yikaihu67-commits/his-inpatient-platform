package com.yueshan.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.domain.InpatientDeposit;
import com.yueshan.backend.dto.InpatientDepositQueryRequest;
import com.yueshan.backend.dto.InpatientDepositRequest;
import com.yueshan.backend.dto.InpatientDepositResponse;
import com.yueshan.backend.dto.InpatientDepositSummaryResponse;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.InpatientDepositService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/deposits", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class InpatientDepositController {
    private final InpatientDepositService service;

    public InpatientDepositController(InpatientDepositService service) {
        this.service = service;
    }

    @PostMapping(value = "/pay", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InpatientDepositResponse>> pay(@Valid @RequestBody InpatientDepositRequest request) {
        InpatientDepositResponse created = InpatientDepositResponse.from(service.pay(request));
        return ResponseEntity.created(URI.create("/api/deposits/" + created.id())).body(ApiResponse.success("预交金缴纳成功", created));
    }

    @PostMapping(value = "/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InpatientDepositResponse>> refund(@Valid @RequestBody InpatientDepositRequest request) {
        InpatientDepositResponse created = InpatientDepositResponse.from(service.refund(request));
        return ResponseEntity.created(URI.create("/api/deposits/" + created.id())).body(ApiResponse.success("预交金退费成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<InpatientDepositResponse>> findPage(@Valid @ModelAttribute InpatientDepositQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InpatientDepositResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(deposit -> ResponseEntity.ok(ApiResponse.success(InpatientDepositResponse.from(deposit))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("预交金记录不存在")));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InpatientDepositResponse>> cancel(@PathVariable Long id) {
        return service.cancel(id)
                .map(deposit -> ResponseEntity.ok(ApiResponse.success("预交金记录取消成功", InpatientDepositResponse.from(deposit))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("预交金记录不存在")));
    }

    @GetMapping("/admission/{admissionId}/summary")
    public ApiResponse<InpatientDepositSummaryResponse> summary(@PathVariable Long admissionId) {
        return ApiResponse.success(service.summary(admissionId));
    }

    private PageResponse<InpatientDepositResponse> mapPage(PageResponse<InpatientDeposit> source) {
        List<InpatientDepositResponse> records = source.records().stream().map(InpatientDepositResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
