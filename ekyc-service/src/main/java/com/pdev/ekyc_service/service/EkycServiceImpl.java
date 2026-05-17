package com.pdev.ekyc_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdev.ekyc_service.client.CitizenServiceClient;
import com.pdev.ekyc_service.dto.*;
import com.pdev.ekyc_service.exception.KycSessionNotFoundException;
import com.pdev.ekyc_service.exception.OtpVerificationFailedException;
import com.pdev.ekyc_service.kafka.events.KycCompletedEvent;
import com.pdev.ekyc_service.kafka.events.KycInitiationEvent;
import com.pdev.ekyc_service.kafka.producer.EkycKafkaProducer;
import com.pdev.ekyc_service.model.KycSession;
import com.pdev.ekyc_service.model.KycStatus;
import com.pdev.ekyc_service.repository.KycSessionRepository;
import com.pdev.ekyc_service.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class EkycServiceImpl implements EkycService {

    private final KycSessionRepository kycSessionRepository;
    private final EkycKafkaProducer kafkaProducer;
    private final UidaiApiClient uidaiApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CitizenServiceClient citizenServiceClient;

    @Override
    public GenerateOtpResponse generateOtp(GenerateOtpRequest request) {

        log.info("Generating OTP for citizen: {}", request.getCitizenId());
        try {
            // Call UIDAI mock API to generate OTP
            UidaiOtpResponse uidaiResponse = uidaiApiClient.generateOtp(request.getMobile(), request.getAadhaarNumber());

            Boolean citizenExists = citizenServiceClient.validateCitizen(request.getCitizenId());
            if (!citizenExists) {
                throw new RuntimeException("Citizen not found");
            }
            log.info("Citizen found: {}", request.getCitizenId());

            if (!"success".equals(uidaiResponse.getStatus())) {
                throw new RuntimeException("UIDAI OTP generation failed: " + uidaiResponse.getMessage());
            }

            // Encrypt Aadhaar
            String encryptedAadhaar = EncryptionUtil.encrypt(request.getAadhaarNumber());

            // Store session with UIDAI txnId
            KycSession session = KycSession.builder()
                    .citizenId(request.getCitizenId())
                    .txnId(uidaiResponse.getTxnId())
                    .aadhaarNumberEncrypted(encryptedAadhaar)
                    .status(KycStatus.PENDING)
                    .build();

            kycSessionRepository.save(session);

            log.info("OTP generated via UIDAI for citizen: {}, txnId: {}", request.getCitizenId(), uidaiResponse.getTxnId());

            return new GenerateOtpResponse(uidaiResponse.getTxnId());

        } catch (Exception e) {
            log.error("Error generating OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate OTP");
        }
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for citizen: {}", request.getTxnId());
        KycSession session = kycSessionRepository.findByTxnId(request.getTxnId())
                .orElseThrow(() -> new KycSessionNotFoundException("Invalid transaction ID"));

        if (session.getStatus() != KycStatus.PENDING) {
            throw new OtpVerificationFailedException("OTP already verified or expired");
        }

        try {
            // Call UIDAI mock API to verify OTP
            UidaiVerifyResponse uidaiResponse = uidaiApiClient.verifyOtp(request.getTxnId(), request.getOtp());

            if (!"success".equals(uidaiResponse.getStatus())) {
                // Update status to FAILED
                session.setStatus(KycStatus.FAILED);
                kycSessionRepository.save(session);
                throw new OtpVerificationFailedException("OTP verification failed: " + uidaiResponse.getMessage());
            }

            // Extract demographic data from UIDAI response
            Map<String, Object> demographicData = Map.of(
                    "name", uidaiResponse.getName(),
                    "dob", uidaiResponse.getDob(),
                    "gender", uidaiResponse.getGender(),
                    "address", uidaiResponse.getAddress(),
                    "phone", uidaiResponse.getPhone()
            );

            String demographicJson = objectMapper.writeValueAsString(demographicData);
            String encryptedDemographicData = EncryptionUtil.encrypt(demographicJson);

            // Update session
            session.setStatus(KycStatus.VERIFIED);
            session.setVerifiedAt(LocalDateTime.now());
            session.setDemographicDataEncrypted(encryptedDemographicData);
            kycSessionRepository.save(session);

            // Publish event
            KycCompletedEvent event = new KycCompletedEvent(
                    session.getCitizenId(),
                    KycStatus.VERIFIED,
                    session.getVerifiedAt(),
                    encryptedDemographicData
            );

            log.info("Publishing KYC completed event: {}", event);
            kafkaProducer.publishKycCompleted(event);

            log.info("OTP verified via UIDAI for citizen: {}", session.getCitizenId());

            return new VerifyOtpResponse(session.getCitizenId(), KycStatus.VERIFIED);

        } catch (OtpVerificationFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Verification failed due to error");
        }
    }

    @Override
    public KycStatusResponse getKycStatus(String citizenId) {
        log.info("Getting KYC status for citizen: {}", citizenId);

        try {
            // First try to find the most recent VERIFIED session
            Optional<KycSession> verifiedSession = kycSessionRepository.findTopByCitizenIdAndStatusOrderByCreatedAtDesc(citizenId, KycStatus.VERIFIED);
            
            KycSession session;
            if (verifiedSession.isPresent()) {
                session = verifiedSession.get();
                log.info("Found VERIFIED KYC session: id={}, txnId={}", session.getId(), session.getTxnId());
            } else {
                // If no verified session, get the most recent session (could be PENDING or FAILED)
                session = kycSessionRepository.findTopByCitizenIdOrderByCreatedAtDesc(citizenId)
                        .orElseThrow(() -> new KycSessionNotFoundException("No KYC session found for citizen: " + citizenId));
                log.info("Found most recent KYC session: id={}, status={}, txnId={}", session.getId(), session.getStatus(), session.getTxnId());
            }

            String demographicData = null;
            if (session.getDemographicDataEncrypted() != null && !session.getDemographicDataEncrypted().isEmpty()) {
                try {
                    demographicData = EncryptionUtil.decrypt(session.getDemographicDataEncrypted());
                    log.info("Successfully decrypted demographic data for citizen: {}", citizenId);
                } catch (Exception e) {
                    log.error("Error decrypting demographic data for citizen {}: {}", citizenId, e.getMessage(), e);
                    // Don't fail the entire request, just return null demographic data
                    demographicData = null;
                }
            }

            return new KycStatusResponse(
                    session.getCitizenId(),
                    session.getStatus(),
                    session.getVerifiedAt(),
                    demographicData
            );
            
        } catch (KycSessionNotFoundException e) {
            log.warn("KYC session not found for citizen: {}", citizenId);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting KYC status for citizen {}: {}", citizenId, e.getMessage(), e);
            throw new RuntimeException("Failed to get KYC status", e);
        }
    }

    @Override
    public void handleKycInitiation(KycInitiationEvent event) {
        // This is called when citizen-service publishes initiation event
        // For now, just log it. In real implementation, could auto-generate OTP
        log.info("Handling KYC initiation for citizen: {}", event.getCitizenId());
    }
}
