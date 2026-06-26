package com.pdev.citizen_service.kafka.consumer;

import com.pdev.citizen_service.kafka.events.DocumentFetchCompletedEvent;
import com.pdev.citizen_service.kafka.events.KycCompletedEvent;
import com.pdev.citizen_service.kafka.events.CertificateIssuedEvent;
import com.pdev.citizen_service.model.Citizen;
import com.pdev.citizen_service.model.Document;
import com.pdev.citizen_service.model.DocumentType;
import com.pdev.citizen_service.model.KycStatus;
import com.pdev.citizen_service.repository.CitizenRepository;
import com.pdev.citizen_service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for citizen-service events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenKafkaConsumer {

    public static final String KYC_VERIFICATION_COMPLETED = "kyc.verification.completed";
    public static final String CITIZEN_SERVICE_GROUP = "citizen-service-group";
    public static final String DOCUMENT_FETCH_COMPLETED = "document.fetch.completed";
    public static final String CERTIFICATE_ISSUED = "certificate.issued";
    private final CitizenRepository citizenRepository;
    private final DocumentRepository documentRepository;

    @KafkaListener(topics = KYC_VERIFICATION_COMPLETED, groupId = CITIZEN_SERVICE_GROUP)
    public void handleKycCompleted(KycCompletedEvent event) {
        try {
            log.info("Received KYC completed event for citizen: {}, status: {}",
                    event.getCitizenId(), event.getStatus());

            Citizen citizen = citizenRepository.findById(event.getCitizenId())
                    .orElseThrow(() -> new RuntimeException("Citizen not found: " + event.getCitizenId()));

            // Update citizen's KYC information based on event status
            citizen.setKycStatus(event.getStatus());

            if (event.getStatus() == KycStatus.VERIFIED) {
                // On successful verification, set verification timestamp and demographic data
                citizen.setKycVerifiedAt(event.getVerifiedAt());
                citizen.setDemographicDataEncrypted(event.getDemographicData());
                log.info("Successfully updated KYC status for citizen: {} to VERIFIED", event.getCitizenId());
            } else if (event.getStatus() == KycStatus.FAILED) {
                citizen.setKycVerifiedAt(null);
                citizen.setDemographicDataEncrypted(null);
                log.info("Updated KYC status for citizen: {} to FAILED", event.getCitizenId());
            } else {
                // For other statuses (like PENDING), just update the status
                log.info("Updated KYC status for citizen: {} to {}", event.getCitizenId(), event.getStatus());
            }

            citizenRepository.save(citizen);

        } catch (Exception e) {
            log.error("Error handling KYC completed event for citizen {}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = DOCUMENT_FETCH_COMPLETED, groupId = CITIZEN_SERVICE_GROUP,
            properties = {
                    "spring.json.value.default.type=com.pdev.citizen_service.kafka.events.DocumentFetchCompletedEvent"
            })
    public void handleDocumentFetchCompleted(DocumentFetchCompletedEvent event) {
        try{
            log.info("Received Document Fetch Completed event for citizen: {}", event.getCitizenId());

            Citizen citizen = citizenRepository.findById(event.getCitizenId())
                    .orElseThrow(() -> new RuntimeException("Citizen not found for document update: " + event.getCitizenId()));
            log.info("docType {}", event.getDocumentType());

            Document document = Document.builder()
                    .citizenId(event.getCitizenId())
                    .documentType(DocumentType.valueOf(event.getDocumentType()))
                    .documentUrl(event.getDocumentUrl())
                    .fetchedAt(event.getCompletedAt())
                    .build();

            documentRepository.save(document);
            log.info("Document record saved for citizen: {}, documentType: {}", event.getCitizenId(), event.getDocumentType());


        } catch (Exception e) {
            log.error("Error handling Document Fetch Completed event for citizen {}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = CERTIFICATE_ISSUED, groupId = CITIZEN_SERVICE_GROUP,
            properties = {
                    "spring.json.value.default.type=com.pdev.citizen_service.kafka.events.CertificateIssuedEvent"
            })
    public void handleCertificateIssued(CertificateIssuedEvent event) {
        try {
            log.info("Received Certificate Issued event for citizen: {}, certNumber: {}, status: {}",
                    event.getCitizenId(), event.getCertificateNumber(), event.getStatus());

            // Persist certificate as a Document record (type CERTIFICATE) so UI can show download link
            Document document = Document.builder()
                    .citizenId(event.getCitizenId())
                    .documentType(DocumentType.CERTIFICATE)
                    .documentUrl(event.getDownloadUrl())
                    .fetchedAt(event.getIssuedAt() == null ? java.time.LocalDateTime.now() : event.getIssuedAt())
                    .build();

            documentRepository.save(document);
            log.info("Certificate record saved for citizen: {}, certificateNumber: {}", event.getCitizenId(), event.getCertificateNumber());

        } catch (Exception e) {
            log.error("Error handling Certificate Issued event for citizen {}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }
}
