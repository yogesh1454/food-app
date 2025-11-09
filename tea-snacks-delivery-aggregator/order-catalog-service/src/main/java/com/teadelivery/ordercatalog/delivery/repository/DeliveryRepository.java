package com.teadelivery.ordercatalog.delivery.repository;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.fsm.DeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    
    Optional<Delivery> findByOrderId(UUID orderId);
    List<Delivery> findByRiderId(UUID riderId);
    List<Delivery> findByState(DeliveryState state);
    List<Delivery> findByRiderIdAndState(UUID riderId, DeliveryState state);
    List<Delivery> findByStateIn(List<DeliveryState> states);
    
    long countByRiderId(UUID riderId);
    long countByState(DeliveryState state);
    long countByRiderIdAndState(UUID riderId, DeliveryState state);
}
