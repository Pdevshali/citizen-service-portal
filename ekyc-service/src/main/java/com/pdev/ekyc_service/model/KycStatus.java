package com.pdev.ekyc_service.model;

/**
 * Enum representing the status of KYC verification process.
 */
public enum KycStatus {
    PENDING,    // OTP generated, waiting for verification
    VERIFIED,   // OTP verified successfully
    FAILED      // OTP verification failed
}
