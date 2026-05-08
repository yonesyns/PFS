package com.fleet.document.controller;

import com.fleet.document.dto.DocumentRequest;
import com.fleet.document.dto.DocumentResponse;
import com.fleet.document.enums.OwnerType;
import com.fleet.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public DocumentResponse createDocument(@Valid @RequestBody DocumentRequest request) {
        return documentService.createDocument(request);
    }

    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    @GetMapping("/owner/{ownerType}/{ownerId}")
    public List<DocumentResponse> getDocumentsByOwner(
            @PathVariable OwnerType ownerType,
            @PathVariable Long ownerId
    ) {
        return documentService.getDocumentsByOwner(ownerType, ownerId);
    }

    @GetMapping("/expiring-soon")
    public List<DocumentResponse> getExpiringSoonDocuments() {
        return documentService.getExpiringSoonDocuments();
    }

    @PutMapping("/{id}")
    public DocumentResponse updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request
    ) {
        return documentService.updateDocument(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}