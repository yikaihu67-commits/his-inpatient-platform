package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.MedicalOrder;
import com.yueshan.backend.dto.MedicalOrderActionRequest;
import com.yueshan.backend.dto.MedicalOrderCreateRequest;
import com.yueshan.backend.dto.MedicalOrderQueryRequest;
import com.yueshan.backend.dto.MedicalOrderUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface MedicalOrderService {

    PageResponse<MedicalOrder> findPage(MedicalOrderQueryRequest query);

    Optional<MedicalOrder> findById(Long id);

    MedicalOrder create(MedicalOrderCreateRequest request);

    Optional<MedicalOrder> update(Long id, MedicalOrderUpdateRequest request);

    boolean deleteById(Long id);

    MedicalOrder submit(Long id, MedicalOrderActionRequest request);

    MedicalOrder check(Long id, MedicalOrderActionRequest request);

    MedicalOrder execute(Long id, MedicalOrderActionRequest request);

    MedicalOrder bill(Long id, MedicalOrderActionRequest request);

    MedicalOrder stop(Long id, MedicalOrderActionRequest request);

    MedicalOrder cancel(Long id, MedicalOrderActionRequest request);
}
