package com.pdev.certificate_service.exception;

/**
 * Thrown when a certificate is not found by ID.
 */
public class CertificateNotFoundException extends RuntimeException {
    public CertificateNotFoundException(String message) {
        super(message);
    }
}
