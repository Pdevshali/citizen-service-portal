package com.pdev.citizen_service.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO for requesting certificates from government services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {
    @NotBlank(message = "Certificate type is required")
    private String certificateType;
    @NotBlank(message = "Purpose is required")
    private String purpose;
    private String remarks;
}
