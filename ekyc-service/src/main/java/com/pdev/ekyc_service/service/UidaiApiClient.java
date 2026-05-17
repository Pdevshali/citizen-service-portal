package com.pdev.ekyc_service.service;

import com.pdev.ekyc_service.dto.UidaiOtpResponse;
import com.pdev.ekyc_service.dto.UidaiVerifyResponse;
import com.pdev.ekyc_service.model.KycSession;
import com.pdev.ekyc_service.repository.KycSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Mock UIDAI API client that simulates UIDAI responses using database persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UidaiApiClient {

    private final KycSessionRepository kycSessionRepository;
    private static final String MOCK_OTP = "123456"; // Fixed mock OTP for testing

    public UidaiOtpResponse generateOtp(Long mobile, String aadhaar) {
        log.info("Mock UIDAI: Generating OTP for Aadhaar: {} and mobile: {}", maskAadhaar(aadhaar), mobile);
        
        // Generate mock transaction ID
        String txnId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        UidaiOtpResponse response = new UidaiOtpResponse();
        response.setTxnId(txnId);
        response.setStatus("success");
        response.setMessage("OTP generated successfully");
        log.info("OPTP is sent to the mobile number: {}", mobile);
        log.info("Mock UIDAI: OTP generated with txnId: {}", txnId);
        return response;
    }

    public UidaiVerifyResponse verifyOtp(String txnId, String otp) {
        log.info("Mock UIDAI: Verifying OTP for txnId: {}", txnId);
        
        UidaiVerifyResponse response = new UidaiVerifyResponse();
        
        // Check if transaction exists in database
        KycSession session = kycSessionRepository.findByTxnId(txnId).orElse(null);
        if (session == null) {
            response.setStatus("failed");
            response.setMessage("Invalid transaction ID");
            return response;
        }
        
        // Verify OTP (always "123456" in mock)
        if (MOCK_OTP.equals(otp)) {
            response.setStatus("success");
            response.setMessage("OTP verified successfully");
            
            // Mock demographic data
            response.setName("John Doe");
            response.setDob("1990-01-01");
            response.setGender("M");
            response.setAddress("123 Main Street, Bangalore, Karnataka 560001");
            response.setPhone("9876543210");
            
            log.info("Mock UIDAI: OTP verified successfully for txnId: {}", txnId);
        } else {
            response.setStatus("failed");
            response.setMessage("Invalid OTP");
            log.warn("Mock UIDAI: OTP verification failed for txnId: {}", txnId);
        }
        
        return response;
    }

    // Utility method to mask Aadhaar for logging
    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) return "****";
        return "****" + aadhaar.substring(aadhaar.length() - 4);
    }
}
