package com.teadelivery.ordercatalog.fsm;

/**
 * Delivery FSM States
 * Represents the 9 states in the delivery lifecycle
 */
public enum DeliveryState {
    PENDING_ASSIGNMENT("Waiting for rider assignment"),
    ASSIGNED("Assigned to a delivery rider"),
    RIDER_ACCEPTED("Rider accepted the delivery"),
    RIDER_ARRIVED_AT_RESTAURANT("Rider arrived at restaurant"),
    PICKED_UP("Order picked up by rider"),
    IN_TRANSIT("Order in transit to customer"),
    ARRIVED_AT_CUSTOMER("Rider arrived at customer location"),
    DELIVERED("Order delivered to customer"),
    CANCELLED("Delivery cancelled");
    
    private final String description;
    
    DeliveryState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this is a terminal state
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
    
    /**
     * Check if delivery is in progress
     */
    public boolean isInProgress() {
        return !isTerminal() && this != PENDING_ASSIGNMENT;
    }
}
