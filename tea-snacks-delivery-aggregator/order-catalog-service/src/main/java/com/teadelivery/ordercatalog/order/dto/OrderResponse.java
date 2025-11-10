package com.teadelivery.ordercatalog.order.dto;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.OrderType;
import com.teadelivery.ordercatalog.order.fsm.PaymentStatus;
import com.teadelivery.ordercatalog.order.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for order details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private UUID orderId;
    private UUID customerId;
    private OrderType orderType;
    private OrderState state;
    private PaymentStatus paymentStatus;
    
    private List<OrderItemResponse> items;
    
    private BigDecimal itemTotal;
    private BigDecimal deliveryCharges;
    private BigDecimal platformFee;
    private BigDecimal gst;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    
    private Map<String, Object> deliveryAddress;
    private String specialInstructions;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime deliveredAt;
    
    /**
     * Convert Order entity to OrderResponse DTO
     */
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .orderType(order.getOrderType())
            .state(order.getState())
            .paymentStatus(order.getPaymentStatus())
            .items(order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList()))
            .itemTotal(order.getItemTotal())
            .deliveryCharges(order.getDeliveryCharges())
            .platformFee(order.getPlatformFee())
            .gst(order.getGst())
            .discount(order.getDiscount())
            .totalAmount(order.getTotalAmount())
            .deliveryAddress(order.getDeliveryAddress())
            .specialInstructions(order.getSpecialInstructions())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .acceptedAt(order.getAcceptedAt())
            .deliveredAt(order.getDeliveredAt())
            .build();
    }
}
