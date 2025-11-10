package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.dto.DeliveryResponseDTO;
import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.delivery.dto.UpdateDeliveryStatusRequestDTO;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryTrigger;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryFSM;
import com.teadelivery.ordercatalog.delivery.rider.model.Rider;
import com.teadelivery.ordercatalog.delivery.rider.repository.RiderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final RiderRepository riderRepository;
    
    public DeliveryService(
        DeliveryRepository deliveryRepository,
        DeliveryFSM deliveryFSM,
        RiderAssignmentService riderAssignmentService,
        RiderRepository riderRepository
    ) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryFSM = deliveryFSM;
        this.riderAssignmentService = riderAssignmentService;
        this.riderRepository = riderRepository;
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
     * Start rider search by order ID
     */
    public void startRiderSearchByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found for order: " + orderId));
        startRiderSearch(delivery.getDeliveryId());
    }
    
    /**
     * Check if delivery exists for order (idempotency check)
     */
    public boolean deliveryExistsForOrder(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId).isPresent();
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
    
    /**
     * Get deliveries for rider with pagination
     */
    public Page<DeliveryResponseDTO> getDeliveriesForRider(
        UUID riderId, 
        String status, 
        Pageable pageable
    ) {
        List<Delivery> deliveries;
        
        switch (status.toUpperCase()) {
            case "AVAILABLE":
                // Get deliveries in RIDER_ASSIGNED state (not yet accepted)
                deliveries = deliveryRepository.findByState(DeliveryState.RIDER_ASSIGNED);
                break;
            case "CURRENT":
                // Get active deliveries for this rider
                deliveries = deliveryRepository.findByStateAndRiderId(
                    DeliveryState.RIDER_ACCEPTED, riderId);
                deliveries.addAll(deliveryRepository.findByStateAndRiderId(
                    DeliveryState.AT_RESTAURANT, riderId));
                deliveries.addAll(deliveryRepository.findByStateAndRiderId(
                    DeliveryState.PICKED_UP, riderId));
                deliveries.addAll(deliveryRepository.findByStateAndRiderId(
                    DeliveryState.OUT_FOR_DELIVERY, riderId));
                break;
            case "COMPLETED":
                // Get completed deliveries for this rider
                deliveries = deliveryRepository.findByStateAndRiderId(
                    DeliveryState.DELIVERED, riderId);
                break;
            default:
                // Get all deliveries for this rider
                deliveries = deliveryRepository.findByRiderId(riderId);
        }
        
        // Convert to Page (simplified - in production use proper pagination)
        List<DeliveryResponseDTO> dtos = deliveries.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return Page.empty(pageable); // TODO: Implement proper pagination
    }
    
    /**
     * Update delivery status
     */
    public DeliveryResponseDTO updateDeliveryStatus(
        UUID riderId,
        UUID deliveryId,
        UpdateDeliveryStatusRequestDTO request
    ) {
        Delivery delivery = getDelivery(deliveryId);
        
        // Verify rider owns this delivery
        if (!riderId.equals(delivery.getRiderId())) {
            throw new IllegalArgumentException(
                "Rider not assigned to this delivery");
        }
        
        // Map status to trigger and fire FSM
        switch (request.getStatus()) {
            case "REACHED_RESTAURANT":
                riderReachedRestaurant(deliveryId);
                break;
            case "PICKED_UP":
                riderPickedUpOrder(deliveryId);
                break;
            case "OUT_FOR_DELIVERY":
                // Already handled in pickup
                break;
            case "DELIVERED":
                riderDeliveredOrder(deliveryId);
                break;
        }
        
        return getDeliveryDTO(deliveryId);
    }
    
    /**
     * Get delivery as DTO
     */
    public DeliveryResponseDTO getDeliveryDTO(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        return toDTO(delivery);
    }
    
    /**
     * Get delivery by order ID as DTO
     */
    public DeliveryResponseDTO getDeliveryByOrderIdDTO(UUID orderId) {
        Delivery delivery = getDeliveryByOrderId(orderId);
        return toDTO(delivery);
    }
    
    /**
     * Get rider location for delivery
     */
    public LocationDTO getRiderLocationForDelivery(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        
        if (delivery.getRiderId() == null) {
            throw new IllegalArgumentException("No rider assigned to this delivery");
        }
        
        Rider rider = riderRepository.findById(delivery.getRiderId())
            .orElseThrow(() -> new IllegalArgumentException("Rider not found"));
        
        if (rider.getCurrentLocation() == null) {
            throw new IllegalArgumentException("Rider location not available");
        }
        
        return LocationDTO.builder()
            .latitude(rider.getCurrentLocation().getY())
            .longitude(rider.getCurrentLocation().getX())
            .build();
    }
    
    /**
     * Helper: Convert Delivery to DTO
     */
    private DeliveryResponseDTO toDTO(Delivery delivery) {
        return DeliveryResponseDTO.builder()
            .deliveryId(delivery.getDeliveryId())
            .orderId(delivery.getOrderId())
            .riderId(delivery.getRiderId())
            .state(delivery.getState())
            .deliveryFee(delivery.getDeliveryFee())
            .riderAssignedAt(delivery.getRiderAssignedAt())
            .riderAcceptedAt(delivery.getRiderAcceptedAt())
            .reachedRestaurantAt(delivery.getReachedRestaurantAt())
            .pickedUpAt(delivery.getPickedUpAt())
            .deliveredAt(delivery.getDeliveredAt())
            .failedAt(delivery.getFailedAt())
            .failureReason(delivery.getFailureReason())
            .restaurantWaitTimeMinutes(delivery.getRestaurantWaitTimeMinutes())
            .totalDeliveryTimeMinutes(delivery.getTotalDeliveryTimeMinutes())
            .createdAt(delivery.getCreatedAt())
            .updatedAt(delivery.getUpdatedAt())
            .build();
    }
}
