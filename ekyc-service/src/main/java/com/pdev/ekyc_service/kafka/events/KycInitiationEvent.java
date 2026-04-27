package com.pdev.ekyc_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event consumed from citizen-service when KYC initiation is requested.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiationEvent {
    private String citizenId;
    private String aadhaarNumber;
}
