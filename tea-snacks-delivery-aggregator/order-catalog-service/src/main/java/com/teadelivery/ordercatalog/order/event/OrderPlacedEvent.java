package com.teadelivery.ordercatalog.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a new order is placed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    
    private UUID orderId;
    private UUID customerId;
    private Long vendorBranchId;
    private String vendorName;
    private String branchName;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItemEvent> items;
    private DeliveryAddressEvent deliveryAddress;
    private LocalDateTime placedAt;
    private String checkoutSessionId;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long menuItemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal price;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressEvent {
        private String fullAddress;
        private Double latitude;
        private Double longitude;
    }
}
