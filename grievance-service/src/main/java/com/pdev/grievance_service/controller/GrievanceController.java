package com.pdev.grievance_service.controller;

import com.pdev.grievance_service.dto.*;
import com.pdev.grievance_service.dto.PageResponse;
import com.pdev.grievance_service.model.GrievanceStatus;
import com.pdev.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('CITIZEN','ADMIN')")
    public ResponseEntity<ApiResponse<GrievanceResponse>> submitGrievance(
            @Valid @RequestBody GrievanceSubmitRequest request) {
        GrievanceResponse grievance = grievanceService.submitGrievance(request);
        ApiResponse<GrievanceResponse> response = ApiResponse.<GrievanceResponse>builder()
                .success(true)
                .message("Grievance submitted successfully")
                .data(grievance)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{grievanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GrievanceResponse>> getGrievance(
            @PathVariable @NotBlank String grievanceId) {
        GrievanceResponse grievance = grievanceService.getGrievance(grievanceId);
        ApiResponse<GrievanceResponse> response = ApiResponse.<GrievanceResponse>builder()
                .success(true)
                .message("Grievance retrieved successfully")
                .data(grievance)
                .build();
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{grievanceId}/status")
        @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<GrievanceResponse>> updateGrievanceStatus(
            @PathVariable @NotBlank String grievanceId,
            @Valid @RequestBody GrievanceStatusUpdateRequest request) {
        GrievanceResponse grievance = grievanceService.updateGrievanceStatus(grievanceId, request);
        ApiResponse<GrievanceResponse> response = ApiResponse.<GrievanceResponse>builder()
                .success(true)
                .message("Grievance status updated successfully")
                .data(grievance)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/citizen/{citizenId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CITIZEN') and #citizenId == authentication.name)")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getGrievancesByCitizen(
            @PathVariable @NotBlank String citizenId) {
        List<GrievanceResponse> grievances = grievanceService.getGrievancesByCitizen(citizenId);
        ApiResponse<List<GrievanceResponse>> response = ApiResponse.<List<GrievanceResponse>>builder()
                .success(true)
                .message("Grievances retrieved successfully")
                .data(grievances)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getMyGrievances() {
        String citizenId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        List<GrievanceResponse> grievances = grievanceService.getGrievancesByCitizen(citizenId);
        ApiResponse<List<GrievanceResponse>> response = ApiResponse.<List<GrievanceResponse>>builder()
                .success(true)
                .message("Grievances retrieved successfully")
                .data(grievances)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getGrievancesByStatus(
            @PathVariable GrievanceStatus status) {
        List<GrievanceResponse> grievances = grievanceService.getGrievancesByStatus(status);
        ApiResponse<List<GrievanceResponse>> response = ApiResponse.<List<GrievanceResponse>>builder()
                .success(true)
                .message("Grievances retrieved successfully")
                .data(grievances)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{grievanceId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GrievanceResponse>> resolveGrievance(
            @PathVariable @NotBlank String grievanceId,
            @Valid @RequestBody GrievanceResolveRequest request) {
        GrievanceResponse grievance = grievanceService.resolveGrievance(grievanceId, request.getResolutionNotes());
        ApiResponse<GrievanceResponse> response = ApiResponse.<GrievanceResponse>builder()
                .success(true)
                .message("Grievance resolved successfully")
                .data(grievance)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sla/check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> checkSlaBreaches() {
        List<GrievanceResponse> breachedGrievances = grievanceService.checkSlaBreaches();
        ApiResponse<List<GrievanceResponse>> response = ApiResponse.<List<GrievanceResponse>>builder()
                .success(true)
                .message("SLA check completed")
                .data(breachedGrievances)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CITIZEN')")
    public ResponseEntity<ApiResponse<PageResponse<GrievanceResponse>>> searchGrievances(
            @ModelAttribute GrievanceSearchRequest request) {

        // Security: Citizens may only query their own grievances.
        // We override citizenId with the authenticated principal so a CITIZEN
        // cannot pass someone else's citizenId as a query param.
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
        boolean isCitizen = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CITIZEN"));
        if (isCitizen) {
            request.setCitizenId(auth.getName());
        }

        PageResponse<GrievanceResponse> pageResponse = grievanceService.searchGrievances(request);
        ApiResponse<PageResponse<GrievanceResponse>> response = ApiResponse.<PageResponse<GrievanceResponse>>builder()
                .success(true)
                .message("Grievances retrieved successfully")
                .data(pageResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
