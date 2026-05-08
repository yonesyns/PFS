package com.fleet.document.mapper;

import com.fleet.document.dto.DocumentRequest;
import com.fleet.document.dto.DocumentResponse;
import com.fleet.document.entity.Document;

public class DocumentMapper {

    public static Document toEntity(DocumentRequest request) {
        return Document.builder()
                .ownerType(request.getOwnerType())
                .ownerId(request.getOwnerId())
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .expirationDate(request.getExpirationDate())
                .status(request.getStatus())
                .build();
    }

    public static DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .ownerType(document.getOwnerType())
                .ownerId(document.getOwnerId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .expirationDate(document.getExpirationDate())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}