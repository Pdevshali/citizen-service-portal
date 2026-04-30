package com.pdev.ekyc_service.kafka.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event consumed from citizen-service when KYC initiation is requested.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiationEvent {
    @NotBlank(message = "Citizen ID is required")
    String citizenId;
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be a 12-digit number")
    private String aadhaarNumber;
}
