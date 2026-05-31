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
import com.yueshan.backend.domain.InpatientDischarge;
import com.yueshan.backend.dto.DischargeCreateRequest;
import com.yueshan.backend.dto.DischargeQueryRequest;
import com.yueshan.backend.dto.DischargeResponse;
import com.yueshan.backend.dto.DischargeSettleRequest;
import com.yueshan.backend.dto.DischargeUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.InpatientDischargeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/discharges", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class InpatientDischargeController {
    private final InpatientDischargeService service;

    public InpatientDischargeController(InpatientDischargeService service) {
        this.service = service;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DischargeResponse>> create(@Valid @RequestBody DischargeCreateRequest request) {
        DischargeResponse created = DischargeResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/discharges/" + created.id()))
                .body(ApiResponse.success("出院结算创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<DischargeResponse>> findPage(@Valid @ModelAttribute DischargeQueryRequest query) {
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<PageResponse<DischargeResponse>> findByPatient(@PathVariable Long patientId,
            @Valid @ModelAttribute DischargeQueryRequest query) {
        query.setPatientId(patientId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<PageResponse<DischargeResponse>> findByAdmission(@PathVariable Long admissionId,
            @Valid @ModelAttribute DischargeQueryRequest query) {
        query.setAdmissionId(admissionId);
        return ApiResponse.success(mapPage(service.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DischargeResponse>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(DischargeResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("出院结算记录不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DischargeResponse>> update(@PathVariable Long id, @Valid @RequestBody DischargeUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("出院结算修改成功", DischargeResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("出院结算记录不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("出院结算记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("出院结算删除成功", null));
    }

    @PostMapping(value = "/{id}/settle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DischargeResponse>> settle(@PathVariable Long id, @Valid @RequestBody DischargeSettleRequest request) {
        return service.settle(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("出院结算成功", DischargeResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("出院结算记录不存在")));
    }

    @PostMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DischargeResponse>> cancel(@PathVariable Long id, @Valid @RequestBody DischargeSettleRequest request) {
        return service.cancel(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("出院结算已取消", DischargeResponse.from(item))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("出院结算记录不存在")));
    }

    private PageResponse<DischargeResponse> mapPage(PageResponse<InpatientDischarge> source) {
        List<DischargeResponse> records = source.records().stream().map(DischargeResponse::from).toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
