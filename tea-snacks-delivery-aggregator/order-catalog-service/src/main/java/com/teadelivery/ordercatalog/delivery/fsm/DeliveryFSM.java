package com.teadelivery.ordercatalog.delivery.fsm;

import com.github.oxo42.stateless4j.StateMachineConfig;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryTrigger;
import com.teadelivery.ordercatalog.common.fsm.BaseStateMachine;
import com.teadelivery.ordercatalog.common.fsm.EventPublisher;
import com.teadelivery.ordercatalog.common.fsm.StateCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Delivery FSM Implementation
 * Manages the 9-state delivery lifecycle
 * As per BE-003-22
 */
@Service
@Slf4j
public class DeliveryFSM extends BaseStateMachine<DeliveryState, DeliveryTrigger> {
    
    private final DeliveryRepository deliveryRepository;
    private final EventPublisher eventPublisher;
    
    public DeliveryFSM(
        StateCacheService stateCacheService,
        DeliveryRepository deliveryRepository,
        EventPublisher eventPublisher
    ) {
        super(stateCacheService);
        this.deliveryRepository = deliveryRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    protected StateMachineConfig<DeliveryState, DeliveryTrigger> configure() {
        StateMachineConfig<DeliveryState, DeliveryTrigger> config = 
            new StateMachineConfig<>();
        
        // PENDING state transitions
        config.configure(DeliveryState.PENDING)
            .permit(DeliveryTrigger.FIND_RIDERS, DeliveryState.SEARCHING_RIDER);
        
        // SEARCHING_RIDER state transitions
        config.configure(DeliveryState.SEARCHING_RIDER)
            .permit(DeliveryTrigger.ASSIGN_RIDER, DeliveryState.RIDER_ASSIGNED)
            .permit(DeliveryTrigger.NO_RIDERS_AVAILABLE, DeliveryState.FAILED);
        
        // RIDER_ASSIGNED state transitions
        config.configure(DeliveryState.RIDER_ASSIGNED)
            .permit(DeliveryTrigger.RIDER_ACCEPT, DeliveryState.RIDER_ACCEPTED)
            .permit(DeliveryTrigger.RIDER_REJECT, DeliveryState.SEARCHING_RIDER)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        // RIDER_ACCEPTED state transitions
        config.configure(DeliveryState.RIDER_ACCEPTED)
            .permit(DeliveryTrigger.REACH_RESTAURANT, DeliveryState.AT_RESTAURANT)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        // AT_RESTAURANT state transitions
        config.configure(DeliveryState.AT_RESTAURANT)
            .permit(DeliveryTrigger.PICKUP_ORDER, DeliveryState.PICKED_UP)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        // PICKED_UP state transitions
        config.configure(DeliveryState.PICKED_UP)
            .permit(DeliveryTrigger.START_DELIVERY, DeliveryState.OUT_FOR_DELIVERY);
        
        // OUT_FOR_DELIVERY state transitions
        config.configure(DeliveryState.OUT_FOR_DELIVERY)
            .permit(DeliveryTrigger.DELIVER_ORDER, DeliveryState.DELIVERED)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        // Configure entry actions
        config.configure(DeliveryState.RIDER_ACCEPTED)
            .onEntry(this::onRiderAccepted);
        
        config.configure(DeliveryState.PICKED_UP)
            .onEntry(this::onPickedUp);
        
        config.configure(DeliveryState.DELIVERED)
            .onEntry(this::onDelivered);
        
        config.configure(DeliveryState.FAILED)
            .onEntry(this::onFailed);
        
        return config;
    }
    
    @Override
    protected DeliveryState loadStateFromDatabase(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
            .map(Delivery::getState)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found: " + deliveryId));
    }
    
    @Override
    protected void persistStateToDatabase(UUID deliveryId, DeliveryState state) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Delivery not found: " + deliveryId));
        
        delivery.setState(state);
        updateTimestamps(delivery, state);
        
        deliveryRepository.save(delivery);
        
        log.info("Persisted delivery state: deliveryId={}, state={}", 
                 deliveryId, state);
    }
    
    @Override
    protected String getEntityType() {
        return "DELIVERY";
    }
    
