package com.pdev.ekyc_service.dto;

import com.pdev.ekyc_service.model.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for POST /api/kyc/verify-otp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {
    private String citizenId;
    private KycStatus status;
}
