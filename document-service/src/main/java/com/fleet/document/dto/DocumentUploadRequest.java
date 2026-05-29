package com.fleet.document.dto;

import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private String entityId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    private LocalDate expiryDate;

    private Map<String, String> metadata;
}
