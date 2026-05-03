package com.pdev.citizen_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka is fully configured via application.yml
 *
 * Spring Boot auto-configuration reads all Kafka properties and creates beans:
 *   ✅ KafkaTemplate<String, Object>
 *   ✅ ConsumerFactory
 *   ✅ KafkaListenerContainerFactory
 *
 * We only need @EnableKafka to activate @KafkaListener scanning.
 * Manual bean definitions are NOT needed with Spring Boot 4.0+.
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    // Auto-configured by Spring Boot from application.yml
}
