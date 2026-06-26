package com.pdev.citizen_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published by citizen-service when a citizen requests a certificate.
 * Consumed by certificate-service on topic: certificate.issuance.requested
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuanceRequestedEvent {

    private String citizenId;
    private String certificateType; // BIRTH, INCOME, CASTE, MARRIAGE, etc.
    private String purpose;
    private String remarks;
    private LocalDateTime requestedAt;

    public CertificateIssuanceRequestedEvent(String citizenId, String certificateType,
                                              String purpose, String remarks) {
        this.citizenId = citizenId;
        this.certificateType = certificateType;
        this.purpose = purpose;
        this.remarks = remarks;
        this.requestedAt = LocalDateTime.now();
    }
}
