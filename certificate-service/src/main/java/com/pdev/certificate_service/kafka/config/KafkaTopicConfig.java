package com.pdev.certificate_service.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * Kafka topic definitions for certificate-service.
 *
 * Topics declared as NewTopic beans are created automatically by KafkaAdmin
 * on startup (if they don't already exist).
 *
 * fatalIfBrokerNotAvailable=false ensures that a temporary broker
 * unavailability (e.g. slow startup, DescribeTopicPartitions API mismatch
 * with older Confluent cp-kafka 7.4 brokers) does NOT crash the application.
 * The admin will retry on the next successful broker connection instead.
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Make KafkaAdmin non-fatal: if the broker is temporarily unreachable at
     * startup, the service still starts and the topic creation is retried later.
     */
    @Autowired
    public void configureKafkaAdmin(KafkaAdmin kafkaAdmin) {
        kafkaAdmin.setFatalIfBrokerNotAvailable(false);
    }

    /**
     * Topic published by certificate-service after a certificate is successfully
     * generated. Consumed by citizen-service / document-service.
     */
    @Bean
    public NewTopic certificateIssuedTopic() {
        return TopicBuilder.name("certificate.issued")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
