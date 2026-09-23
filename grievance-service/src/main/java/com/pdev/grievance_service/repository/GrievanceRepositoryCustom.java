package com.pdev.grievance_service.repository;

import com.pdev.grievance_service.dto.GrievanceSearchRequest;
import com.pdev.grievance_service.model.Grievance;

import java.util.List;

public interface GrievanceRepositoryCustom {

    List<Grievance> searchGrievances(
            GrievanceSearchRequest request,
            int offset
    );

    long countGrievances(GrievanceSearchRequest request);
}
