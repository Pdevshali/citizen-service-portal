package com.pdev.grievance_service.service;

import com.pdev.grievance_service.dto.GrievanceResponse;
import com.pdev.grievance_service.dto.GrievanceSearchRequest;
import com.pdev.grievance_service.dto.GrievanceStatusUpdateRequest;
import com.pdev.grievance_service.dto.GrievanceSubmitRequest;
import com.pdev.grievance_service.dto.PageResponse;
import com.pdev.grievance_service.exception.GrievanceNotFoundException;
import com.pdev.grievance_service.kafka.events.GrievanceResolvedEvent;
import com.pdev.grievance_service.kafka.events.GrievanceSlaBreachedEvent;
import com.pdev.grievance_service.kafka.events.GrievanceSubmittedEvent;
import com.pdev.grievance_service.kafka.producer.GrievanceKafkaProducer;
import com.pdev.grievance_service.model.Grievance;
import com.pdev.grievance_service.model.GrievanceStatus;
import com.pdev.grievance_service.repository.GrievanceRepository;
import com.pdev.grievance_service.util.QueryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrievanceServiceImpl implements GrievanceService {

    private final GrievanceRepository grievanceRepository;
    private final GrievanceKafkaProducer kafkaProducer;
    private final ModelMapper modelMapper;

    @Override
    public GrievanceResponse submitGrievance(GrievanceSubmitRequest request) {
        // Derive citizenId from request if present, otherwise use the authenticated principal (safer)
        String citizenId = request.getCitizenId();
        if (citizenId == null || citizenId.isBlank()) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            citizenId = (auth != null && auth.getName() != null) ? auth.getName() : null;
        }
        log.info("Submitting grievance for citizen: {}", citizenId);
        
        String referenceNumber = generateReferenceNumber();
        LocalDateTime expectedResolutionDate = calculateExpectedResolutionDate(request.getPriority());
        
        Grievance grievance = Grievance.builder()
                .citizenId(citizenId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .referenceNumber(referenceNumber)
                .status(GrievanceStatus.SUBMITTED)
                .expectedResolutionDate(expectedResolutionDate)
                .attachmentUrl(request.getAttachmentUrl())
                .build();
        
        Grievance savedGrievance = grievanceRepository.save(grievance);
        log.info("Grievance submitted successfully: {}", savedGrievance.getId());
        
        // Publish event to Kafka
        GrievanceSubmittedEvent event = GrievanceSubmittedEvent.builder()
                .grievanceId(savedGrievance.getId())
                .referenceNumber(referenceNumber)
                .citizenId(savedGrievance.getCitizenId())
                .title(savedGrievance.getTitle())
                .description(savedGrievance.getDescription())
                .category(savedGrievance.getCategory())
                .priority(savedGrievance.getPriority())
                .submittedAt(savedGrievance.getSubmittedAt())
                .build();
        kafkaProducer.publishGrievanceSubmittedEvent(event);
        
        return modelMapper.map(savedGrievance, GrievanceResponse.class);
    }

    @Override
    public GrievanceResponse getGrievance(String grievanceId) {
        log.info("Fetching grievance: {}", grievanceId);
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new GrievanceNotFoundException("Grievance not found with ID: " + grievanceId));
        return modelMapper.map(grievance, GrievanceResponse.class);
    }

    /** In this method, we will update the status of a grievance.
     * It will also update the assigned officer, resolution notes, priority, and status.
     * @param grievanceId the ID of the grievance to update
     * @param request the request containing the new status, assigned officer, resolution notes, priority, and status
     * @return the updated grievance
     * **/
    @Override
    public GrievanceResponse updateGrievanceStatus(String grievanceId, GrievanceStatusUpdateRequest request) {
        log.info("Updating grievance status: {}, newStatus: {}", grievanceId, request.getStatus());
        
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new GrievanceNotFoundException("Grievance not found with ID: " + grievanceId));
        
        grievance.setStatus(request.getStatus());
        
        if (request.getAssignedOfficer() != null) {
            grievance.setAssignedOfficer(request.getAssignedOfficer());
        }
        
        if (request.getResolutionNotes() != null) {
            grievance.setResolutionNotes(request.getResolutionNotes());
        }
        
        if (request.getPriority() != null) {
            grievance.setPriority(request.getPriority());
        }
        
        if (request.getStatus() == GrievanceStatus.ACKNOWLEDGED) {
            grievance.setAcknowledgedAt(LocalDateTime.now());
        } else if (request.getStatus() == GrievanceStatus.RESOLVED) {
            grievance.setResolvedAt(LocalDateTime.now());
        } else if (request.getStatus() == GrievanceStatus.CLOSED) {
            grievance.setClosedAt(LocalDateTime.now());
        }
        
        Grievance updatedGrievance = grievanceRepository.save(grievance);
        log.info("Grievance status updated: {}", grievanceId);
        
        return modelMapper.map(updatedGrievance, GrievanceResponse.class);
    }

    @Override
    public List<GrievanceResponse> getGrievancesByCitizen(String citizenId) {
        log.info("Fetching grievances for citizen: {}", citizenId);
        List<Grievance> grievances = grievanceRepository.findByCitizenId(citizenId);
        return grievances.stream()
                .map(g -> modelMapper.map(g, GrievanceResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<GrievanceResponse> getGrievancesByStatus(GrievanceStatus status) {
        log.info("Fetching grievances by status: {}", status);
        List<Grievance> grievances = grievanceRepository.findByStatus(status);
        return grievances.stream()
                .map(g -> modelMapper.map(g, GrievanceResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(fixedDelay = 3600000) // Run every hour
    public List<GrievanceResponse> checkSlaBreaches() {
        log.info("Checking for SLA breaches...");
        LocalDateTime now = LocalDateTime.now();
        
        List<Grievance> breachedGrievances = grievanceRepository
                .findByStatusAndExpectedResolutionDateBefore(GrievanceStatus.IN_PROGRESS, now);
        
        List<GrievanceResponse> result = breachedGrievances.stream()
                .map(g -> {
                    log.warn("SLA breach detected for grievance: {}", g.getId());
                    
                    GrievanceSlaBreachedEvent event = GrievanceSlaBreachedEvent.builder()
                            .grievanceId(g.getId())
                            .referenceNumber(g.getReferenceNumber())
                            .citizenId(g.getCitizenId())
                            .title(g.getTitle())
                            .category(g.getCategory())
                            .priority(g.getPriority())
                            .expectedResolutionDate(g.getExpectedResolutionDate())
                            .detectedAt(now)
                            .build();
                    kafkaProducer.publishGrievanceSlaBreachedEvent(event);
                    
                    return modelMapper.map(g, GrievanceResponse.class);
                })
                .collect(Collectors.toList());
        
        return result;
    }

    @Override
    public GrievanceResponse resolveGrievance(String grievanceId, String resolutionNotes) {
        log.info("Resolving grievance: {}", grievanceId);
        
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new GrievanceNotFoundException("Grievance not found with ID: " + grievanceId));
        
        grievance.setStatus(GrievanceStatus.RESOLVED);
        grievance.setResolutionNotes(resolutionNotes);
        grievance.setResolvedAt(LocalDateTime.now());
        
        Grievance resolvedGrievance = grievanceRepository.save(grievance);
        log.info("Grievance resolved: {}", grievanceId);
        
        // Publish event to Kafka
        GrievanceResolvedEvent event = GrievanceResolvedEvent.builder()
                .grievanceId(resolvedGrievance.getId())
                .referenceNumber(resolvedGrievance.getReferenceNumber())
                .citizenId(resolvedGrievance.getCitizenId())
                .title(resolvedGrievance.getTitle())
                .category(resolvedGrievance.getCategory())
                .resolutionNotes(resolutionNotes)
                .resolvedAt(resolvedGrievance.getResolvedAt())
                .build();
        kafkaProducer.publishGrievanceResolvedEvent(event);
        
        return modelMapper.map(resolvedGrievance, GrievanceResponse.class);
    }

    @Override
    public PageResponse<GrievanceResponse> searchGrievances(
            GrievanceSearchRequest request) {

        // 1. Normalize page
        int page = QueryValidator.normalizePage(
                request.getPage()
        );

        // 2. Normalize size
        int size = QueryValidator.normalizeSize(
                request.getSize()
        );

        // 3. Allowed sorting fields
        Map<String, String> allowedSortFields = Map.of(
                "createdAt", "g.submitted_at",
                "updatedAt", "g.updated_at",
                "status", "g.status",
                "title", "g.title"
        );

        // 4. Normalize sort field
        String sortBy = QueryValidator.normalizeSortBy(
                request.getSortBy(),
                allowedSortFields
        );

        // 5. Normalize sort direction
        String sortDir = QueryValidator.normalizeSortDir(
                request.getSortDir()
        );

        // 6. Calculate database offset
        int offset = page * size;

        // 7. Put normalized values back into request
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        // 8. Fetch paginated data
        List<Grievance> grievances =
                grievanceRepository.searchGrievances(
                        request,
                        offset
                );

        // 9. Fetch total count (same filters, no LIMIT/OFFSET)
        long totalElements = grievanceRepository.countGrievances(request);

        // 10. Compute pagination metadata
        int totalPages = size > 0
                ? (int) Math.ceil((double) totalElements / size)
                : 0;
        boolean hasNext = (long) (page + 1) * size < totalElements;
        boolean hasPrevious = page > 0;

        // 11. Convert Entity -> Response DTO
        List<GrievanceResponse> content = grievances.stream()
                .map(grievance ->
                        modelMapper.map(
                                grievance,
                                GrievanceResponse.class
                        )
                )
                .toList();

        // 12. Build and return PageResponse
        return PageResponse.<GrievanceResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
    }

    private String generateReferenceNumber() {
        return "GRV-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) 
               + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private LocalDateTime calculateExpectedResolutionDate(com.pdev.grievance_service.model.Priority priority) {
        return switch (priority) {
            case LOW -> LocalDateTime.now().plus(30, ChronoUnit.DAYS);
            case MEDIUM -> LocalDateTime.now().plus(15, ChronoUnit.DAYS);
            case HIGH -> LocalDateTime.now().plus(7, ChronoUnit.DAYS);
            case CRITICAL -> LocalDateTime.now().plus(2, ChronoUnit.DAYS);
        };
    }

}