    /**
     * Update timestamps based on state transition
     */
    private void updateTimestamps(Delivery delivery, DeliveryState state) {
        Instant now = Instant.now();
        
        switch (state) {
            case RIDER_ASSIGNED:
                delivery.setRiderAssignedAt(now);
                break;
            case RIDER_ACCEPTED:
                delivery.setRiderAcceptedAt(now);
                break;
            case AT_RESTAURANT:
                delivery.setReachedRestaurantAt(now);
                break;
            case PICKED_UP:
                delivery.setPickedUpAt(now);
                calculateRestaurantWaitTime(delivery);
                break;
            case DELIVERED:
                delivery.setDeliveredAt(now);
                calculateTotalDeliveryTime(delivery);
                break;
            case FAILED:
                delivery.setFailedAt(now);
                break;
        }
    }
    
    /**
     * Entry action for RIDER_ACCEPTED state
     */
    private void onRiderAccepted(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        
        // Publish event to update Order FSM
        eventPublisher.publishDeliveryStateChange(
            deliveryId,
            delivery.getOrderId(),
            DeliveryState.RIDER_ASSIGNED.name(),
            DeliveryState.RIDER_ACCEPTED.name(),
            DeliveryTrigger.RIDER_ACCEPT.name(),
            delivery.getRiderId(),
            null
        );
        
        log.info("Rider accepted delivery: deliveryId={}, riderId={}", 
                 deliveryId, delivery.getRiderId());
    }
    
    /**
     * Entry action for PICKED_UP state
     */
    private void onPickedUp(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        
        // Publish event to update Order FSM
        eventPublisher.publishDeliveryStateChange(
            deliveryId,
            delivery.getOrderId(),
            DeliveryState.AT_RESTAURANT.name(),
            DeliveryState.PICKED_UP.name(),
            DeliveryTrigger.PICKUP_ORDER.name(),
            delivery.getRiderId(),
            null
        );
        
        log.info("Order picked up: deliveryId={}, riderId={}", 
                 deliveryId, delivery.getRiderId());
    }
    
    /**
     * Entry action for DELIVERED state
     */
    private void onDelivered(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        
        // Publish event to update Order FSM
        eventPublisher.publishDeliveryStateChange(
            deliveryId,
            delivery.getOrderId(),
            DeliveryState.OUT_FOR_DELIVERY.name(),
            DeliveryState.DELIVERED.name(),
            DeliveryTrigger.DELIVER_ORDER.name(),
            delivery.getRiderId(),
            null
        );
        
        log.info("Order delivered: deliveryId={}, riderId={}, totalTime={} min", 
                 deliveryId, delivery.getRiderId(), 
                 delivery.getTotalDeliveryTimeMinutes());
    }
    
    /**
     * Entry action for FAILED state
     */
    private void onFailed(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        
        // Publish event to update Order FSM (cancel order)
        eventPublisher.publishDeliveryStateChange(
            deliveryId,
            delivery.getOrderId(),
            delivery.getState().name(),
            DeliveryState.FAILED.name(),
            DeliveryTrigger.FAIL_DELIVERY.name(),
            delivery.getRiderId(),
            null
        );
        
        log.error("Delivery failed: deliveryId={}, reason={}", 
                  deliveryId, delivery.getFailureReason());
    }
    
    /**
     * Calculate restaurant wait time
     */
    private void calculateRestaurantWaitTime(Delivery delivery) {
        if (delivery.getReachedRestaurantAt() != null && 
            delivery.getPickedUpAt() != null) {
            long waitTime = Duration.between(
                delivery.getReachedRestaurantAt(),
                delivery.getPickedUpAt()
            ).toMinutes();
            delivery.setRestaurantWaitTimeMinutes((int) waitTime);
        }
    }
    
    /**
     * Calculate total delivery time
     */
    private void calculateTotalDeliveryTime(Delivery delivery) {
        if (delivery.getCreatedAt() != null && 
            delivery.getDeliveredAt() != null) {
            long totalTime = Duration.between(
                delivery.getCreatedAt(),
                delivery.getDeliveredAt()
            ).toMinutes();
            delivery.setTotalDeliveryTimeMinutes((int) totalTime);
        }
    }
}
