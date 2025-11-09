package com.teadelivery.ordercatalog.order.model;

import com.teadelivery.ordercatalog.fsm.OrderState;
import com.teadelivery.ordercatalog.fsm.OrderType;
import com.teadelivery.ordercatalog.fsm.PaymentStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Order Entity with FSM Support
 * Represents a customer order with complete state machine lifecycle
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
    @Index(name = "idx_orders_state", columnList = "state"),
    @Index(name = "idx_orders_order_type", columnList = "order_type"),
    @Index(name = "idx_orders_parent_order_id", columnList = "parent_order_id"),
    @Index(name = "idx_orders_created_at", columnList = "created_at DESC"),
    @Index(name = "idx_orders_payment_status", columnList = "payment_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {
    
    // ========== Primary Key ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;
    
    // ========== Order Type & Hierarchy ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType = OrderType.SINGLE;
    
    @Column(name = "parent_order_id")
    private UUID parentOrderId;
    
    // ========== Customer Info ==========
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    // ========== FSM State ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private OrderState state = OrderState.CREATED;
    
    // ========== Pricing ==========
    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;
    
    @Column(name = "delivery_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharges;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;
    
    @Column(name = "gst", nullable = false, precision = 10, scale = 2)
    private BigDecimal gst = BigDecimal.ZERO;
    
    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    // ========== Payment ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 32)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_method", length = 32)
    private String paymentMethod;
    
    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;
    
    // ========== Delivery Address ==========
    @Type(JsonBinaryType.class)
    @Column(name = "delivery_address", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> deliveryAddress;
    
    @Column(name = "delivery_latitude", precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;
    
    @Column(name = "delivery_longitude", precision = 11, scale = 8)
    private BigDecimal deliveryLongitude;
    
    // ========== Special Instructions ==========
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    // ========== Timestamps ==========
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
    
    @Column(name = "payment_confirmed_at")
    private LocalDateTime paymentConfirmedAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "preparing_started_at")
    private LocalDateTime preparingStartedAt;
    
    @Column(name = "ready_at")
    private LocalDateTime readyAt;
    
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // ========== Estimated Times ==========
    @Column(name = "estimated_prep_time_minutes")
    private Integer estimatedPrepTimeMinutes;
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    // ========== Cancellation ==========
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;
    
    // ========== Metadata ==========
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    // ========== Relationships ==========
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // ========== Helper Methods ==========
    
    /**
     * Add order item to the order
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    /**
     * Calculate item total from order items
     */
    public void calculateItemTotal() {
        this.itemTotal = orderItems.stream()
            .map(item -> item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total amount including all charges
     */
    public void calculateTotalAmount() {
        this.totalAmount = itemTotal
            .add(deliveryCharges)
            .add(platformFee)
            .add(gst)
            .subtract(discount != null ? discount : BigDecimal.ZERO);
    }
    
    /**
     * Update state timestamp based on new state
     */
    public void updateStateTimestamp(OrderState newState) {
        LocalDateTime now = LocalDateTime.now();
        switch (newState) {
            case VALIDATED -> this.validatedAt = now;
            case PAYMENT_CONFIRMED -> this.paymentConfirmedAt = now;
            case ACCEPTED -> this.acceptedAt = now;
            case PREPARING -> this.preparingStartedAt = now;
            case READY_FOR_PICKUP -> this.readyAt = now;
            case PICKED_UP -> this.pickedUpAt = now;
            case DELIVERED -> this.deliveredAt = now;
            case CANCELLED -> this.cancelledAt = now;
        }
    }
    
    /**
     * Check if order is in a terminal state
     */
    public boolean isTerminal() {
        return state != null && state.isTerminal();
    }
    
    /**
     * Check if order can be cancelled
     */
    public boolean isCancellable() {
        return state != null && state.isCancellable();
    }
}
