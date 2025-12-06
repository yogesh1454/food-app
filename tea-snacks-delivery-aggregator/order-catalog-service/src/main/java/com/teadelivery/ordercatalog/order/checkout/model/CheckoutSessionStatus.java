package com.teadelivery.ordercatalog.order.checkout.model;

/**
 * Checkout session status enum
 * Represents the lifecycle state of a checkout session
 */
public enum CheckoutSessionStatus {
    READY_FOR_COMMIT("Ready to Place Order"),
    IN_PROGRESS("Processing"),
    VALIDATION_FAILED("Validation Failed"),
    COMMITTED("Order Placed"),
    EXPIRED("Session Expired");

    private final String displayName;

    CheckoutSessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
