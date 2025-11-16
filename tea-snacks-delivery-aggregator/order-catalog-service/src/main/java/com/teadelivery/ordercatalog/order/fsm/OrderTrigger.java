package com.teadelivery.ordercatalog.order.fsm;

/**
 * Order FSM Triggers
 * Represents the 14 triggers that cause state transitions
 */
public enum OrderTrigger {
    VALIDATE_ORDER("Validate order details"),
    VALIDATION_FAILED("Order validation failed"),
    CONFIRM_PAYMENT("Confirm payment successful"),
    PAYMENT_FAILED("Payment processing failed"),
    NOTIFY_VENDOR("Notify vendor of new order"),
    ACCEPT_ORDER("Vendor accepts the order"),
    REJECT_ORDER("Vendor rejects the order"),
    TIMEOUT_ACCEPTANCE("Vendor acceptance timeout"),
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
