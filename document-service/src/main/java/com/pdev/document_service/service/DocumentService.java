package com.pdev.document_service.service;

import com.pdev.document_service.dto.DocumentResponse;
import com.pdev.document_service.kafka.events.DocumentFetchRequestedEvent;

import java.util.List;

public interface DocumentService {
    void fetchDocumentFromDigiLocker(DocumentFetchRequestedEvent event);

    List<DocumentResponse> getDocumentsByCitizenId(String citizenId);
}
