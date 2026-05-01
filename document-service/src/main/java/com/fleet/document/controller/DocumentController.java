package com.fleet.document.controller;

import com.fleet.document.dto.DocumentDTO;
import com.fleet.document.model.DocumentStatus;
import com.fleet.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("entityId") Long entityId,
            @RequestParam("entityType") String entityType,
            @RequestParam(value = "description", required = false) String description) throws IOException {
        
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentType(com.fleet.document.model.DocumentType.valueOf(documentType));
        documentDTO.setEntityId(entityId);
        documentDTO.setEntityType(entityType);
        documentDTO.setDescription(description);
        
        DocumentDTO uploaded = documentService.uploadDocument(file, documentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }
    
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByEntity(
            @PathVariable Long entityId,
            @RequestParam String entityType) {
        return ResponseEntity.ok(documentService.getDocumentsByEntity(entityId, entityType));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<DocumentDTO> updateDocumentStatus(
            @PathVariable Long id,
            @RequestParam DocumentStatus status) {
        return ResponseEntity.ok(documentService.updateDocumentStatus(id, status));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) throws IOException {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}