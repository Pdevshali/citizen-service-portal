package com.pdev.citizen_service.kafka.producer;

import com.pdev.citizen_service.kafka.events.KycInitiationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for citizen-service events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenKafkaProducer {

    private final KafkaTemplate<String, KycInitiationEvent> kafkaTemplate;
    private static final String KYC_INITIATION_TOPIC = "kyc.initiation.requested";

    public void publishKycInitiationEvent(KycInitiationEvent event) {
        try {
            log.info("Publishing KYC initiation event: citizenId={}, requestId={}", 
                    event.getCitizenId(), event.getRequestId());
            
            kafkaTemplate.send(KYC_INITIATION_TOPIC, event.getCitizenId(), event)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            log.info("Successfully published KYC initiation event for citizen: {}", event.getCitizenId());
                        } else {
                            log.error("Failed to publish KYC initiation event for citizen {}: {}",
                                    event.getCitizenId(), exception.getMessage(), exception);
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing KYC initiation event for citizen {}: {}", 
                    event.getCitizenId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish KYC initiation event", e);
        }
    }
}