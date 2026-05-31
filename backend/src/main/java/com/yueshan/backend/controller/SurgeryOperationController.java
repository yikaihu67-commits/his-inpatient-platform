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
import com.yueshan.backend.domain.SurgeryOperation;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.SurgeryActionRequest;
import com.yueshan.backend.dto.SurgeryCreateRequest;
import com.yueshan.backend.dto.SurgeryQueryRequest;
import com.yueshan.backend.dto.SurgeryResponse;
import com.yueshan.backend.dto.SurgeryUpdateRequest;
import com.yueshan.backend.service.SurgeryOperationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/surgeries", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SurgeryOperationController {
    private final SurgeryOperationService service;

    public SurgeryOperationController(SurgeryOperationService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SurgeryResponse>> create(@Valid @RequestBody SurgeryCreateRequest request) {
        SurgeryResponse created = SurgeryResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/surgeries/" + created.id()))
                .body(ApiResponse.success("手术申请创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<SurgeryResponse>> findPage(@Valid @ModelAttribute SurgeryQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<PageResponse<SurgeryResponse>> findByPatient(@PathVariable Long patientId,
            @Valid @ModelAttribute SurgeryQueryRequest query) {
        query.setPatientId(patientId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<PageResponse<SurgeryResponse>> findByAdmission(@PathVariable Long admissionId,
            @Valid @ModelAttribute SurgeryQueryRequest query) {
        query.setAdmissionId(admissionId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SurgeryResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(SurgeryResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("手术记录不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SurgeryResponse>> update(@PathVariable Long id, @Valid @RequestBody SurgeryUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("手术记录修改成功", SurgeryResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("手术记录不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("手术记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("手术记录删除成功", null));
    }

    @PostMapping("/{id}/schedule")
    public ApiResponse<SurgeryResponse> schedule(@PathVariable Long id, @Valid @RequestBody(required = false) SurgeryActionRequest request) {
        return ApiResponse.success("手术安排成功", SurgeryResponse.from(service.schedule(id, request)));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<SurgeryResponse> start(@PathVariable Long id, @Valid @RequestBody(required = false) SurgeryActionRequest request) {
        return ApiResponse.success("手术已开始", SurgeryResponse.from(service.start(id, request)));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<SurgeryResponse> complete(@PathVariable Long id, @Valid @RequestBody(required = false) SurgeryActionRequest request) {
        return ApiResponse.success("手术已完成", SurgeryResponse.from(service.complete(id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SurgeryResponse> cancel(@PathVariable Long id, @Valid @RequestBody(required = false) SurgeryActionRequest request) {
        return ApiResponse.success("手术已取消", SurgeryResponse.from(service.cancel(id, request)));
    }

    @PostMapping("/{id}/bill")
    public ApiResponse<SurgeryResponse> bill(@PathVariable Long id, @Valid @RequestBody(required = false) SurgeryActionRequest request) {
        return ApiResponse.success("手术费用生成成功", SurgeryResponse.from(service.bill(id, request)));
    }

    private PageResponse<SurgeryResponse> mapPage(PageResponse<SurgeryOperation> source) {
        List<SurgeryResponse> records = source.records().stream().map(SurgeryResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
