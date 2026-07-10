package com.pdev.notification_service.client;

import com.pdev.notification_service.dto.ApiResponse;
import com.pdev.notification_service.dto.CitizenProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citizen-service")
public interface CitizenServiceClient {

    @GetMapping("/api/citizens/{id}/profile")
    ApiResponse<CitizenProfileResponse> getCitizenProfile(@PathVariable("id") String citizenId);
}
