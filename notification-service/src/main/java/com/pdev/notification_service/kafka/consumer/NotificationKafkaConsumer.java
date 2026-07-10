package com.pdev.notification_service.kafka.consumer;

import com.pdev.notification_service.kafka.events.CertificateReadyEvent;
import com.pdev.notification_service.kafka.events.DocumentFetchCompletedEvent;
import com.pdev.notification_service.kafka.events.GrievanceSlaBreachedEvent;
import com.pdev.notification_service.kafka.events.KycCompletedEvent;
import com.pdev.notification_service.model.NotificationType;
import com.pdev.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Notification Service — Kafka Consumer
 *
 * Pattern: identical to citizen-service.
 *  - The default type in YAML is KycCompletedEvent.
 *  - Each @KafkaListener that consumes a different type overrides
 *    spring.json.value.default.type in its own properties block.
 *  - Listener methods receive the fully deserialized POJO — no manual parsing.
 *
 * ┌─────────────────────────────────┬───────────────────────────────────────┐
 * │ Topic                           │ Producer                              │
 * ├─────────────────────────────────┼───────────────────────────────────────┤
 * │ kyc.verification.completed      │ ekyc-service                          │
 * │ document.fetch.completed        │ document-service                      │
 * │ certificate.issued              │ certificate-service                   │
 * │ grievance.sla.breached          │ grievance-service                     │
 * └─────────────────────────────────┴───────────────────────────────────────┘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    public static final String KYC_VERIFICATION_COMPLETED = "kyc.verification.completed";
    private static final String NOTIFICATION_GROUP = "notification-service-group";

    private final NotificationService notificationService;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. KYC Completed — from ekyc-service
    //    Default type in YAML = KycCompletedEvent, so no override needed here.
    // ─────────────────────────────────────────────────────────────────────────

    @KafkaListener(
            topics  = KYC_VERIFICATION_COMPLETED,
            groupId = NOTIFICATION_GROUP
    )
    public void handleKycCompleted(KycCompletedEvent event) {
        try {
            log.info("[KAFKA] ← kyc.verification.completed | citizenId={} status={}",
                    event.getCitizenId(), event.getStatus());

            String message = buildKycMessage(event);
            notificationService.sendAll(event.getCitizenId(), NotificationType.KYC_VERIFIED, message);

            log.info("[KAFKA] ✓ Processed kyc.verification.completed for citizenId={}", event.getCitizenId());
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error processing kyc.verification.completed for citizenId={}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Document Fetch Completed — from document-service
    //    Override default type per-listener (same pattern as citizen-service).
    // ─────────────────────────────────────────────────────────────────────────

    @KafkaListener(
            topics  = "document.fetch.completed",
            groupId = NOTIFICATION_GROUP,
            properties = {
                "spring.json.value.default.type=com.pdev.notification_service.kafka.events.DocumentFetchCompletedEvent"
            }
    )
    public void handleDocumentFetchCompleted(DocumentFetchCompletedEvent event) {
        try {
            log.info("[KAFKA] ← document.fetch.completed | citizenId={} type={}",
                    event.getCitizenId(), event.getDocumentType());

            String message = buildDocumentMessage(event);
            notificationService.sendAll(event.getCitizenId(), NotificationType.DOC_READY, message);

            log.info("[KAFKA] ✓ Processed document.fetch.completed for citizenId={}", event.getCitizenId());
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error processing document.fetch.completed for citizenId={}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Certificate Issued — from certificate-service
    // ─────────────────────────────────────────────────────────────────────────

    @KafkaListener(
            topics  = "certificate.issued",
            groupId = NOTIFICATION_GROUP,
            properties = {
                "spring.json.value.default.type=com.pdev.notification_service.kafka.events.CertificateReadyEvent"
            }
    )
    public void handleCertificateIssued(CertificateReadyEvent event) {
        try {
            log.info("[KAFKA] ← certificate.issued | citizenId={} type={} status={}",
                    event.getCitizenId(), event.getCertificateType(), event.getStatus());

            String message = buildCertificateMessage(event);
            notificationService.sendAll(event.getCitizenId(), NotificationType.CERT_READY, message);

            log.info("[KAFKA] ✓ Processed certificate.issued for citizenId={}", event.getCitizenId());
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error processing certificate.issued for citizenId={}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Grievance SLA Breached — from grievance-service
    // ─────────────────────────────────────────────────────────────────────────

    @KafkaListener(
            topics  = "grievance.sla.breached",
            groupId = NOTIFICATION_GROUP,
            properties = {
                "spring.json.value.default.type=com.pdev.notification_service.kafka.events.GrievanceSlaBreachedEvent"
            }
    )
    public void handleGrievanceSlaBreached(GrievanceSlaBreachedEvent event) {
        try {
            log.info("[KAFKA] ← grievance.sla.breached | citizenId={} grievanceId={}",
                    event.getCitizenId(), event.getGrievanceId());

            String message = buildSlaBreachMessage(event);
            notificationService.sendAll(event.getCitizenId(), NotificationType.SLA_BREACH, message);

            log.info("[KAFKA] ✓ Processed grievance.sla.breached for citizenId={}", event.getCitizenId());
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error processing grievance.sla.breached for citizenId={}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper message builders
    // ─────────────────────────────────────────────────────────────────────────

    private String buildKycMessage(KycCompletedEvent event) {
        if ("VERIFIED".equalsIgnoreCase(event.getStatus())) {
            return String.format(
                    "Dear citizen, your KYC verification has been completed successfully on %s. " +
                    "You can now access all citizen services.",
                    event.getVerifiedAt() != null ? event.getVerifiedAt().toLocalDate() : "N/A");
        }
        return "Dear citizen, your KYC verification could not be completed. " +
               "Please re-initiate the process or contact support.";
    }

    private String buildDocumentMessage(DocumentFetchCompletedEvent event) {
        return String.format(
                "Dear citizen, your %s document is now ready. Download it here: %s",
                event.getDocumentType(),
                event.getDocumentUrl() != null ? event.getDocumentUrl() : "Portal → My Documents");
    }

    private String buildCertificateMessage(CertificateReadyEvent event) {
        if ("GENERATED".equalsIgnoreCase(event.getStatus())) {
            return String.format(
                    "Dear citizen, your %s certificate (No. %s) has been issued. Download it here: %s",
                    event.getCertificateType(),
                    event.getCertificateNumber(),
                    event.getDownloadUrl() != null ? event.getDownloadUrl() : "Portal → My Certificates");
        }
        return String.format(
                "Dear citizen, there was an issue generating your %s certificate. " +
                "Please raise a grievance or contact support.",
                event.getCertificateType());
    }

    private String buildSlaBreachMessage(GrievanceSlaBreachedEvent event) {
        return String.format(
                "Dear citizen, your grievance (ID: %s) regarding \"%s\" has exceeded its SLA deadline of %s. " +
                "We sincerely apologise for the delay. Our team is prioritising your case.",
                event.getGrievanceId(),
                event.getSubject(),
                event.getSlaDeadline() != null ? event.getSlaDeadline().toLocalDate() : "N/A");
    }
}
