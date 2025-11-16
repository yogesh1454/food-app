package com.teadelivery.ordercatalog.order.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Order State Audit Entity
 * Records all state transitions for orders
 */
@Entity
@Table(name = "order_state_audit", indexes = {
    @Index(name = "idx_order_state_audit_order_id", columnList = "order_id"),
    @Index(name = "idx_order_state_audit_transitioned_at", columnList = "transitioned_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id")
    private UUID auditId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    // ========== State Transition ==========
    @Column(name = "from_state", length = 32)
    private String fromState;
    
    @Column(name = "to_state", nullable = false, length = 32)
    private String toState;
    
    @Column(name = "trigger_name", nullable = false, length = 50)
    private String triggerName;
    
    // ========== Context ==========
    @Column(name = "triggered_by")
    private UUID triggeredBy;
    
    @Column(name = "triggered_by_role", length = 20)
    private String triggeredByRole;
    
    // ========== Timestamp ==========
    @Column(name = "transitioned_at", nullable = false)
    private LocalDateTime transitionedAt = LocalDateTime.now();
    
    // ========== Additional Info ==========
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    // ========== Helper Methods ==========
    
    /**
     * Create audit record for state transition
     */
    public static OrderStateAudit create(
        UUID orderId,
        String fromState,
        String toState,
        String triggerName,
        UUID triggeredBy,
        String triggeredByRole
    ) {
        OrderStateAudit audit = new OrderStateAudit();
        audit.setOrderId(orderId);
        audit.setFromState(fromState);
        audit.setToState(toState);
        audit.setTriggerName(triggerName);
        audit.setTriggeredBy(triggeredBy);
        audit.setTriggeredByRole(triggeredByRole);
        audit.setTransitionedAt(LocalDateTime.now());
        return audit;
    }
}
