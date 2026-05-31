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
import com.yueshan.backend.domain.InpatientFee;
import com.yueshan.backend.dto.InpatientFeeCreateRequest;
import com.yueshan.backend.dto.InpatientFeeQueryRequest;
import com.yueshan.backend.dto.InpatientFeeResponse;
import com.yueshan.backend.dto.InpatientFeeSummaryResponse;
import com.yueshan.backend.dto.InpatientFeeUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.InpatientFeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/fees", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class InpatientFeeController {
    private final InpatientFeeService service;

    public InpatientFeeController(InpatientFeeService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InpatientFeeResponse>> create(@Valid @RequestBody InpatientFeeCreateRequest request) {
        InpatientFeeResponse created = InpatientFeeResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/fees/" + created.id())).body(ApiResponse.success("费用创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<InpatientFeeResponse>> findPage(@Valid @ModelAttribute InpatientFeeQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InpatientFeeResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(fee -> ResponseEntity.ok(ApiResponse.success(InpatientFeeResponse.from(fee))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("费用不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InpatientFeeResponse>> update(@PathVariable Long id, @Valid @RequestBody InpatientFeeUpdateRequest request) {
        return service.update(id, request)
                .map(fee -> ResponseEntity.ok(ApiResponse.success("费用修改成功", InpatientFeeResponse.from(fee))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("费用不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("费用不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("费用删除成功", null));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InpatientFeeResponse>> cancel(@PathVariable Long id) {
        return service.cancel(id)
                .map(fee -> ResponseEntity.ok(ApiResponse.success("费用取消成功", InpatientFeeResponse.from(fee))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("费用不存在")));
    }

    @GetMapping("/admission/{admissionId}/summary")
    public ApiResponse<InpatientFeeSummaryResponse> summary(@PathVariable Long admissionId) {
        return ApiResponse.success(service.summary(admissionId));
    }

    private PageResponse<InpatientFeeResponse> mapPage(PageResponse<InpatientFee> source) {
        List<InpatientFeeResponse> records = source.records().stream().map(InpatientFeeResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
