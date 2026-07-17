package com.pdev.document_service.controller;

import com.pdev.document_service.dto.ApiResponse;
import com.pdev.document_service.dto.DocumentResponse;
import com.pdev.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByCitizen(
            @PathVariable String citizenId) {
        List<DocumentResponse> documents = documentService.getDocumentsByCitizenId(citizenId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Documents retrieved successfully", documents));
    }

    @GetMapping("/{citizenId}/list")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> listDocuments(@PathVariable String citizenId) {
        List<DocumentResponse> documents = documentService.getDocumentsByCitizenId(citizenId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Documents retrieved successfully", documents));
    }
}
