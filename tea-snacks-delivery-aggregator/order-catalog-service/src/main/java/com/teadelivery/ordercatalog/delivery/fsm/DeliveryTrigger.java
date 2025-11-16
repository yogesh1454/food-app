package com.teadelivery.ordercatalog.delivery.fsm;

/**
 * Delivery FSM Triggers
 * Represents the 9 triggers that cause state transitions in the delivery lifecycle
 * As per BE-003-22
 */
public enum DeliveryTrigger {
    FIND_RIDERS,           // PENDING → SEARCHING_RIDER
    ASSIGN_RIDER,          // SEARCHING_RIDER → RIDER_ASSIGNED
    RIDER_ACCEPT,          // RIDER_ASSIGNED → RIDER_ACCEPTED
    RIDER_REJECT,          // RIDER_ASSIGNED → SEARCHING_RIDER
    NO_RIDERS_AVAILABLE,   // SEARCHING_RIDER → FAILED
    REACH_RESTAURANT,      // RIDER_ACCEPTED → AT_RESTAURANT
    PICKUP_ORDER,          // AT_RESTAURANT → PICKED_UP
    START_DELIVERY,        // PICKED_UP → OUT_FOR_DELIVERY
    DELIVER_ORDER,         // OUT_FOR_DELIVERY → DELIVERED
    FAIL_DELIVERY          // Any state → FAILED
}
