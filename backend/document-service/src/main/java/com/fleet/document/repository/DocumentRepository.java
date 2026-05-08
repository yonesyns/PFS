package com.fleet.document.repository;

import com.fleet.document.entity.Document;
import com.fleet.document.enums.DocumentStatus;
import com.fleet.document.enums.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId);

    List<Document> findByStatus(DocumentStatus status);

    List<Document> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate);
}