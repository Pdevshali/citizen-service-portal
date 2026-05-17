package com.pdev.ekyc_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event consumed from citizen-service when KYC initiation is requested.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiationEvent {
    
    private String citizenId;
    private String aadhaarNumber;
    private Long mobile;
    private LocalDateTime initiatedAt;
    private String requestId;
    
    public KycInitiationEvent(String citizenId, String aadhaarNumber, Long mobile, LocalDateTime initiatedAt) {
        this.citizenId = citizenId;
        this.aadhaarNumber = aadhaarNumber;
        this.mobile = mobile;
        this.initiatedAt = initiatedAt;
        this.requestId = "REQ_" + System.currentTimeMillis();
    }
}
