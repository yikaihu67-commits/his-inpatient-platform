package com.yueshan.backend.service;

import java.time.LocalDate;
import java.util.List;

import com.yueshan.backend.dto.selfservice.PatientSelfServiceSessionResponse;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceVerifyRequest;
import com.yueshan.backend.dto.selfservice.SelfServiceAdmissionInfoResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillDetailResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillSummaryResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceDischargeResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceExamLabResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceSurgeryResponse;

public interface PatientSelfService {
    PatientSelfServiceSessionResponse verify(PatientSelfServiceVerifyRequest request);

    SelfServiceAdmissionInfoResponse admissionInfo(Long patientId, Long admissionId);

    SelfServiceBillSummaryResponse billSummary(Long patientId, Long admissionId);

    List<SelfServiceBillDetailResponse> billDetails(Long patientId, Long admissionId, LocalDate startDate, LocalDate endDate,
                                                    String itemCategory, String status);

    List<SelfServiceExamLabResponse> examLabs(Long patientId, Long admissionId);

    List<SelfServiceSurgeryResponse> surgeries(Long patientId, Long admissionId);

    SelfServiceDischargeResponse discharge(Long patientId, Long admissionId);
}
