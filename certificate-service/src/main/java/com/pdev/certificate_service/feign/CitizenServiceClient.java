package com.pdev.certificate_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to validate citizen existence via citizen-service.
 *
 * Calls: GET /api/citizens/{id}/validate
 * Returns: true if citizen exists, false otherwise.
 *
 * The service name "citizen-service" must match the spring.application.name
 * registered in Eureka by citizen-service.
 */
@FeignClient(name = "citizen-service")
public interface CitizenServiceClient {

    @GetMapping("/api/citizens/{id}/validate")
    Boolean validateCitizen(@PathVariable("id") String citizenId);
}
