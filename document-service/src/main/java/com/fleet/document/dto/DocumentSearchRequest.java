package com.fleet.document.dto;

import com.fleet.document.entity.DocumentStatus;
import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {
    private EntityType entityType;
    private String entityId;
    private DocumentType documentType;
    private DocumentStatus status;
}
