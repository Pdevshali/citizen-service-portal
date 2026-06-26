package com.pdev.certificate_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Certificate entity — persisted in certificateDb.
 * One record per certificate issuance request.
 */
@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private String id;

    @Column(name = "citizen_id", nullable = false)
    private String citizenId;

    @Column(name = "certificate_type", nullable = false)
    private String certificateType; // BIRTH, INCOME, CASTE, MARRIAGE, etc.

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.PENDING;

    @Column(name = "certificate_number", unique = true)
    private String certificateNumber; // UUID-based, generated on issuance

    @Column(name = "download_url")
    private String downloadUrl; // e.g. /certs/{certificateNumber}.pdf

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
