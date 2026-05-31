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
import com.yueshan.backend.domain.Staff;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.StaffCreateRequest;
import com.yueshan.backend.dto.StaffQueryRequest;
import com.yueshan.backend.dto.StaffResponse;
import com.yueshan.backend.dto.StaffUpdateRequest;
import com.yueshan.backend.service.StaffService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/staff", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class StaffController {
    private final StaffService service;

    public StaffController(StaffService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<StaffResponse>> create(@Valid @RequestBody StaffCreateRequest request) {
        StaffResponse created = StaffResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/staff/" + created.id()))
                .body(ApiResponse.success("人员创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<StaffResponse>> findPage(@Valid @ModelAttribute StaffQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(StaffResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("人员不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<StaffResponse>> update(@PathVariable Long id,
            @Valid @RequestBody StaffUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("人员修改成功", StaffResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("人员不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("人员不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("人员删除成功", null));
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<StaffResponse>> enable(@PathVariable Long id) {
        return service.enable(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success("人员启用成功", StaffResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("人员不存在")));
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<StaffResponse>> disable(@PathVariable Long id) {
        return service.disable(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success("人员停用成功", StaffResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("人员不存在")));
    }

    private PageResponse<StaffResponse> mapPage(PageResponse<Staff> source) {
        List<StaffResponse> records = source.records().stream().map(StaffResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
