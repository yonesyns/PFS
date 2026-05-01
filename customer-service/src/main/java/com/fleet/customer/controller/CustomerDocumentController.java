package com.fleet.customer.controller;

import com.fleet.customer.dto.CustomerDocumentDTO;
import com.fleet.customer.model.CustomerDocument;
import com.fleet.customer.service.CustomerDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/documents")
@RequiredArgsConstructor
public class CustomerDocumentController {

    private final CustomerDocumentService documentService;

    @GetMapping
    public ResponseEntity<List<CustomerDocument>> getDocuments(@PathVariable Long customerId) {
        return ResponseEntity.ok(documentService.getDocuments(customerId));
    }

    @PostMapping
    public ResponseEntity<CustomerDocument> addDocument(
            @PathVariable Long customerId,
            @RequestBody CustomerDocumentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.addDocument(customerId, dto));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long customerId, @PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
