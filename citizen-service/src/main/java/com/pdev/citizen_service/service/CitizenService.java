package com.pdev.citizen_service.service;


import com.pdev.citizen_service.dto.*;

import java.util.List;

public interface CitizenService {

    CitizenProfileResponse registerCitizen(CitizenRegistrationRequest request);

    CitizenProfileResponse getCitizenProfile(String id);

    void initiateKyc(KycInitiateRequest request);

    void fetchDocument(DocumentFetchRequest request);

    void requestCertificate(String citizenId, CertificateRequest request);

    List<ServiceRequestResponse> getAvailableServices(String citizenId);

    Boolean existsByCitizenId(String id);

    // ── Keycloak-authenticated "current user" operations ─────────────────────

    /**
     * Returns the citizen profile linked to the given Keycloak user ID.
     * Throws {@link com.pdev.citizen_service.exception.CitizenNotFoundException}
     * when the user has not yet completed onboarding.
     *
     * @param keycloakUserId JWT sub claim forwarded by the gateway as X-User-Id
     */
    CitizenProfileResponse getMe(String keycloakUserId);

    /**
     * Creates a new citizen profile linked to the authenticated Keycloak account.
     * Email and keycloakUserId are taken from JWT headers — never from the request body.
     *
     * @param keycloakUserId JWT sub claim (X-User-Id header)
     * @param email          JWT email claim (X-User-Email header)
     * @param request        citizen profile fields supplied by the user in the form
     */
    CitizenProfileResponse onboardCitizen(String keycloakUserId, String email, OnboardingRequest request);
}

