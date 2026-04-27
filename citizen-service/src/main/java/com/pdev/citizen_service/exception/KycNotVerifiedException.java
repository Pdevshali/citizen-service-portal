package com.pdev.citizen_service.exception;

/**
 * Exception thrown when KYC verification is required but not completed.
 */
public class KycNotVerifiedException extends RuntimeException {

    public KycNotVerifiedException(String message) {
        super(message);
    }

    public KycNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}

