package com.teadelivery.ordercatalog.order.checkout.dto;

import com.teadelivery.ordercatalog.order.dto.OrderItemResponse;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.OrderType;
import com.teadelivery.ordercatalog.order.fsm.PaymentStatus;
import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import com.teadelivery.ordercatalog.order.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Comprehensive response DTO for checkout commit
 * Contains all details needed by UI to render order confirmation screen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitCheckoutResponse {

    // ========== Order Identification ==========
    private UUID orderId;
    private String orderNumber; // Human-readable order number
    private String checkoutSessionId;

    // ========== Order Status ==========
    private OrderType orderType;
    private OrderState state;
    private String stateDisplayName;
    private boolean isSuccess;
    private String message;

    // ========== Customer Info ==========
    private UUID customerId;

    // ========== Vendor Info ==========
    private VendorDetails vendor;

    // ========== Payment Info ==========
    private PaymentDetails payment;

    // ========== Order Items ==========
    private List<OrderItemResponse> items;
    private int totalItemCount;

    // ========== Pricing Breakdown ==========
    private PricingBreakdown pricing;

    // ========== Delivery Info ==========
    private DeliveryInfo delivery;

    // ========== Timestamps ==========
    private LocalDateTime orderPlacedAt;
    private LocalDateTime estimatedDeliveryTime;

    // ========== Nested DTOs ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorDetails {
        private Long vendorId;
        private Long vendorBranchId;
        private String vendorName;
        private String branchName;
        private String branchPhone;
        private String branchAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetails {
        private PaymentStatus status;
        private String statusDisplayName;
        private String method;
        private String methodDisplayName;
        private String transactionId;
        private BigDecimal amountPaid;
        private LocalDateTime paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingBreakdown {
        private BigDecimal itemTotal;
        private BigDecimal deliveryCharges;
        private BigDecimal platformFee;
        private BigDecimal gst;
        private BigDecimal discount;
        private BigDecimal totalAmount;
        private String currency;

        // Detailed breakdown for UI
        private String itemTotalLabel;
        private String deliveryLabel;
        private String taxesLabel;
        private String discountLabel;
        private String totalLabel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfo {
        private DeliveryAddress address;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String specialInstructions;
        private Integer estimatedPrepTimeMinutes;
        private Integer estimatedDeliveryDurationMinutes;
        private Integer totalEstimatedMinutes;
        private String deliveryTimeRange; // e.g., "30-45 mins"
    }

    /**
     * Create response from Order entity with vendor details
     */
    public static CommitCheckoutResponse from(Order order, String vendorName, String branchName) {
        // Calculate total items
        int totalItems = order.getOrderItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();

        // Generate human-readable order number
        String orderNumber = "ORD-" + order.getOrderId().toString().substring(0, 8).toUpperCase();

        // Estimate delivery (prep 25min + delivery 20min)
        int prepTime = 25;
        int deliveryTime = 20;
        int totalTime = prepTime + deliveryTime;

        return CommitCheckoutResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(orderNumber)
                .checkoutSessionId(order.getCheckoutSessionId())
                .orderType(order.getOrderType())
                .state(order.getState())
                .stateDisplayName(getStateDisplayName(order.getState()))
                .isSuccess(true)
                .message("Order placed successfully!")
                .customerId(order.getCustomerId())
                .vendor(VendorDetails.builder()
                        .vendorId(order.getVendorId())
                        .vendorBranchId(order.getVendorBranchId())
                        .vendorName(vendorName)
                        .branchName(branchName)
                        .build())
                .payment(PaymentDetails.builder()
                        .status(order.getPaymentStatus())
                        .statusDisplayName(getPaymentStatusDisplayName(order.getPaymentStatus()))
                        .method(order.getPaymentMethod())
                        .methodDisplayName(getPaymentMethodDisplayName(order.getPaymentMethod()))
                        .transactionId(order.getPaymentTransactionId())
                        .amountPaid(order.getTotalAmount())
                        .paidAt(order.getPaymentConfirmedAt())
                        .build())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .totalItemCount(totalItems)
                .pricing(PricingBreakdown.builder()
                        .itemTotal(order.getItemTotal())
                        .deliveryCharges(order.getDeliveryCharges())
                        .platformFee(order.getPlatformFee())
                        .gst(order.getGst())
                        .discount(order.getDiscount())
                        .totalAmount(order.getTotalAmount())
                        .currency("INR")
                        .itemTotalLabel("Item Total")
                        .deliveryLabel("Delivery Charges")
                        .taxesLabel("Taxes & Fees")
                        .discountLabel("Discount")
                        .totalLabel("Total Amount")
                        .build())
                .delivery(DeliveryInfo.builder()
                        .address(order.getDeliveryAddress())
                        .latitude(order.getDeliveryLatitude())
                        .longitude(order.getDeliveryLongitude())
                        .specialInstructions(order.getSpecialInstructions())
                        .estimatedPrepTimeMinutes(prepTime)
                        .estimatedDeliveryDurationMinutes(deliveryTime)
                        .totalEstimatedMinutes(totalTime)
                        .deliveryTimeRange(totalTime + "-" + (totalTime + 15) + " mins")
                        .build())
                .orderPlacedAt(order.getCreatedAt())
                .estimatedDeliveryTime(order.getCreatedAt().plusMinutes(totalTime))
                .build();
    }

    private static String getStateDisplayName(OrderState state) {
        if (state == null)
            return "Unknown";
        return switch (state) {
            case CREATED -> "Order Placed";
            case VALIDATED -> "Order Confirmed";
            case PAYMENT_CONFIRMED -> "Payment Received";
            case PENDING_ACCEPTANCE -> "Waiting for Restaurant";
            case ACCEPTED -> "Restaurant Accepted";
            case PREPARING -> "Being Prepared";
            case READY_FOR_PICKUP -> "Ready for Pickup";
            case ASSIGNED_TO_RIDER -> "Rider Assigned";
            case PICKED_UP -> "Out for Delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case REJECTED -> "Rejected";
            default -> state.name();
        };
    }

    private static String getPaymentStatusDisplayName(PaymentStatus status) {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case PENDING -> "Pending";
            case AUTHORIZED -> "Authorized";
            case CAPTURED -> "Paid";
            case FAILED -> "Failed";
            case REFUNDED -> "Refunded";
            case PARTIALLY_REFUNDED -> "Partially Refunded";
            default -> status.name();
        };
    }

    private static String getPaymentMethodDisplayName(String method) {
        if (method == null)
            return "Unknown";
        return switch (method.toUpperCase()) {
            case "UPI" -> "UPI";
            case "CARD" -> "Credit/Debit Card";
            case "WALLET" -> "Wallet";
            case "COD" -> "Cash on Delivery";
            case "NETBANKING" -> "Net Banking";
            default -> method;
        };
    }
}
