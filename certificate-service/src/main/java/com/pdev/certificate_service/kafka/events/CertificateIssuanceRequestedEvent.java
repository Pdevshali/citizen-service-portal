package com.pdev.certificate_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inbound event — published by citizen-service when a citizen requests a certificate.
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
}
