package com.pdev.document_service.repository;

import com.pdev.document_service.model.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {
    // CRUD methods for DocumentRecord
}

