package com.pdev.certificate_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the certificate-service.
 * Validates citizens via Feign → generates certificate → publishes Kafka event.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class CertificateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateServiceApplication.class, args);
    }
}
