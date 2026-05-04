package com.pdev.citizen_service.kafka.producer;

import com.pdev.citizen_service.kafka.events.KycInitiationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for citizen-service.
 *
 * FIX: Use raw KafkaTemplate + @Autowired instead of
 * KafkaTemplate<String, SpecificType> + @RequiredArgsConstructor.
 *
 * WHY:
 * Spring Boot auto-config creates ONE KafkaTemplate bean internally
 * with raw/wildcard generics. When you ask for
 * KafkaTemplate<String, KycInitiationEvent> via constructor injection,
 * Spring cannot match its raw bean to your specific generic type → ERROR.
 *
 * @Autowired on a raw KafkaTemplate field works because Spring matches
 * by bean type (KafkaTemplate) without checking generic parameters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenKafkaProducer {

    // KafkaTemplate<String, Object> matches the bean in KafkaConfig exactly
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String KYC_INITIATION_TOPIC      = "kyc.initiation.requested";
    
    public void publishKycInitiationEvent(KycInitiationEvent event) {
        log.info("[KAFKA] Publishing KYC initiation: citizenId={}", event.getCitizenId());
        kafkaTemplate.send(KYC_INITIATION_TOPIC, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null)
                        log.info("[KAFKA] ✓ KYC initiation sent: citizenId={}", event.getCitizenId());
                    else
                        log.error("[KAFKA] ✗ KYC initiation failed: {}", ex.getMessage(), ex);
                });
    }

}