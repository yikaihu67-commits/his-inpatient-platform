package com.yueshan.backend.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Patient {

    private Long id;

    private String patientNo;

    private String name;

    private String gender;

    private String idCard;

    private String phone;

    private LocalDate birthDate;

    private String address;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
