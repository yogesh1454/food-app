package com.teadelivery.ordercatalog.order.repository;

import com.teadelivery.ordercatalog.order.model.OrderStateAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStateAuditRepository extends JpaRepository<OrderStateAudit, UUID> {
    
    List<OrderStateAudit> findByOrderIdOrderByTransitionedAtDesc(UUID orderId);
    List<OrderStateAudit> findByOrderIdAndToState(UUID orderId, String toState);
    List<OrderStateAudit> findByTransitionedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<OrderStateAudit> findByTriggeredBy(UUID triggeredBy);
    
    long countByOrderId(UUID orderId);
}
