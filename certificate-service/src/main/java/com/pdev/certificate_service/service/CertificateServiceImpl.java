package com.pdev.certificate_service.service;

import com.pdev.certificate_service.dto.CertificateResponse;
import com.pdev.certificate_service.exception.CertificateNotFoundException;
import com.pdev.certificate_service.model.Certificate;
import com.pdev.certificate_service.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CertificateService.
 * Handles all read-side operations; writes are driven by Kafka events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;

    @Override
    public CertificateResponse getCertificateById(String id) {
        log.info("Fetching certificate with id={}", id);
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException("Certificate not found with id: " + id));
        return mapToResponse(certificate);
    }

    @Override
    public List<CertificateResponse> getCertificatesByCitizenId(String citizenId) {
        log.info("Fetching certificates for citizenId={}", citizenId);
        return certificateRepository.findByCitizenId(citizenId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private CertificateResponse mapToResponse(Certificate cert) {
        return CertificateResponse.builder()
                .id(cert.getId())
                .citizenId(cert.getCitizenId())
                .certificateType(cert.getCertificateType())
                .purpose(cert.getPurpose())
                .status(cert.getStatus())
                .certificateNumber(cert.getCertificateNumber())
                .downloadUrl(cert.getDownloadUrl())
                .issuedAt(cert.getIssuedAt())
                .remarks(cert.getRemarks())
                .createdAt(cert.getCreatedAt())
                .build();
    }
}
