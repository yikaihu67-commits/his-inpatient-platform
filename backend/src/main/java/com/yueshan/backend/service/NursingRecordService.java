package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.NursingRecord;
import com.yueshan.backend.dto.NursingRecordActionRequest;
import com.yueshan.backend.dto.NursingRecordCreateRequest;
import com.yueshan.backend.dto.NursingRecordQueryRequest;
import com.yueshan.backend.dto.NursingRecordUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface NursingRecordService {
    PageResponse<NursingRecord> findPage(NursingRecordQueryRequest query);
    Optional<NursingRecord> findById(Long id);
    NursingRecord create(NursingRecordCreateRequest request);
    Optional<NursingRecord> update(Long id, NursingRecordUpdateRequest request);
    NursingRecord execute(Long id, NursingRecordActionRequest request);
    NursingRecord review(Long id, NursingRecordActionRequest request);
    NursingRecord cancel(Long id, NursingRecordActionRequest request);
    NursingRecord bill(Long id, NursingRecordActionRequest request);
}
