package com.teadelivery.ordercatalog.delivery.repository;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Delivery Repository
 * As per BE-003-22
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    
    Optional<Delivery> findByOrderId(UUID orderId);
    
    List<Delivery> findByState(DeliveryState state);
    
    List<Delivery> findByRiderId(UUID riderId);
    
    List<Delivery> findByStateAndRiderId(DeliveryState state, UUID riderId);
}
