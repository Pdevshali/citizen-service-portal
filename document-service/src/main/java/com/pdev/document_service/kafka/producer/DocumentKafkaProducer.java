package com.pdev.document_service.kafka.producer;

import com.pdev.document_service.kafka.events.DocumentFetchCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentKafkaProducer {
    public static final String DOCUMENT_FETCH_COMPLETED = "document.fetch.completed";
    // KafkaTemplate<String, Object> matches the bean in KafkaConfig exactly
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDocumentFetchCompleted(DocumentFetchCompletedEvent event) {
        log.info("[KAFKA] Publishing document fetch completed: citizenId={}, documentType={}",
                event.getCitizenId(), event.getDocumentType());

        kafkaTemplate.send(DOCUMENT_FETCH_COMPLETED, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null)
                        log.info("[KAFKA] ✓ Document fetch completed sent: citizenId={}", event.getCitizenId());
                    else
                        log.error("[KAFKA] ✗ Document fetch completed failed: {}", ex.getMessage(), ex);
                });
    }
}
