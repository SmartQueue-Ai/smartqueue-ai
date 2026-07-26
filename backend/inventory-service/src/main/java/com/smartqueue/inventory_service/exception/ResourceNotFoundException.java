package com.smartqueue.inventory_service.exception;

public class ResourceNotFoundException extends InventoryException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
