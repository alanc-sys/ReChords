package com.misacordes.application.entities;

public enum DeletionStatus {
    PENDING,   // Solicitud pendiente de revisión
    APPROVED,  // Admin aprobó la eliminación (canción será eliminada)
    REJECTED   // Admin rechazó la eliminación
}


