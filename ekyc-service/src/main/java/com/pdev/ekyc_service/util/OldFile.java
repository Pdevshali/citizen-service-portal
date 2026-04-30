package com.pdev.ekyc_service.util;

import com.pdev.ekyc_service.dto.UidaiOtpRequest;
import com.pdev.ekyc_service.dto.UidaiOtpResponse;
import com.pdev.ekyc_service.dto.UidaiVerifyRequest;
import com.pdev.ekyc_service.dto.UidaiVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for interacting with UIDAI mock API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OldFile {

    private final WebClient webClient;

    @Value("${uidai.api.base-url:https://mock.uidai.gov.in}")
    private String baseUrl;

    public UidaiOtpResponse generateOtp(String aadhaar) {
        UidaiOtpRequest request = new UidaiOtpRequest();
        request.setAadhaarNumber(aadhaar);

        log.info("Calling UIDAI mock API to generate OTP for Aadhaar: {}", maskAadhaar(aadhaar));

        return webClient.post()
                .uri(baseUrl + "/otp/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UidaiOtpResponse.class)
                .doOnNext(response -> log.info("UIDAI OTP generation response: {}", response.getStatus()))
                .doOnError(error -> log.error("Error calling UIDAI OTP generate API: {}", error.getMessage()))
                .block(); // Synchronous for simplicity
    }

    public UidaiVerifyResponse verifyOtp(String txnId, String otp) {
        UidaiVerifyRequest request = new UidaiVerifyRequest();
        request.setTxnId(txnId);
        request.setOtp(otp);

        log.info("Calling UIDAI mock API to verify OTP for txnId: {}", txnId);

        return webClient.post()
                .uri(baseUrl + "/otp/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UidaiVerifyResponse.class)
                .doOnNext(response -> log.info("UIDAI OTP verification response: {}", response.getStatus()))
                .doOnError(error -> log.error("Error calling UIDAI OTP verify API: {}", error.getMessage()))
                .block(); // Synchronous for simplicity
    }

    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) return "****";
        return "****" + aadhaar.substring(aadhaar.length() - 4);
    }
}
