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
}
