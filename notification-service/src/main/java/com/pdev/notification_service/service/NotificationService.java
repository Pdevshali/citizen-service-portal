package com.pdev.notification_service.service;

import com.pdev.notification_service.model.NotificationChannel;
import com.pdev.notification_service.model.NotificationType;

/**
 * Contract for the notification dispatch layer.
 *
 * Each method accepts the minimum citizen context and a pre-built
 * message body, dispatches the notification on every channel,
 * and persists an audit row in NotificationLog.
 */
public interface NotificationService {

    /**
     * Send a notification across all channels and persist the audit log.
     *
     * @param citizenId        Citizen receiving the notification
     * @param notificationType Business event that triggered the notification
     * @param channel          Delivery channel (SMS | EMAIL | PUSH)
     * @param message          Human-readable message body
     */
    void send(String citizenId,
              NotificationType notificationType,
              NotificationChannel channel,
              String message);

    /**
     * Convenience overload: dispatches on all three channels at once.
     *
     * @param citizenId        Citizen receiving the notification
     * @param notificationType Business event that triggered the notification
     * @param message          Human-readable message body
     */
    void sendAll(String citizenId,
                 NotificationType notificationType,
                 String message);
}
