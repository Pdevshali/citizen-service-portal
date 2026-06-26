package com.pdev.certificate_service.kafka.producer;

import com.pdev.certificate_service.kafka.events.CertificateIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for certificate-service.
 *
 * Uses KafkaTemplate<String, Object> to match the auto-configured bean
 * (same pattern as CitizenKafkaProducer to avoid generic-type injection errors).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateKafkaProducer {

    private static final String CERTIFICATE_ISSUED_TOPIC = "certificate.issued";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a certificate.issued event after successful generation.
     *
     * @param event the outbound event containing certificate metadata
     */
    public void publishCertificateIssuedEvent(CertificateIssuedEvent event) {
        log.info("[KAFKA] Publishing certificate.issued: citizenId={}, certNumber={}",
                event.getCitizenId(), event.getCertificateNumber());

        kafkaTemplate.send(CERTIFICATE_ISSUED_TOPIC, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA] ✓ certificate.issued sent: citizenId={}, certNumber={}",
                                event.getCitizenId(), event.getCertificateNumber());
                    } else {
                        log.error("[KAFKA] ✗ certificate.issued failed for citizenId={}: {}",
                                event.getCitizenId(), ex.getMessage(), ex);
                    }
                });
    }
}
