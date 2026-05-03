package com.pdev.citizen_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core citizen entity persisted in PostgreSQL.
 *
 * Fields are intentionally minimal for this sprint —
 * document references and certificate links will be added
 * once document-service and certificate-service are wired in.
 */
@Entity
@Table(name = "citizens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder    
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private String id;

    // ── Personal details ────────────────────────────────────────────────────

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "aadhaar_number", unique = true)
    private String aadhaarNumber; // stored hashed in production; plain for dev

    @Column(name = "address")
    private String address;

    @Column(name = "state")
    private String state;

    @Column(name = "pincode", length = 6)
    private String pincode;

    // ── KYC tracking ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_initiated_at")
    private LocalDateTime kycInitiatedAt;

    @Column(name = "kyc_verified_at")
    private LocalDateTime kycVerifiedAt;

    @Column(name = "demographic_data_encrypted")
    private String demographicDataEncrypted;

    // ── Audit ────────────────────────────────────────────────────────────────

    @Column(name = "registered_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
