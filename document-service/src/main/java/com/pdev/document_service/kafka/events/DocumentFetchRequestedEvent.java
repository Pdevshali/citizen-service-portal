package com.pdev.document_service.kafka.events;

import com.pdev.document_service.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFetchRequestedEvent {
    private String citizenId;
    private String aadhaarNumber;
    private DocumentType documentType;
    private LocalDateTime initiatedAt;
    private String requestId;

    public DocumentFetchRequestedEvent(String citizenId, String aadhaarNumber, DocumentType documentType, LocalDateTime initiatedAt) {
        this.citizenId = citizenId;
        this.aadhaarNumber = aadhaarNumber;
        this.documentType = documentType;
        this.initiatedAt = initiatedAt;
        this.requestId = "REQ_" + System.currentTimeMillis();
    }
}

