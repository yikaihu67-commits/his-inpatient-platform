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
import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.dto.DrugCreateRequest;
import com.yueshan.backend.dto.DrugQueryRequest;
import com.yueshan.backend.dto.DrugResponse;
import com.yueshan.backend.dto.DrugUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.DrugService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/drugs", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class DrugController {
    private final DrugService service;
    public DrugController(DrugService service) { this.service = service; }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DrugResponse>> create(@Valid @RequestBody DrugCreateRequest request) {
        DrugResponse created = DrugResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/drugs/" + created.id())).body(ApiResponse.success("药品创建成功", created));
    }
    @GetMapping
    public ApiResponse<PageResponse<DrugResponse>> findPage(@Valid @ModelAttribute DrugQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DrugResponse>> findById(@PathVariable Long id) {
        return service.findById(id).map(d -> ResponseEntity.ok(ApiResponse.success(DrugResponse.from(d))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("药品不存在")));
    }
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DrugResponse>> update(@PathVariable Long id, @Valid @RequestBody DrugUpdateRequest request) {
        return service.update(id, request).map(d -> ResponseEntity.ok(ApiResponse.success("药品修改成功", DrugResponse.from(d))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("药品不存在")));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) return ResponseEntity.status(404).body(ApiResponse.error("药品不存在"));
        return ResponseEntity.ok(ApiResponse.success("药品删除成功", null));
    }
    private PageResponse<DrugResponse> mapPage(PageResponse<Drug> source) {
        List<DrugResponse> records = source.records().stream().map(DrugResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
