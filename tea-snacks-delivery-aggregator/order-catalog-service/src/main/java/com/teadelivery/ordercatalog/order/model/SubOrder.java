package com.teadelivery.ordercatalog.order.model;

import com.teadelivery.ordercatalog.fsm.SubOrderState;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SubOrder Entity
 * Represents a vendor-specific sub-order within a multi-vendor order
 */
@Entity
@Table(name = "sub_orders", indexes = {
    @Index(name = "idx_sub_orders_parent_order_id", columnList = "parent_order_id"),
    @Index(name = "idx_sub_orders_restaurant_id", columnList = "restaurant_id"),
    @Index(name = "idx_sub_orders_state", columnList = "state")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sub_order_id")
    private UUID subOrderId;
    
    @Column(name = "parent_order_id", nullable = false)
    private UUID parentOrderId;
    
    // ========== Restaurant Info ==========
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;
    
    // ========== Sub-order State ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private SubOrderState state = SubOrderState.PENDING_ACCEPTANCE;
    
    // ========== Items ==========
    @Type(JsonBinaryType.class)
    @Column(name = "items", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> items = new HashMap<>();
    
    // ========== Pricing ==========
    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;
    
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
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "ready_at")
    private LocalDateTime readyAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    // ========== Preparation Time ==========
    @Column(name = "estimated_prep_time_minutes")
    private Integer estimatedPrepTimeMinutes;
    
    @Column(name = "actual_prep_time_minutes")
    private Integer actualPrepTimeMinutes;
    
    // ========== Metadata ==========
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    // ========== Helper Methods ==========
    
    /**
     * Update state timestamp based on new state
     */
    public void updateStateTimestamp(SubOrderState newState) {
        LocalDateTime now = LocalDateTime.now();
        switch (newState) {
            case ACCEPTED -> this.acceptedAt = now;
            case READY_FOR_PICKUP -> {
                this.readyAt = now;
                // Calculate actual prep time if accepted
                if (this.acceptedAt != null) {
                    this.actualPrepTimeMinutes = (int) java.time.Duration
                        .between(this.acceptedAt, now).toMinutes();
                }
            }
            case REJECTED -> this.rejectedAt = now;
        }
    }
    
    /**
     * Check if sub-order is in a terminal state
     */
    public boolean isTerminal() {
        return state == SubOrderState.READY_FOR_PICKUP ||
               state == SubOrderState.CANCELLED ||
               state == SubOrderState.REJECTED;
    }
    
    /**
     * Check if sub-order can be cancelled
     */
    public boolean isCancellable() {
        return state == SubOrderState.PENDING_ACCEPTANCE ||
               state == SubOrderState.ACCEPTED;
    }
}
