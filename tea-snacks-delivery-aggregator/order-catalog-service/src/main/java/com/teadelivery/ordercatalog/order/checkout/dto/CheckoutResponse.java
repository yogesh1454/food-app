package com.teadelivery.ordercatalog.order.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for checkout calculation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    
    private String checkoutSessionId;
    private CheckoutStatus status;
    private LocalDateTime expiresAt;
    
    private VendorInfo vendor;
    private List<CheckoutItem> items;
    private PricingDetails pricing;
    private DeliveryEstimate deliveryEstimate;
    private ValidationResults validations;
    private List<CheckoutError> errors;
    
    /**
     * Checkout status
     */
    public enum CheckoutStatus {
        READY_FOR_COMMIT,
        IN_PROGRESS,        // Session locked for order creation
        VALIDATION_FAILED,
        COMMITTED,
        EXPIRED
    }
    
    /**
     * Vendor information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorInfo {
        private String vendorId;
        private String vendorName;
        private Long vendorBranchId;
        private String branchName;
        private Integer estimatedPrepTime;
        private Boolean isAcceptingOrders;
    }
    
    /**
     * Checkout item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutItem {
        private Long menuItemId;
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private Map<String, Object> customizations;
        private Boolean isAvailable;
        private Integer stockQuantity;
    }
    
    /**
     * Pricing details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingDetails {
        private BigDecimal itemTotal;
        private BigDecimal discount;
        private DiscountDetails discountDetails;
        private BigDecimal subtotalAfterDiscount;
        private BigDecimal deliveryCharges;
        private DeliveryDetails deliveryDetails;
        private BigDecimal platformFee;
        private BigDecimal gst;
        private GstDetails gstDetails;
        private BigDecimal totalAmount;
        private String currency;
    }
    
    /**
     * Discount details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountDetails {
        private String couponCode;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal maxDiscount;
        private BigDecimal appliedDiscount;
    }
    
    /**
     * Delivery details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryDetails {
        private Double distance;
        private String distanceUnit;
        private String deliveryZone;
        private BigDecimal baseFee;
        private BigDecimal distanceFee;
    }
    
    /**
     * GST details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstDetails {
        private BigDecimal cgst;
        private BigDecimal sgst;
        private Integer gstRate;
    }
    
    /**
     * Delivery estimate
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryEstimate {
        private LocalDateTime estimatedDeliveryTime;
        private Integer estimatedPrepTime;
        private Integer estimatedDeliveryDuration;
        private Integer totalEstimatedTime;
    }
    
    /**
     * Validation results
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResults {
        private Boolean allItemsAvailable;
        private Boolean deliveryAddressValid;
        private Boolean deliveryZoneServiceable;
        private Boolean vendorAcceptingOrders;
        private Boolean paymentMethodSupported;
    }
    
    /**
     * Checkout error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutError {
        private String code;
        private String message;
        private String field;
        private String severity;
        private Map<String, Object> metadata;
    }
}
