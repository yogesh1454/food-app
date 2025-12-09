package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.dto.DeliveryResponseDTO;
import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.delivery.dto.UpdateDeliveryStatusRequestDTO;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryStateMachineFactory;
import com.teadelivery.ordercatalog.delivery.model.Rider;
import com.teadelivery.ordercatalog.delivery.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Delivery Service
 * Main service for delivery operations
 * 
 * Updated to use DeliveryStateMachineFactory (Instance-per-Delivery pattern)
 * as per BE-003-22
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStateMachineFactory fsmFactory;
    private final RiderAssignmentService riderAssignmentService;
    private final RiderRepository riderRepository;

    // ========== Delivery Creation ==========

    /**
     * Create delivery for an order
     */
    public Delivery createDelivery(
            UUID orderId,
            String pickupLocation,
            String deliveryLocation,
            BigDecimal deliveryFee) {
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
     * Check if delivery exists for order (idempotency check)
     */
    public boolean deliveryExistsForOrder(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId).isPresent();
    }

    // ========== Rider Search & Assignment ==========

    /**
     * Start rider search for delivery
     * Uses FSM to transition PENDING → SEARCHING_RIDER
     */
    public Delivery startRiderSearch(UUID deliveryId) {
        log.info("Starting rider search for delivery: {}", deliveryId);

        Delivery delivery = getDelivery(deliveryId);

        // Use FSM to transition
        Delivery saved = fsmFactory.create(delivery)
                .withActor(null, "SYSTEM")
                .findRiders();

        // Trigger async rider assignment
        riderAssignmentService.findAndAssignRider(deliveryId);

        return saved;
    }

    /**
     * Start rider search by order ID
     */
    public Delivery startRiderSearchByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Delivery not found for order: " + orderId));
        return startRiderSearch(delivery.getDeliveryId());
    }

    /**
     * Assign rider to delivery
     * SEARCHING_RIDER → RIDER_ASSIGNED
     */
    public Delivery assignRider(UUID deliveryId, UUID riderId) {
        log.info("Assigning rider {} to delivery {}", riderId, deliveryId);

        Delivery delivery = getDelivery(deliveryId);

        return fsmFactory.create(delivery)
                .withActor(null, "SYSTEM")
                .assignRider(riderId);
    }

    // ========== Rider Actions ==========

    /**
     * Rider accepts delivery
     * RIDER_ASSIGNED → RIDER_ACCEPTED
     */
    public Delivery riderAcceptDelivery(UUID deliveryId, UUID riderId) {
        log.info("Rider {} accepting delivery {}", riderId, deliveryId);

        Delivery delivery = getDelivery(deliveryId);

        // Verify rider is assigned to this delivery
        if (!riderId.equals(delivery.getRiderId())) {
            throw new IllegalArgumentException("Rider not assigned to this delivery");
        }

        return fsmFactory.create(delivery)
                .withActor(riderId, "RIDER")
                .riderAccept();
    }

    /**
     * Rider rejects delivery
     * RIDER_ASSIGNED → SEARCHING_RIDER
     */
    public Delivery riderRejectDelivery(UUID deliveryId, UUID riderId, String reason) {
        log.info("Rider {} rejecting delivery {}: {}", riderId, deliveryId, reason);

        Delivery delivery = getDelivery(deliveryId);

        Delivery saved = fsmFactory.create(delivery)
                .withActor(riderId, "RIDER")
                .riderReject(reason);

        // Reassign to another rider
        riderAssignmentService.findAndAssignRider(deliveryId);

        return saved;
    }

    /**
     * Rider reached restaurant
     * RIDER_ACCEPTED → AT_RESTAURANT
     */
    public Delivery riderReachedRestaurant(UUID deliveryId, UUID riderId) {
        log.info("Rider reached restaurant for delivery {}", deliveryId);

        Delivery delivery = getDelivery(deliveryId);
        verifyRiderOwnsDelivery(delivery, riderId);

        return fsmFactory.create(delivery)
                .withActor(riderId, "RIDER")
                .reachRestaurant();
    }

    /**
     * Rider picked up order
     * AT_RESTAURANT → PICKED_UP → OUT_FOR_DELIVERY
     */
    public Delivery riderPickedUpOrder(UUID deliveryId, UUID riderId) {
        log.info("Rider picked up order for delivery {}", deliveryId);

        Delivery delivery = getDelivery(deliveryId);
        verifyRiderOwnsDelivery(delivery, riderId);

        // Pickup
        Delivery saved = fsmFactory.create(delivery)
                .withActor(riderId, "RIDER")
                .pickup();

        // Immediately start delivery
        saved = fsmFactory.create(saved)
                .withActor(riderId, "RIDER")
                .startDelivery();

        return saved;
    }

    /**
     * Rider delivered order
     * OUT_FOR_DELIVERY → DELIVERED
     */
    public Delivery riderDeliveredOrder(UUID deliveryId, UUID riderId) {
        log.info("Rider delivered order for delivery {}", deliveryId);

        Delivery delivery = getDelivery(deliveryId);
        verifyRiderOwnsDelivery(delivery, riderId);

        return fsmFactory.create(delivery)
                .withActor(riderId, "RIDER")
                .deliver();
    }

    // ========== Failure Handling ==========

    /**
     * No riders available
     * SEARCHING_RIDER → FAILED
     */
    public Delivery noRidersAvailable(UUID deliveryId) {
        log.warn("No riders available for delivery {}", deliveryId);

        Delivery delivery = getDelivery(deliveryId);

        return fsmFactory.create(delivery)
                .withActor(null, "SYSTEM")
                .noRidersAvailable();
    }

    /**
     * Fail delivery
     * Any state → FAILED
     */
    public Delivery failDelivery(UUID deliveryId, String reason) {
        log.error("Failing delivery {}: {}", deliveryId, reason);

        Delivery delivery = getDelivery(deliveryId);

        return fsmFactory.create(delivery)
                .withActor(null, "SYSTEM")
                .fail(reason);
    }

    // ========== Query Methods ==========

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
            Pageable pageable) {
        List<Delivery> deliveries = new ArrayList<>();

        switch (status.toUpperCase()) {
            case "AVAILABLE":
                // Get deliveries in RIDER_ASSIGNED state (not yet accepted)
                deliveries = deliveryRepository.findByState(DeliveryState.RIDER_ASSIGNED);
                break;
            case "CURRENT":
                // Get active deliveries for this rider
                deliveries.addAll(deliveryRepository.findByStateAndRiderId(
                        DeliveryState.RIDER_ACCEPTED, riderId));
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
     * Update delivery status (for rider app)
     */
    public DeliveryResponseDTO updateDeliveryStatus(
            UUID riderId,
            UUID deliveryId,
            UpdateDeliveryStatusRequestDTO request) {
        Delivery delivery = getDelivery(deliveryId);
        verifyRiderOwnsDelivery(delivery, riderId);

        // Map status to FSM action
        Delivery saved = switch (request.getStatus().toUpperCase()) {
            case "REACHED_RESTAURANT" -> riderReachedRestaurant(deliveryId, riderId);
            case "PICKED_UP" -> riderPickedUpOrder(deliveryId, riderId);
            case "DELIVERED" -> riderDeliveredOrder(deliveryId, riderId);
            default -> throw new IllegalArgumentException("Unknown status: " + request.getStatus());
        };

        return toDTO(saved);
    }

    // ========== DTO Methods ==========

    /**
     * Get delivery as DTO
     */
    public DeliveryResponseDTO getDeliveryDTO(UUID deliveryId) {
        return toDTO(getDelivery(deliveryId));
    }

    /**
     * Get delivery by order ID as DTO
     */
    public DeliveryResponseDTO getDeliveryByOrderIdDTO(UUID orderId) {
        return toDTO(getDeliveryByOrderId(orderId));
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

    // ========== Helper Methods ==========

    private void verifyRiderOwnsDelivery(Delivery delivery, UUID riderId) {
        if (riderId != null && !riderId.equals(delivery.getRiderId())) {
            throw new IllegalArgumentException("Rider not assigned to this delivery");
        }
    }

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
