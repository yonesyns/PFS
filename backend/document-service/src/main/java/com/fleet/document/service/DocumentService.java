package com.fleet.document.service;

import com.fleet.document.dto.DocumentRequest;
import com.fleet.document.dto.DocumentResponse;
import com.fleet.document.enums.OwnerType;

import java.util.List;

public interface DocumentService {

    DocumentResponse createDocument(DocumentRequest request);

    List<DocumentResponse> getAllDocuments();

    DocumentResponse getDocumentById(Long id);

    List<DocumentResponse> getDocumentsByOwner(OwnerType ownerType, Long ownerId);

    List<DocumentResponse> getExpiringSoonDocuments();

    DocumentResponse updateDocument(Long id, DocumentRequest request);

    void deleteDocument(Long id);
}