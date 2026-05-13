package com.pdev.document_service.kafka.events;

import com.pdev.document_service.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFetchCompletedEvent {
    private String citizenId;
    private DocumentType documentType;
    private String documentUrl;
    private LocalDateTime completedAt;
}

