package com.teadelivery.ordercatalog.fsm;

/**
 * SubOrder FSM States
 * Represents the states in the sub-order lifecycle (restaurant-specific)
 */
public enum SubOrderState {
    PENDING_ACCEPTANCE("Waiting for restaurant acceptance"),
    ACCEPTED("Restaurant accepted the sub-order"),
    PREPARING("Food being prepared"),
    READY_FOR_PICKUP("Food ready for rider pickup"),
    CANCELLED("Sub-order cancelled"),
    REJECTED("Sub-order rejected by restaurant");
    
    private final String description;
    
    SubOrderState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this is a terminal state
     */
    public boolean isTerminal() {
        return this == READY_FOR_PICKUP ||
               this == CANCELLED ||
               this == REJECTED;
    }
}
