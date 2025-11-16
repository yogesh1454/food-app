package com.teadelivery.ordercatalog.payment.exception;

/**
 * Exception thrown when payment token is invalid
 */
public class InvalidPaymentTokenException extends PaymentException {
    
    public InvalidPaymentTokenException(String message) {
        super("ERR_INVALID_PAYMENT_TOKEN", message);
    }
    
    public InvalidPaymentTokenException(String message, Throwable cause) {
        super("ERR_INVALID_PAYMENT_TOKEN", message, cause);
    }
}
