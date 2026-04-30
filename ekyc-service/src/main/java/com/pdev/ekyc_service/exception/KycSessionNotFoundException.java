package com.pdev.ekyc_service.exception;

/**
 * Thrown when a KYC session is not found.
 */
public class KycSessionNotFoundException extends RuntimeException {
    public KycSessionNotFoundException(String message) {
        super(message);
    }
}
