package com.fleet.notification.controller;

import com.fleet.commons.dto.ApiResponse;
import com.fleet.commons.dto.PageResponse;
import com.fleet.notification.dto.NotificationRequest;
import com.fleet.notification.dto.NotificationResponse;
import com.fleet.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/api/notifications")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification sent successfully"));
    }

    @GetMapping("/api/notifications/recipient/{recipientId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByRecipient(
            @PathVariable UUID recipientId) {
        List<NotificationResponse> response = notificationService.getNotificationsByRecipient(recipientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/notifications/recipient/{recipientId}/paginated")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotificationsByRecipientPaginated(
            @PathVariable UUID recipientId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<NotificationResponse> response = notificationService.getNotificationsByRecipient(recipientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @GetMapping("/api/notifications/recipient/{recipientId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @PathVariable UUID recipientId) {
        long count = notificationService.getUnreadCount(recipientId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}