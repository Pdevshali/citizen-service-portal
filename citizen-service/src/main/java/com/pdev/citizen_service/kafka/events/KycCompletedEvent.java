package com.pdev.citizen_service.kafka.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pdev.citizen_service.model.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event consumed from e-KYC service when KYC verification is completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycCompletedEvent {
    private String citizenId;
    private KycStatus status; // "VERIFIED"
    private LocalDateTime verifiedAt;
    private String demographicData; // Encrypted JSON
}
