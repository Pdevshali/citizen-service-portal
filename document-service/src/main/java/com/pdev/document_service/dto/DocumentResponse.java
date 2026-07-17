package com.pdev.document_service.dto;

import com.pdev.document_service.model.DocumentType;
import com.pdev.document_service.model.FetchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String citizenId;
    private DocumentType documentType;
    private FetchStatus status;
    private String documentUrl;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}

