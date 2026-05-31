package com.fleet.notification.entity;

public enum NotificationType {
    MAINTENANCE_DUE,        // Maintenance prévue prochainement
    MAINTENANCE_OVERDUE,    // Maintenance en retard
    MAINTENANCE_COMPLETED,  // Maintenance terminée
    VEHICLE_STATUS_CHANGED, // Changement de statut véhicule
    INSURANCE_EXPIRING,     // Assurance qui expire
    TECHNICAL_INSPECTION_DUE, // CT à renouveler
    CUSTOMER_VALIDATED,     // Client validé
    CUSTOMER_REJECTED,      // Client rejeté
    INVOICE_CREATED,        // Facture créée
    INVOICE_PAID,           // Facture payée
    INVOICE_OVERDUE,        // Facture impayée
    SUBSCRIPTION_RENEWAL,   // Renouvellement abonnement
    WELCOME,                // Email de bienvenue
    ALERT_CRITICAL,         // Alerte critique
    ALERT_WARNING,          // Alerte warning
    ALERT_INFO              // Alerte info
}
