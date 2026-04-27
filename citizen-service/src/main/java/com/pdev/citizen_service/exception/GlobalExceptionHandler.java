package com.pdev.citizen_service.exception;

import com.pdev.citizen_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for all controllers.
 * Catches exceptions and returns clean JSON responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCitizenNotFound(CitizenNotFoundException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CitizenAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCitizenAlreadyExists(CitizenAlreadyExistsException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(KycNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleKycNotVerified(KycNotVerifiedException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, "An unexpected error occurred", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
