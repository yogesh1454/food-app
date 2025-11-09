package com.teadelivery.ordercatalog.fsm;

/**
 * Order FSM States
 * Represents the 13 states in the order lifecycle
 */
public enum OrderState {
    CREATED("Order created, awaiting validation"),
    VALIDATED("Order validated, awaiting payment"),
    PAYMENT_CONFIRMED("Payment confirmed, notifying restaurant"),
    PENDING_ACCEPTANCE("Waiting for restaurant acceptance"),
    ACCEPTED("Restaurant accepted, preparing to start"),
    PREPARING("Food being prepared"),
    READY_FOR_PICKUP("Food ready, awaiting rider pickup"),
    ASSIGNED_TO_RIDER("Assigned to delivery rider"),
    PICKED_UP("Rider picked up the order"),
    DELIVERED("Order delivered to customer"),
    CLOSED("Order closed successfully"),
    CANCELLED("Order cancelled"),
    REJECTED("Order rejected by restaurant");
    
    private final String description;
    
    OrderState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if order can be cancelled in this state
     */
    public boolean isCancellable() {
        return this == CREATED ||
               this == VALIDATED ||
               this == PAYMENT_CONFIRMED ||
               this == PENDING_ACCEPTANCE ||
               this == ACCEPTED;
    }
    
    /**
     * Check if this is a terminal state
     */
    public boolean isTerminal() {
        return this == DELIVERED ||
               this == CLOSED ||
               this == CANCELLED ||
               this == REJECTED;
    }
    
    /**
     * Check if order is in progress
     */
    public boolean isInProgress() {
        return !isTerminal() && this != CREATED;
    }
}
