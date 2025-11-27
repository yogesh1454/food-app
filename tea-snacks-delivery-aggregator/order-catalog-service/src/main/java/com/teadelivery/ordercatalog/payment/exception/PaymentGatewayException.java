package com.teadelivery.ordercatalog.payment.exception;

/**
 * Exception thrown when payment gateway fails
 */
public class PaymentGatewayException extends PaymentException {
    
    public PaymentGatewayException(String message) {
        super("ERR_PAYMENT_GATEWAY_FAILURE", message);
    }
    
    public PaymentGatewayException(String message, Throwable cause) {
        super("ERR_PAYMENT_GATEWAY_FAILURE", message, cause);
    }
}
