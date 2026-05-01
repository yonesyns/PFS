package com.fleet.customer.service;

import com.fleet.customer.dto.CustomerDocumentDTO;
import com.fleet.customer.model.Customer;
import com.fleet.customer.model.CustomerDocument;
import com.fleet.customer.repository.CustomerDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerDocumentService {

    private final CustomerDocumentRepository documentRepository;
    private final CustomerService customerService;

    public List<CustomerDocument> getDocuments(Long customerId) {
        return documentRepository.findByCustomerId(customerId);
    }

    public CustomerDocument addDocument(Long customerId, CustomerDocumentDTO dto) {
        Customer customer = customerService.getCustomerById(customerId);
        CustomerDocument doc = new CustomerDocument();
        doc.setCustomer(customer);
        doc.setDocumentType(dto.getDocumentType());
        doc.setFileName(dto.getFileName());
        doc.setFileUrl(dto.getFileUrl());
        return documentRepository.save(doc);
    }

    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }
}
