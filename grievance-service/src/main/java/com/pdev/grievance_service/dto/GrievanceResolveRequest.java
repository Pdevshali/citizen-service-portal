package com.pdev.grievance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceResolveRequest {

    @NotBlank(message = "Resolution notes are required")
    @Size(min = 5, max = 2000, message = "Resolution notes must be between 5 and 2000 characters")
    private String resolutionNotes;
}
