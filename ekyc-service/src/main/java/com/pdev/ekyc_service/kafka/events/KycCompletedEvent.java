package com.pdev.ekyc_service.kafka.events;

import com.pdev.ekyc_service.model.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published to citizen-service when KYC verification is completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycCompletedEvent {
    private String citizenId;
    private KycStatus status;
    private LocalDateTime verifiedAt;
    private String demographicData; // Encrypted JSON
}
