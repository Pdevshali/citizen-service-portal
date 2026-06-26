package com.pdev.certificate_service.model;

/**
 * Lifecycle states of a certificate.
 */
public enum CertificateStatus {
    /** Request received, processing not yet started. */
    PENDING,
    /** Certificate successfully generated and available for download. */
    GENERATED,
    /** Processing failed (e.g., citizen not found, Feign error). */
    FAILED
}
