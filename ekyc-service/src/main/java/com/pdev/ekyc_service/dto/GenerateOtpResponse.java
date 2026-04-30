package com.pdev.ekyc_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for POST /api/kyc/generate-otp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpResponse {
    private String txnId;
}
