package com.teadelivery.ordercatalog.order.fsm;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.teadelivery.ordercatalog.common.fsm.EventPublisher;
import com.teadelivery.ordercatalog.common.util.GeometryUtils;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import com.teadelivery.ordercatalog.menu.service.MenuService;
import com.teadelivery.ordercatalog.notification.service.NotificationService;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.model.OrderStateAudit;
import com.teadelivery.ordercatalog.order.repository.OrderItemRepository;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.repository.OrderStateAuditRepository;
import com.teadelivery.ordercatalog.order.service.OrderTimeoutService;
import com.teadelivery.ordercatalog.order.service.OrderValidationService;
import com.teadelivery.ordercatalog.payment.service.PaymentService;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Order State Machine - Instance per Order
 * 
 * Each order gets its own FSM instance, allowing:
 * - Full access to the order object in all callbacks
 * - Proper use of onEntry() callbacks for business logic
 * - Centralized state transition, persistence, and auditing
 * 
 * States: CREATED → VALIDATED → PAYMENT_CONFIRMED → PENDING_ACCEPTANCE →
 * ACCEPTED →
 * PREPARING → READY_FOR_PICKUP → ASSIGNED_TO_RIDER → PICKED_UP → DELIVERED →
 * CLOSED
 * 
 * Terminal States: CANCELLED, REJECTED
 */
@Slf4j
public class OrderStateMachine {

    // The order this FSM manages - available to all callbacks
    private final Order order;

    // The Stateless4j state machine
    private final StateMachine<OrderState, OrderTrigger> sm;

    // Dependencies for business logic
    private final OrderRepository orderRepository;
    private final OrderStateAuditRepository auditRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final OrderTimeoutService timeoutService;
    private final DeliveryService deliveryService;
    private final PaymentService paymentService;
    private final OrderValidationService validationService;
    private final MenuService menuService;
    private final VendorBranchRepository vendorBranchRepository;

    // Track previous state for audit
    private OrderState previousState;

    // Additional context for transitions
    private String transitionReason;
    private String cancelledBy;
    private UUID actorId;
    private String actorType;

    /**
     * Create FSM for an order with all dependencies
     */
    public OrderStateMachine(
            Order order,
            OrderRepository orderRepository,
            OrderStateAuditRepository auditRepository,
            OrderItemRepository orderItemRepository,
            EventPublisher eventPublisher,
            NotificationService notificationService,
            OrderTimeoutService timeoutService,
            DeliveryService deliveryService,
            PaymentService paymentService,
            OrderValidationService validationService,
            MenuService menuService,
            VendorBranchRepository vendorBranchRepository) {
        this.order = order;
        this.orderRepository = orderRepository;
        this.auditRepository = auditRepository;
        this.orderItemRepository = orderItemRepository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
        this.timeoutService = timeoutService;
        this.deliveryService = deliveryService;
        this.paymentService = paymentService;
        this.validationService = validationService;
        this.menuService = menuService;
        this.vendorBranchRepository = vendorBranchRepository;

        // Initialize FSM with order's current state
        this.sm = new StateMachine<>(order.getState(), createConfig());
        this.previousState = order.getState();
    }

    /**
     * Configure the state machine with all transitions and callbacks
     */
    private StateMachineConfig<OrderState, OrderTrigger> createConfig() {
        StateMachineConfig<OrderState, OrderTrigger> config = new StateMachineConfig<>();

        // ========== CREATED → VALIDATED / REJECTED ==========
        config.configure(OrderState.CREATED)
                .permit(OrderTrigger.VALIDATE_ORDER, OrderState.VALIDATED)
                .permit(OrderTrigger.VALIDATION_FAILED, OrderState.REJECTED)
                .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);

        // ========== VALIDATED → PAYMENT_CONFIRMED / REJECTED / CANCELLED ==========
        config.configure(OrderState.VALIDATED)
                .permit(OrderTrigger.CONFIRM_PAYMENT, OrderState.PAYMENT_CONFIRMED)
                .permit(OrderTrigger.PAYMENT_FAILED, OrderState.REJECTED)
                .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
                .onEntry(this::onValidated);

        // ========== PAYMENT_CONFIRMED → PENDING_ACCEPTANCE / CANCELLED ==========
        config.configure(OrderState.PAYMENT_CONFIRMED)
                .permit(OrderTrigger.NOTIFY_VENDOR, OrderState.PENDING_ACCEPTANCE)
                .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
                .onEntry(this::onPaymentConfirmed);

