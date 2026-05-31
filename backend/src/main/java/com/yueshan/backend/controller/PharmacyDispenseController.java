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
import com.yueshan.backend.domain.PharmacyDispense;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PharmacyDispenseActionRequest;
import com.yueshan.backend.dto.PharmacyDispenseCreateRequest;
import com.yueshan.backend.dto.PharmacyDispenseQueryRequest;
import com.yueshan.backend.dto.PharmacyDispenseResponse;
import com.yueshan.backend.service.PharmacyDispenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/pharmacy/dispenses", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class PharmacyDispenseController {
    private final PharmacyDispenseService service;

    public PharmacyDispenseController(PharmacyDispenseService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PharmacyDispenseResponse>> create(@Valid @RequestBody PharmacyDispenseCreateRequest request) {
        PharmacyDispenseResponse created = PharmacyDispenseResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/pharmacy/dispenses/" + created.id()))
                .body(ApiResponse.success("发药单创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<PharmacyDispenseResponse>> findPage(@Valid @ModelAttribute PharmacyDispenseQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PharmacyDispenseResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(PharmacyDispenseResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("发药单不存在")));
    }

    @PostMapping(value = "/{id}/dispense", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PharmacyDispenseResponse> dispense(@PathVariable Long id, @Valid @RequestBody PharmacyDispenseActionRequest request) {
        return ApiResponse.success("发药成功", PharmacyDispenseResponse.from(service.dispense(id, request)));
    }

    @PostMapping(value = "/{id}/return", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PharmacyDispenseResponse> returnDrug(@PathVariable Long id, @Valid @RequestBody PharmacyDispenseActionRequest request) {
        return ApiResponse.success("退药成功", PharmacyDispenseResponse.from(service.returnDrug(id, request)));
    }

    @PostMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PharmacyDispenseResponse> cancel(@PathVariable Long id, @Valid @RequestBody PharmacyDispenseActionRequest request) {
        return ApiResponse.success("发药单取消成功", PharmacyDispenseResponse.from(service.cancel(id, request)));
    }

    private PageResponse<PharmacyDispenseResponse> mapPage(PageResponse<PharmacyDispense> source) {
        List<PharmacyDispenseResponse> records = source.records().stream().map(PharmacyDispenseResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
