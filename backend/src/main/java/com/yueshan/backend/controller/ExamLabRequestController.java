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
import com.yueshan.backend.domain.ExamLabRequest;
import com.yueshan.backend.dto.ExamLabRequestActionRequest;
import com.yueshan.backend.dto.ExamLabRequestCreateRequest;
import com.yueshan.backend.dto.ExamLabRequestQueryRequest;
import com.yueshan.backend.dto.ExamLabRequestResponse;
import com.yueshan.backend.dto.ExamLabRequestUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.ExamLabRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/exam-lab-requests", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class ExamLabRequestController {
    private final ExamLabRequestService service;

    public ExamLabRequestController(ExamLabRequestService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ExamLabRequestResponse>> create(@Valid @RequestBody ExamLabRequestCreateRequest request) {
        ExamLabRequestResponse created = ExamLabRequestResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/exam-lab-requests/" + created.id()))
                .body(ApiResponse.success("检查检验申请创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<ExamLabRequestResponse>> findPage(@Valid @ModelAttribute ExamLabRequestQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<PageResponse<ExamLabRequestResponse>> findByPatient(@PathVariable Long patientId,
            @Valid @ModelAttribute ExamLabRequestQueryRequest query) {
        query.setPatientId(patientId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<PageResponse<ExamLabRequestResponse>> findByAdmission(@PathVariable Long admissionId,
            @Valid @ModelAttribute ExamLabRequestQueryRequest query) {
        query.setAdmissionId(admissionId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamLabRequestResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(ExamLabRequestResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("检查检验申请不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ExamLabRequestResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ExamLabRequestUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("检查检验申请修改成功", ExamLabRequestResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("检查检验申请不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("检查检验申请不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("检查检验申请删除成功", null));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<ExamLabRequestResponse> submit(@PathVariable Long id,
            @Valid @RequestBody(required = false) ExamLabRequestActionRequest request) {
        return ApiResponse.success("检查检验申请提交成功", ExamLabRequestResponse.from(service.submit(id, request)));
    }

    @PostMapping("/{id}/schedule")
    public ApiResponse<ExamLabRequestResponse> schedule(@PathVariable Long id,
            @Valid @RequestBody(required = false) ExamLabRequestActionRequest request) {
        return ApiResponse.success("检查检验安排成功", ExamLabRequestResponse.from(service.schedule(id, request)));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<ExamLabRequestResponse> complete(@PathVariable Long id,
            @Valid @RequestBody(required = false) ExamLabRequestActionRequest request) {
        return ApiResponse.success("检查检验完成成功", ExamLabRequestResponse.from(service.complete(id, request)));
    }

    @PostMapping("/{id}/report")
    public ApiResponse<ExamLabRequestResponse> report(@PathVariable Long id,
            @Valid @RequestBody(required = false) ExamLabRequestActionRequest request) {
        return ApiResponse.success("检查检验报告录入成功", ExamLabRequestResponse.from(service.report(id, request)));
    }

    @PostMapping("/{id}/bill")
    public ApiResponse<ExamLabRequestResponse> bill(@PathVariable Long id,
            @Valid @RequestBody(required = false) ExamLabRequestActionRequest request) {
        return ApiResponse.success("检查检验费用生成成功", ExamLabRequestResponse.from(service.bill(id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ExamLabRequestResponse> cancel(@PathVariable Long id,
            @Valid @RequestBody(required = false) ExamLabRequestActionRequest request) {
        return ApiResponse.success("检查检验申请已取消", ExamLabRequestResponse.from(service.cancel(id, request)));
    }

    private PageResponse<ExamLabRequestResponse> mapPage(PageResponse<ExamLabRequest> source) {
        List<ExamLabRequestResponse> records = source.records().stream().map(ExamLabRequestResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
