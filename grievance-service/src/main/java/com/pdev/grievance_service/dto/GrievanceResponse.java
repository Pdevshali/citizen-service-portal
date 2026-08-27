package com.pdev.grievance_service.dto;

import com.pdev.grievance_service.model.GrievanceCategory;
import com.pdev.grievance_service.model.GrievanceStatus;
import com.pdev.grievance_service.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceResponse {
    private String id;
    private String citizenId;
    private String referenceNumber;
    private String title;
    private String description;
    private GrievanceCategory category;
    private GrievanceStatus status;
    private Priority priority;
    private LocalDateTime submittedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime expectedResolutionDate;
    private String assignedOfficer;
    private String resolutionNotes;
    private String attachmentUrl;
    private LocalDateTime updatedAt;
}
