package com.pdev.ekyc_service.kafka.producer;

import com.pdev.ekyc_service.kafka.events.KycCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EkycKafkaProducer {

    // KafkaTemplate<String, Object> matches the bean in KafkaConfig exactly
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String KYC_COMPLETED_TOPIC = "kyc.verification.completed";

    public void publishKycCompleted(KycCompletedEvent event) {
        log.info("[KAFKA] Publishing KYC completed: citizenId={}, status={}",
                event.getCitizenId(), event.getStatus());

        kafkaTemplate.send(KYC_COMPLETED_TOPIC, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null)
                        log.info("[KAFKA] ✓ KYC completed sent: citizenId={}", event.getCitizenId());
                    else
                        log.error("[KAFKA] ✗ KYC completed failed: {}", ex.getMessage(), ex);
                });
    }
}