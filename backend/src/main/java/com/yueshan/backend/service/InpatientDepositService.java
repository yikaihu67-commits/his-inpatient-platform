package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.InpatientDeposit;
import com.yueshan.backend.dto.InpatientDepositQueryRequest;
import com.yueshan.backend.dto.InpatientDepositRequest;
import com.yueshan.backend.dto.InpatientDepositSummaryResponse;
import com.yueshan.backend.dto.PageResponse;

public interface InpatientDepositService {
    InpatientDeposit pay(InpatientDepositRequest request);
    InpatientDeposit refund(InpatientDepositRequest request);
    PageResponse<InpatientDeposit> findPage(InpatientDepositQueryRequest query);
    Optional<InpatientDeposit> findById(Long id);
    Optional<InpatientDeposit> cancel(Long id);
    InpatientDepositSummaryResponse summary(Long admissionId);
}
