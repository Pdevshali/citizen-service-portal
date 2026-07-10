package com.pdev.notification_service.model;

/**
 * The delivery channel used to dispatch a notification.
 *
 * SMS   – Text message to the citizen's registered mobile number
 * EMAIL – E-mail to the citizen's registered address
 * PUSH  – In-app push notification
 */
public enum NotificationChannel {
    SMS,
    EMAIL,
    PUSH
}
