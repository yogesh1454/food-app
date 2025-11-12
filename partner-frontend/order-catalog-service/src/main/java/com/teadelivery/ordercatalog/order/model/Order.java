package com.teadelivery.ordercatalog.order.model;

import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
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

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
    @Index(name = "idx_orders_branch_id", columnList = "branch_id"),
    @Index(name = "idx_orders_status", columnList = "order_status"),
    @Index(name = "idx_orders_ordered_at", columnList = "ordered_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private VendorBranch branch;
    
    @Column(name = "delivery_partner_id")
    private UUID deliveryPartnerId;
    
    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus = "PENDING";
    
    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus = "PENDING";
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Type(JsonBinaryType.class)
    @Column(name = "delivery_details", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> deliveryDetails;
    
    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt = LocalDateTime.now();
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
            .map(item -> item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
