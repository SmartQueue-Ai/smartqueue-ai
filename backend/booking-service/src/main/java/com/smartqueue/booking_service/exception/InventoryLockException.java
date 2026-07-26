package com.smartqueue.booking_service.exception;

public class InventoryLockException extends BookingException {

    public InventoryLockException(String message) {
        super(message);
    }

    public InventoryLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
