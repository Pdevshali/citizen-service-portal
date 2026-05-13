package com.pdev.document_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String citizenId;
    private String aadhaarNumber;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    private FetchStatus status;

    private String documentUrl;

    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

}
