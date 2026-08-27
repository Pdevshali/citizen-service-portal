package com.pdev.grievance_service.repository;

import com.pdev.grievance_service.model.Grievance;
import com.pdev.grievance_service.model.GrievanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, String> {
    
    Optional<Grievance> findByReferenceNumber(String referenceNumber);
    
    List<Grievance> findByCitizenId(String citizenId);
    
    List<Grievance> findByCitizenIdAndStatus(String citizenId, GrievanceStatus status);
    
    List<Grievance> findByStatus(GrievanceStatus status);
    
    List<Grievance> findByStatusAndExpectedResolutionDateBefore(GrievanceStatus status, LocalDateTime date);
    
    List<Grievance> findByAssignedOfficer(String officerId);
}
