package com.teadelivery.ordercatalog.order.exception;

import java.util.UUID;

/**
 * Exception thrown when a duplicate order is detected
 */
public class DuplicateOrderException extends RuntimeException {
    
    private final UUID existingOrderId;
    private final String sessionId;
    
    public DuplicateOrderException(String message, UUID existingOrderId, String sessionId) {
        super(message);
        this.existingOrderId = existingOrderId;
        this.sessionId = sessionId;
    }
    
    public UUID getExistingOrderId() {
        return existingOrderId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
}
