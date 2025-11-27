package com.teadelivery.ordercatalog.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create order from checkout session
 * This is the second step of the two-step checkout flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderFromCheckoutRequest {
    
    /**
     * Checkout session ID from step 1
     */
    @NotBlank(message = "Checkout session ID is required")
    private String checkoutSessionId;
    
    /**
     * Payment token for GPay transactions
     * Required only if payment method is GPay
     */
    private String paymentToken;
}
