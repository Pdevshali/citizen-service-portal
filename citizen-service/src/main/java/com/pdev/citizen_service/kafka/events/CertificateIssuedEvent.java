package com.pdev.citizen_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inbound event — produced by certificate-service when a certificate is generated.
 * Topic: certificate.issued
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuedEvent {

    private String certificateId;
    private String citizenId;
    private String certificateType;
    private String certificateNumber;
    private String downloadUrl;
    private String status; // GENERATED or FAILED
    private LocalDateTime issuedAt;
}
