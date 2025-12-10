package com.teadelivery.ordercatalog.order.checkout.exception;

import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class CheckoutValidationException extends RuntimeException {
    private final List<OrderDetailsResponse.CheckoutError> errors;

    public CheckoutValidationException(String message, List<OrderDetailsResponse.CheckoutError> errors) {
        super(message);
        this.errors = errors;
    }
}
