package com.pdev.citizen_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single available government service entry
 * returned by GET /api/citizens/{id}/services
 *
 * This will be enriched once service-catalog-service is added.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private String serviceId;
    private String serviceName;
    private String description;
    private String status;        // e.g. AVAILABLE, PENDING, COMPLETED
    private boolean kycRequired;
}
