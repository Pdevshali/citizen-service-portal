package com.pdev.ekyc_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a KYC session for OTP-based verification.
 * Stores encrypted PII data and session state.
 */
@Entity
@Table(name = "kyc_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "citizen_id", nullable = false)
    private String citizenId;

    @Column(name = "txn_id", unique = true, nullable = false)
    private String txnId;

    @Column(name = "aadhaar_number_encrypted", nullable = false)
    private String aadhaarNumberEncrypted; // AES-256 encrypted

    @Column(name = "otp_hash")
    private String otpHash; // SHA-256 hash of OTP

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "demographic_data_encrypted")
    private String demographicDataEncrypted; // AES-256 encrypted JSON

    @PreUpdate
    void onUpdate() {
        // No update timestamp needed
    }
}
