package com.pdev.grievance_service.kafka.producer;

import com.pdev.grievance_service.kafka.events.GrievanceResolvedEvent;
import com.pdev.grievance_service.kafka.events.GrievanceSlaBreachedEvent;
import com.pdev.grievance_service.kafka.events.GrievanceSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrievanceKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String GRIEVANCE_SUBMITTED_TOPIC = "grievance.submitted";
    private static final String GRIEVANCE_SLA_BREACHED_TOPIC = "grievance.sla.breached";
    private static final String GRIEVANCE_RESOLVED_TOPIC = "grievance.resolved";

    public void publishGrievanceSubmittedEvent(GrievanceSubmittedEvent event) {
        log.info("[KAFKA] Publishing grievance.submitted: grievanceId={}, citizenId={}", 
                event.getGrievanceId(), event.getCitizenId());
        kafkaTemplate.send(GRIEVANCE_SUBMITTED_TOPIC, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA] ✓ grievance.submitted sent: grievanceId={}", event.getGrievanceId());
                    } else {
                        log.error("[KAFKA] ✗ grievance.submitted failed: {}", ex.getMessage(), ex);
                    }
                });
    }

    public void publishGrievanceSlaBreachedEvent(GrievanceSlaBreachedEvent event) {
        log.info("[KAFKA] Publishing grievance.sla.breached: grievanceId={}, citizenId={}", 
                event.getGrievanceId(), event.getCitizenId());
        kafkaTemplate.send(GRIEVANCE_SLA_BREACHED_TOPIC, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA] ✓ grievance.sla.breached sent: grievanceId={}", event.getGrievanceId());
                    } else {
                        log.error("[KAFKA] ✗ grievance.sla.breached failed: {}", ex.getMessage(), ex);
                    }
                });
    }

    public void publishGrievanceResolvedEvent(GrievanceResolvedEvent event) {
        log.info("[KAFKA] Publishing grievance.resolved: grievanceId={}, citizenId={}", 
                event.getGrievanceId(), event.getCitizenId());
        kafkaTemplate.send(GRIEVANCE_RESOLVED_TOPIC, event.getCitizenId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA] ✓ grievance.resolved sent: grievanceId={}", event.getGrievanceId());
                    } else {
                        log.error("[KAFKA] ✗ grievance.resolved failed: {}", ex.getMessage(), ex);
                    }
                });
    }
}
