package com.pdev.grievance_service.exception;

public class GrievanceNotFoundException extends RuntimeException {
    public GrievanceNotFoundException(String message) {
        super(message);
    }

    public GrievanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
