package com.yueshan.backend.service;

import java.util.Optional;

import com.yueshan.backend.domain.InpatientDischarge;
import com.yueshan.backend.dto.DischargeCreateRequest;
import com.yueshan.backend.dto.DischargeQueryRequest;
import com.yueshan.backend.dto.DischargeSettleRequest;
import com.yueshan.backend.dto.DischargeUpdateRequest;
import com.yueshan.backend.dto.PageResponse;

public interface InpatientDischargeService {
    InpatientDischarge create(DischargeCreateRequest request);
    PageResponse<InpatientDischarge> findPage(DischargeQueryRequest query);
    Optional<InpatientDischarge> findById(Long id);
    Optional<InpatientDischarge> update(Long id, DischargeUpdateRequest request);
    boolean deleteById(Long id);
    Optional<InpatientDischarge> settle(Long id, DischargeSettleRequest request);
    Optional<InpatientDischarge> cancel(Long id, DischargeSettleRequest request);
}
