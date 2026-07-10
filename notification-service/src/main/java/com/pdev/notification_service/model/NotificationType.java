package com.pdev.notification_service.model;

/**
 * Categorises what triggered the notification.
 *
 * KYC_VERIFIED   – e-KYC verification completed (from ekyc-service)
 * DOC_READY      – Document fetch completed (from document-service)
 * CERT_READY     – Certificate generated (from certificate-service)
 * SLA_BREACH     – Grievance SLA breached (from grievance-service)
 */
public enum NotificationType {
    KYC_VERIFIED,
    DOC_READY,
    CERT_READY,
    SLA_BREACH
}
