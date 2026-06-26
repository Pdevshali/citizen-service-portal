package com.pdev.certificate_service.repository;

import com.pdev.certificate_service.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Certificate entities.
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

    /**
     * Find all certificates belonging to a specific citizen.
     */
    List<Certificate> findByCitizenId(String citizenId);
}
