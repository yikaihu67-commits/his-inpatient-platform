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
import com.yueshan.backend.domain.MedicalOrder;
import com.yueshan.backend.dto.MedicalOrderActionRequest;
import com.yueshan.backend.dto.MedicalOrderCreateRequest;
import com.yueshan.backend.dto.MedicalOrderQueryRequest;
import com.yueshan.backend.dto.MedicalOrderResponse;
import com.yueshan.backend.dto.MedicalOrderUpdateRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.service.MedicalOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class MedicalOrderController {

    private final MedicalOrderService medicalOrderService;

    public MedicalOrderController(MedicalOrderService medicalOrderService) {
        this.medicalOrderService = medicalOrderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MedicalOrderResponse>> create(@Valid @RequestBody MedicalOrderCreateRequest request) {
        MedicalOrderResponse created = MedicalOrderResponse.from(medicalOrderService.create(request));
        return ResponseEntity
                .created(URI.create("/api/orders/" + created.id()))
                .body(ApiResponse.success("医嘱创建成功", created));
    }

    @GetMapping
    public ApiResponse<PageResponse<MedicalOrderResponse>> findPage(@Valid @ModelAttribute MedicalOrderQueryRequest query) {
        return ApiResponse.success(mapPage(medicalOrderService.findPage(query)));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<PageResponse<MedicalOrderResponse>> findByPatient(
            @PathVariable Long patientId,
            @Valid @ModelAttribute MedicalOrderQueryRequest query) {
        query.setPatientId(patientId);
        return ApiResponse.success(mapPage(medicalOrderService.findPage(query)));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<PageResponse<MedicalOrderResponse>> findByAdmission(
            @PathVariable Long admissionId,
            @Valid @ModelAttribute MedicalOrderQueryRequest query) {
        query.setAdmissionId(admissionId);
        return ApiResponse.success(mapPage(medicalOrderService.findPage(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalOrderResponse>> findById(@PathVariable Long id) {
        return medicalOrderService.findById(id)
                .map(order -> ResponseEntity.ok(ApiResponse.success(MedicalOrderResponse.from(order))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("医嘱不存在")));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MedicalOrderResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicalOrderUpdateRequest request) {
        return medicalOrderService.update(id, request)
                .map(order -> ResponseEntity.ok(ApiResponse.success("医嘱修改成功", MedicalOrderResponse.from(order))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("医嘱不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!medicalOrderService.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("医嘱不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("医嘱删除成功", null));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<MedicalOrderResponse> submit(@PathVariable Long id) {
        return ApiResponse.success("医嘱提交成功", MedicalOrderResponse.from(medicalOrderService.submit(id, null)));
    }

    @PostMapping("/{id}/check")
    public ApiResponse<MedicalOrderResponse> check(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalOrderActionRequest request) {
        return ApiResponse.success("医嘱核对成功", MedicalOrderResponse.from(medicalOrderService.check(id, request)));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<MedicalOrderResponse> execute(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalOrderActionRequest request) {
        return ApiResponse.success("医嘱执行成功", MedicalOrderResponse.from(medicalOrderService.execute(id, request)));
    }

    @PostMapping("/{id}/bill")
    public ApiResponse<MedicalOrderResponse> bill(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) MedicalOrderActionRequest request) {
        return ApiResponse.success("医嘱费用生成成功", MedicalOrderResponse.from(medicalOrderService.bill(id, request)));
    }

    @PostMapping("/{id}/stop")
    public ApiResponse<MedicalOrderResponse> stop(@PathVariable Long id) {
        return ApiResponse.success("医嘱停止成功", MedicalOrderResponse.from(medicalOrderService.stop(id, null)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<MedicalOrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("医嘱作废成功", MedicalOrderResponse.from(medicalOrderService.cancel(id, null)));
    }

    private PageResponse<MedicalOrderResponse> mapPage(PageResponse<MedicalOrder> source) {
        List<MedicalOrderResponse> records = source.records().stream()
                .map(MedicalOrderResponse::from)
                .toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }
}
