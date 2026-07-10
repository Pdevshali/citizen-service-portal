package com.pdev.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service — Entry Point
 *
 * Responsibilities:
 *   - Consumes Kafka events from ekyc-service, document-service,
 *     certificate-service, and grievance-service
 *   - Sends mock SMS / Email / Push notifications to citizens
 *   - Persists every notification attempt in NotificationLog for audit
 *
 * Port  : 8087
 * Topics: kyc.verification.completed
 *         document.fetch.completed
 *         certificate.issued
 *         grievance.sla.breached
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
