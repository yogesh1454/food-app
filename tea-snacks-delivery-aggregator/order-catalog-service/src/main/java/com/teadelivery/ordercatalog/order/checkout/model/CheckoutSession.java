package com.teadelivery.ordercatalog.order.checkout.model;

import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutRequest;
import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutResponse;
import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Checkout session stored in Redis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String checkoutSessionId;
    private CheckoutResponse.CheckoutStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String orderId;  // Set after order creation
    
    // User and vendor info
    private UUID userId;
    private Long vendorBranchId;
    private String vendorId;
    private String vendorName;
    
    // Order details
    private List<CheckoutRequest.CartItemRequest> items;
    private DeliveryAddress deliveryAddress;
    private CheckoutRequest.GeoLocation deliveryLocation;
    
    // Pricing
    private CheckoutResponse.PricingDetails pricing;
    
    // Payment
    private String paymentMethod;
    
    // Delivery preferences
    private String scheduledDeliveryTime;
    private Boolean contactlessDelivery;
    private Boolean leaveAtDoor;
    private String deliveryInstructions;
    
    // Validation results
    private CheckoutResponse.ValidationResults validations;
    
    // Metadata
    private Map<String, Object> metadata;
}
