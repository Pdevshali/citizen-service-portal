package com.pdev.ekyc_service.config;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.pdev.ekyc_service.kafka.events.KycInitiateRequest;
import com.pdev.ekyc_service.kafka.events.KycInitiationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;

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
 * Nothing else needed.
 */
@EnableKafka
@Configuration
public class KafkaConfig {
    // Intentionally empty — Spring Boot auto-config does all the work
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public KafkaTemplate<String, KycInitiateRequest> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, KycInitiateRequest> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,      bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG,                   "all");
        config.put(ProducerConfig.RETRIES_CONFIG,                3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,     true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public ConsumerFactory<String, KycInitiationEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Connection
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,  bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,           groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,  "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Key deserializer — plain String
        config.put(KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

        // Value deserializer — ErrorHandlingDeserializer wraps JsonDeserializer
        // This is the Spring Kafka 4.x recommended approach
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);

        // Tell ErrorHandlingDeserializer which delegate to use
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                "org.springframework.kafka.support.serializer.JsonDeserializer");

        // Tell JsonDeserializer which class to deserialize into
        config.put("spring.json.value.default.type",
                "com.pdev.ekyc_service.kafka.events.KycInitiationEvent");

        // Trust our package — without this deserialization will fail
        config.put("spring.json.trusted.packages", "com.pdev.*");

        // Do not require type headers in the message
        config.put("spring.json.use.type.headers", false);

        return new DefaultKafkaConsumerFactory<>(config);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KycInitiationEvent>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KycInitiationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}