package com.fleet.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.notification.dto.NotificationRequest;
import com.fleet.notification.entity.NotificationChannel;
import com.fleet.notification.entity.NotificationType;
import com.fleet.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${fleet.rabbitmq.queue.vehicle:vehicle.events}")
    public void handleVehicleEvent(Message message) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received vehicle event JSON: {}", json.substring(0, Math.min(200, json.length())));

        try {
            Map<String, Object> event = objectMapper.readValue(json, Map.class);
            String eventType = (String) event.get("eventType");
            if (eventType == null) return;

            switch (eventType) {
                case "VehicleStatusChangedEvent" -> handleVehicleStatusChanged(event);
                case "MaintenanceScheduledEvent" -> handleMaintenanceScheduled(event);
                case "MaintenanceOverdueEvent" -> handleMaintenanceOverdue(event);
                case "MaintenanceCompletedEvent" -> handleMaintenanceCompleted(event);
                case "MaintenanceStartedEvent" -> handleMaintenanceStarted(event);
                case "MaintenanceCancelledEvent" -> handleMaintenanceCancelled(event);
            }
        } catch (Exception e) {
            log.error("Failed to parse vehicle event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "${fleet.rabbitmq.queue.customer:customer.events}")
    public void handleCustomerEvent(Message message) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received customer event JSON: {}", json.substring(0, Math.min(200, json.length())));

        try {
            Map<String, Object> event = objectMapper.readValue(json, Map.class);
            String eventType = (String) event.get("eventType");
            if (eventType == null) return;

            if ("CustomerValidatedEvent".equals(eventType)) {
                handleCustomerValidated(event);
            } else if ("CustomerCreatedEvent".equals(eventType)) {
                handleCustomerCreated(event);
            }
        } catch (Exception e) {
            log.error("Failed to parse customer event: {}", e.getMessage());
        }
    }

    // === HANDLERS VEHICLE ===

    private void handleVehicleStatusChanged(Map<String, Object> event) {
        String newStatus = (String) event.get("newStatus");
        String plateNumber = (String) event.get("plateNumber");
        UUID customerId = getUUID(event.get("customerId"));

        if ("MAINTENANCE".equals(newStatus)) {
            sendNotification(customerId, null, null, NotificationType.VEHICLE_STATUS_CHANGED,
                    "Véhicule en maintenance",
                    "Le véhicule " + plateNumber + " est passé en maintenance.",
                    null);
        } else if ("ACTIVE".equals(newStatus)) {
            sendNotification(customerId, null, null, NotificationType.VEHICLE_STATUS_CHANGED,
                    "Véhicule disponible",
                    "Le véhicule " + plateNumber + " est de nouveau disponible.",
                    null);
        }
    }

    private void handleMaintenanceScheduled(Map<String, Object> event) {
        UUID customerId = getUUID(event.get("customerId"));
        String plateNumber = (String) event.get("plateNumber");
        String maintenanceType = (String) event.get("maintenanceType");
        String scheduledDate = (String) event.get("scheduledDate");

        sendNotification(customerId, null, null, NotificationType.MAINTENANCE_DUE,
                "🔧 Maintenance planifiée - " + plateNumber,
                "Une maintenance " + maintenanceType + " est planifiée le " + scheduledDate +
                        " pour le véhicule " + plateNumber + ".",
                "maintenance-alert");
    }

    private void handleMaintenanceStarted(Map<String, Object> event) {
        UUID customerId = getUUID(event.get("customerId"));
        String plateNumber = (String) event.get("plateNumber");
        String maintenanceType = (String) event.get("maintenanceType");

        sendNotification(customerId, null, null, NotificationType.MAINTENANCE_DUE,
                "🔧 Maintenance démarrée - " + plateNumber,
                "La maintenance " + maintenanceType + " du véhicule " + plateNumber + " a démarré.",
                null);
    }

    private void handleMaintenanceOverdue(Map<String, Object> event) {
        UUID customerId = getUUID(event.get("customerId"));
        String plateNumber = (String) event.get("plateNumber");
        String maintenanceType = (String) event.get("maintenanceType");
        String scheduledDate = (String) event.get("scheduledDate");
        Number overdueDays = (Number) event.get("overdueDays");

        sendNotification(customerId, null, null, NotificationType.MAINTENANCE_OVERDUE,
                "⚠️ Maintenance EN RETARD - " + plateNumber,
                "La maintenance " + maintenanceType + " prévue le " + scheduledDate +
                        " pour le véhicule " + plateNumber + " est EN RETARD de " +
                        (overdueDays != null ? overdueDays.intValue() : "?") + " jours !",
                "maintenance-overdue");
    }

    private void handleMaintenanceCompleted(Map<String, Object> event) {
        UUID customerId = getUUID(event.get("customerId"));
        String plateNumber = (String) event.get("plateNumber");
        String maintenanceType = (String) event.get("maintenanceType");
        Number actualCost = (Number) event.get("actualCost");

        String costStr = actualCost != null ? String.format("%.2f", actualCost.doubleValue() / 100) + " €" : "N/A";

        sendNotification(customerId, null, null, NotificationType.MAINTENANCE_COMPLETED,
                "✅ Maintenance terminée - " + plateNumber,
                "La maintenance " + maintenanceType + " du véhicule " + plateNumber +
                        " est terminée. Coût: " + costStr,
                null);
    }

    private void handleMaintenanceCancelled(Map<String, Object> event) {
        UUID customerId = getUUID(event.get("customerId"));
        String plateNumber = (String) event.get("plateNumber");
        String maintenanceType = (String) event.get("maintenanceType");
        String reason = (String) event.get("cancellationReason");

        sendNotification(customerId, null, null, NotificationType.MAINTENANCE_DUE,
                "❌ Maintenance annulée - " + plateNumber,
                "La maintenance " + maintenanceType + " du véhicule " + plateNumber +
                        " a été annulée. Raison: " + (reason != null ? reason : "Non spécifiée"),
                null);
    }

    // === HANDLERS CUSTOMER ===

    private void handleCustomerValidated(Map<String, Object> event) {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        if (payload == null) {
            log.warn("CustomerValidatedEvent without payload");
            return;
        }

        UUID customerId = getUUID(payload.get("customerId"));
        String companyName = (String) payload.get("companyName");
        String email = (String) payload.get("email");

        if (email == null) {
            log.warn("No email found for customer {}, skipping notification", customerId);
            return;
        }

        sendNotification(customerId, email, null, NotificationType.CUSTOMER_VALIDATED,
                "🎉 Bienvenue sur Fleet Management !",
                "Votre compte " + (companyName != null ? companyName : "") + " a été validé. Vous pouvez maintenant gérer votre flotte de véhicules.",
                "welcome");
    }

    private void handleCustomerCreated(Map<String, Object> event) {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        if (payload == null) return;

        UUID customerId = getUUID(payload.get("customerId"));
        String email = (String) payload.get("email");
        String companyName = (String) payload.get("companyName");

        if (email != null) {
            sendNotification(customerId, email, null, NotificationType.WELCOME,
                    "Bienvenue " + (companyName != null ? companyName : "") + " !",
                    "Votre inscription a été enregistrée. Elle est en attente de validation par un administrateur.",
                    null);
        }
    }

    // === HELPER ===

    private void sendNotification(UUID recipientId, String email, String phone,
                                  NotificationType type, String subject, String content, String templateName) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(recipientId)
                    .recipientEmail(email)
                    .recipientPhone(phone)
                    .type(type)
                    .channel(NotificationChannel.EMAIL)
                    .subject(subject)
                    .content(content)
                    .templateName(templateName)
                    .build();

            notificationService.sendNotification(request);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    private UUID getUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        return UUID.fromString(value.toString());
    }
}