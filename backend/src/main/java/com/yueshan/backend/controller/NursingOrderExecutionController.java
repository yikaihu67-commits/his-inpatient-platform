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
import com.yueshan.backend.domain.NursingOrderExecution;
import com.yueshan.backend.dto.NursingExecutionActionRequest;
import com.yueshan.backend.dto.NursingExecutionCreateRequest;
import com.yueshan.backend.dto.NursingExecutionQueryRequest;
import com.yueshan.backend.dto.NursingExecutionResponse;
import com.yueshan.backend.dto.NursingExecutionUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.NursingOrderExecutionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/nursing/executions", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class NursingOrderExecutionController {
    private final NursingOrderExecutionService service;

    public NursingOrderExecutionController(NursingOrderExecutionService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<NursingExecutionResponse>> create(@Valid @RequestBody NursingExecutionCreateRequest request) {
        NursingExecutionResponse created = NursingExecutionResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/nursing/executions/" + created.id()))
                .body(ApiResponse.success("护士执行记录创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<NursingExecutionResponse>> findPage(@Valid @ModelAttribute NursingExecutionQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NursingExecutionResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(NursingExecutionResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("护士执行记录不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<NursingExecutionResponse>> update(@PathVariable Long id, @Valid @RequestBody NursingExecutionUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("护士执行记录修改成功", NursingExecutionResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("护士执行记录不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("护士执行记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("护士执行记录删除成功", null));
    }

    @PostMapping(value = "/{id}/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<NursingExecutionResponse> execute(@PathVariable Long id, @Valid @RequestBody NursingExecutionActionRequest request) {
        return ApiResponse.success("护士执行成功", NursingExecutionResponse.from(service.execute(id, request)));
    }

    @PostMapping(value = "/{id}/fail", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<NursingExecutionResponse> fail(@PathVariable Long id, @Valid @RequestBody NursingExecutionActionRequest request) {
        return ApiResponse.success("护士执行记录已标记失败", NursingExecutionResponse.from(service.fail(id, request)));
    }

    @PostMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<NursingExecutionResponse> cancel(@PathVariable Long id, @Valid @RequestBody NursingExecutionActionRequest request) {
        return ApiResponse.success("护士执行记录已取消", NursingExecutionResponse.from(service.cancel(id, request)));
    }

    private PageResponse<NursingExecutionResponse> mapPage(PageResponse<NursingOrderExecution> source) {
        List<NursingExecutionResponse> records = source.records().stream().map(NursingExecutionResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
