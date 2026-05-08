package com.fleet.document.dto;

import com.fleet.document.enums.DocumentStatus;
import com.fleet.document.enums.DocumentType;
import com.fleet.document.enums.OwnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentRequest {

    @NotNull
    private OwnerType ownerType;

    @NotNull
    private Long ownerId;

    @NotNull
    private DocumentType documentType;

    @NotBlank
    private String fileName;

    private String fileUrl;

    private LocalDate expirationDate;

    private DocumentStatus status;
}