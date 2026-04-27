package com.pdev.citizen_service.exception;
/**
 * Exception thrown when citizen already exists during registration.
 */
public class CitizenAlreadyExistsException extends RuntimeException {
    public CitizenAlreadyExistsException(String message) {
        super(message);
    }
    public CitizenAlreadyExistsException(String message, Throwable cause) {
        super(message,cause);
    }
}
