package com.pdev.citizen_service.repository;

import com.pdev.citizen_service.model.Document;
import com.pdev.citizen_service.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByCitizenId(String citizenId);

    Optional<Document> findByCitizenIdAndDocumentType(String citizenId, DocumentType documentType);
}
