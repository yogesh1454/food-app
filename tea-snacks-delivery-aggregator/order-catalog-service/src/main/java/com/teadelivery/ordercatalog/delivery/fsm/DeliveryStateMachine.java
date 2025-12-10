package com.teadelivery.ordercatalog.delivery.fsm;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.teadelivery.ordercatalog.common.fsm.EventPublisher;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.model.DeliveryStateAudit;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryStateAuditRepository;
import com.teadelivery.ordercatalog.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Delivery State Machine - Instance-per-Delivery Pattern
 * 
 * Each Delivery gets its own FSM instance with direct access to the Delivery
 * object.
 * This centralizes:
 * - State transitions with validation
 * - Persistence (save delivery after transition)
 * - Auditing (create audit record)
 * - Event publishing (notify Order FSM of delivery state changes)
 * - Timestamp updates
 * 
 * Usage:
 * Delivery saved = fsmFactory.create(delivery)
 * .withActor(riderId, "RIDER")
 * .riderAccept();
 * 
 * As per BE-003-22 and BE-004-XX
 */
@Slf4j
public class DeliveryStateMachine {

    // The delivery this FSM instance manages (direct reference!)
    private final Delivery delivery;

    // The underlying Stateless4j state machine
    private final StateMachine<DeliveryState, DeliveryTrigger> sm;

    // Injected dependencies
    private final DeliveryRepository deliveryRepository;
    private final DeliveryStateAuditRepository auditRepository;
    private final EventPublisher eventPublisher;
    private final NotificationService notificationService;

    // Actor context for auditing
    private UUID actorId;
    private String actorType = "SYSTEM";

    // Transition context
    private DeliveryState previousState;
    private String failureReason;

    /**
     * Package-private constructor - use DeliveryStateMachineFactory to create
     * instances
     */
    DeliveryStateMachine(
            Delivery delivery,
            DeliveryRepository deliveryRepository,
            DeliveryStateAuditRepository auditRepository,
            EventPublisher eventPublisher,
            NotificationService notificationService) {
        this.delivery = delivery;
        this.deliveryRepository = deliveryRepository;
        this.auditRepository = auditRepository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;

        // Configure and create state machine starting from current delivery state
        StateMachineConfig<DeliveryState, DeliveryTrigger> config = configure();
        this.sm = new StateMachine<>(
                delivery.getState() != null ? delivery.getState() : DeliveryState.PENDING,
                config);
    }

    /**
     * Set actor context for auditing
     */
    public DeliveryStateMachine withActor(UUID actorId, String actorType) {
        this.actorId = actorId;
        this.actorType = actorType != null ? actorType : "SYSTEM";
        return this;
    }

    // ========== FSM Configuration ==========

    private StateMachineConfig<DeliveryState, DeliveryTrigger> configure() {
        StateMachineConfig<DeliveryState, DeliveryTrigger> config = new StateMachineConfig<>();

        // ========== PENDING → SEARCHING_RIDER ==========
        config.configure(DeliveryState.PENDING)
                .permit(DeliveryTrigger.FIND_RIDERS, DeliveryState.SEARCHING_RIDER)
                .onEntry(this::onPending);

        // ========== SEARCHING_RIDER → RIDER_ASSIGNED / FAILED ==========
        config.configure(DeliveryState.SEARCHING_RIDER)
                .permit(DeliveryTrigger.ASSIGN_RIDER, DeliveryState.RIDER_ASSIGNED)
                .permit(DeliveryTrigger.NO_RIDERS_AVAILABLE, DeliveryState.FAILED)
                .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED)
                .onEntry(this::onSearchingRider);

