package com.yueshan.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.domain.NursingRecord;
import com.yueshan.backend.dto.NursingRecordActionRequest;
import com.yueshan.backend.dto.NursingRecordCreateRequest;
import com.yueshan.backend.dto.NursingRecordQueryRequest;
import com.yueshan.backend.dto.NursingRecordResponse;
import com.yueshan.backend.dto.NursingRecordUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.NursingRecordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/nursing/records", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class NursingRecordController {
    private final NursingRecordService service;

    public NursingRecordController(NursingRecordService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<NursingRecordResponse>> create(@Valid @RequestBody NursingRecordCreateRequest request) {
        NursingRecordResponse created = NursingRecordResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/nursing/records/" + created.id()))
                .body(ApiResponse.success("护理记录创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<NursingRecordResponse>> findPage(@Valid @ModelAttribute NursingRecordQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<PageResponse<NursingRecordResponse>> findByPatient(@PathVariable Long patientId,
            @Valid @ModelAttribute NursingRecordQueryRequest query) {
        query.setPatientId(patientId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<PageResponse<NursingRecordResponse>> findByAdmission(@PathVariable Long admissionId,
            @Valid @ModelAttribute NursingRecordQueryRequest query) {
        query.setAdmissionId(admissionId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NursingRecordResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(NursingRecordResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("护理记录不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<NursingRecordResponse>> update(@PathVariable Long id,
            @Valid @RequestBody NursingRecordUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("护理记录修改成功", NursingRecordResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("护理记录不存在")));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<NursingRecordResponse> execute(@PathVariable Long id,
            @Valid @RequestBody(required = false) NursingRecordActionRequest request) {
        return ApiResponse.success("护理记录执行成功", NursingRecordResponse.from(service.execute(id, request)));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<NursingRecordResponse> review(@PathVariable Long id,
            @Valid @RequestBody(required = false) NursingRecordActionRequest request) {
        return ApiResponse.success("护理记录已查看", NursingRecordResponse.from(service.review(id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<NursingRecordResponse> cancel(@PathVariable Long id,
            @Valid @RequestBody(required = false) NursingRecordActionRequest request) {
        return ApiResponse.success("护理记录取消成功", NursingRecordResponse.from(service.cancel(id, request)));
    }

    @PostMapping("/{id}/bill")
    public ApiResponse<NursingRecordResponse> bill(@PathVariable Long id,
            @Valid @RequestBody(required = false) NursingRecordActionRequest request) {
        return ApiResponse.success("护理费用生成成功", NursingRecordResponse.from(service.bill(id, request)));
    }

    private PageResponse<NursingRecordResponse> mapPage(PageResponse<NursingRecord> source) {
        List<NursingRecordResponse> records = source.records().stream().map(NursingRecordResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