        // ========== PENDING_ACCEPTANCE → ACCEPTED / REJECTED / CANCELLED ==========
        config.configure(OrderState.PENDING_ACCEPTANCE)
                .permit(OrderTrigger.ACCEPT_ORDER, OrderState.ACCEPTED)
                .permit(OrderTrigger.REJECT_ORDER, OrderState.REJECTED)
                .permit(OrderTrigger.TIMEOUT_ACCEPTANCE, OrderState.REJECTED)
                .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
                .onEntry(this::onPendingAcceptance);

        // ========== ACCEPTED → PREPARING / CANCELLED ==========
        config.configure(OrderState.ACCEPTED)
                .permit(OrderTrigger.START_PREPARATION, OrderState.PREPARING)
                .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
                .onEntry(this::onAccepted);

        // ========== PREPARING → READY_FOR_PICKUP (No Cancellation) ==========
        config.configure(OrderState.PREPARING)
                .permit(OrderTrigger.MARK_READY, OrderState.READY_FOR_PICKUP)
                .onEntry(this::onPreparing);

        // ========== READY_FOR_PICKUP → ASSIGNED_TO_RIDER ==========
        config.configure(OrderState.READY_FOR_PICKUP)
                .permit(OrderTrigger.ASSIGN_RIDER, OrderState.ASSIGNED_TO_RIDER)
                .onEntry(this::onReadyForPickup);

        // ========== ASSIGNED_TO_RIDER → PICKED_UP ==========
        config.configure(OrderState.ASSIGNED_TO_RIDER)
                .permit(OrderTrigger.RIDER_PICKUP, OrderState.PICKED_UP)
                .onEntry(this::onRiderAssigned);

        // ========== PICKED_UP → DELIVERED ==========
        config.configure(OrderState.PICKED_UP)
                .permit(OrderTrigger.DELIVER_ORDER, OrderState.DELIVERED)
                .onEntry(this::onPickedUp);

        // ========== DELIVERED → CLOSED ==========
        config.configure(OrderState.DELIVERED)
                .permit(OrderTrigger.CLOSE_ORDER, OrderState.CLOSED)
                .onEntry(this::onDelivered);

        // ========== CANCELLED (Terminal) ==========
        config.configure(OrderState.CANCELLED)
                .onEntry(this::onCancelled);

        // ========== REJECTED (Terminal) ==========
        config.configure(OrderState.REJECTED)
                .onEntry(this::onRejected);

        // ========== CLOSED (Terminal) ==========
        config.configure(OrderState.CLOSED)
                .onEntry(this::onClosed);