        // ========== RIDER_ASSIGNED → RIDER_ACCEPTED / SEARCHING_RIDER / FAILED
        // ==========
        config.configure(DeliveryState.RIDER_ASSIGNED)
                .permit(DeliveryTrigger.RIDER_ACCEPT, DeliveryState.RIDER_ACCEPTED)
                .permit(DeliveryTrigger.RIDER_REJECT, DeliveryState.SEARCHING_RIDER)
                .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED)
                .onEntry(this::onRiderAssigned);

        // ========== RIDER_ACCEPTED → AT_RESTAURANT / FAILED ==========
        config.configure(DeliveryState.RIDER_ACCEPTED)
                .permit(DeliveryTrigger.REACH_RESTAURANT, DeliveryState.AT_RESTAURANT)
                .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED)
                .onEntry(this::onRiderAccepted);

        // ========== AT_RESTAURANT → PICKED_UP / FAILED ==========
        config.configure(DeliveryState.AT_RESTAURANT)
                .permit(DeliveryTrigger.PICKUP_ORDER, DeliveryState.PICKED_UP)
                .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED)
                .onEntry(this::onAtRestaurant);

        // ========== PICKED_UP → OUT_FOR_DELIVERY / FAILED ==========
        config.configure(DeliveryState.PICKED_UP)
                .permit(DeliveryTrigger.START_DELIVERY, DeliveryState.OUT_FOR_DELIVERY)
                .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED)
                .onEntry(this::onPickedUp);

        // ========== OUT_FOR_DELIVERY → DELIVERED / FAILED ==========
        config.configure(DeliveryState.OUT_FOR_DELIVERY)
                .permit(DeliveryTrigger.DELIVER_ORDER, DeliveryState.DELIVERED)
                .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED)
                .onEntry(this::onOutForDelivery);

        // ========== DELIVERED (Terminal) ==========
        config.configure(DeliveryState.DELIVERED)
                .onEntry(this::onDelivered);

        // ========== FAILED (Terminal) ==========
        config.configure(DeliveryState.FAILED)
                .onEntry(this::onFailed);

        return config;
    }

    // ========== Public API Methods ==========

    /**
     * Start searching for riders
     * PENDING → SEARCHING_RIDER
     */
    public Delivery findRiders() {
        return fireAndSave(DeliveryTrigger.FIND_RIDERS, "FIND_RIDERS");
    }

    /**
     * Assign a rider to the delivery
     * SEARCHING_RIDER → RIDER_ASSIGNED
     */
    public Delivery assignRider(UUID riderId) {
        delivery.setRiderId(riderId);
        delivery.setRiderAssignedAt(Instant.now());
        return fireAndSave(DeliveryTrigger.ASSIGN_RIDER, "RIDER_ASSIGNED");
    }

    /**
     * Rider accepts the delivery
     * RIDER_ASSIGNED → RIDER_ACCEPTED
     */
    public Delivery riderAccept() {
        delivery.setRiderAcceptedAt(Instant.now());
        return fireAndSave(DeliveryTrigger.RIDER_ACCEPT, "RIDER_ACCEPTED");
    }

    /**
     * Rider rejects the delivery
     * RIDER_ASSIGNED → SEARCHING_RIDER
     */
    public Delivery riderReject(String reason) {
        this.failureReason = reason;
        delivery.setRetryCount(delivery.getRetryCount() + 1);
        delivery.setRiderId(null); // Clear rejected rider
        delivery.setRiderAssignedAt(null);
        return fireAndSave(DeliveryTrigger.RIDER_REJECT, "RIDER_REJECTED");
    }

    /**
     * No riders available after retries
     * SEARCHING_RIDER → FAILED
     */
    public Delivery noRidersAvailable() {
        this.failureReason = "No riders available after " + delivery.getRetryCount() + " attempts";
        delivery.setFailureReason(this.failureReason);
        delivery.setFailedAt(Instant.now());
        return fireAndSave(DeliveryTrigger.NO_RIDERS_AVAILABLE, "NO_RIDERS_AVAILABLE");
    }

    /**
     * Rider reached the restaurant
     * RIDER_ACCEPTED → AT_RESTAURANT
     */
    public Delivery reachRestaurant() {
        delivery.setReachedRestaurantAt(Instant.now());
        return fireAndSave(DeliveryTrigger.REACH_RESTAURANT, "REACHED_RESTAURANT");
    }

    /**
     * Rider picked up the order
     * AT_RESTAURANT → PICKED_UP
     */
    public Delivery pickup() {
        delivery.setPickedUpAt(Instant.now());
        calculateRestaurantWaitTime();
        return fireAndSave(DeliveryTrigger.PICKUP_ORDER, "ORDER_PICKED_UP");
    }

    /**
     * Rider started delivery to customer
     * PICKED_UP → OUT_FOR_DELIVERY
     */
    public Delivery startDelivery() {
        return fireAndSave(DeliveryTrigger.START_DELIVERY, "DELIVERY_STARTED");
    }

    /**
     * Order delivered to customer
     * OUT_FOR_DELIVERY → DELIVERED
     */
    public Delivery deliver() {
        delivery.setDeliveredAt(Instant.now());
        calculateTotalDeliveryTime();
        return fireAndSave(DeliveryTrigger.DELIVER_ORDER, "ORDER_DELIVERED");
    }

    /**
     * Delivery failed
     * Any state → FAILED
     */
    public Delivery fail(String reason) {
        this.failureReason = reason;
        delivery.setFailureReason(reason);
        delivery.setFailedAt(Instant.now());
        return fireAndSave(DeliveryTrigger.FAIL_DELIVERY, "DELIVERY_FAILED");
    }

    // ========== onEntry Callbacks ==========

    private void onPending() {
        log.debug("Delivery {} entering PENDING state", delivery.getDeliveryId());
    }

    private void onSearchingRider() {
        log.info("Delivery {} searching for riders", delivery.getDeliveryId());
        // TODO: Trigger async rider search
    }

    private void onRiderAssigned() {
        log.info("Delivery {} rider assigned: {}", delivery.getDeliveryId(), delivery.getRiderId());

        // Notify rider of assignment
        if (delivery.getRiderId() != null) {
            notificationService.notifyRiderOfDeliveryRequest(
                    delivery.getRiderId(),
                    delivery.getDeliveryId(),
                    delivery.getOrderId(),
                    delivery.getPickupLocation(),
                    delivery.getDeliveryLocation(),
                    delivery.getDeliveryFee());
        }
    }

    private void onRiderAccepted() {
        log.info("Delivery {} rider accepted: {}", delivery.getDeliveryId(), delivery.getRiderId());

        // Notify customer that rider is assigned
        notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Delivery partner assigned! On the way to restaurant.");
    }

    private void onAtRestaurant() {
        log.info("Delivery {} rider at restaurant", delivery.getDeliveryId());

        // Notify customer
        notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Delivery partner has arrived at the restaurant");
    }

    private void onPickedUp() {
        log.info("Delivery {} order picked up", delivery.getDeliveryId());

        // Notify customer
        notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Your order has been picked up and is on the way!");
    }

    private void onOutForDelivery() {
        log.info("Delivery {} out for delivery", delivery.getDeliveryId());
    }

    private void onDelivered() {
        log.info("Delivery {} completed. Total time: {} min",
                delivery.getDeliveryId(),
                delivery.getTotalDeliveryTimeMinutes());

        // Notify customer
        notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Your order has been delivered. Enjoy your meal!");

        // TODO: Release rider for next delivery
        // TODO: Calculate rider earnings
    }

    private void onFailed() {
        log.error("Delivery {} failed: {}", delivery.getDeliveryId(), delivery.getFailureReason());

        // Notify customer
        notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Sorry, delivery failed: " + delivery.getFailureReason());
    }

    // ========== Core FSM Methods ==========

    /**
     * Fire trigger and save delivery with audit
     */
    private Delivery fireAndSave(DeliveryTrigger trigger, String action) {
        // Validate transition is allowed
        if (!sm.canFire(trigger)) {
            throw new IllegalStateException(
                    String.format("Cannot fire %s in state %s for delivery %s",
                            trigger, delivery.getState(), delivery.getDeliveryId()));
        }

        // Store previous state for audit
        previousState = delivery.getState();

        // Fire trigger - this calls onEntry() with access to this.delivery
        sm.fire(trigger);

        // Sync delivery state with FSM
        DeliveryState newState = sm.getState();
        delivery.setState(newState);

        // Persist delivery
        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Create audit record
        createAuditRecord(previousState, newState, action);

        // Publish event (for Order FSM integration)
        publishStateChange(previousState, newState, action);

        log.info("Delivery {} transitioned: {} → {} ({})",
                delivery.getDeliveryId(), previousState, newState, action);

        return savedDelivery;
    }

    /**
     * Check if a trigger can be fired
     */
    public boolean canFire(DeliveryTrigger trigger) {
        return sm.canFire(trigger);
    }

    /**
     * Get current state
     */
    public DeliveryState getCurrentState() {
        return sm.getState();
    }

    /**
     * Get the delivery
     */
    public Delivery getDelivery() {
        return delivery;
    }

    // ========== Helper Methods ==========

    private void createAuditRecord(DeliveryState fromState, DeliveryState toState, String action) {
        try {
            DeliveryStateAudit audit = DeliveryStateAudit.create(
                    delivery.getDeliveryId(),
                    fromState != null ? fromState.name() : null,
                    toState.name(),
                    action,
                    actorId != null ? actorId : delivery.getRiderId(),
                    actorType);

            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to create audit record for delivery: {}", delivery.getDeliveryId(), e);
        }
    }

    private void publishStateChange(DeliveryState fromState, DeliveryState toState, String action) {
        try {
            eventPublisher.publishDeliveryStateChange(
                    delivery.getDeliveryId(),
                    delivery.getOrderId(),
                    fromState != null ? fromState.name() : null,
                    toState.name(),
                    action,
                    delivery.getRiderId(),
                    null);
        } catch (Exception e) {
            log.error("Failed to publish state change for delivery: {}", delivery.getDeliveryId(), e);
        }
    }

    private void calculateRestaurantWaitTime() {
        if (delivery.getReachedRestaurantAt() != null && delivery.getPickedUpAt() != null) {
            long waitTime = Duration.between(
                    delivery.getReachedRestaurantAt(),
                    delivery.getPickedUpAt()).toMinutes();
            delivery.setRestaurantWaitTimeMinutes((int) waitTime);
        }
    }

    private void calculateTotalDeliveryTime() {
        if (delivery.getCreatedAt() != null && delivery.getDeliveredAt() != null) {
            long totalTime = Duration.between(
                    delivery.getCreatedAt(),
                    delivery.getDeliveredAt()).toMinutes();
            delivery.setTotalDeliveryTimeMinutes((int) totalTime);
        }
    }
}
