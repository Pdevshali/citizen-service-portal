package com.pdev.grievance_service.dto;

import com.pdev.grievance_service.model.GrievanceStatus;
import com.pdev.grievance_service.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceSearchRequest {

    private String citizenId;
    private GrievanceStatus status;
    private String search;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Priority priority;

    private int page = 0;
    private int size = 5;

    private String sortBy = "createdAt";
    private String sortDir = "desc";
}