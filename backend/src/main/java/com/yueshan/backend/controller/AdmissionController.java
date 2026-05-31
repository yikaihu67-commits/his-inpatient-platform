package com.yueshan.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.AdmissionCreateRequest;
import com.yueshan.backend.dto.AdmissionQueryRequest;
import com.yueshan.backend.dto.AdmissionResponse;
import com.yueshan.backend.dto.AdmissionUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.AdmissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/admissions", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class AdmissionController {

    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AdmissionResponse>> create(@Valid @RequestBody AdmissionCreateRequest request) {
        AdmissionResponse created = AdmissionResponse.from(admissionService.create(request));
        return ResponseEntity
                .created(URI.create("/api/admissions/" + created.id()))
                .body(ApiResponse.success("入院登记创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<AdmissionResponse>> findPage(@Valid @ModelAttribute AdmissionQueryRequest query) {
        return ApiResponse.success(mapPage(admissionService.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdmissionResponse>> findById(@PathVariable Long id) {
        return admissionService.findById(id)
                .map(admission -> ResponseEntity.ok(ApiResponse.success(AdmissionResponse.from(admission))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("入院登记不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AdmissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AdmissionUpdateRequest request) {
        return admissionService.update(id, request)
                .map(admission -> ResponseEntity.ok(ApiResponse.success("入院登记修改成功", AdmissionResponse.from(admission))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("入院登记不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!admissionService.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("入院登记不存在"));
        }

        return ResponseEntity.ok(ApiResponse.success("入院登记删除成功", null));
    }

    @PostMapping("/{id}/admit")
    public ResponseEntity<ApiResponse<AdmissionResponse>> admit(@PathVariable Long id) {
        return admissionService.admit(id)
                .map(admission -> ResponseEntity.ok(ApiResponse.success("办理入院成功", AdmissionResponse.from(admission))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("入院登记不存在")));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AdmissionResponse>> cancel(@PathVariable Long id) {
        return admissionService.cancel(id)
                .map(admission -> ResponseEntity.ok(ApiResponse.success("取消入院成功", AdmissionResponse.from(admission))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("入院登记不存在")));
    }

    private PageResponse<AdmissionResponse> mapPage(PageResponse<InpatientAdmission> source) {
        List<AdmissionResponse> records = source.records().stream()
                .map(AdmissionResponse::from)
                .toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
