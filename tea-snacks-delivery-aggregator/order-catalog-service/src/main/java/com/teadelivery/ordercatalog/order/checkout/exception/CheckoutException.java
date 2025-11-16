package com.teadelivery.ordercatalog.order.checkout.exception;

/**
 * Base exception for checkout-related errors
 */
public class CheckoutException extends RuntimeException {
    
    private final String errorCode;
    
    public CheckoutException(String message) {
        super(message);
        this.errorCode = "CHECKOUT_ERROR";
    }
    
    public CheckoutException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public CheckoutException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CHECKOUT_ERROR";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
