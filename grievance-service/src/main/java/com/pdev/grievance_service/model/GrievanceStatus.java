package com.pdev.grievance_service.model;

public enum GrievanceStatus {
    SUBMITTED,      // Initial state when grievance is submitted
    ACKNOWLEDGED,   // Officer has acknowledged the grievance
    IN_PROGRESS,    // Currently being resolved
    RESOLVED,       // Grievance has been resolved
    CLOSED,         // Case is closed
    REJECTED        // Grievance was rejected
}
