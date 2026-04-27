package com.pdev.citizen_service.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO for fetching documents after KYC verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFetchRequest {
    @NotBlank(message = "Document type is required")
    private String documentType;
    private String remarks;
}
