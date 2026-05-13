package com.pdev.document_service.dto;

import com.pdev.document_service.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFetchReq {
    private String citizenId;
    private DocumentType documentType;
    // Getters, setters, constructors
}

