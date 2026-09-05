package com.pdev.api_gatway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway entry point.
 * Registers with Eureka for service discovery and acts as the single entry point
 * for all citizen-services-portal microservices.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatwayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatwayApplication.class, args);
	}

}
