package com.teadelivery.ordercatalog.delivery.fsm;

/**
 * Delivery FSM States
 * Represents the 9 states in the delivery lifecycle as per BE-003-22
 */
public enum DeliveryState {
    PENDING("Delivery created, ready to find riders"),
    SEARCHING_RIDER("Actively searching for available riders"),
    RIDER_ASSIGNED("Rider selected, awaiting acceptance"),
    RIDER_ACCEPTED("Rider accepted, navigating to restaurant"),
    AT_RESTAURANT("Rider reached restaurant, picking up order"),
    PICKED_UP("Rider picked up order, ready to deliver"),
    OUT_FOR_DELIVERY("Rider en route to customer"),
    DELIVERED("Order successfully delivered"),
    FAILED("Delivery failed (terminal state)");
    
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
        return this == DELIVERED || this == FAILED;
    }
    
    /**
     * Check if delivery is in progress
     */
    public boolean isInProgress() {
        return !isTerminal() && this != PENDING;
    }
}
