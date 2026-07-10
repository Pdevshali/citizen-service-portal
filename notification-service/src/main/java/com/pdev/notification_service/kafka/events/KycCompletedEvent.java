package com.pdev.notification_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirror of ekyc-service's KycCompletedEvent.
 *
 * Topic   : kyc.verification.completed
 * Producer: ekyc-service
 * Consumer: notification-service (this service)
 *
 * NOTE: We intentionally keep only the fields that notification-service
 * needs. Adding a shared-lib is overkill for a demo; mirroring DTOs is
 * the pragmatic trade-off.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycCompletedEvent {

    /** Citizen whose KYC was processed. */
    private String citizenId;

    /** VERIFIED | FAILED — notification content depends on this. */
    private String status;

    /** When verification concluded. */
    private LocalDateTime verifiedAt;
}
