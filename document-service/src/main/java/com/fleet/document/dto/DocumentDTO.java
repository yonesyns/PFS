package com.fleet.document.dto;

import com.fleet.document.model.DocumentStatus;
import com.fleet.document.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentDTO {
    private Long id;
    
    @NotBlank(message = "File name is required")
    private String fileName;
    
    @NotBlank(message = "File type is required")
    private String fileType;
    
    @NotNull(message = "File size is required")
    private Long fileSize;
    
    private String filePath;
    
    @NotNull(message = "Document type is required")
    private DocumentType documentType;
    
    @NotNull(message = "Entity ID is required")
    private Long entityId;
    
    @NotBlank(message = "Entity type is required")
    private String entityType;
    
    private DocumentStatus status;
    private String description;
    private String uploadedBy;
}