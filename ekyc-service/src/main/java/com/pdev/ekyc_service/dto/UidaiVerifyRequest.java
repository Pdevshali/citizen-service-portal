package com.pdev.ekyc_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to UIDAI mock API for OTP verification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UidaiVerifyRequest {
    private String txnId;
    private String otp;
}
