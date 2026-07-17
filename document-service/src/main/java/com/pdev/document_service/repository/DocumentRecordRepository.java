package com.pdev.document_service.repository;

import com.pdev.document_service.model.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {
    List<DocumentRecord> findByCitizenIdOrderByRequestedAtDesc(String citizenId);
}
