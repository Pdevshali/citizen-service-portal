package com.pdev.document_service.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    @GetMapping("/{citizenId}/list")
    public String listDocuments(@PathVariable String citizenId) {
        // Return list of documents for citizen
        return "[]";
    }
}

