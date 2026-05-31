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
import com.yueshan.backend.domain.DictItem;
import com.yueshan.backend.dto.DictItemCreateRequest;
import com.yueshan.backend.dto.DictItemQueryRequest;
import com.yueshan.backend.dto.DictItemResponse;
import com.yueshan.backend.dto.DictItemUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.DictItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/dict-items", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class DictItemController {
    private final DictItemService service;

    public DictItemController(DictItemService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DictItemResponse>> create(@Valid @RequestBody DictItemCreateRequest request) {
        DictItemResponse created = DictItemResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/dict-items/" + created.id()))
                .body(ApiResponse.success("字典项创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<DictItemResponse>> findPage(@Valid @ModelAttribute DictItemQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DictItemResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(DictItemResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("字典项不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DictItemResponse>> update(@PathVariable Long id,
            @Valid @RequestBody DictItemUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("字典项修改成功", DictItemResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("字典项不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("字典项不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("字典项删除成功", null));
    }

    @GetMapping("/type/{dictType}")
    public ApiResponse<List<DictItemResponse>> findEnabledByType(@PathVariable String dictType) {
        return ApiResponse.success(service.findEnabledByType(dictType).stream().map(DictItemResponse::from).toList());
    }

    private PageResponse<DictItemResponse> mapPage(PageResponse<DictItem> source) {
        List<DictItemResponse> records = source.records().stream().map(DictItemResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
