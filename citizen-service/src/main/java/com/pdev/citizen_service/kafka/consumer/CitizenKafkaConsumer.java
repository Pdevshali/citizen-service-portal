package com.pdev.citizen_service.kafka.consumer;

import com.pdev.citizen_service.kafka.events.KycCompletedEvent;
import com.pdev.citizen_service.model.Citizen;
import com.pdev.citizen_service.model.KycStatus;
import com.pdev.citizen_service.repository.CitizenRepository;
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

    private final CitizenRepository citizenRepository;

    @KafkaListener(topics = "kyc.verification.completed", groupId = "citizen-service-group")
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
}
