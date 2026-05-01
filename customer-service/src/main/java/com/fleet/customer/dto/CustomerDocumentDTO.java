package com.fleet.customer.dto;

import lombok.Data;

@Data
public class CustomerDocumentDTO {
    private Long id;
    private String documentType;
    private String fileName;
    private String fileUrl;
}
