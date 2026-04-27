package com.pdev.ekyc_service.controller;

import com.pdev.ekyc_service.dto.*;
import com.pdev.ekyc_service.service.EkycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/kyc")
@RequiredArgsConstructor
public class EkycController {

    private final EkycService ekycService;

    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponse<GenerateOtpResponse>> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        GenerateOtpResponse response = ekycService.generateOtp(request);
        ApiResponse<GenerateOtpResponse> apiResponse = new ApiResponse<>(true, "OTP generated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = ekycService.verifyOtp(request);
        ApiResponse<VerifyOtpResponse> apiResponse = new ApiResponse<>(true, "OTP verified successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{citizenId}/status")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getKycStatus(@PathVariable String citizenId) {
        KycStatusResponse response = ekycService.getKycStatus(citizenId);
        ApiResponse<KycStatusResponse> apiResponse = new ApiResponse<>(true, "KYC status retrieved successfully", response);
        return ResponseEntity.ok(apiResponse);
    }
}
