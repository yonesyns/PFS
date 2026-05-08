package com.fleet.document.dto;

import com.fleet.document.enums.DocumentStatus;
import com.fleet.document.enums.DocumentType;
import com.fleet.document.enums.OwnerType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {

    private Long id;
    private OwnerType ownerType;
    private Long ownerId;
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private LocalDate expirationDate;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}