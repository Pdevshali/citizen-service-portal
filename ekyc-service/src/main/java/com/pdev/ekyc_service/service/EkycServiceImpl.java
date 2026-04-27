package com.pdev.ekyc_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EkycServiceImpl implements EkycService {

    private final KycSessionRepository kycSessionRepository;
    private final EkycKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public GenerateOtpResponse generateOtp(GenerateOtpRequest request) {
        try {
            // Generate unique txnId
            String txnId = generateTxnId();

            // Encrypt Aadhaar
            String encryptedAadhaar = EncryptionUtil.encrypt(request.getAadhaarNumber());

            // Generate OTP (mock)
            String otp = generateMockOtp();
            String otpHash = hashOtp(otp);

            // Create session
            KycSession session = KycSession.builder()
                    .citizenId(request.getCitizenId())
                    .txnId(txnId)
                    .aadhaarNumberEncrypted(encryptedAadhaar)
                    .otpHash(otpHash)
                    .status(KycStatus.PENDING)
                    .build();

            kycSessionRepository.save(session);

            log.info("Generated OTP for citizen: {}, txnId: {}", request.getCitizenId(), txnId);
            // In real implementation, send OTP via SMS/email, but for demo, log it
            log.info("Mock OTP for txnId {}: {}", txnId, otp);

            return new GenerateOtpResponse(txnId);

        } catch (Exception e) {
            log.error("Error generating OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate OTP");
        }
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        KycSession session = kycSessionRepository.findByTxnId(request.getTxnId())
                .orElseThrow(() -> new KycSessionNotFoundException("Invalid transaction ID"));

        if (session.getStatus() != KycStatus.PENDING) {
            throw new OtpVerificationFailedException("OTP already verified or expired");
        }

        // Verify OTP
        String providedOtpHash = hashOtp(request.getOtp());
        if (!providedOtpHash.equals(session.getOtpHash())) {
            // Update status to FAILED
            session.setStatus(KycStatus.FAILED);
            kycSessionRepository.save(session);
            throw new OtpVerificationFailedException("Invalid OTP");
        }

        // OTP verified, extract demographic data (mock)
        String demographicData = generateMockDemographicData(session.getCitizenId());
        try {
            String encryptedDemographicData = EncryptionUtil.encrypt(demographicData);

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
            kafkaProducer.publishKycCompletedEvent(event);

            log.info("OTP verified for citizen: {}", session.getCitizenId());

            return new VerifyOtpResponse(session.getCitizenId(), KycStatus.VERIFIED);

        } catch (Exception e) {
            log.error("Error encrypting demographic data: {}", e.getMessage(), e);
            throw new RuntimeException("Verification failed due to encryption error");
        }
    }

    @Override
    public KycStatusResponse getKycStatus(String citizenId) {
        KycSession session = kycSessionRepository.findByCitizenId(citizenId)
                .orElseThrow(() -> new KycSessionNotFoundException("No KYC session found for citizen: " + citizenId));

        String demographicData = null;
        if (session.getDemographicDataEncrypted() != null) {
            try {
                demographicData = EncryptionUtil.decrypt(session.getDemographicDataEncrypted());
            } catch (Exception e) {
                log.error("Error decrypting demographic data: {}", e.getMessage(), e);
            }
        }

        return new KycStatusResponse(
                session.getCitizenId(),
                session.getStatus(),
                session.getVerifiedAt(),
                demographicData
        );
    }

    @Override
    public void handleKycInitiation(KycInitiationEvent event) {
        // This is called when citizen-service publishes initiation event
        // For now, just log it. In real implementation, could auto-generate OTP
        log.info("Handling KYC initiation for citizen: {}", event.getCitizenId());
    }

    private String generateTxnId() {
        return "TXN" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    private String generateMockOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateMockDemographicData(String citizenId) {
        // Mock demographic data based on citizenId
        Map<String, Object> data = Map.of(
                "name", "Mock Citizen " + citizenId.substring(0, 8),
                "dob", "1990-01-01",
                "gender", "M",
                "address", "Mock Address, India",
                "phone", "9876543210"
        );
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}
