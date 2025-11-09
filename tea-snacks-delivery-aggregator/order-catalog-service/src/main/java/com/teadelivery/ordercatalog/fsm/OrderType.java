package com.teadelivery.ordercatalog.fsm;

/**
 * Order Type
 * Indicates if order is from single or multiple restaurants
 */
public enum OrderType {
    SINGLE("Single restaurant order"),
    MULTI_RESTAURANT("Multi-restaurant order");
    
    private final String description;
    
    OrderType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
