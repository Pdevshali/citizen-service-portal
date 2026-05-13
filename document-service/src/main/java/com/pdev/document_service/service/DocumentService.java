package com.pdev.document_service.service;

import com.pdev.document_service.kafka.events.DocumentFetchRequestedEvent;

public interface DocumentService {
    void fetchDocumentFromDigiLocker(DocumentFetchRequestedEvent event);
}
