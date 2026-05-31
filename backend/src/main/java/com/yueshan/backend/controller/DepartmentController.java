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
import com.yueshan.backend.domain.Department;
import com.yueshan.backend.dto.DepartmentCreateRequest;
import com.yueshan.backend.dto.DepartmentQueryRequest;
import com.yueshan.backend.dto.DepartmentResponse;
import com.yueshan.backend.dto.DepartmentUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.DepartmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/departments", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class DepartmentController {
    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentResponse created = DepartmentResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/departments/" + created.id()))
                .body(ApiResponse.success("科室创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<DepartmentResponse>> findPage(@Valid @ModelAttribute DepartmentQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(DepartmentResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("科室不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(@PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("科室修改成功", DepartmentResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("科室不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("科室不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("科室删除成功", null));
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<DepartmentResponse>> enable(@PathVariable Long id) {
        return service.enable(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success("科室启用成功", DepartmentResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("科室不存在")));
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<DepartmentResponse>> disable(@PathVariable Long id) {
        return service.disable(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success("科室停用成功", DepartmentResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("科室不存在")));
    }

    private PageResponse<DepartmentResponse> mapPage(PageResponse<Department> source) {
        List<DepartmentResponse> records = source.records().stream().map(DepartmentResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
