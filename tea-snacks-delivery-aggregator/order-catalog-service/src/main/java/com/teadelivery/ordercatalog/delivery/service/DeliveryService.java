package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.fsm.DeliveryState;
import com.teadelivery.ordercatalog.fsm.DeliveryTrigger;
import com.teadelivery.ordercatalog.fsm.delivery.DeliveryFSM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Delivery Service
 * Main service for delivery operations
 * As per BE-003-22
 */
@Service
@Slf4j
@Transactional
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    private final DeliveryFSM deliveryFSM;
    private final RiderAssignmentService riderAssignmentService;
    
    public DeliveryService(
        DeliveryRepository deliveryRepository,
        DeliveryFSM deliveryFSM,
        RiderAssignmentService riderAssignmentService
    ) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryFSM = deliveryFSM;
        this.riderAssignmentService = riderAssignmentService;
    }
    
    /**
     * Create delivery for an order
     */
    public Delivery createDelivery(
        UUID orderId,
        String pickupLocation,
        String deliveryLocation,
        BigDecimal deliveryFee
    ) {
        Delivery delivery = Delivery.builder()
            .orderId(orderId)
            .state(DeliveryState.PENDING)
            .pickupLocation(pickupLocation)
            .deliveryLocation(deliveryLocation)
            .deliveryFee(deliveryFee)
            .searchRadiusKm(2.0)
            .retryCount(0)
            .build();
        
        delivery = deliveryRepository.save(delivery);
        
        log.info("Created delivery: deliveryId={}, orderId={}", 
                 delivery.getDeliveryId(), orderId);
        
        return delivery;
    }
    
    /**
     * Start rider search for delivery
     */
    public void startRiderSearch(UUID deliveryId) {
        deliveryFSM.fire(deliveryId, DeliveryTrigger.FIND_RIDERS);
        riderAssignmentService.findAndAssignRider(deliveryId);
    }
    
    /**
     * Rider accepts delivery
     */
    public void riderAcceptDelivery(UUID deliveryId, UUID riderId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found: " + deliveryId));
        
        delivery.setRiderId(riderId);
        deliveryRepository.save(delivery);
        
        deliveryFSM.fire(deliveryId, DeliveryTrigger.RIDER_ACCEPT);
        
        log.info("Rider accepted delivery: deliveryId={}, riderId={}", 
                 deliveryId, riderId);
    }
    
    /**
     * Rider rejects delivery
     */
    public void riderRejectDelivery(UUID deliveryId, UUID riderId, String reason) {
        deliveryFSM.fire(deliveryId, DeliveryTrigger.RIDER_REJECT);
        
        // Reassign to another rider
        riderAssignmentService.findAndAssignRider(deliveryId);
        
        log.info("Rider rejected delivery: deliveryId={}, riderId={}, reason={}", 
                 deliveryId, riderId, reason);
    }
    
    /**
     * Rider reached restaurant
     */
    public void riderReachedRestaurant(UUID deliveryId) {
        deliveryFSM.fire(deliveryId, DeliveryTrigger.REACH_RESTAURANT);
        log.info("Rider reached restaurant: deliveryId={}", deliveryId);
    }
    
    /**
     * Rider picked up order
     */
    public void riderPickedUpOrder(UUID deliveryId) {
        deliveryFSM.fire(deliveryId, DeliveryTrigger.PICKUP_ORDER);
        deliveryFSM.fire(deliveryId, DeliveryTrigger.START_DELIVERY);
        log.info("Rider picked up order: deliveryId={}", deliveryId);
    }
    
    /**
     * Rider delivered order
     */
    public void riderDeliveredOrder(UUID deliveryId) {
        deliveryFSM.fire(deliveryId, DeliveryTrigger.DELIVER_ORDER);
        log.info("Rider delivered order: deliveryId={}", deliveryId);
    }
    
    /**
     * Fail delivery
     */
    public void failDelivery(UUID deliveryId, String reason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found: " + deliveryId));
        
        delivery.setFailureReason(reason);
        deliveryRepository.save(delivery);
        
        deliveryFSM.fire(deliveryId, DeliveryTrigger.FAIL_DELIVERY);
        
        log.error("Delivery failed: deliveryId={}, reason={}", deliveryId, reason);
    }
    
    /**
     * Get delivery by ID
     */
    public Delivery getDelivery(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found: " + deliveryId));
    }
    
    /**
     * Get delivery by order ID
     */
    public Delivery getDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found for order: " + orderId));
    }
}
