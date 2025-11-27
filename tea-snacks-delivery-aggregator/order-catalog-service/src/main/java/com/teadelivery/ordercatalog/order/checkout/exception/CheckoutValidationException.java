package com.teadelivery.ordercatalog.order.checkout.exception;

import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class CheckoutValidationException extends RuntimeException {
    private final List<CheckoutResponse.CheckoutError> errors;
    
    public CheckoutValidationException(String message, List<CheckoutResponse.CheckoutError> errors) {
        super(message);
        this.errors = errors;
    }
}

