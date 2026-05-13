package com.pdev.citizen_service.controller;

import com.pdev.citizen_service.dto.*;
import com.pdev.citizen_service.model.Citizen;
import com.pdev.citizen_service.service.CitizenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/citizens")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenService citizenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CitizenProfileResponse>> registerCitizen(@Valid @RequestBody CitizenRegistrationRequest request) {
        CitizenProfileResponse citizen = citizenService.registerCitizen(request);
        ApiResponse<CitizenProfileResponse> response = new ApiResponse<>(true, "Citizen registered successfully", citizen);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateCitizen(@PathVariable String id){
        return ResponseEntity.ok(citizenService.existsByCitizenId(id));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<CitizenProfileResponse>> getCitizenProfile(@PathVariable String id) {
        CitizenProfileResponse profile = citizenService.getCitizenProfile(id);
        ApiResponse<CitizenProfileResponse> response = new ApiResponse<>(true, "Profile retrieved successfully", profile);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/initiate-kyc")
    public ResponseEntity<ApiResponse<Void>> initiateKyc(@Valid @RequestBody KycInitiateRequest request) {
        citizenService.initiateKyc(request);
        ApiResponse<Void> response = new ApiResponse<>(true, "KYC initiation started successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fetch-document")
    public ResponseEntity<ApiResponse<Void>> fetchDocument(@Valid @RequestBody @NotNull DocumentFetchRequest request) {
        citizenService.fetchDocument(request);
        ApiResponse<Void> response = new ApiResponse<>(true, "Document fetch request submitted successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/request-certificate")
    public ResponseEntity<ApiResponse<Void>> requestCertificate(@PathVariable String id, @Valid @RequestBody CertificateRequest request) {
        citizenService.requestCertificate(id, request);
        ApiResponse<Void> response = new ApiResponse<>(true, "Certificate request submitted successfully", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAvailableServices(@PathVariable String id) {
        List<ServiceRequestResponse> services = citizenService.getAvailableServices(id);
        ApiResponse<List<ServiceRequestResponse>> response = new ApiResponse<>(true, "Services retrieved successfully", services);
        return ResponseEntity.ok(response);
    }
}
