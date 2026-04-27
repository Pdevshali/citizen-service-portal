package com.pdev.citizen_service.model;

/**
 * Represents the KYC verification lifecycle of a citizen.
 *
 * PENDING        → Default on registration; KYC not yet started.
 * INITIATED      → citizen called /initiate-kyc; event published to ekyc-service.
 * VERIFIED       → ekyc-service confirmed Aadhaar OTP; profile is fully verified.
 * FAILED         → Verification attempt failed (wrong OTP / UIDAI rejection).
 */
public enum KycStatus {
    PENDING,
    INITIATED,
    VERIFIED,
    FAILED
}
