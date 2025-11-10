package com.teadelivery.ordercatalog.order.fsm;

/**
 * Order FSM Triggers
 * Represents the 12 triggers that cause state transitions
 */
public enum OrderTrigger {
    VALIDATE_ORDER("Validate order details"),
    CONFIRM_PAYMENT("Confirm payment successful"),
    NOTIFY_RESTAURANT("Notify restaurant of new order"),
    ACCEPT_ORDER("Restaurant accepts the order"),
    REJECT_ORDER("Restaurant rejects the order"),
    TIMEOUT_ACCEPTANCE("Restaurant acceptance timeout"),
    START_PREPARATION("Start food preparation"),
    MARK_READY("Mark food ready for pickup"),
    ASSIGN_RIDER("Assign delivery rider"),
    RIDER_PICKUP("Rider picks up the order"),
    DELIVER_ORDER("Deliver order to customer"),
    CLOSE_ORDER("Close the order"),
    CANCEL_ORDER("Cancel the order");
    
    private final String description;
    
    OrderTrigger(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
