package com.pdev.notification_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit entity — one row per notification attempt.
 *
 * Stores the citizenId, what type of notification was sent,
 * which channel was used, the message body, and whether
 * the dispatch succeeded.
 *
 * Table: notification_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_log")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The citizen who received (or should have received) the notification. */
    @Column(name = "citizen_id", nullable = false)
    private String citizenId;

    /** What business event triggered this notification. */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    /** The channel used to deliver the notification. */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    /** The actual message text that was dispatched. */
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    /**
     * Delivery outcome.
     * TRUE  = dispatched without exception (mock success).
     * FALSE = an exception occurred during dispatch.
     */
    @Column(name = "success", nullable = false)
    private boolean success;

    /** Optional: error detail when success=false. */
    @Column(name = "error_detail", length = 500)
    private String errorDetail;

    /** When the notification was attempted. */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
