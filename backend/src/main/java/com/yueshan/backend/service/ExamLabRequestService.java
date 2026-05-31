package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.ExamLabRequest;
import com.yueshan.backend.dto.ExamLabRequestActionRequest;
import com.yueshan.backend.dto.ExamLabRequestCreateRequest;
import com.yueshan.backend.dto.ExamLabRequestQueryRequest;
import com.yueshan.backend.dto.ExamLabRequestUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface ExamLabRequestService {
    ExamLabRequest create(ExamLabRequestCreateRequest request);
    PageResponse<ExamLabRequest> findPage(ExamLabRequestQueryRequest query);
    Optional<ExamLabRequest> findById(Long id);
    Optional<ExamLabRequest> update(Long id, ExamLabRequestUpdateRequest request);
    boolean deleteById(Long id);
    ExamLabRequest submit(Long id, ExamLabRequestActionRequest request);
    ExamLabRequest schedule(Long id, ExamLabRequestActionRequest request);
    ExamLabRequest complete(Long id, ExamLabRequestActionRequest request);
    ExamLabRequest report(Long id, ExamLabRequestActionRequest request);
    ExamLabRequest bill(Long id, ExamLabRequestActionRequest request);
    ExamLabRequest cancel(Long id, ExamLabRequestActionRequest request);
}
