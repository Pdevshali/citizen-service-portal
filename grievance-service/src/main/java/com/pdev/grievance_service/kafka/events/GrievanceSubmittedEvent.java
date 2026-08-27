package com.pdev.grievance_service.kafka.events;

import com.pdev.grievance_service.model.GrievanceCategory;
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
public class GrievanceSubmittedEvent {
    private String grievanceId;
    private String referenceNumber;
    private String citizenId;
    private String title;
    private String description;
    private GrievanceCategory category;
    private Priority priority;
    private LocalDateTime submittedAt;
}
