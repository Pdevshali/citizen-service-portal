package com.pdev.notification_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirror of certificate-service's CertificateIssuedEvent.
 *
 * Topic   : certificate.issued
 * Producer: certificate-service
 * Consumer: notification-service (this service)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateReadyEvent {

    /** Unique certificate identifier. */
    private String certificateId;

    /** Citizen who owns the certificate. */
    private String citizenId;

    /** E.g. BIRTH, DEATH, INCOME, CASTE. */
    private String certificateType;

    /** Human-readable certificate number for display. */
    private String certificateNumber;

    /** URL to download / view the certificate. */
    private String downloadUrl;

    /** GENERATED | FAILED */
    private String status;

    /** When the certificate was issued. */
    private LocalDateTime issuedAt;
}
