package com.yueshan.backend.dto.selfservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SelfServiceSurgeryResponse {
    private String surgeryName;
    private String status;
    private LocalDateTime plannedTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String primaryDoctorName;
    private String operatingRoom;
    private BigDecimal surgeryFee;
}
