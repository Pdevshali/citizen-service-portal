package com.pdev.citizen_service.repository;

import com.pdev.citizen_service.model.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Citizen entity with custom query methods.
 */
@Repository
public interface CitizenRepository extends JpaRepository<Citizen, String> {

    Optional<Citizen> findByEmail(String email);

    Optional<Citizen> findByPhone(String phone);

    Optional<Citizen> findByAadhaarNumber(String aadhaarNumber);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByAadhaarNumber(String aadhaarNumber);
}
