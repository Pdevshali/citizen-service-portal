package com.pdev.certificate_service.controller;

import com.pdev.certificate_service.dto.ApiResponse;
import com.pdev.certificate_service.dto.CertificateResponse;
import com.pdev.certificate_service.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for certificate read endpoints.
 *
 * Certificates are created asynchronously via Kafka — use these endpoints
 * to poll status or list all certificates for a citizen.
 */
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * GET /api/certificates/{id}
     * Returns full details and current status of a certificate.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificate(@PathVariable String id) {
        CertificateResponse certificate = certificateService.getCertificateById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Certificate retrieved successfully", certificate));
    }

    /**
     * GET /api/certificates/citizen/{citizenId}
     * Returns all certificates issued to a specific citizen.
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getCertificatesByCitizen(
            @PathVariable String citizenId) {
        List<CertificateResponse> certificates = certificateService.getCertificatesByCitizenId(citizenId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Certificates retrieved successfully", certificates));
    }
}
