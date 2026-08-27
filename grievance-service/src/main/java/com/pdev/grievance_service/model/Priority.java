package com.pdev.grievance_service.model;

public enum Priority {
    LOW,        // Low priority grievance (SLA: 30 days)
    MEDIUM,     // Medium priority grievance (SLA: 15 days)
    HIGH,       // High priority grievance (SLA: 7 days)
    CRITICAL    // Critical/Urgent (SLA: 48 hours)
}
