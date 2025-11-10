package com.teadelivery.ordercatalog.delivery.audit.repository;

import com.teadelivery.ordercatalog.delivery.audit.model.DeliveryStateAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryStateAuditRepository extends JpaRepository<DeliveryStateAudit, UUID> {
    
    List<DeliveryStateAudit> findByDeliveryIdOrderByTransitionedAtDesc(UUID deliveryId);
    List<DeliveryStateAudit> findByDeliveryIdAndToState(UUID deliveryId, String toState);
    List<DeliveryStateAudit> findByTransitionedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<DeliveryStateAudit> findByTriggeredBy(UUID triggeredBy);
    
    long countByDeliveryId(UUID deliveryId);
}
