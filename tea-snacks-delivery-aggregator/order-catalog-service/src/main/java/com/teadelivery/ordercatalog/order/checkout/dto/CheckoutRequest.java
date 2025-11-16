package com.teadelivery.ordercatalog.order.checkout.dto;

import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for checkout calculation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Vendor branch ID is required")
    private Long vendorBranchId;
    
    @NotNull(message = "Delivery address is required")
    @Valid
    private DeliveryAddress deliveryAddress;
    
    @Valid
    private GeoLocation deliveryLocation;
    
    @NotEmpty(message = "Cart items cannot be empty")
    @Valid
    private List<CartItemRequest> items;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod;
    
    private String couponCode;
    
    private String scheduledDeliveryTime;
    
    private Boolean contactlessDelivery;
    
    private Boolean leaveAtDoor;
    
    private String deliveryInstructions;
    
    /**
     * Cart item request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemRequest {
        
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        private Map<String, Object> customizations;
        
        private String specialInstructions;
    }
    
    /**
     * Geo location
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoLocation {
        
        @NotNull(message = "Latitude is required")
        private BigDecimal latitude;
        
        @NotNull(message = "Longitude is required")
        private BigDecimal longitude;
    }
}
