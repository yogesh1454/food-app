package com.teadelivery.ordercatalog.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSessionStatus;
import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unified Response DTO for order operations across checkout and order domains
 * Works for /calculate (pre-order), /commit (post-order), and /orders (order
 * retrieval)
 * Uses @JsonInclude to exclude null fields for cleaner responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailsResponse {

    // ========== Session Info (always present) ==========
    private String checkoutSessionId;
    private CheckoutSessionStatus status;
    private String statusDisplayName;
    private LocalDateTime expiresAt;

    // ========== Order Info (only after commit) ==========
    private UUID orderId;
    private String orderNumber; // Human-readable order number like "ORD-0E5709AE"
    private String orderState; // FSM state: PENDING_ACCEPTANCE, ACCEPTED, PREPARING, etc.
    private String orderStateDisplayName; // Human-readable: "Waiting for Restaurant"
    private LocalDateTime orderPlacedAt;
    private Boolean isSuccess;
    private String message;

    // ========== Customer Info ==========
    private UUID customerId;

    // ========== Vendor Info (always present) ==========
    private VendorInfo vendor;

    // ========== Items (always present) ==========
    private List<CheckoutItem> items;
    private Integer totalItemCount;

    // ========== Pricing (always present) ==========
    private PricingDetails pricing;

    // ========== Delivery Info (always present) ==========
    private DeliveryInfo delivery;

    // ========== Payment Info (populated after commit) ==========
    private PaymentInfo payment;

    // ========== Validations (only for calculate) ==========
    private ValidationResults validations;

    // ========== Errors (only when validation fails) ==========
    private List<CheckoutError> errors;

    /**
     * Vendor information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VendorInfo {
        private Long vendorId;
        private String vendorName;
        private Long vendorBranchId;
        private String branchName;
        private String branchPhone;
        private String branchAddress;
        private Integer estimatedPrepTime;
        private Boolean isAcceptingOrders;
    }

    /**
     * Checkout item - used for both calculate and commit
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CheckoutItem {
        private UUID orderItemId; // Only after commit
        private Long menuItemId;
        private String name;
        private String description;
        private String imageUrl;
        private String categoryName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private Map<String, Object> customizations;
        private String specialInstructions;
        private Boolean isAvailable; // Only for calculate
        private Integer stockQuantity; // Only for calculate
    }

    /**
     * Pricing details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PricingDetails {
        private BigDecimal itemTotal;
        private BigDecimal discount;
        private DiscountDetails discountDetails;
        private BigDecimal subtotalAfterDiscount;
        private BigDecimal deliveryCharges;
        private DeliveryChargeDetails deliveryDetails;
        private BigDecimal platformFee;
        private BigDecimal gst;
        private GstDetails gstDetails;
        private BigDecimal totalAmount;
        private String currency;

        // Labels for UI rendering
        private String itemTotalLabel;
        private String deliveryLabel;
        private String taxesLabel;
        private String discountLabel;
        private String totalLabel;
    }

    /**
     * Discount details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DiscountDetails {
        private String couponCode;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal maxDiscount;
        private BigDecimal appliedDiscount;
    }

    /**
     * Delivery charge details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryChargeDetails {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GstDetails {
        private BigDecimal cgst;
        private BigDecimal sgst;
        private Integer gstRate;
    }

    /**
     * Delivery information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryInfo {
        private DeliveryAddress address;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String specialInstructions;
        private LocalDateTime estimatedDeliveryTime;
        private Integer estimatedPrepTime;
        private Integer estimatedDeliveryDuration;
        private Integer totalEstimatedTime;
        private String deliveryTimeRange; // e.g., "45-60 mins"
    }

    /**
     * Payment information (populated after commit)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentInfo {
        private String status;
        private String statusDisplayName;
        private String method;
        private String methodDisplayName;
        private String transactionId;
        private BigDecimal amountPaid;
        private LocalDateTime paidAt;
    }

    /**
     * Validation results (only for calculate)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CheckoutError {
        private String code;
        private String message;
        private String field;
        private String severity;
        private Map<String, Object> metadata;
    }

    // ========== Legacy getters for backward compatibility ==========

    /**
     * Legacy getter for delivery estimate (maps to new structure)
     */
    public DeliveryEstimate getDeliveryEstimate() {
        if (delivery == null)
            return null;
        return DeliveryEstimate.builder()
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .estimatedPrepTime(delivery.getEstimatedPrepTime())
                .estimatedDeliveryDuration(delivery.getEstimatedDeliveryDuration())
                .totalEstimatedTime(delivery.getTotalEstimatedTime())
                .build();
    }

    /**
     * Legacy class for backward compatibility
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryEstimate {
        private LocalDateTime estimatedDeliveryTime;
        private Integer estimatedPrepTime;
        private Integer estimatedDeliveryDuration;
        private Integer totalEstimatedTime;
    }

    // Backward compatibility: map old DeliveryDetails to new DeliveryChargeDetails
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryDetails {
        private Double distance;
        private String distanceUnit;
        private String deliveryZone;
        private BigDecimal baseFee;
        private BigDecimal distanceFee;
    }
}
