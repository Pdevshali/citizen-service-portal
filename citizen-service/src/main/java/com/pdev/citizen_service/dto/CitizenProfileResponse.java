package com.pdev.citizen_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pdev.citizen_service.model.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response payload for GET /api/citizens/{id}/profile
 * Never exposes raw Aadhaar or internal DB fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenProfileResponse {

    @JsonIgnore
    private String keycloakUserId;

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String state;
    private String pincode;
    private KycStatus kycStatus;
    private LocalDateTime registeredAt;
    private LocalDateTime kycVerifiedAt;
}
