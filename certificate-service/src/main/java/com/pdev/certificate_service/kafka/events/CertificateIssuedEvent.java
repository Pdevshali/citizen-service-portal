package com.pdev.certificate_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbound event — published by certificate-service after a certificate is generated.
 * Topic: certificate.issued
 * Consumed by: citizen-service (optional), notification-service
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
