package com.pdev.notification_service.repository;

import com.pdev.notification_service.model.NotificationLog;
import com.pdev.notification_service.model.NotificationChannel;
import com.pdev.notification_service.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Audit repository for NotificationLog.
 *
 * Spring Data JPA auto-implements all CRUD methods.
 * Custom finders allow querying the audit log by citizen,
 * notification type, or delivery channel.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /** All notifications sent to a specific citizen. */
    List<NotificationLog> findByCitizenId(String citizenId);

    /** All notifications of a given type (e.g. all KYC_VERIFIED). */
    List<NotificationLog> findByNotificationType(NotificationType notificationType);

    /** All failed notifications (for retry or alerting). */
    List<NotificationLog> findBySuccess(boolean success);

    /** Notifications by citizen AND channel. */
    List<NotificationLog> findByCitizenIdAndChannel(String citizenId, NotificationChannel channel);
}
