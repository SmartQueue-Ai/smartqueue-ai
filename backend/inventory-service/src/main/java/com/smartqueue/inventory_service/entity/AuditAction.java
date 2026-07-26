package com.smartqueue.inventory_service.entity;

public enum AuditAction {
    LOCK_REQUESTED,
    LOCK_ACQUIRED,
    LOCK_RELEASED,
    LOCK_EXPIRED,
    QUANTITY_MUTATED
}
