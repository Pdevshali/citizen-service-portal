package com.pdev.citizen_service.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO for initiating KYC verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiateRequest {
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be a 12-digit number")
    private String aadhaarNumber;
}
