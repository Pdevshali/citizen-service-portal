package com.pdev.citizen_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Payload for POST /api/citizens/me/onboarding
 *
 * <p>This DTO intentionally does NOT contain {@code email} or {@code keycloakUserId}.
 * Those values are derived exclusively from the validated JWT claims forwarded by the
 * API Gateway ({@code X-User-Id} and {@code X-User-Email} headers).
 * This prevents a malicious client from impersonating another user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be a 12-digit number")
    private String aadhaarNumber;

    private String address;

    private String state;

    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be 6 digits")
    private String pincode;
}
