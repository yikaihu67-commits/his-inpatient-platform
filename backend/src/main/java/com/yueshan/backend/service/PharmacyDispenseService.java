package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.PharmacyDispense;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.dto.PharmacyDispenseActionRequest;
import com.yueshan.backend.dto.PharmacyDispenseCreateRequest;
import com.yueshan.backend.dto.PharmacyDispenseQueryRequest;

public interface PharmacyDispenseService {
    PageResponse<PharmacyDispense> findPage(PharmacyDispenseQueryRequest query);
    Optional<PharmacyDispense> findById(Long id);
    PharmacyDispense create(PharmacyDispenseCreateRequest request);
    PharmacyDispense dispense(Long id, PharmacyDispenseActionRequest request);
    PharmacyDispense returnDrug(Long id, PharmacyDispenseActionRequest request);
    PharmacyDispense cancel(Long id, PharmacyDispenseActionRequest request);
}
