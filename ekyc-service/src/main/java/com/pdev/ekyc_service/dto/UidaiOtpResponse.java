package com.pdev.ekyc_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from UIDAI mock API for OTP generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UidaiOtpResponse {
    private String txnId;
    private String status; // "success" or "failure"
    private String message;
}
