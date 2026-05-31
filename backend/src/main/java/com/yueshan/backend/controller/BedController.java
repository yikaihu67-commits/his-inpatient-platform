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
import com.yueshan.backend.domain.Bed;
import com.yueshan.backend.dto.BedAssignRequest;
import com.yueshan.backend.dto.BedCreateRequest;
import com.yueshan.backend.dto.BedQueryRequest;
import com.yueshan.backend.dto.BedResponse;
import com.yueshan.backend.dto.BedUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.BedService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/beds", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<BedResponse>> create(@Valid @RequestBody BedCreateRequest request) {
        BedResponse created = BedResponse.from(bedService.create(request));
        return ResponseEntity
                .created(URI.create("/api/beds/" + created.id()))
                .body(ApiResponse.success("床位创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<BedResponse>> findPage(@Valid @ModelAttribute BedQueryRequest query) {
        return ApiResponse.success(mapPage(bedService.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BedResponse>> findById(@PathVariable Long id) {
        return bedService.findById(id)
                .map(bed -> ResponseEntity.ok(ApiResponse.success(BedResponse.from(bed))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("床位不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<BedResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BedUpdateRequest request) {
        return bedService.update(id, request)
                .map(bed -> ResponseEntity.ok(ApiResponse.success("床位修改成功", BedResponse.from(bed))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("床位不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!bedService.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("床位不存在"));
        }

        return ResponseEntity.ok(ApiResponse.success("床位删除成功", null));
    }

    @PostMapping(value = "/{bedId}/assign", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BedResponse> assign(
            @PathVariable Long bedId,
            @Valid @RequestBody BedAssignRequest request) {
        return ApiResponse.success("床位分配成功", BedResponse.from(bedService.assign(bedId, request)));
    }

    @PostMapping("/{bedId}/release")
    public ApiResponse<BedResponse> release(@PathVariable Long bedId) {
        return ApiResponse.success("床位释放成功", BedResponse.from(bedService.release(bedId)));
    }

    private PageResponse<BedResponse> mapPage(PageResponse<Bed> source) {
        List<BedResponse> records = source.records().stream()
                .map(BedResponse::from)
                .toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
