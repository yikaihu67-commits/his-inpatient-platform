package com.yueshan.backend.dto.mobile;

import java.util.List;

import com.yueshan.backend.dto.selfservice.SelfServiceAdmissionInfoResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillDetailResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillSummaryResponse;

public record PatientMobileHomeResponse(
        SelfServiceAdmissionInfoResponse admissionInfo,
        SelfServiceBillSummaryResponse billSummary,
        List<SelfServiceBillDetailResponse> recentFees) {
}
