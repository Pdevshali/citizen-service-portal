package com.pdev.certificate_service.kafka.consumer;

import com.pdev.certificate_service.feign.CitizenServiceClient;
import com.pdev.certificate_service.kafka.events.CertificateIssuanceRequestedEvent;
import com.pdev.certificate_service.kafka.events.CertificateIssuedEvent;
import com.pdev.certificate_service.kafka.producer.CertificateKafkaProducer;
import com.pdev.certificate_service.model.Certificate;
import com.pdev.certificate_service.model.CertificateStatus;
import com.pdev.certificate_service.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka consumer for certificate-service.
 *
 * Listens to certificate.issuance.requested events from citizen-service,
 * validates the citizen via Feign, generates a mock certificate, persists it,
 * then publishes certificate.issued.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateKafkaConsumer {

    public static final String CERTIFICATE_ISSUANCE_REQUESTED = "certificate.issuance.requested";
    public static final String CERTIFICATE_SERVICE_GROUP = "certificate-service-group";

    private final CertificateRepository certificateRepository;
    private final CertificateKafkaProducer kafkaProducer;
    private final CitizenServiceClient citizenServiceClient;

    @KafkaListener(topics = CERTIFICATE_ISSUANCE_REQUESTED, groupId = CERTIFICATE_SERVICE_GROUP)
    public void handleCertificateIssuanceRequested(CertificateIssuanceRequestedEvent event) {
        log.info("[CERTIFICATE] Received issuance request for citizenId={}, type={}",
                event.getCitizenId(), event.getCertificateType());

        // Step 1: Create initial PENDING record
        Certificate certificate = Certificate.builder()
                .citizenId(event.getCitizenId())
                .certificateType(event.getCertificateType())
                .purpose(event.getPurpose())
                .remarks(event.getRemarks())
                .status(CertificateStatus.PENDING)
                .build();
        certificate = certificateRepository.save(certificate);

        try {
            // Step 2: Validate citizen exists via Feign call to citizen-service
            Boolean citizenExists = citizenServiceClient.validateCitizen(event.getCitizenId());
            if (citizenExists == null || !citizenExists) {
                log.warn("[CERTIFICATE] Citizen not found: citizenId={}", event.getCitizenId());
                markFailed(certificate, "Citizen not found");
                return;
            }

            // Step 3: Generate certificate number and mock download URL
            String certNumber = "CERT-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
            String downloadUrl = "/api/certificates/download/" + certNumber + ".pdf";
            LocalDateTime issuedAt = LocalDateTime.now();

            // Step 4: Update record to GENERATED
            certificate.setStatus(CertificateStatus.GENERATED);
            certificate.setCertificateNumber(certNumber);
            certificate.setDownloadUrl(downloadUrl);
            certificate.setIssuedAt(issuedAt);
            certificateRepository.save(certificate);
            log.info("[CERTIFICATE] ✓ Certificate generated: certNumber={}, citizenId={}", certNumber, event.getCitizenId());

            // Step 5: Publish certificate.issued event
            CertificateIssuedEvent issuedEvent = new CertificateIssuedEvent(
                    certificate.getId(),
                    event.getCitizenId(),
                    event.getCertificateType(),
                    certNumber,
                    downloadUrl,
                    CertificateStatus.GENERATED.name(),
                    issuedAt
            );
            kafkaProducer.publishCertificateIssuedEvent(issuedEvent);

        } catch (Exception e) {
            log.error("[CERTIFICATE] ✗ Failed to process certificate for citizenId={}: {}",
                    event.getCitizenId(), e.getMessage(), e);
            markFailed(certificate, e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void markFailed(Certificate certificate, String reason) {
        certificate.setStatus(CertificateStatus.FAILED);
        certificate.setRemarks("Processing failed: " + reason);
        certificateRepository.save(certificate);
        log.warn("[CERTIFICATE] Certificate marked FAILED for citizenId={}: {}", certificate.getCitizenId(), reason);
    }
}
