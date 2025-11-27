package com.teadelivery.ordercatalog.order.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for committing a checkout session to create an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitCheckoutRequest {
    
    @NotBlank(message = "Checkout session ID is required")
    private String checkoutSessionId;
    
    private String paymentTransactionId;
    
    private String paymentMethod;
}
