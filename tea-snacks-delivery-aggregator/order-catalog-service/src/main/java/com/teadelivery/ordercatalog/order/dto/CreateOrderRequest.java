package com.teadelivery.ordercatalog.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new order
 * Contains all information needed when customer clicks "Place Order"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    // ========== Vendor Information ==========
    
    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;
    
    @NotBlank(message = "Vendor name is required")
    private String vendorName;
    
    @NotNull(message = "Vendor branch ID is required")
    private Long vendorBranchId; // Specific branch from which order is placed
    
    // ========== Order Items ==========
    
    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    // ========== Delivery Information ==========
    
    @NotNull(message = "Delivery address is required")
    @Valid
    private DeliveryAddressRequest deliveryAddress;
    
    @NotNull(message = "Delivery location coordinates are required")
    @Valid
    private LocationRequest deliveryLocation;
    
    private String deliveryInstructions;
    
    // ========== Order Preferences ==========
    
    @Size(max = 500, message = "Special instructions must be less than 500 characters")
    private String specialInstructions;
    
    private Boolean contactlessDelivery;
    
    private Boolean leaveAtDoor;
    
    // ========== Payment Information ==========
    
    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentRequest payment;
    
    // ========== Pricing Information (from cart) ==========
    
    @NotNull(message = "Pricing details are required")
    @Valid
    private PricingRequest pricing;
    
    // ========== Additional Metadata ==========
    
    private String deviceId;
    
    private String appVersion;
    
    private String platform; // ANDROID, IOS, WEB
    
    private Map<String, Object> metadata; // For any additional custom fields
    
    // ========== Nested DTOs ==========
    
    /**
     * Payment details for the order
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequest {
        
        @NotBlank(message = "Payment method is required")
        @Pattern(regexp = "CARD|UPI|WALLET|COD", message = "Invalid payment method")
        private String method; // CARD, UPI, WALLET, COD
        
        private String instrumentId; // Card/UPI ID for saved instruments
        
        private String transactionId; // For tracking payment gateway transactions
        
        private Map<String, Object> metadata; // Additional payment-specific data
    }
    
    /**
     * Pricing breakdown for the order
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingRequest {
        
        @NotNull(message = "Item total is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Item total must be greater than 0")
        private BigDecimal itemTotal;
        
        @NotNull(message = "Delivery charges are required")
        @DecimalMin(value = "0.0", message = "Delivery charges must be non-negative")
        private BigDecimal deliveryCharges;
        
        @DecimalMin(value = "0.0", message = "Platform fee must be non-negative")
        private BigDecimal platformFee;
        
        @DecimalMin(value = "0.0", message = "GST must be non-negative")
        private BigDecimal gst;
        
        @DecimalMin(value = "0.0", message = "Discount must be non-negative")
        private BigDecimal discount;
        
        private String couponCode;
        
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
        private BigDecimal totalAmount;
    }
    
    /**
     * Delivery address details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressRequest {
        
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;
        
        private String addressLine2;
        
        @NotBlank(message = "Landmark is required")
        private String landmark;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "\\d{6}", message = "Invalid pincode")
        private String pincode;
        
        @NotBlank(message = "Address type is required")
        @Pattern(regexp = "HOME|WORK|OTHER", message = "Invalid address type")
        private String addressType;
        
        private String label; // Custom label like "Home", "Office", "Mom's House"
    }
    
    /**
     * GPS location coordinates
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationRequest {
        
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Invalid latitude")
        @DecimalMax(value = "90.0", message = "Invalid latitude")
        private Double latitude;
        
        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Invalid longitude")
        @DecimalMax(value = "180.0", message = "Invalid longitude")
        private Double longitude;
    }
}
