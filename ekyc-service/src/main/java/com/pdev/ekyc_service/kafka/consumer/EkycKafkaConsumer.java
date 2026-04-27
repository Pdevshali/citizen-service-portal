package com.pdev.ekyc_service.kafka.consumer;

import com.pdev.ekyc_service.kafka.events.KycInitiationEvent;
import com.pdev.ekyc_service.service.EkycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for handling KYC-related events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EkycKafkaConsumer {

    private final EkycService ekycService;

    @KafkaListener(topics = "kyc.initiation.requested", groupId = "ekyc-service-group")
    public void handleKycInitiationEvent(KycInitiationEvent event) {
        log.info("Received KYC initiation event for citizen: {}", event.getCitizenId());
        try {
            ekycService.handleKycInitiation(event);
        } catch (Exception e) {
            log.error("Error processing KYC initiation event: {}", e.getMessage(), e);
        }
    }
}
