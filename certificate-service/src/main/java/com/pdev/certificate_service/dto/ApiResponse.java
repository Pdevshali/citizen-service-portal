package com.pdev.certificate_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard wrapper for ALL API responses.
 * Ensures consistent JSON structure (matches citizen-service ApiResponse pattern).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
