package com.pdev.ekyc_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Kafka is fully configured via application.yml
 *
 * Spring Boot auto-configuration reads:
 *   spring.kafka.bootstrap-servers
 *   spring.kafka.producer.*
 *   spring.kafka.consumer.*
 *
 * and creates these beans automatically:
 *   ✅ KafkaTemplate<String, Object>
 *   ✅ ConsumerFactory
 *   ✅ KafkaListenerContainerFactory
 *
 * We only need @EnableKafka here to activate @KafkaListener scanning.
 * Manual bean definitions are NOT needed - they cause issues with deprecated serializers.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }
}