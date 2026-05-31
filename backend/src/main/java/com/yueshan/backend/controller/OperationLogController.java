package com.yueshan.backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.domain.OperationLog;
import com.yueshan.backend.dto.OperationLogQueryRequest;
import com.yueshan.backend.dto.OperationLogResponse;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.OperationLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/operation-logs", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class OperationLogController {
    private final OperationLogService service;

    public OperationLogController(OperationLogService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<OperationLogResponse>> findPage(@Valid @ModelAttribute OperationLogQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OperationLogResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(OperationLogResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("操作日志不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("操作日志不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("操作日志删除成功", null));
    }

    private PageResponse<OperationLogResponse> mapPage(PageResponse<OperationLog> source) {
        List<OperationLogResponse> records = source.records().stream().map(OperationLogResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
