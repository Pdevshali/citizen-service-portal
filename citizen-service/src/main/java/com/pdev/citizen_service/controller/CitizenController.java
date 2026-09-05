package com.pdev.citizen_service.controller;

import com.pdev.citizen_service.dto.*;
import com.pdev.citizen_service.service.CitizenService;
import jakarta.validation.Valid;
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

    // ── Public / legacy endpoints ────────────────────────────────────────────

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

    // ── Keycloak-authenticated "current user" endpoints ──────────────────────

    /**
     * Returns the citizen profile for the currently authenticated user.
     *
     * <p>Identity is derived from the {@code X-User-Id} header, which the API
     * Gateway populates from the validated JWT {@code sub} claim. The client
     * never supplies the citizenId — there is no ID in the URL.
     *
     * <p>Returns 404 when the authenticated user has not yet completed onboarding.
     * The Angular app uses this signal to redirect to /onboarding.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CitizenProfileResponse>> getMe(
            @RequestHeader("X-User-Id") String keycloakUserId) {
        CitizenProfileResponse profile = citizenService.getMe(keycloakUserId);
        ApiResponse<CitizenProfileResponse> response =
                new ApiResponse<>(true, "Profile retrieved successfully", profile);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates the citizen profile for the currently authenticated user.
     *
     * <p>{@code keycloakUserId} (X-User-Id) and {@code email} (X-User-Email) are
     * injected from gateway headers — the request body must NOT contain them.
     * This prevents a malicious client from impersonating another Keycloak account.
     *
     * <p>Returns 409 (via {@link com.pdev.citizen_service.exception.CitizenAlreadyExistsException})
     * when the user has already onboarded.
     */
    @PostMapping("/me/onboarding")
    public ResponseEntity<ApiResponse<CitizenProfileResponse>> onboardCitizen(
            @RequestHeader("X-User-Id")    String keycloakUserId,
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody OnboardingRequest request) {
        CitizenProfileResponse citizen = citizenService.onboardCitizen(keycloakUserId, email, request);
        ApiResponse<CitizenProfileResponse> response =
                new ApiResponse<>(true, "Citizen onboarded successfully", citizen);
        return ResponseEntity.ok(response);
    }
}

