package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.domain.InpatientFee;
import com.yueshan.backend.domain.PharmacyDispense;
import com.yueshan.backend.dto.InpatientFeeCreateRequest;
import com.yueshan.backend.dto.InpatientFeeQueryRequest;
import com.yueshan.backend.dto.InpatientFeeSummaryResponse;
import com.yueshan.backend.dto.InpatientFeeUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface InpatientFeeService {
    PageResponse<InpatientFee> findPage(InpatientFeeQueryRequest query);
    Optional<InpatientFee> findById(Long id);
    InpatientFee create(InpatientFeeCreateRequest request);
    Optional<InpatientFee> update(Long id, InpatientFeeUpdateRequest request);
    boolean deleteById(Long id);
    Optional<InpatientFee> cancel(Long id);
    InpatientFeeSummaryResponse summary(Long admissionId);
    void createDrugDispenseFee(PharmacyDispense dispense, Drug drug);
    void createDrugReturnFee(PharmacyDispense dispense, Drug drug);
}
