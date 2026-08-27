package com.pdev.grievance_service.service;

import com.pdev.grievance_service.dto.GrievanceResponse;
import com.pdev.grievance_service.dto.GrievanceStatusUpdateRequest;
import com.pdev.grievance_service.dto.GrievanceSubmitRequest;
import com.pdev.grievance_service.model.GrievanceStatus;

import java.util.List;

public interface GrievanceService {
    
    GrievanceResponse submitGrievance(GrievanceSubmitRequest request);
    
    GrievanceResponse getGrievance(String grievanceId);
    
    GrievanceResponse updateGrievanceStatus(String grievanceId, GrievanceStatusUpdateRequest request);
    
    List<GrievanceResponse> getGrievancesByCitizen(String citizenId);
    
    List<GrievanceResponse> getGrievancesByStatus(GrievanceStatus status);
    
    List<GrievanceResponse> checkSlaBreaches();
    
    GrievanceResponse resolveGrievance(String grievanceId, String resolutionNotes);
}
