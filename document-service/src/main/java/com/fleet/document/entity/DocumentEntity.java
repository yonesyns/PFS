package com.fleet.document.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Document(collection = "documents")
@CompoundIndex(name = "entity_idx", def = "{'entityType': 1, 'entityId': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    private String id;

    @Indexed
    private String fileName;

    private String originalName;

    private String contentType;

    private Long size;

    @Indexed
    private DocumentType documentType;

    @Indexed
    private EntityType entityType;

    @Indexed
    private String entityId;

    private String storagePath;

    private UUID uploadedBy;

    @CreatedDate
    private Instant uploadedAt;

    private LocalDate expiryDate;

    @Indexed
    @Builder.Default
    private DocumentStatus status = DocumentStatus.ACTIVE;

    private Map<String, String> metadata;

    @LastModifiedDate
    private Instant updatedAt;
}
