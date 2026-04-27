package com.pdev.ekyc_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from UIDAI mock API for OTP verification.
 * Includes demographic data if successful.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UidaiVerifyResponse {
    private String status; // "success" or "failure"
    private String message;
    private String aadhaar;
    private String name;
    private String dob;
    private String gender;
    private String address;
    private String phone;
}
