package com.pdev.notification_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirror of grievance-service's GrievanceSlaBreachedEvent.
 *
 * Topic   : grievance.sla.breached
 * Producer: grievance-service
 * Consumer: notification-service (this service)
 *
 * Fired when a grievance crosses its SLA deadline without resolution.
 * Notification goes to BOTH the citizen and (in production) the assigned officer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceSlaBreachedEvent {

    /** The grievance that breached SLA. */
    private String grievanceId;

    /** Citizen who raised the grievance. */
    private String citizenId;

    /** Subject / title of the grievance. */
    private String subject;

    /** The SLA deadline that was missed. */
    private LocalDateTime slaDeadline;

    /** When the breach was detected. */
    private LocalDateTime breachedAt;
}
