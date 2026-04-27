package com.pdev.ekyc_service.exception;

/**
 * Thrown when OTP verification fails.
 */
public class OtpVerificationFailedException extends RuntimeException {
    public OtpVerificationFailedException(String message) {
        super(message);
    }
}
