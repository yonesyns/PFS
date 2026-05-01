package com.fleet.document.service;

import com.fleet.document.dto.DocumentDTO;
import com.fleet.document.model.Document;
import com.fleet.document.model.DocumentStatus;
import com.fleet.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String uploadDir = "uploads/";
    
    public DocumentDTO uploadDocument(MultipartFile file, DocumentDTO documentDTO) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        Document document = mapToEntity(documentDTO);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFilePath(filePath.toString());
        document.setStatus(DocumentStatus.UPLOADED);
        
        Document saved = documentRepository.save(document);
        kafkaTemplate.send("document-events", "document.uploaded", saved);
        return mapToDTO(saved);
    }
    
    public DocumentDTO getDocument(Long id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        return mapToDTO(document);
    }
    
    public List<DocumentDTO> getDocumentsByEntity(Long entityId, String entityType) {
        return documentRepository.findByEntityIdAndEntityType(entityId, entityType).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public DocumentDTO updateDocumentStatus(Long id, DocumentStatus status) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        
        document.setStatus(status);
        Document updated = documentRepository.save(document);
        kafkaTemplate.send("document-events", "document.status.updated", updated);
        return mapToDTO(updated);
    }
    
    public void deleteDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        
        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        documentRepository.delete(document);
        kafkaTemplate.send("document-events", "document.deleted", document);
    }
    
    private DocumentDTO mapToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setFilePath(document.getFilePath());
        dto.setDocumentType(document.getDocumentType());
        dto.setEntityId(document.getEntityId());
        dto.setEntityType(document.getEntityType());
        dto.setStatus(document.getStatus());
        dto.setDescription(document.getDescription());
        dto.setUploadedBy(document.getUploadedBy());
        return dto;
    }
    
    private Document mapToEntity(DocumentDTO dto) {
        Document document = new Document();
        document.setDocumentType(dto.getDocumentType());
        document.setEntityId(dto.getEntityId());
        document.setEntityType(dto.getEntityType());
        document.setDescription(dto.getDescription());
        document.setUploadedBy(dto.getUploadedBy());
        return document;
    }
}