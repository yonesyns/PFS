package com.fleet.document.repository;

import com.fleet.document.model.Document;
import com.fleet.document.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEntityIdAndEntityType(Long entityId, String entityType);
    List<Document> findByDocumentType(DocumentType documentType);
    List<Document> findByEntityType(String entityType);
}