package com.fleet.document.controller;

import com.fleet.commons.dto.ApiResponse;
import com.fleet.commons.dto.PageResponse;
import com.fleet.document.dto.DocumentResponse;
import com.fleet.document.dto.DocumentSearchRequest;
import com.fleet.document.dto.DocumentUploadRequest;
import com.fleet.document.entity.DocumentStatus;
import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import com.fleet.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for managing documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") EntityType entityType,
            @RequestParam("entityId") String entityId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestHeader("X-User-Id") UUID uploadedBy) {

        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .documentType(documentType)
                .expiryDate(expiryDate != null && !expiryDate.isBlank() ? LocalDate.parse(expiryDate) : null)
                .build();

        DocumentResponse response = documentService.uploadDocument(file, request, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document uploaded successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable String id) {
        DocumentResponse response = documentService.getDocument(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document file")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
        byte[] fileBytes = documentService.downloadDocument(id);
        DocumentResponse doc = documentService.getDocument(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + doc.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(fileBytes);
    }

    @GetMapping
    @Operation(summary = "Search documents")
    public ResponseEntity<ApiResponse<PageResponse<DocumentResponse>>> searchDocuments(
            @RequestParam EntityType entityType,
            @RequestParam String entityId,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) DocumentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        DocumentSearchRequest request = DocumentSearchRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .documentType(documentType)
                .status(status)
                .build();

        PageResponse<DocumentResponse> response = documentService.searchDocuments(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get documents expiring soon")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getExpiringDocuments(
            @RequestParam(defaultValue = "30") int days) {
        List<DocumentResponse> response = documentService.getExpiringDocuments(days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully"));
    }
}
