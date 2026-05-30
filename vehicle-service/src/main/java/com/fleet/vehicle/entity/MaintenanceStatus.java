package com.fleet.vehicle.entity;

public enum MaintenanceStatus {
    SCHEDULED,      // Planifié, pas encore commencé
    IN_PROGRESS,    // En cours
    COMPLETED,      // Terminé
    CANCELLED,      // Annulé
    OVERDUE         // En retard (date dépassée)
}