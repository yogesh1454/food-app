package com.teadelivery.ordercatalog.order.checkout.exception;

public class CheckoutSessionNotFoundException extends RuntimeException {
    public CheckoutSessionNotFoundException(String message) {
        super(message);
    }
}

