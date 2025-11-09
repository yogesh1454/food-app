package com.teadelivery.ordercatalog.fsm;

/**
 * Payment Status
 * Represents the payment state of an order
 */
public enum PaymentStatus {
    PENDING("Payment pending"),
    AUTHORIZED("Payment authorized"),
    CAPTURED("Payment captured"),
    FAILED("Payment failed"),
    REFUNDED("Payment refunded"),
    PARTIALLY_REFUNDED("Payment partially refunded");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
