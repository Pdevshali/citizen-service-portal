package com.pdev.notification_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirror of document-service's DocumentFetchCompletedEvent.
 *
 * Topic   : document.fetch.completed
 * Producer: document-service
 * Consumer: notification-service (this service)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFetchCompletedEvent {

    /** Citizen who requested the document fetch. */
    private String citizenId;

    /** Type of document (e.g. AADHAAR, PAN, DRIVING_LICENSE). */
    private String documentType;

    /** Pre-signed / CDN URL from which the citizen can download the document. */
    private String documentUrl;

    /** When the document became available. */
    private LocalDateTime completedAt;
}
