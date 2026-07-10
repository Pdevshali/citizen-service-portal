package com.pdev.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenProfileResponse {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String state;
    private String pincode;
    private String kycStatus;
    private LocalDateTime registeredAt;
    private LocalDateTime kycVerifiedAt;
}
