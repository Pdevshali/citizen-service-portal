package com.pdev.document_service.kafka.consumer;

import com.pdev.document_service.kafka.events.DocumentFetchRequestedEvent;
import com.pdev.document_service.service.DocumentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentKafkaConsumer {
    public static final String DOCUMENT_FETCH_REQUESTED = "document.fetch.requested";
    public static final String DOCUMENT_SERVICE_GROUP = "document-service-group";
    private final DocumentService documentService;

    public DocumentKafkaConsumer(DocumentService documentService) {
        this.documentService = documentService;
    }

    @KafkaListener(topics = DOCUMENT_FETCH_REQUESTED, groupId = DOCUMENT_SERVICE_GROUP)
    public void listen(DocumentFetchRequestedEvent event) {
        documentService.fetchDocumentFromDigiLocker(event);
    }
}
