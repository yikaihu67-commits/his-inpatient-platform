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
import com.yueshan.backend.domain.MedicalRecord;
import com.yueshan.backend.dto.MedicalRecordActionRequest;
import com.yueshan.backend.dto.MedicalRecordCreateRequest;
import com.yueshan.backend.dto.MedicalRecordQueryRequest;
import com.yueshan.backend.dto.MedicalRecordResponse;
import com.yueshan.backend.dto.MedicalRecordUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.MedicalRecordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/medical-records", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class MedicalRecordController {
    private final MedicalRecordService service;

    public MedicalRecordController(MedicalRecordService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> create(@Valid @RequestBody MedicalRecordCreateRequest request) {
        MedicalRecordResponse created = MedicalRecordResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/medical-records/" + created.id()))
                .body(ApiResponse.success("病历记录创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<MedicalRecordResponse>> findPage(@Valid @ModelAttribute MedicalRecordQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<PageResponse<MedicalRecordResponse>> findByPatient(@PathVariable Long patientId,
            @Valid @ModelAttribute MedicalRecordQueryRequest query) {
        query.setPatientId(patientId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<PageResponse<MedicalRecordResponse>> findByAdmission(@PathVariable Long admissionId,
            @Valid @ModelAttribute MedicalRecordQueryRequest query) {
        query.setAdmissionId(admissionId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(MedicalRecordResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("病历记录不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> update(@PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("病历记录修改成功", MedicalRecordResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("病历记录不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("病历记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("病历记录删除成功", null));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<MedicalRecordResponse> submit(@PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalRecordActionRequest request) {
        return ApiResponse.success("病历记录提交成功", MedicalRecordResponse.from(service.submit(id, request)));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<MedicalRecordResponse> archive(@PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalRecordActionRequest request) {
        return ApiResponse.success("病历记录归档成功", MedicalRecordResponse.from(service.archive(id, request)));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<MedicalRecordResponse> review(@PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalRecordActionRequest request) {
        return ApiResponse.success("病历记录审核成功", MedicalRecordResponse.from(service.review(id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<MedicalRecordResponse> cancel(@PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalRecordActionRequest request) {
        return ApiResponse.success("病历记录已作废", MedicalRecordResponse.from(service.cancel(id, request)));
    }

    private PageResponse<MedicalRecordResponse> mapPage(PageResponse<MedicalRecord> source) {
        List<MedicalRecordResponse> records = source.records().stream().map(MedicalRecordResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
