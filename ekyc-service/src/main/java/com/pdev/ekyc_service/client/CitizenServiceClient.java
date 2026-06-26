package com.pdev.ekyc_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citizen-service")
public interface CitizenServiceClient {

    @GetMapping("api/citizens/{id}/validate")
    Boolean validateCitizen(@PathVariable("id") String id);
}
