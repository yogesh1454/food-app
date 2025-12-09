package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryStateMachineFactory;
import com.teadelivery.ordercatalog.notification.service.NotificationService;
import com.teadelivery.ordercatalog.delivery.model.Rider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Rider Assignment Service
 * Handles finding and assigning riders to deliveries
 * 
 * Updated to use DeliveryStateMachineFactory
 * As per BE-003-23
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RiderAssignmentService {

    private final DeliveryRepository deliveryRepository;
    private final RiderRankingService rankingService;
    private final DeliveryStateMachineFactory fsmFactory;
    private final NotificationService notificationService;

    /**
     * Find and assign rider to delivery
     */
    public void findAndAssignRider(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Delivery not found: " + deliveryId));

        // Parse location from JSONB (simplified - in production, use proper JSON
        // parsing)
        double searchRadius = delivery.getSearchRadiusKm();

        // TODO: Extract actual coordinates from delivery.getPickupLocation()
        // For now, using placeholder coordinates
        double longitude = 77.5946; // Bangalore
        double latitude = 12.9716;

        // Find available riders
        List<Rider> availableRiders = rankingService.findAvailableRiders(
                longitude, latitude, searchRadius);

        if (availableRiders.isEmpty()) {
            log.warn("No riders available: deliveryId={}, radius={}",
                    deliveryId, searchRadius);
            handleNoRidersAvailable(delivery);
            return;
        }

        // Rank riders
        List<Rider> rankedRiders = rankingService.rankRiders(
                availableRiders, longitude, latitude);

        // Get top rider
        Rider topRider = rankedRiders.get(0);

        log.info("Assigning top rider to delivery: deliveryId={}, riderId={}",
                deliveryId, topRider.getRiderId());

        // Use FSM to assign rider
        fsmFactory.create(delivery)
                .withActor(null, "SYSTEM")
                .assignRider(topRider.getRiderId());

        // Send assignment notification to rider
        sendAssignmentRequest(delivery, topRider);
    }

    /**
     * Handle no riders available scenario
     */
    private void handleNoRidersAvailable(Delivery delivery) {
        int retryCount = delivery.getRetryCount();

        if (retryCount >= 3) {
            // Failed after 3 retries
            log.error("No riders available after 3 retries: deliveryId={}",
                    delivery.getDeliveryId());

            // Use FSM to fail delivery
            fsmFactory.create(delivery)
                    .withActor(null, "SYSTEM")
                    .noRidersAvailable();

            // Notify customer
            notificationService.notifyCustomer(
                    delivery.getOrderId(),
                    "Unable to find delivery partner. " +
                            "Your order has been cancelled and refund initiated.");

            return;
        }

        // Retry with increased incentives
        delivery.setRetryCount(retryCount + 1);

        // Increase delivery fee by 20%
        BigDecimal currentFee = delivery.getDeliveryFee();
        if (currentFee != null) {
            BigDecimal newFee = currentFee.multiply(new BigDecimal("1.2"));
            delivery.setDeliveryFee(newFee);
        }

        // Expand search radius
        double currentRadius = delivery.getSearchRadiusKm();
        double newRadius = Math.min(currentRadius * 1.5, 10.0); // Max 10 km
        delivery.setSearchRadiusKm(newRadius);

        deliveryRepository.save(delivery);

        log.info("Retrying rider assignment (attempt {}): deliveryId={}, fee={}, radius={}",
                retryCount + 1, delivery.getDeliveryId(),
                delivery.getDeliveryFee(), newRadius);

        // Retry assignment
        findAndAssignRider(delivery.getDeliveryId());

        // Notify customer
        notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Finding delivery partner... This may take a few more minutes.");
    }

    /**
     * Send assignment request to rider
     */
    private void sendAssignmentRequest(Delivery delivery, Rider rider) {
        // Send push notification to rider
        notificationService.notifyRiderOfDeliveryRequest(
                rider.getRiderId(),
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                "Restaurant Address", // TODO: Parse from delivery.getPickupLocation()
                "Customer Address", // TODO: Parse from delivery.getDeliveryLocation()
                delivery.getDeliveryFee());

        log.info("Sent assignment request: deliveryId={}, riderId={}",
                delivery.getDeliveryId(), rider.getRiderId());
    }
}
