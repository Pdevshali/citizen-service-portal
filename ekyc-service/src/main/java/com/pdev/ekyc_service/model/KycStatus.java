package com.pdev.ekyc_service.model;

/**
 * Enum representing the status of KYC verification process.
 * Must match citizen-service KycStatus for event compatibility.
 */
public enum KycStatus {
    PENDING,    // Default state, OTP not yet generated
    INITIATED,  // OTP generated, waiting for verification (not used in ekyc-service)
    VERIFIED,   // OTP verified successfully
    FAILED      // OTP verification failed
}
