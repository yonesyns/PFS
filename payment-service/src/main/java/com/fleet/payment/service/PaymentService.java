package com.fleet.payment.service;

import com.fleet.payment.dto.PaymentDTO;
import com.fleet.payment.model.Payment;
import com.fleet.payment.model.PaymentStatus;
import com.fleet.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public PaymentDTO processPayment(PaymentDTO paymentDTO) {
        Payment payment = mapToEntity(paymentDTO);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionId(UUID.randomUUID().toString());
        
        Payment saved = paymentRepository.save(payment);
        
        // Simulate payment processing
        boolean success = simulatePaymentProcessing();
        
        if (success) {
            saved.setStatus(PaymentStatus.COMPLETED);
            saved.setProcessedAt(LocalDateTime.now());
            kafkaTemplate.send("payment-events", "payment.completed", saved);
        } else {
            saved.setStatus(PaymentStatus.FAILED);
            kafkaTemplate.send("payment-events", "payment.failed", saved);
        }
        
        Payment processed = paymentRepository.save(saved);
        return mapToDTO(processed);
    }
    
    public PaymentDTO getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToDTO(payment);
    }
    
    public List<PaymentDTO> getPaymentsByCustomer(Long customerId) {
        return paymentRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public List<PaymentDTO> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    private boolean simulatePaymentProcessing() {
        // Simulate 90% success rate
        return Math.random() > 0.1;
    }
    
    private PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setCustomerId(payment.getCustomerId());
        dto.setBookingId(payment.getBookingId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setMethod(payment.getMethod());
        dto.setTransactionId(payment.getTransactionId());
        dto.setDescription(payment.getDescription());
        return dto;
    }
    
    private Payment mapToEntity(PaymentDTO dto) {
        Payment payment = new Payment();
        payment.setCustomerId(dto.getCustomerId());
        payment.setBookingId(dto.getBookingId());
        payment.setAmount(dto.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setDescription(dto.getDescription());
        return payment;
    }
}