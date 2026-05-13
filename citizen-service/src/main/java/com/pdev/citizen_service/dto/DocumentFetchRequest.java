package com.pdev.citizen_service.dto;
import com.pdev.citizen_service.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for fetching documents after KYC verification.
 * documentType is kept as String for API input validation,
 * then converted to enum in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFetchRequest {
    @NotBlank(message = "Citizen ID is required")
    String citizenId;

    String aadhaarNumber;

    @NotBlank(message = "Document type is required")
    private String documentType;

    /**
     * Convert documentType string to enum
     */
    public DocumentType getDocumentTypeAsEnum() {
        if (documentType == null || documentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Document type cannot be null or empty");
        }
        try {
            return DocumentType.valueOf(documentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid document type: " + documentType + ". Valid types are: AADHAAR, PAN, PASSPORT");
        }
    }
}
