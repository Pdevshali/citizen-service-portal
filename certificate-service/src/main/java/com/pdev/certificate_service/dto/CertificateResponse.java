package com.pdev.certificate_service.dto;

import com.pdev.certificate_service.model.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO returned by GET /api/certificates/{id}
 * and GET /api/certificates/citizen/{citizenId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    private String id;
    private String citizenId;
    private String certificateType;
    private String purpose;
    private CertificateStatus status;
    private String certificateNumber;
    private String downloadUrl;
    private LocalDateTime issuedAt;
    private String remarks;
    private LocalDateTime createdAt;
}
