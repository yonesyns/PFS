package com.fleet.document.dto;

import com.fleet.document.entity.DocumentStatus;
import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String id;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long size;
    private DocumentType documentType;
    private EntityType entityType;
    private String entityId;
    private UUID uploadedBy;
    private Instant uploadedAt;
    private LocalDate expiryDate;
    private DocumentStatus status;
    private Map<String, String> metadata;
    private String downloadUrl;
}
