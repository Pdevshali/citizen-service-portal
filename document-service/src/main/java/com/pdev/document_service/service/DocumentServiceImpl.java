package com.pdev.document_service.service;

import com.pdev.document_service.kafka.events.DocumentFetchRequestedEvent;
import com.pdev.document_service.kafka.events.DocumentFetchCompletedEvent;
import com.pdev.document_service.kafka.producer.DocumentKafkaProducer;
import com.pdev.document_service.model.DocumentRecord;
import com.pdev.document_service.model.FetchStatus;
import com.pdev.document_service.repository.DocumentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRecordRepository repository;
    private final DocumentKafkaProducer kafkaProducer;

    @Override
    public void fetchDocumentFromDigiLocker(DocumentFetchRequestedEvent event) {

        log.info("Fetching document from DigiLocker for citizenId: {}", event.getCitizenId());
        // 2. Save initial record
        DocumentRecord record = new DocumentRecord();
        record.setCitizenId(event.getCitizenId());
        record.setAadhaarNumber(event.getAadhaarNumber());
        record.setDocumentType(event.getDocumentType());
        record.setStatus(FetchStatus.IN_PROGRESS);
        record.setRequestedAt(event.getInitiatedAt());
        repository.save(record);

        // 2. Mock DigiLocker fetch and Minio upload
        String documentUrl = "https://minio.mock/documents/" + event.getCitizenId() + "/" + event.getDocumentType() + ".pdf";

        // 3. Update record
        record.setStatus(FetchStatus.COMPLETED);
        record.setDocumentUrl(documentUrl);
        record.setCompletedAt(LocalDateTime.now());
        repository.save(record);
        log.info("document saved in db with document type: {} and citizenId : {} ", event.getDocumentType(), event.getCitizenId());

        // 4. Publish completed event
        DocumentFetchCompletedEvent completedEvent = new DocumentFetchCompletedEvent();
        completedEvent.setCitizenId(event.getCitizenId());
        completedEvent.setDocumentType(event.getDocumentType());
        completedEvent.setDocumentUrl(documentUrl);
        completedEvent.setCompletedAt(LocalDateTime.now());
        log.info("document fetch completed with document url: {} ", documentUrl);
        kafkaProducer.publishDocumentFetchCompleted(completedEvent);
        log.info("document fetch completed event published for citizenId: {}", event.getCitizenId());
    }
}
