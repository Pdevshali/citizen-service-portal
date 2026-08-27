package com.pdev.grievance_service.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GrievanceKafkaConsumer {

    @KafkaListener(topics = "kyc.completed", groupId = "grievance-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onKycCompleted(String message) {
        log.info("[KAFKA] Received event from kyc.completed: {}", message);
    }

    @KafkaListener(topics = "document.fetch.completed", groupId = "grievance-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onDocumentFetchCompleted(String message) {
        log.info("[KAFKA] Received event from document.fetch.completed: {}", message);
    }

    @KafkaListener(topics = "certificate.ready", groupId = "grievance-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onCertificateReady(String message) {
        log.info("[KAFKA] Received event from certificate.ready: {}", message);
    }
}
