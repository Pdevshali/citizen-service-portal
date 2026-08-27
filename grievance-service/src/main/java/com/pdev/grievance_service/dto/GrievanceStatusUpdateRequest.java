package com.pdev.grievance_service.dto;

import com.pdev.grievance_service.model.GrievanceStatus;
import com.pdev.grievance_service.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private GrievanceStatus status;

    private String assignedOfficer;

    private String resolutionNotes;

    private Priority priority;
}
