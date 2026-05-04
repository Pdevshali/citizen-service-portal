package com.pdev.ekyc_service.kafka.consumer;

import com.pdev.ekyc_service.dto.GenerateOtpRequest;
import com.pdev.ekyc_service.kafka.events.KycInitiationEvent;
import com.pdev.ekyc_service.service.EkycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * ekyc-service Kafka Consumer
 *
 * Consumes: kyc.initiation.requested ← published by citizen-service
 *
 * Flow:
 *   citizen-service publishes KycInitiationEvent
 *     → this consumer receives it
 *     → calls EkycService.generateOtp()
 *     → UidaiMockClient generates OTP
 *     → OTP stored in Redis with 10-min TTL
 *     → Citizen submits OTP via POST /api/kyc/verify-otp
 *     → EkycKafkaProducer publishes kyc.verification.completed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EkycKafkaConsumer {

    private final EkycService ekycService;

    @KafkaListener(
            topics  = "kyc.initiation.requested",
            groupId = "ekyc-service-group"
    )
    public void handleKycInitiationEvent(KycInitiationEvent event) {
        log.info("[KAFKA] Received KYC initiation: citizenId={}", event.getCitizenId());
        try {
            // Build GenerateOtpRequest from the Kafka event fields
            // and reuse the same EkycService.generateOtp() method
            // that the REST endpoint uses — no code duplication
            GenerateOtpRequest request = GenerateOtpRequest.builder()
                    .citizenId(event.getCitizenId())
                    .aadhaarNumber(event.getAadhaarNumber())
                    .build();

            ekycService.generateOtp(request);
            log.info("[KAFKA] ✓ OTP generated for citizenId={}", event.getCitizenId());

        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error processing KYC initiation for citizenId={}: {}",
                    event.getCitizenId(), e.getMessage(), e);
            // Not rethrowing — prevents infinite retry loop on same broken message
            // Production improvement: publish to Dead Letter Topic (DLT) here
        }
    }
}