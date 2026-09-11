package com.pdev.citizen_service.service;

import com.pdev.citizen_service.dto.*;
import com.pdev.citizen_service.exception.CitizenAlreadyExistsException;
import com.pdev.citizen_service.exception.CitizenNotFoundException;
import com.pdev.citizen_service.exception.KycNotVerifiedException;
import com.pdev.citizen_service.kafka.events.CertificateIssuanceRequestedEvent;
import com.pdev.citizen_service.kafka.events.DocumentFetchRequestedEvent;
import com.pdev.citizen_service.kafka.events.KycInitiationEvent;
import com.pdev.citizen_service.kafka.producer.CitizenKafkaProducer;
import com.pdev.citizen_service.model.Citizen;
import com.pdev.citizen_service.model.KycStatus;
import com.pdev.citizen_service.repository.CitizenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenServiceImpl implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final CitizenKafkaProducer kafkaProducer;

    @Override
    public CitizenProfileResponse registerCitizen(CitizenRegistrationRequest request) {
        // Check for existing citizen
        log.info("entering registerCitizen method");
        if (citizenRepository.existsByEmail(request.getEmail())) {
            throw new CitizenAlreadyExistsException("Citizen with email " + request.getEmail() + " already exists");
        }
        if (citizenRepository.existsByPhone(request.getPhone())) {
            throw new CitizenAlreadyExistsException("Citizen with phone " + request.getPhone() + " already exists");
        }
        if (citizenRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            throw new CitizenAlreadyExistsException("Citizen with Aadhaar " + request.getAadhaarNumber() + " already exists");
        }

        // Create and save citizen
        Citizen citizen = Citizen.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .aadhaarNumber(request.getAadhaarNumber())
                .address(request.getAddress())
                .state(request.getState())
                .pincode(request.getPincode())
                .kycStatus(KycStatus.PENDING)
                .build();

        Citizen savedCitizen = citizenRepository.save(citizen);
        return mapToCitizenResponse(savedCitizen);
    }

    @Override
    public CitizenProfileResponse getCitizenProfile(String id) {
        log.info("entering getCitizenProfile method");
        Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new CitizenNotFoundException("Citizen with id " + id + " not found"));
        return mapToCitizenResponse(citizen);
    }

    private CitizenProfileResponse mapToCitizenResponse(Citizen citizen) {
        return CitizenProfileResponse.builder()
                .keycloakUserId(citizen.getKeycloakUserId())
                .id(citizen.getId())
                .fullName(citizen.getFullName())
                .email(citizen.getEmail())
                .phone(citizen.getPhone())
                .dateOfBirth(citizen.getDateOfBirth())
                .address(citizen.getAddress())
                .state(citizen.getState())
                .pincode(citizen.getPincode())
                .kycStatus(citizen.getKycStatus())
                .registeredAt(citizen.getRegisteredAt())
                .kycVerifiedAt(citizen.getKycVerifiedAt())
                .build();
    }

    @Override
    public void initiateKyc(KycInitiateRequest request) {
        log.info("initiating initiateKyc method for citizenId: {}", request.getCitizenId());

        Citizen citizen = citizenRepository.findById(request.getCitizenId())
                .orElseThrow(() -> new CitizenNotFoundException("Citizen with id " + request.getCitizenId() + " not found"));

        // Guard: block if already fully VERIFIED
        if (citizen.getKycStatus() == KycStatus.VERIFIED) {
            throw new IllegalStateException("KYC is already verified for citizen " + request.getCitizenId());
        }

        // Guard: if INITIATED, allow retry only if it has been stuck for more than 10 minutes
        // (handles the case where ekyc-service was down and the OTP was never sent/received)
        if (citizen.getKycStatus() == KycStatus.INITIATED) {
            LocalDateTime initiatedAt = citizen.getKycInitiatedAt();
            boolean stuckTooLong = initiatedAt == null ||
                    initiatedAt.isBefore(LocalDateTime.now().minusMinutes(10));
            if (!stuckTooLong) {
                throw new IllegalStateException(
                        "KYC is already in progress for citizen " + request.getCitizenId() +
                        ". Please wait 10 minutes before retrying.");
            }
            // Stuck for too long — reset and allow re-initiation
            log.warn("KYC was stuck in INITIATED for citizen {} since {}. Resetting for retry.",
                    request.getCitizenId(), initiatedAt);
            citizen.setKycStatus(KycStatus.PENDING);
        }

        // Validate Aadhaar number is registered
        if (citizen.getAadhaarNumber() == null || citizen.getAadhaarNumber().isEmpty()) {
            throw new IllegalArgumentException("Citizen has no Aadhaar number registered. Please complete registration first.");
        }

        // Verify Aadhaar number matches
        if (!citizen.getAadhaarNumber().equals(request.getAadhaarNumber())) {
            log.warn("Aadhaar mismatch for citizen {}: expected={}, provided={}",
                    request.getCitizenId(), citizen.getAadhaarNumber(), request.getAadhaarNumber());
            throw new IllegalArgumentException("Aadhaar number does not match citizen records");
        }

        // Update citizen KYC status
        citizen.setKycStatus(KycStatus.INITIATED);
        citizen.setKycInitiatedAt(LocalDateTime.now());
        citizenRepository.save(citizen);
        log.info("Updated citizen {} to INITIATED status", request.getCitizenId());

        // Publish KycInitiationEvent to ekyc-service via Kafka
        try {
            KycInitiationEvent event = new KycInitiationEvent(
                    request.getCitizenId(),
                    request.getAadhaarNumber(),
                    request.getPhone(),
                    citizen.getKycInitiatedAt()
            );
            kafkaProducer.publishKycInitiationEvent(event);
            log.info("KYC initiation event published for citizen: {}", request.getCitizenId());
        } catch (Exception e) {
            log.error("Failed to publish KYC initiation event for citizen {}: {}", request.getCitizenId(), e.getMessage(), e);
            throw new RuntimeException("Failed to initiate KYC: " + e.getMessage(), e);
        }
    }

    @Override
    public void fetchDocument(DocumentFetchRequest request) {
        log.info("fetching documents for citizenId: {}", request.getCitizenId());
        Citizen citizen = citizenRepository.findById(request.getCitizenId())
                .orElseThrow(() -> new CitizenNotFoundException("Citizen with id " + request.getCitizenId() + " not found"));

        if (citizen.getKycStatus() != KycStatus.VERIFIED) {
            throw new KycNotVerifiedException("KYC must be verified to fetch documents");
        }

        // Convert documentType string to enum
        log.info("Publishing DocumentFetchRequestEvent for citizen: {}", request.getCitizenId());
        try {
             DocumentFetchRequestedEvent event = new DocumentFetchRequestedEvent(
                     request.getCitizenId(),
                     request.getAadhaarNumber(),
                     request.getDocumentTypeAsEnum(),
                     LocalDateTime.now()
             );
             kafkaProducer.publishDocumentFetchRequestEvent(event);
            log.info("Document fetch request event published for citizen: {}", request.getCitizenId());
        } catch (Exception e) {
            log.error("Failed to publish document fetch request event for citizen {}: {}", request.getCitizenId(), e.getMessage(), e);
            throw new RuntimeException("Failed to fetch document: " + e.getMessage(), e);
        }
    }

    @Override
    public void requestCertificate(String citizenId, CertificateRequest request) {
        log.info("requestCertificate called for citizenId={}, type={}", citizenId, request.getCertificateType());

        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new CitizenNotFoundException("Citizen with id " + citizenId + " not found"));

        if (citizen.getKycStatus() != KycStatus.VERIFIED) {
            throw new KycNotVerifiedException("KYC must be verified to request certificates");
        }

        try {
            CertificateIssuanceRequestedEvent event = new CertificateIssuanceRequestedEvent(
                    citizenId,
                    request.getCertificateType(),
                    request.getPurpose(),
                    request.getRemarks()
            );
            kafkaProducer.publishCertificateIssuanceRequestedEvent(event);
            log.info("Certificate issuance request published for citizenId={}, type={}",
                    citizenId, request.getCertificateType());
        } catch (Exception e) {
            log.error("Failed to publish certificate issuance request for citizenId={}: {}",
                    citizenId, e.getMessage(), e);
            throw new RuntimeException("Failed to request certificate: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceRequestResponse> getAvailableServices(String citizenId) {
        // Ensure citizen exists
        citizenRepository.findById(citizenId)
                .orElseThrow(() -> new CitizenNotFoundException("Citizen with id " + citizenId + " not found"));

        // TODO: Fetch from service-catalog-service; for now, return hardcoded list
        return Arrays.asList(
                new ServiceRequestResponse("1", "Aadhaar Card", "Get your Aadhaar card", "AVAILABLE", false),
                new ServiceRequestResponse("2", "PAN Card", "Apply for PAN card", "AVAILABLE", true),
                new ServiceRequestResponse("3", "Driving License", "Renew driving license", "AVAILABLE", true),
                new ServiceRequestResponse("4", "Birth Certificate", "Request birth certificate", "AVAILABLE", true)
        );
    }

    @Override
    public Boolean existsByCitizenId(String id) {
        log.info("Checking if citizen with id {} exists", id);
        return citizenRepository.existsById(id);
    }

    // ── Keycloak-authenticated "current user" operations ─────────────────────

    /**
     * Returns the citizen profile linked to the given Keycloak user ID.
     * Called by GET /api/citizens/me — keycloakUserId comes from the JWT
     * (X-User-Id header forwarded by the gateway).
     */
    @Override
    public CitizenProfileResponse getMe(String keycloakUserId) {
        log.info("getMe called for keycloakUserId: {}", keycloakUserId);
        Citizen citizen = citizenRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new CitizenNotFoundException(
                        "No citizen profile found for the authenticated user. Please complete onboarding."));
        return mapToCitizenResponse(citizen);
    }

    /**
     * Creates a new citizen profile linked to the authenticated Keycloak account.
     * Called by POST /api/citizens/me/onboarding.
     *
     * <p>The {@code keycloakUserId} and {@code email} are derived from the JWT
     * (X-User-Id and X-User-Email headers); the client cannot supply them.
     */
    @Override
    public CitizenProfileResponse onboardCitizen(String keycloakUserId, String email, OnboardingRequest request) {
        log.info("onboardCitizen called for keycloakUserId: {}", keycloakUserId);

        // Guard: already onboarded
        if (citizenRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new CitizenAlreadyExistsException(
                    "A citizen profile already exists for the authenticated user.");
        }
        // Guard: phone uniqueness
        if (citizenRepository.existsByPhone(request.getPhone())) {
            throw new CitizenAlreadyExistsException(
                    "Citizen with phone " + request.getPhone() + " already exists");
        }
        // Guard: Aadhaar uniqueness
        if (citizenRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            throw new CitizenAlreadyExistsException(
                    "Citizen with Aadhaar " + request.getAadhaarNumber() + " already exists");
        }

        Citizen citizen = Citizen.builder()
                .keycloakUserId(keycloakUserId)
                .email(email)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .aadhaarNumber(request.getAadhaarNumber())
                .address(request.getAddress())
                .state(request.getState())
                .pincode(request.getPincode())
                .kycStatus(KycStatus.PENDING)
                .build();

        Citizen savedCitizen = citizenRepository.save(citizen);
        log.info("Citizen onboarded successfully: citizenId={}, keycloakUserId={}",
                savedCitizen.getId(), keycloakUserId);
        return mapToCitizenResponse(savedCitizen);
    }
}

