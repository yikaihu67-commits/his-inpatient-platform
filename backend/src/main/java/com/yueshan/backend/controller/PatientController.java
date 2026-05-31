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
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PatientCreateRequest;
import com.yueshan.backend.dto.PatientQueryRequest;
import com.yueshan.backend.dto.PatientResponse;
import com.yueshan.backend.dto.PatientUpdateRequest;
import com.yueshan.backend.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/patients", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PatientResponse>> findAll(@Valid @ModelAttribute PatientQueryRequest query) {
        PageResponse<PatientResponse> page = mapPage(patientService.findPage(query));
        return ApiResponse.success(page);
    }

    private PageResponse<PatientResponse> mapPage(PageResponse<com.yueshan.backend.domain.Patient> source) {
        List<PatientResponse> records = source.records().stream()
                .map(PatientResponse::from)
                .toList();
        return new PageResponse<>(source.total(), source.page(), source.pageSize(), records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> findById(@PathVariable Long id) {
        return patientService.findById(id)
                .map(patient -> ResponseEntity.ok(ApiResponse.success(PatientResponse.from(patient))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("患者不存在")));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PatientResponse>> create(@Valid @RequestBody PatientCreateRequest request) {
        PatientResponse created = PatientResponse.from(patientService.create(request));
        return ResponseEntity
                .created(URI.create("/api/patients/" + created.id()))
                .body(ApiResponse.success("创建成功", created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PatientResponse>> update(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(id, request)
                .map(updated -> ResponseEntity.ok(ApiResponse.success("修改成功", PatientResponse.from(updated))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("患者不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        if (!patientService.deleteById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("患者不存在"));
        }

        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
