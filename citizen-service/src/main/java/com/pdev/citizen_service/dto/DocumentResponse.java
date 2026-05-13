package com.pdev.citizen_service.dto;

import com.pdev.citizen_service.model.DocumentType;
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
    private String id;
    private String citizenId;
    private DocumentType documentType;
    private String documentUrl;
    private LocalDateTime fetchedAt;
}