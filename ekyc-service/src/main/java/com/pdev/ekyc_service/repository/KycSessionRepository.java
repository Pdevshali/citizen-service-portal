package com.pdev.ekyc_service.repository;

import com.pdev.ekyc_service.model.KycSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for KycSession entity.
 */
@Repository
public interface KycSessionRepository extends JpaRepository<KycSession, String> {

    Optional<KycSession> findByTxnId(String txnId);

    Optional<KycSession> findByCitizenId(String citizenId);
    
    Optional<KycSession> findTopByCitizenIdOrderByCreatedAtDesc(String citizenId);
}
