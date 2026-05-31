package com.yueshan.backend.dto.mobile;

import com.yueshan.backend.dto.selfservice.PatientSelfServiceSessionResponse;

public record PatientMobileLoginResponse(String sessionKey, PatientSelfServiceSessionResponse patient) {
}
