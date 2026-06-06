package com.fleet.document.service;

import com.fleet.commons.dto.PageResponse;
import com.fleet.commons.exception.BadRequestException;
import com.fleet.commons.exception.ResourceNotFoundException;
import com.fleet.document.dto.DocumentResponse;
import com.fleet.document.dto.DocumentSearchRequest;
import com.fleet.document.dto.DocumentUploadRequest;
import com.fleet.document.entity.DocumentEntity;
import com.fleet.document.entity.DocumentStatus;
import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import com.fleet.document.repository.DocumentRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket:fleet-documents}")
    private String bucketName;

    @Value("${minio.endpoint:http://minio:9000}")
    private String minioEndpoint;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png"
    );

    public DocumentResponse uploadDocument(MultipartFile file, DocumentUploadRequest request, UUID uploadedBy) {
        log.info("Uploading document: {} for entity: {}/{}", 
                file.getOriginalFilename(), request.getEntityType(), request.getEntityId());

        validateFile(file);

        try {
            String fileName = generateFileName(request, file.getOriginalFilename());
            String storagePath = buildStoragePath(request, fileName);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            DocumentEntity document = DocumentEntity.builder()
                    .fileName(fileName)
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .documentType(request.getDocumentType())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .storagePath(storagePath)
                    .uploadedBy(uploadedBy)
                    .expiryDate(request.getExpiryDate())
                    .status(DocumentStatus.ACTIVE)
                    .metadata(request.getMetadata())
                    .build();

            DocumentEntity saved = documentRepository.save(document);
            log.info("Document uploaded with id: {}", saved.getId());

            return toResponse(saved);

        } catch (Exception e) {
            log.error("Failed to upload document", e);
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }

    public DocumentResponse getDocument(String id) {
        DocumentEntity document = findDocumentById(id);
        return toResponse(document);
    }

    public byte[] downloadDocument(String id) {
        DocumentEntity document = findDocumentById(id);

        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(document.getStoragePath())
                .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download document: {}", id, e);
            throw new BadRequestException("Failed to download document: " + e.getMessage());
        }
    }

    public PageResponse<DocumentResponse> searchDocuments(DocumentSearchRequest request, Pageable pageable) {
        Page<DocumentEntity> page;

        if (request.getDocumentType() != null && request.getStatus() != null) {
            page = documentRepository.findByEntityTypeAndEntityIdAndDocumentTypeAndStatus(
                    request.getEntityType(), request.getEntityId(), request.getDocumentType(), request.getStatus(), pageable);
        } else if (request.getDocumentType() != null) {
            page = documentRepository.findByEntityTypeAndEntityIdAndDocumentType(
                    request.getEntityType(), request.getEntityId(), request.getDocumentType(), pageable);
        } else if (request.getStatus() != null) {
            page = documentRepository.findByEntityTypeAndEntityIdAndStatus(
                    request.getEntityType(), request.getEntityId(), request.getStatus(), pageable);
        } else {
            page = documentRepository.findByEntityTypeAndEntityId(
                    request.getEntityType(), request.getEntityId(), pageable);
        }

        return buildPageResponse(page);
    }

    public void deleteDocument(String id) {
        DocumentEntity document = findDocumentById(id);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(document.getStoragePath())
                    .build());

            document.setStatus(DocumentStatus.ARCHIVED);
            documentRepository.save(document);

            log.info("Document archived: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete document from storage: {}", id, e);
            throw new BadRequestException("Failed to delete document: " + e.getMessage());
        }
    }

    public void createDocumentFolder(EntityType entityType, String entityId) {
        log.info("Creating document folder for {}:{}", entityType, entityId);
        // In MinIO, folders are virtual - we just log this
        // Actual folder structure is created when files are uploaded
    }

    public void markInvoiceAsPaid(String invoiceId) {
        List<DocumentEntity> invoices = documentRepository.findByDocumentTypeAndEntityId(
                DocumentType.INVOICE, invoiceId);

        for (DocumentEntity doc : invoices) {
            doc.setStatus(DocumentStatus.PAID);
            documentRepository.save(doc);
            log.info("Invoice document {} marked as PAID", doc.getId());
        }
    }

    public void checkExpiredDocuments() {
        log.info("Checking expired documents...");
        LocalDate today = LocalDate.now();
        List<DocumentEntity> expired = documentRepository.findByStatusAndExpiryDateBefore(
                DocumentStatus.ACTIVE, today);

        for (DocumentEntity doc : expired) {
            doc.setStatus(DocumentStatus.EXPIRED);
            documentRepository.save(doc);
            log.info("Document {} marked as EXPIRED", doc.getId());
        }
    }

    public List<DocumentResponse> getExpiringDocuments(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return documentRepository.findExpiringDocuments(expiryDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DocumentEntity findDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size of 10MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("File type not allowed. Allowed types: PDF, JPG, PNG");
        }
    }

    private String generateFileName(DocumentUploadRequest request, String originalName) {
        String extension = originalName != null && originalName.contains(".") 
                ? originalName.substring(originalName.lastIndexOf(".")) 
                : "";
        return UUID.randomUUID() + extension;
    }

    private String buildStoragePath(DocumentUploadRequest request, String fileName) {
        return String.format("%s/%s/%s/%s",
                request.getEntityType().name().toLowerCase(),
                request.getEntityId(),
                request.getDocumentType().name().toLowerCase(),
                fileName);
    }

    private DocumentResponse toResponse(DocumentEntity document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalName(document.getOriginalName())
                .contentType(document.getContentType())
                .size(document.getSize())
                .documentType(document.getDocumentType())
                .entityType(document.getEntityType())
                .entityId(document.getEntityId())
                .uploadedBy(document.getUploadedBy())
                .uploadedAt(document.getUploadedAt())
                .expiryDate(document.getExpiryDate())
                .status(document.getStatus())
                .metadata(document.getMetadata())
                .downloadUrl(minioEndpoint + "/" + bucketName + "/" + document.getStoragePath())
                .build();
    }

    private PageResponse<DocumentResponse> buildPageResponse(Page<DocumentEntity> page) {
        return PageResponse.<DocumentResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
