package com.pdev.ekyc_service.dto;

import com.pdev.ekyc_service.model.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload for GET /api/kyc/{citizenId}/status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycStatusResponse {
    private String citizenId;
    private KycStatus status;
    private LocalDateTime verifiedAt;
    private String demographicData; // Decrypted JSON
}
