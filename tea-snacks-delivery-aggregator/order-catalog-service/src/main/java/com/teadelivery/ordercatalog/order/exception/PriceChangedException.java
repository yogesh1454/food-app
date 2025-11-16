package com.teadelivery.ordercatalog.order.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when price has changed significantly since checkout
 */
public class PriceChangedException extends RuntimeException {
    
    private final BigDecimal expectedPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal difference;
    
    public PriceChangedException(String message, BigDecimal expectedPrice, BigDecimal currentPrice) {
        super(message);
        this.expectedPrice = expectedPrice;
        this.currentPrice = currentPrice;
        this.difference = currentPrice.subtract(expectedPrice).abs();
    }
    
    public BigDecimal getExpectedPrice() {
        return expectedPrice;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public BigDecimal getDifference() {
        return difference;
    }
}
