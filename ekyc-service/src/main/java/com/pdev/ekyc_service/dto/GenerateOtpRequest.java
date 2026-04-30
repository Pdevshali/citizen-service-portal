package com.pdev.ekyc_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/kyc/generate-otp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpRequest {

    @NotBlank(message = "Citizen ID is required")
    private String citizenId;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be a 12-digit number")
    private String aadhaarNumber;
}
