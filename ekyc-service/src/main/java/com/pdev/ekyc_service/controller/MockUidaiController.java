package com.pdev.ekyc_service.controller;

import com.pdev.ekyc_service.dto.UidaiOtpRequest;
import com.pdev.ekyc_service.dto.UidaiOtpResponse;
import com.pdev.ekyc_service.dto.UidaiVerifyRequest;
import com.pdev.ekyc_service.dto.UidaiVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Mock UIDAI API controller for testing purposes.
 * This simulates the real UIDAI OTP generation and verification endpoints.
 */
@RestController
@RequestMapping("/mock-uidai")
@Slf4j
public class MockUidaiController {

    // In-memory storage for mock OTP sessions
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, String> txnStore = new ConcurrentHashMap<>();

    @PostMapping("/otp/generate")
    public ResponseEntity<UidaiOtpResponse> generateOtp(@RequestBody UidaiOtpRequest request) {
        log.info("Mock UIDAI: Generating OTP for Aadhaar: {}", maskAadhaar(request.getAadhaarNumber()));
        
        // Generate mock transaction ID
        String txnId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String mockOtp = "123456"; // Fixed mock OTP for testing
        
        // Store the mock OTP
        otpStore.put(txnId, mockOtp);
        txnStore.put(txnId, request.getAadhaarNumber());
        
        UidaiOtpResponse response = new UidaiOtpResponse();
        response.setTxnId(txnId);
        response.setStatus("success");
        response.setMessage("OTP generated successfully");
        
        log.info("Mock UIDAI: OTP generated with txnId: {}", txnId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<UidaiVerifyResponse> verifyOtp(@RequestBody UidaiVerifyRequest request) {
        log.info("Mock UIDAI: Verifying OTP for txnId: {}", request.getTxnId());
        
        UidaiVerifyResponse response = new UidaiVerifyResponse();
        
        // Check if transaction exists
        String storedOtp = otpStore.get(request.getTxnId());
        if (storedOtp == null) {
            response.setStatus("failed");
            response.setMessage("Invalid transaction ID");
            return ResponseEntity.ok(response);
        }
        
        // Verify OTP (always "123456" in mock)
        if ("123456".equals(request.getOtp())) {
            response.setStatus("success");
            response.setMessage("OTP verified successfully");
            
            // Mock demographic data
            response.setName("John Doe");
            response.setDob("1990-01-01");
            response.setGender("M");
            response.setAddress("123 Main Street, Bangalore, Karnataka 560001");
            response.setPhone("9876543210");
            
            log.info("Mock UIDAI: OTP verified successfully for txnId: {}", request.getTxnId());
        } else {
            response.setStatus("failed");
            response.setMessage("Invalid OTP");
            log.warn("Mock UIDAI: OTP verification failed for txnId: {}", request.getTxnId());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Mock UIDAI API",
            "activeSessions", String.valueOf(otpStore.size())
        ));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        otpStore.clear();
        txnStore.clear();
        log.info("Mock UIDAI: All sessions cleared");
        return ResponseEntity.ok(Map.of("message", "Mock UIDAI reset successfully"));
    }

    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) return "****";
        return "****" + aadhaar.substring(aadhaar.length() - 4);
    }
}
