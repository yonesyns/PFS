package com.fleet.notification.entity;

public enum NotificationStatus {
    PENDING,      // En attente d'envoi
    SENT,         // Envoyé avec succès
    FAILED,       // Échec d'envoi
    DELIVERED,    // Livré (SMS/email confirmé)
    READ          // Lu par le destinataire
}
