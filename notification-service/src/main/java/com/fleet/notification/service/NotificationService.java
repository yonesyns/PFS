package com.fleet.notification.service;

import com.fleet.commons.dto.PageResponse;
import com.fleet.notification.dto.NotificationRequest;
import com.fleet.notification.dto.NotificationResponse;
import com.fleet.notification.entity.Notification;
import com.fleet.notification.entity.NotificationChannel;
import com.fleet.notification.entity.NotificationStatus;
import com.fleet.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final ObjectMapper objectMapper;

    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending notification to recipient: {} via {}", request.getRecipientId(), request.getChannel());

        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .type(request.getType())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .content(request.getContent())
                .templateName(request.getTemplateName())
                .templateData(serializeTemplateData(request.getTemplateData()))
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Envoi immédiat
        processNotification(saved);

        return toResponse(saved);
    }

    public void processNotification(Notification notification) {
        try {
            switch (notification.getChannel()) {
                case EMAIL -> {
                    if (notification.getTemplateName() != null) {
                        emailService.sendHtmlEmail(notification);
                    } else {
                        emailService.sendSimpleEmail(notification);
                    }
                }
                case SMS -> smsService.sendSms(notification);
                case PUSH, IN_APP -> log.info("{} notification stored for recipient: {}",
                        notification.getChannel(), notification.getRecipientId());
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);
            log.info("Notification {} sent successfully via {}", notification.getId(), notification.getChannel());

        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }

    public void retryFailedNotifications() {
        log.info("Retrying failed notifications...");
        List<Notification> failed = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 3);

        for (Notification notification : failed) {
            log.info("Retrying notification: {} (attempt {})",
                    notification.getId(), notification.getRetryCount() + 1);
            notification.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(notification);
            processNotification(notification);
        }
    }

    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
    }

    // === QUERIES ===

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotificationsByRecipient(UUID recipientId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByRecipientId(recipientId, pageable);
        List<NotificationResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndStatus(recipientId, NotificationStatus.SENT);
    }

    // === PRIVATE ===

    private String serializeTemplateData(Map<String, Object> data) {
        if (data == null) return null;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize template data", e);
            return null;
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .recipientEmail(notification.getRecipientEmail())
                .type(notification.getType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
