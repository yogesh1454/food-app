package com.teadelivery.ordercatalog.payment.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when wallet balance is insufficient
 */
public class InsufficientFundsException extends PaymentException {
    
    private final BigDecimal required;
    private final BigDecimal available;
    
    public InsufficientFundsException(BigDecimal required, BigDecimal available) {
        super("ERR_INSUFFICIENT_FUNDS", 
            String.format("Insufficient funds. Required: ₹%s, Available: ₹%s", required, available));
        this.required = required;
        this.available = available;
    }
    
    public BigDecimal getRequired() {
        return required;
    }
    
    public BigDecimal getAvailable() {
        return available;
    }
}
