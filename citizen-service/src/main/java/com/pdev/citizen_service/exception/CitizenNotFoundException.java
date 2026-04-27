package com.pdev.citizen_service.exception;
/**
 * Exception thrown when citizen is not found.
 */
public class CitizenNotFoundException extends RuntimeException {
    public CitizenNotFoundException(String message) {
        super(message);
    }
    public CitizenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
