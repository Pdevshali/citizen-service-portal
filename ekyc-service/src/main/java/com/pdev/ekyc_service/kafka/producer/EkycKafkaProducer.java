package com.pdev.ekyc_service.kafka.producer;

import com.pdev.ekyc_service.kafka.events.KycCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for publishing KYC-related events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EkycKafkaProducer {

    private final KafkaTemplate<String, KycCompletedEvent> kafkaTemplate;

    private static final String KYC_COMPLETED_TOPIC = "kyc.verification.completed";

    public void publishKycCompletedEvent(KycCompletedEvent event) {
        try {
            log.info("Publishing KYC completed event for citizen: {}", event.getCitizenId());
            kafkaTemplate.send(KYC_COMPLETED_TOPIC, event.getCitizenId(), event)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            log.info("Successfully published KYC completed event for citizen: {}", event.getCitizenId());
                        } else {
                            log.error("Failed to publish KYC completed event for citizen {}: {}",
                                    event.getCitizenId(), exception.getMessage(), exception);
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing KYC completed event for citizen {}: {}",
                    event.getCitizenId(), e.getMessage(), e);
        }
    }
}
