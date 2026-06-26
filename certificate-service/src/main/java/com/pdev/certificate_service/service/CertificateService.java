package com.pdev.certificate_service.service;

import com.pdev.certificate_service.dto.CertificateResponse;

import java.util.List;

/**
 * Service interface for certificate read operations.
 * Write operations happen exclusively via Kafka consumer.
 */
public interface CertificateService {

    /**
     * Get a single certificate by its ID.
     */
    CertificateResponse getCertificateById(String id);

    /**
     * Get all certificates issued to a specific citizen.
     */
    List<CertificateResponse> getCertificatesByCitizenId(String citizenId);
}
