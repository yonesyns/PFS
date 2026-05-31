package com.fleet.notification.dto;

import com.fleet.notification.entity.NotificationChannel;
import com.fleet.notification.entity.NotificationStatus;
import com.fleet.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID recipientId;
    private String recipientEmail;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String content;
    private NotificationStatus status;
    private Instant sentAt;
    private Instant createdAt;
}
