package com.pdev.citizen_service.kafka.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentFetchCompletedEvent {
    private String citizenId;
    private String documentType;
    private String documentUrl;
    private LocalDateTime completedAt;

    // Add other fields as needed, match citizen-service
    // Getters, setters, constructors
}