        return config;
    }

    // ========== Entry Callbacks - All have access to this.order ==========

    private void onValidated() {
        log.info("Order validated: {} - reserving inventory", order.getOrderId());

        // Reserve inventory
        Map<Long, Integer> itemQuantities = buildItemQuantitiesMap();
        boolean reserved = menuService.reserveStock(itemQuantities);
        if (!reserved) {
            log.error("Failed to reserve inventory for order: {}", order.getOrderId());
        }

        // Schedule payment timeout (5 minutes)
        timeoutService.schedulePaymentProcessingTimeout(order.getOrderId());

        // Update order timestamp
        order.updateStateTimestamp(OrderState.VALIDATED);
    }

    private void onPaymentConfirmed() {
        log.info("Payment confirmed for order: {}", order.getOrderId());

        // Cancel payment timeout
        timeoutService.cancelPaymentProcessingTimeout(order.getOrderId());

        // Update payment confirmed timestamp
        order.setPaymentConfirmedAt(LocalDateTime.now());
        order.updateStateTimestamp(OrderState.PAYMENT_CONFIRMED);
    }

    private void onPendingAcceptance() {
        log.info("Order submitted to vendor: {} - awaiting acceptance", order.getOrderId());

        // Schedule restaurant acceptance timeout (2 minutes)
        timeoutService.scheduleRestaurantAcceptanceTimeout(order.getOrderId());

        // Notify restaurant (TODO: implement restaurant notification)
        // notificationService.notifyRestaurant(order.getVendorId(), "New order: " +
        // order.getOrderId());

        order.updateStateTimestamp(OrderState.PENDING_ACCEPTANCE);
    }

    private void onAccepted() {
        log.info("Order accepted by vendor: {}", order.getOrderId());

        // Cancel acceptance timeout
        timeoutService.cancelRestaurantAcceptanceTimeout(order.getOrderId());

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Your order has been accepted by the restaurant!");

        order.updateStateTimestamp(OrderState.ACCEPTED);
    }

    private void onPreparing() {
        log.info("Order preparation started: {}", order.getOrderId());

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Your food is being prepared!");

        // Schedule prep timeout (30 min)
        // timeoutService.schedulePreparationTimeout(order.getOrderId());

        order.updateStateTimestamp(OrderState.PREPARING);
    }

    private void onReadyForPickup() {
        log.info("Order ready for pickup: {}", order.getOrderId());

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Your food is ready! Finding a delivery partner...");

        // NOTE: Delivery creation and rider search are triggered asynchronously
        // via the OrderEventSqsConsumer when this state change event is published.
        // Do NOT call deliveryService here to avoid race conditions.

        order.updateStateTimestamp(OrderState.READY_FOR_PICKUP);
    }

    private void onRiderAssigned() {
        log.info("Rider assigned to order: {}", order.getOrderId());

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Delivery partner assigned! They're on their way to the restaurant.");

        order.updateStateTimestamp(OrderState.ASSIGNED_TO_RIDER);
    }

    private void onPickedUp() {
        log.info("Order picked up by rider: {}", order.getOrderId());

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Your order has been picked up! It's on the way.");

        order.updateStateTimestamp(OrderState.PICKED_UP);
    }

    private void onDelivered() {
        log.info("Order delivered: {}", order.getOrderId());

        // Settle payments
        try {
            paymentService.settlePayments(order);
            log.info("Payment settlement completed for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to settle payments for order: {}", order.getOrderId(), e);
        }

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Your order has been delivered! Enjoy your meal! Please rate your experience.");

        order.updateStateTimestamp(OrderState.DELIVERED);
    }

    private void onCancelled() {
        log.info("Order cancelled: {} by {}", order.getOrderId(), cancelledBy);

        // Release inventory if it was reserved
        if (previousState == OrderState.VALIDATED ||
                previousState == OrderState.PAYMENT_CONFIRMED ||
                previousState == OrderState.PENDING_ACCEPTANCE ||
                previousState == OrderState.ACCEPTED) {

            Map<Long, Integer> itemQuantities = buildItemQuantitiesMap();
            menuService.releaseStock(itemQuantities);
        }

        // Calculate and process refund
        BigDecimal refundAmount = paymentService.calculateRefundAmount(order, cancelledBy);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                String refundTxnId = paymentService.processRefund(order, refundAmount,
                        "Order cancelled: " + transitionReason);
                log.info("Refund processed: orderId={}, amount={}, txnId={}",
                        order.getOrderId(), refundAmount, refundTxnId);

                notificationService.notifyCustomer(
                        order.getOrderId(),
                        String.format("Your order has been cancelled. Refund of ₹%s will be processed.", refundAmount));
            } catch (Exception e) {
                log.error("Failed to process refund for order: {}", order.getOrderId(), e);
            }
        }

        // Cancel any active timeouts
        cancelAllTimeouts();

        order.setCancelledBy(cancelledBy);
        order.setCancellationReason(transitionReason);
        order.updateStateTimestamp(OrderState.CANCELLED);
    }

    private void onRejected() {
        log.info("Order rejected: {} - reason: {}", order.getOrderId(), transitionReason);

        // Release inventory
        Map<Long, Integer> itemQuantities = buildItemQuantitiesMap();
        menuService.releaseStock(itemQuantities);

        // Process full refund for rejection
        try {
            BigDecimal refundAmount = order.getTotalAmount();
            String refundTxnId = paymentService.processRefund(order, refundAmount,
                    "Order rejected: " + transitionReason);
            log.info("Full refund processed for rejected order: {}, txnId={}",
                    order.getOrderId(), refundTxnId);
        } catch (Exception e) {
            log.error("Failed to process refund for rejected order: {}", order.getOrderId(), e);
        }

        // Notify customer
        notificationService.notifyCustomer(
                order.getOrderId(),
                "Sorry, your order was rejected. Full refund will be processed. Reason: " + transitionReason);

        // Cancel any active timeouts
        cancelAllTimeouts();

        order.setCancellationReason(transitionReason);
        order.updateStateTimestamp(OrderState.REJECTED);
    }

    private void onClosed() {
        log.info("Order closed: {}", order.getOrderId());
        order.updateStateTimestamp(OrderState.CLOSED);
    }

    // ========== Public API Methods ==========

    /**
     * Validate order (CREATED → VALIDATED)
     * Performs validation checks and reserves inventory
     */
    public Order validate() {
        log.info("Validating order: {}", order.getOrderId());

        // Pre-transition validation
        OrderValidationService.ValidationResult result = validationService.validateOrder(order);
        if (!result.isValid()) {
            log.error("Order validation failed: {}", result.getErrorMessage());
            transitionReason = result.getErrorMessage();
            return fireAndSave(OrderTrigger.VALIDATION_FAILED, "VALIDATION_FAILED");
        }

        return fireAndSave(OrderTrigger.VALIDATE_ORDER, "ORDER_VALIDATED");
    }

    /**
     * Confirm payment (VALIDATED → PAYMENT_CONFIRMED)
     */
    public Order confirmPayment() {
        log.info("Confirming payment for order: {}", order.getOrderId());

        // Verify payment
        boolean verified = paymentService.verifyPayment(order);
        if (!verified) {
            log.error("Payment verification failed for order: {}", order.getOrderId());
            transitionReason = "Payment verification failed";
            return fireAndSave(OrderTrigger.PAYMENT_FAILED, "PAYMENT_FAILED");
        }

        return fireAndSave(OrderTrigger.CONFIRM_PAYMENT, "PAYMENT_CONFIRMED");
    }

    /**
     * Submit to vendor (PAYMENT_CONFIRMED → PENDING_ACCEPTANCE)
     */
    public Order submitToVendor() {
        log.info("Submitting order to vendor: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.NOTIFY_VENDOR, "SUBMITTED_TO_VENDOR");
    }

    /**
     * Accept order (PENDING_ACCEPTANCE → ACCEPTED)
     */
    public Order accept() {
        log.info("Vendor accepting order: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.ACCEPT_ORDER, "ORDER_ACCEPTED");
    }

    /**
     * Reject order (PENDING_ACCEPTANCE → REJECTED)
     */
    public Order reject(String reason) {
        log.info("Vendor rejecting order: {} - {}", order.getOrderId(), reason);
        transitionReason = reason;
        return fireAndSave(OrderTrigger.REJECT_ORDER, "ORDER_REJECTED");
    }

    /**
     * Handle timeout (PENDING_ACCEPTANCE → REJECTED)
     */
    public Order timeout() {
        log.warn("Order timeout: {}", order.getOrderId());
        transitionReason = "Vendor acceptance timeout (2 minutes)";
        return fireAndSave(OrderTrigger.TIMEOUT_ACCEPTANCE, "TIMEOUT_REJECTION");
    }

    /**
     * Start preparation (ACCEPTED → PREPARING)
     */
    public Order startPreparing() {
        log.info("Starting preparation for order: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.START_PREPARATION, "PREPARATION_STARTED");
    }

    /**
     * Mark ready (PREPARING → READY_FOR_PICKUP)
     */
    public Order markReady() {
        log.info("Marking order ready: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.MARK_READY, "ORDER_READY");
    }

    /**
     * Assign rider (READY_FOR_PICKUP → ASSIGNED_TO_RIDER)
     */
    public Order assignRider() {
        log.info("Assigning rider to order: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.ASSIGN_RIDER, "RIDER_ASSIGNED");
    }

    /**
     * Pickup order (ASSIGNED_TO_RIDER → PICKED_UP)
     */
    public Order pickup() {
        log.info("Rider picking up order: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.RIDER_PICKUP, "ORDER_PICKED_UP");
    }

    /**
     * Deliver order (PICKED_UP → DELIVERED)
     */
    public Order deliver() {
        log.info("Delivering order: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.DELIVER_ORDER, "ORDER_DELIVERED");
    }

    /**
     * Close order (DELIVERED → CLOSED)
     */
    public Order close() {
        log.info("Closing order: {}", order.getOrderId());
        return fireAndSave(OrderTrigger.CLOSE_ORDER, "ORDER_CLOSED");
    }

    /**
     * Cancel order (from cancellable states → CANCELLED)
     */
    public Order cancel(String cancelledByUser, String reason) {
        if (!order.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled in state: " + order.getState());
        }

        log.info("Cancelling order: {} by {} - {}", order.getOrderId(), cancelledByUser, reason);
        this.cancelledBy = cancelledByUser;
        this.transitionReason = reason;
        return fireAndSave(OrderTrigger.CANCEL_ORDER, "ORDER_CANCELLED");
    }

    // ========== Core FSM Methods ==========

    /**
     * Fire trigger and save order with audit
     */
    private Order fireAndSave(OrderTrigger trigger, String action) {
        // Validate transition is allowed
        if (!sm.canFire(trigger)) {
            throw new IllegalStateException(
                    String.format("Cannot fire %s in state %s for order %s",
                            trigger, order.getState(), order.getOrderId()));
        }

        // Store previous state for audit
        previousState = order.getState();

        // Fire trigger - this calls onEntry() with access to this.order
        sm.fire(trigger);

        // Sync order state with FSM
        OrderState newState = sm.getState();
        order.setState(newState);

        // Persist order
        Order savedOrder = orderRepository.save(order);

        // Create audit record
        createAuditRecord(previousState, newState, action);

        // Publish event
        publishStateChange(previousState, newState);

        log.info("Order {} transitioned: {} → {} ({})",
                order.getOrderId(), previousState, newState, action);

        return savedOrder;
    }

    /**
     * Check if a trigger can be fired
     */
    public boolean canFire(OrderTrigger trigger) {
        return sm.canFire(trigger);
    }

    /**
     * Get current state
     */
    public OrderState getState() {
        return sm.getState();
    }

    /**
     * Get the order
     */
    public Order getOrder() {
        return order;
    }

    // ========== Helper Methods ==========

    private void createAuditRecord(OrderState fromState, OrderState toState, String action) {
        try {
            // Use the static factory method from OrderStateAudit
            OrderStateAudit audit = OrderStateAudit.create(
                    order.getOrderId(),
                    fromState != null ? fromState.name() : null,
                    toState.name(),
                    action, // triggerName
                    actorId != null ? actorId : order.getCustomerId(),
                    actorType != null ? actorType : "SYSTEM");

            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to create audit record for order: {}", order.getOrderId(), e);
        }
    }

    private void publishStateChange(OrderState fromState, OrderState toState) {
        try {
            // Build enhanced metadata with location info for delivery creation
            Map<String, Object> enrichedMetadata = new HashMap<>();
            if (order.getMetadata() != null) {
                enrichedMetadata.putAll(order.getMetadata());
            }

            // For READY_FOR_PICKUP state, add pickup and delivery location data
            if (toState == OrderState.READY_FOR_PICKUP) {
                // Get pickup location from Order's PostGIS Point geometry
                if (order.getPickupLocation() != null) {
                    String pickupLocationJson = GeometryUtils.toJson(order.getPickupLocation());
                    enrichedMetadata.put("pickupLocation", pickupLocationJson);
                    log.debug("Added pickup location for order {}: {}", order.getOrderId(), pickupLocationJson);
                } else {
                    log.warn("Order {} missing pickup location", order.getOrderId());
                }

                // Get delivery location from Order's PostGIS Point geometry
                if (order.getDeliveryLocation() != null) {
                    String deliveryLocationJson = GeometryUtils.toJson(order.getDeliveryLocation());
                    enrichedMetadata.put("deliveryLocation", deliveryLocationJson);
                    log.debug("Added delivery location for order {}: {}", order.getOrderId(), deliveryLocationJson);
                } else {
                    log.warn("Order {} missing delivery location", order.getOrderId());
                }

                // Add delivery fee
                enrichedMetadata.put("deliveryFee", order.getDeliveryCharges());
            }

            eventPublisher.publishOrderStateChange(
                    order.getOrderId(),
                    fromState != null ? fromState.name() : null,
                    toState.name(),
                    null,
                    order.getCustomerId(),
                    null,
                    enrichedMetadata);
        } catch (Exception e) {
            log.error("Failed to publish state change for order: {}", order.getOrderId(), e);
        }
    }

    private Map<Long, Integer> buildItemQuantitiesMap() {
        Map<Long, Integer> itemQuantities = new HashMap<>();

        try {
            List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
            for (OrderItem item : items) {
                itemQuantities.put(item.getMenuItemId(), item.getQuantity());
            }
        } catch (Exception e) {
            log.error("Error building item quantities map for order: {}", order.getOrderId(), e);
        }

        return itemQuantities;
    }

    private void cancelAllTimeouts() {
        try {
            timeoutService.cancelPaymentProcessingTimeout(order.getOrderId());
            timeoutService.cancelRestaurantAcceptanceTimeout(order.getOrderId());
            timeoutService.cancelRiderAssignmentTimeout(order.getOrderId());
        } catch (Exception e) {
            log.warn("Error cancelling timeouts for order: {}", order.getOrderId(), e);
        }
    }

    /**
     * Set actor info for audit trail
     */
    public OrderStateMachine withActor(UUID actorId, String actorType) {
        this.actorId = actorId;
        this.actorType = actorType;
        return this;
    }
}
