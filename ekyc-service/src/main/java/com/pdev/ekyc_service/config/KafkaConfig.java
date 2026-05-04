package com.pdev.ekyc_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdev.ekyc_service.kafka.events.KycInitiationEvent;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for ekyc-service.
 * Spring Kafka 4.0+ - Uses Jackson ObjectMapper without deprecated serializers.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Custom Kafka Serializer using Jackson ObjectMapper (not deprecated).
     */
    public static class JacksonSerializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper;

        public JacksonSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(String topic, T data) {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize to JSON: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Custom Kafka Deserializer using Jackson ObjectMapper (not deprecated).
     */
    public static class JacksonDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper objectMapper;
        private final Class<T> targetClass;

        public JacksonDeserializer(ObjectMapper objectMapper, Class<T> targetClass) {
            this.objectMapper = objectMapper;
            this.targetClass = targetClass;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, targetClass);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize from JSON: " + e.getMessage(), e);
            }
        }
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("acks", "all");
        configProps.put("retries", 3);
        configProps.put("enable.idempotence", true);

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(configProps);
        factory.setValueSerializerSupplier(() -> new JacksonSerializer<>(objectMapper));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, KycInitiationEvent> consumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("group.id", "ekyc-service-group");
        configProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configProps.put("auto.offset.reset", "earliest");
        configProps.put("enable.auto.commit", false);

        DefaultKafkaConsumerFactory<String, KycInitiationEvent> factory = new DefaultKafkaConsumerFactory<>(configProps);
        factory.setValueDeserializerSupplier(() -> new JacksonDeserializer<>(objectMapper, KycInitiationEvent.class));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KycInitiationEvent> kafkaListenerContainerFactory(ConsumerFactory<String, KycInitiationEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, KycInitiationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }
}