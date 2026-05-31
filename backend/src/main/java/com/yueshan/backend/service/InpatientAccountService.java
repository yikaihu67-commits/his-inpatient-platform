package com.yueshan.backend.service;

import com.yueshan.backend.dto.InpatientAccountSummaryResponse;

public interface InpatientAccountService {
    InpatientAccountSummaryResponse summary(Long admissionId);
}
