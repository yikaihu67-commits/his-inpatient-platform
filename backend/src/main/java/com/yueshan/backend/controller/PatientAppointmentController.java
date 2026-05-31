package com.yueshan.backend.controller;

import java.net.URI;

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
import com.yueshan.backend.domain.PatientAppointment;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PatientAppointmentActionRequest;
import com.yueshan.backend.dto.PatientAppointmentCreateRequest;
import com.yueshan.backend.dto.PatientAppointmentQueryRequest;
import com.yueshan.backend.dto.PatientAppointmentUpdateRequest;
import com.yueshan.backend.service.PatientAppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/patient-appointments", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
@RequiredArgsConstructor
public class PatientAppointmentController {
    private final PatientAppointmentService service;

    @GetMapping
    public ApiResponse<PageResponse<PatientAppointment>> findPage(@Valid @ModelAttribute PatientAppointmentQueryRequest query) {
        return ApiResponse.success(service.findPage(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientAppointment>> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(item)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("预约不存在")));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PatientAppointment>> create(@Valid @RequestBody PatientAppointmentCreateRequest request) {
        PatientAppointment created = service.create(request);
        return ResponseEntity.created(URI.create("/api/patient-appointments/" + created.getId()))
                .body(ApiResponse.success("预约创建成功", created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PatientAppointment>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody PatientAppointmentUpdateRequest request) {
        return service.update(id, request)
                .map(item -> ResponseEntity.ok(ApiResponse.success("预约修改成功", item)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("预约不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("预约删除成功", null));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<PatientAppointment> confirm(@PathVariable Long id,
                                                   @RequestBody(required = false) PatientAppointmentActionRequest request) {
        return ApiResponse.success("预约确认成功", service.confirm(id, request == null ? new PatientAppointmentActionRequest() : request));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<PatientAppointment> complete(@PathVariable Long id,
                                                    @RequestBody(required = false) PatientAppointmentActionRequest request) {
        return ApiResponse.success("预约完成成功", service.complete(id, request == null ? new PatientAppointmentActionRequest() : request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PatientAppointment> cancel(@PathVariable Long id,
                                                  @RequestBody(required = false) PatientAppointmentActionRequest request) {
        return ApiResponse.success("预约取消成功", service.cancel(id, request == null ? new PatientAppointmentActionRequest() : request));
    }
}
