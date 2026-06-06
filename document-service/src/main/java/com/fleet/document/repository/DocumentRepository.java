package com.fleet.document.repository;

import com.fleet.document.entity.DocumentEntity;
import com.fleet.document.entity.DocumentStatus;
import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {

    Page<DocumentEntity> findByEntityTypeAndEntityId(EntityType entityType, String entityId, Pageable pageable);

    Page<DocumentEntity> findByEntityTypeAndEntityIdAndDocumentType(
            EntityType entityType, String entityId, DocumentType documentType, Pageable pageable);

    Page<DocumentEntity> findByEntityTypeAndEntityIdAndDocumentTypeAndStatus(
            EntityType entityType, String entityId, DocumentType documentType, DocumentStatus status, Pageable pageable);

    Page<DocumentEntity> findByEntityTypeAndEntityIdAndStatus(
            EntityType entityType, String entityId, DocumentStatus status, Pageable pageable);

    List<DocumentEntity> findByStatusAndExpiryDateBefore(DocumentStatus status, LocalDate date);

    List<DocumentEntity> findByDocumentTypeAndEntityId(DocumentType documentType, String entityId);

    Optional<DocumentEntity> findByStoragePath(String storagePath);

    long countByEntityTypeAndEntityId(EntityType entityType, String entityId);

    @Query("{ 'status': 'ACTIVE', 'expiryDate': { $lt: ?0 } }")
    List<DocumentEntity> findExpiringDocuments(LocalDate date);
}
