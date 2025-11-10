package com.teadelivery.ordercatalog.order.status.model;

/**
 * Customer-Facing Status Enum
 * Simplifies 22 internal FSM states into 8 customer-friendly states
 * As per BE-004-27
 */
public enum CustomerStatus {
    
    ORDER_PLACED(
        "Your order has been placed",
        "We're confirming with the restaurant",
        0,
        true
    ),
    
    ORDER_CONFIRMED(
        "Restaurant is preparing your food",
        "Estimated prep time: 15-20 minutes",
        15,
        true
    ),
    
    PREPARING(
        "Your food is being prepared",
        "Almost ready for pickup",
        40,
        false
    ),
    
    RIDER_ASSIGNED(
        "Delivery partner assigned",
        "Heading to the restaurant",
        60,
        false
    ),
    
    READY_FOR_PICKUP(
        "Food is ready, waiting for pickup",
        "Delivery partner arriving soon",
        70,
        false
    ),
    
    OUT_FOR_DELIVERY(
        "Your order is on the way",
        "Track your delivery partner",
        85,
        false
    ),
    
    DELIVERED(
        "Your order has been delivered",
        "Enjoy your meal!",
        100,
        false
    ),
    
    CANCELLED(
        "Your order was cancelled",
        "Refund will be processed in 3-5 days",
        0,
        false
    );
    
    private final String primaryMessage;
    private final String secondaryMessage;
    private final int progressPercentage;
    private final boolean canCancel;
    
    CustomerStatus(String primaryMessage, String secondaryMessage, 
                   int progressPercentage, boolean canCancel) {
        this.primaryMessage = primaryMessage;
        this.secondaryMessage = secondaryMessage;
        this.progressPercentage = progressPercentage;
        this.canCancel = canCancel;
    }
    
    public String getPrimaryMessage() {
        return primaryMessage;
    }
    
    public String getSecondaryMessage() {
        return secondaryMessage;
    }
    
    public int getProgressPercentage() {
        return progressPercentage;
    }
    
    public boolean canCancel() {
        return canCancel;
    }
    
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
    
    public boolean isActive() {
        return !isTerminal();
    }
}
