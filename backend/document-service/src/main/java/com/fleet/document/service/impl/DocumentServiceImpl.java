package com.fleet.document.service.impl;

import com.fleet.document.dto.DocumentRequest;
import com.fleet.document.dto.DocumentResponse;
import com.fleet.document.entity.Document;
import com.fleet.document.enums.DocumentStatus;
import com.fleet.document.enums.OwnerType;
import com.fleet.document.exception.ResourceNotFoundException;
import com.fleet.document.mapper.DocumentMapper;
import com.fleet.document.repository.DocumentRepository;
import com.fleet.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Override
    public DocumentResponse createDocument(DocumentRequest request) {
        Document document = DocumentMapper.toEntity(request);

        if (document.getStatus() == null) {
            document.setStatus(calculateStatus(document.getExpirationDate()));
        }

        Document savedDocument = documentRepository.save(document);
        return DocumentMapper.toResponse(savedDocument);
    }

    @Override
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    @Override
    public DocumentResponse getDocumentById(Long id) {
        Document document = findDocumentOrThrow(id);
        return DocumentMapper.toResponse(document);
    }

    @Override
    public List<DocumentResponse> getDocumentsByOwner(OwnerType ownerType, Long ownerId) {
        return documentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    @Override
    public List<DocumentResponse> getExpiringSoonDocuments() {
        return documentRepository.findByStatus(DocumentStatus.EXPIRING_SOON)
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    @Override
    public DocumentResponse updateDocument(Long id, DocumentRequest request) {
        Document document = findDocumentOrThrow(id);

        document.setOwnerType(request.getOwnerType());
        document.setOwnerId(request.getOwnerId());
        document.setDocumentType(request.getDocumentType());
        document.setFileName(request.getFileName());
        document.setFileUrl(request.getFileUrl());
        document.setExpirationDate(request.getExpirationDate());

        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        } else {
            document.setStatus(calculateStatus(request.getExpirationDate()));
        }

        Document updatedDocument = documentRepository.save(document);
        return DocumentMapper.toResponse(updatedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        Document document = findDocumentOrThrow(id);
        documentRepository.delete(document);
    }

    private Document findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    private DocumentStatus calculateStatus(LocalDate expirationDate) {
        if (expirationDate == null) {
            return DocumentStatus.VALID;
        }

        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(30);

        if (expirationDate.isBefore(today)) {
            return DocumentStatus.EXPIRED;
        }

        if (!expirationDate.isAfter(soonLimit)) {
            return DocumentStatus.EXPIRING_SOON;
        }

        return DocumentStatus.VALID;
    }
}