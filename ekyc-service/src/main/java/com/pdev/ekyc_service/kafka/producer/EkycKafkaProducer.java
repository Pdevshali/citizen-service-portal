package com.pdev.ekyc_service.kafka.producer;

import com.pdev.ekyc_service.kafka.events.KycInitiateRequest;
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

    private final KafkaTemplate<String, KycInitiateRequest> kafkaTemplate;

    private static final String KYC_COMPLETED_TOPIC = "kyc.verification.completed";

    public void publishKycCompletedEvent(KycInitiateRequest event) {
        log.info("Publishing KYC completed event for citizen: {}", event.getCitizenId());
        kafkaTemplate.send(KYC_COMPLETED_TOPIC, event.getCitizenId(), event);
    }
}
