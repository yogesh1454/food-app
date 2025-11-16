package com.teadelivery.ordercatalog.order.fsm;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.teadelivery.ordercatalog.common.fsm.EventPublisher;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import com.teadelivery.ordercatalog.menu.service.MenuService;
import com.teadelivery.ordercatalog.notification.service.NotificationService;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.repository.OrderItemRepository;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.service.OrderTimeoutService;
import com.teadelivery.ordercatalog.order.service.OrderValidationService;
import com.teadelivery.ordercatalog.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order Finite State Machine
 * Manages the complete lifecycle of an order with 13 states and 14 triggers
 * 
 * States: CREATED, VALIDATED, PAYMENT_CONFIRMED, PENDING_ACCEPTANCE, ACCEPTED,
 *         PREPARING, READY_FOR_PICKUP, ASSIGNED_TO_RIDER, PICKED_UP, DELIVERED,
 *         CLOSED, CANCELLED, REJECTED
 * 
 * Cancellation Policy:
 * - Can cancel: CREATED, VALIDATED, PAYMENT_CONFIRMED, PENDING_ACCEPTANCE, ACCEPTED
 * - Cannot cancel: PREPARING onwards (food already being prepared)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFSM {
    
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final OrderTimeoutService timeoutService;
    private final DeliveryService deliveryService;
    private final PaymentService paymentService;
    private final OrderValidationService validationService;
    private final MenuService menuService;
    private final OrderItemRepository orderItemRepository;
    
    private final StateMachineConfig<OrderState, OrderTrigger> config;
    
    public OrderFSM(
        OrderRepository orderRepository,
        EventPublisher eventPublisher,
        NotificationService notificationService,
        OrderTimeoutService timeoutService,
        DeliveryService deliveryService,
        PaymentService paymentService,
        OrderValidationService validationService,
        MenuService menuService,
        OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
        this.timeoutService = timeoutService;
        this.deliveryService = deliveryService;
        this.paymentService = paymentService;
        this.validationService = validationService;
        this.menuService = menuService;
        this.orderItemRepository = orderItemRepository;
        this.config = configureStateMachine();
    }
    
    private StateMachineConfig<OrderState, OrderTrigger> configureStateMachine() {
        StateMachineConfig<OrderState, OrderTrigger> config = new StateMachineConfig<>();
        
        // ========== CREATED → VALIDATED / REJECTED ==========
        config.configure(OrderState.CREATED)
            .permit(OrderTrigger.VALIDATE_ORDER, OrderState.VALIDATED)
            .permit(OrderTrigger.VALIDATION_FAILED, OrderState.REJECTED)
            .onEntry(this::onOrderCreated);
        
        // ========== VALIDATED → PAYMENT_CONFIRMED / REJECTED / CANCELLED ==========
        config.configure(OrderState.VALIDATED)
            .permit(OrderTrigger.CONFIRM_PAYMENT, OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.PAYMENT_FAILED, OrderState.REJECTED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onOrderValidated);
        
        // ========== PAYMENT_CONFIRMED → PENDING_ACCEPTANCE ==========
        config.configure(OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.NOTIFY_VENDOR, OrderState.PENDING_ACCEPTANCE)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onPaymentConfirmed);
        
        // ========== PENDING_ACCEPTANCE → ACCEPTED / REJECTED ==========
        config.configure(OrderState.PENDING_ACCEPTANCE)
            .permit(OrderTrigger.ACCEPT_ORDER, OrderState.ACCEPTED)
            .permit(OrderTrigger.REJECT_ORDER, OrderState.REJECTED)
            .permit(OrderTrigger.TIMEOUT_ACCEPTANCE, OrderState.REJECTED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onPendingAcceptance);
        
        // ========== ACCEPTED → PREPARING ==========
        config.configure(OrderState.ACCEPTED)
            .permit(OrderTrigger.START_PREPARATION, OrderState.PREPARING)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onOrderAccepted);
        
        // ========== PREPARING → READY_FOR_PICKUP (No Cancellation Allowed) ==========
        config.configure(OrderState.PREPARING)
            .permit(OrderTrigger.MARK_READY, OrderState.READY_FOR_PICKUP)
            .onEntry(this::onPreparingStarted);
        
        // ========== READY_FOR_PICKUP → ASSIGNED_TO_RIDER (No Cancellation Allowed) ==========
        config.configure(OrderState.READY_FOR_PICKUP)
            .permit(OrderTrigger.ASSIGN_RIDER, OrderState.ASSIGNED_TO_RIDER)
            .onEntry(this::onReadyForPickup);
        
        // ========== ASSIGNED_TO_RIDER → PICKED_UP (No Cancellation Allowed) ==========
        config.configure(OrderState.ASSIGNED_TO_RIDER)
            .permit(OrderTrigger.RIDER_PICKUP, OrderState.PICKED_UP)
            .onEntry(this::onRiderAssigned);
        
        // ========== PICKED_UP → DELIVERED (No Cancellation Allowed) ==========
        config.configure(OrderState.PICKED_UP)
            .permit(OrderTrigger.DELIVER_ORDER, OrderState.DELIVERED)
            .onEntry(this::onPickedUp);
        
        // ========== DELIVERED → CLOSED ==========
        config.configure(OrderState.DELIVERED)
            .permit(OrderTrigger.CLOSE_ORDER, OrderState.CLOSED)
            .onEntry(this::onDelivered);
        
        // ========== CANCELLED (Terminal State) ==========
        config.configure(OrderState.CANCELLED)
            .onEntry(this::onCancelled);
        
        // ========== REJECTED (Terminal State) ==========
        config.configure(OrderState.REJECTED)
            .onEntry(this::onRejected);
        
        // ========== CLOSED (Terminal State - After Delivery) ==========
        config.configure(OrderState.CLOSED)
            .onEntry(this::onClosed);
        
        return config;
    }
    
    /**
     * Validate if transition is allowed
     */
    private void validateTransition(Order order, OrderTrigger trigger) {
        StateMachine<OrderState, OrderTrigger> sm = new StateMachine<>(order.getState(), config);
        if (!sm.canFire(trigger)) {
            throw new IllegalStateException(
                String.format("Cannot fire trigger %s in state %s for order %s",
                    trigger, order.getState(), order.getOrderId())
            );
        }
    }
    
    // ========== State Entry Actions with Business Logic ==========
    
    private void onOrderCreated() {
        log.info("Order created - awaiting validation");
        // Entry actions for CREATED state
        // - Order ID generated (done in OrderService)
        // - Initial state set (done in OrderService)
    }
    
    private void onOrderValidated() {
        log.info("Order validated - awaiting payment confirmation");
        // Entry actions for VALIDATED state
        // - Validation complete
        // - Ready for payment processing
    }
    
    private void onPaymentConfirmed() {
        log.info("Payment confirmed - submitting to vendor");
        // Entry actions for PAYMENT_CONFIRMED state
        // - Payment captured/reserved (done in OrderService)
        // - Set Redis TTL for auto-cancel (5 min)
        // Note: Timeout scheduling moved to OrderService to avoid circular dependency
    }
    
    private void onPendingAcceptance() {
        log.info("Order pending vendor acceptance - timeout in 2 minutes");
        // Entry actions for PENDING_ACCEPTANCE state
        // - Restaurant notification (handled in OrderService)
        // - Acceptance timeout scheduled (handled in OrderService)
    }
    
    private void onOrderAccepted() {
        log.info("Order accepted by vendor - ready to start preparation");
        // Entry actions for ACCEPTED state
        // - Cancel acceptance timeout (done in OrderService)
        // - Notify customer of acceptance
    }
    
    private void onPreparingStarted() {
        log.info("Order preparation started");
        // Entry actions for PREPARING state
        // - Notify customer (food being prepared)
        // - Schedule prep timeout (30 min)
    }
    
    private void onReadyForPickup() {
        log.info("Order ready for pickup - assigning rider");
        // Entry actions for READY_FOR_PICKUP state
        // - Notify customer (food ready)
        // - Publish event to trigger Delivery FSM
        // - Start rider search
    }
    
    private void onRiderAssigned() {
        log.info("Rider assigned - awaiting pickup");
        // Entry actions for ASSIGNED_TO_RIDER state
        // - Notify customer (rider assigned)
        // - Notify restaurant (rider coming)
    }
    
    private void onPickedUp() {
        log.info("Order picked up by rider - in transit");
        // Entry actions for PICKED_UP state
        // - Notify customer (order picked up, on the way)
    }
    
    private void onDelivered() {
        log.info("Order delivered successfully");
        // Entry actions for DELIVERED state
        // - Notify customer (order delivered)
        // - Settle payments (restaurant, rider, platform)
        // - Trigger rating prompt
    }
    
    private void onCancelled() {
        log.info("Order cancelled");
        // Entry actions for CANCELLED state
        // - Process refund (based on cancellation state)
        // - Notify all parties
        // - Release inventory
    }
    
    private void onRejected() {
        log.info("Order rejected by vendor");
        // Entry actions for REJECTED state
        // - Process full refund
        // - Notify customer with reason
    }
    
    private void onClosed() {
        log.info("Order closed - lifecycle complete");
        // Entry actions for CLOSED state
        // - Archive order
        // - Update analytics
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Publish order state change event
     * Helper method to simplify event publishing from FSM methods
     */
    private void publishStateChange(Order order, OrderState newState) {
        try {
            eventPublisher.publishOrderStateChange(
                order.getOrderId(),
                order.getState() != null ? order.getState().name() : null,
                newState.name(),
                null, // trigger - can be enhanced later
                order.getCustomerId(),
                null, // restaurantId - TODO: add to Order model
                order.getMetadata()
            );
        } catch (Exception e) {
            log.error("Failed to publish state change for order: {}", order.getOrderId(), e);
        }
    }
    
    // ========== Public API Methods ==========
    
    /**
     * Validate order (CREATED → VALIDATED)
     * Performs all 8 validation checks as per design document
     */
    public void validateOrder(Order order) {
        validateTransition(order, OrderTrigger.VALIDATE_ORDER);
        log.info("Validating order: {}", order.getOrderId());
        
        // Perform comprehensive validation
        OrderValidationService.ValidationResult result = validationService.validateOrder(order);
        
        if (!result.isValid()) {
            // Validation failed - transition to REJECTED
            String errorMessage = result.getErrorMessage();
            log.error("Order validation failed: {}, errors: {}", order.getOrderId(), errorMessage);
            validationFailed(order, errorMessage);
            throw new IllegalStateException("Order validation failed: " + errorMessage);
        }
        
        // Transition state
        order.setState(OrderState.VALIDATED);
        order.updateStateTimestamp(OrderState.VALIDATED);
        
        // Reserve inventory via MenuService
        java.util.Map<Long, Integer> itemQuantities = buildItemQuantitiesMap(order);
        boolean inventoryReserved = menuService.reserveStock(itemQuantities);
        if (!inventoryReserved) {
            log.error("Failed to reserve inventory for order: {}", order.getOrderId());
            validationFailed(order, "Failed to reserve inventory");
            throw new IllegalStateException("Failed to reserve inventory");
        }
        
        // Publish event
        publishStateChange(order, OrderState.VALIDATED);
        
        // Schedule payment timeout (5 minutes)
        timeoutService.schedulePaymentProcessingTimeout(order.getOrderId());
        
        log.info("Order validated successfully: {}", order.getOrderId());
    }
    
    /**
     * Confirm payment (VALIDATED → PAYMENT_CONFIRMED)
     * Verifies payment and transitions to confirmed state
     */
    public void confirmPayment(Order order) {
        validateTransition(order, OrderTrigger.CONFIRM_PAYMENT);
        log.info("Confirming payment for order: {}", order.getOrderId());
        
        // Verify payment
        boolean paymentVerified = paymentService.verifyPayment(order);
        if (!paymentVerified) {
            log.error("Payment verification failed for order: {}", order.getOrderId());
            paymentFailed(order, "Payment verification failed");
            throw new IllegalStateException("Payment verification failed");
        }
        
        // Cancel payment timeout
        timeoutService.cancelPaymentProcessingTimeout(order.getOrderId());
        
        // Transition state
        order.setState(OrderState.PAYMENT_CONFIRMED);
        order.updateStateTimestamp(OrderState.PAYMENT_CONFIRMED);
        
        // Publish event
        publishStateChange(order, OrderState.PAYMENT_CONFIRMED);
        
        log.info("Payment confirmed for order: {}", order.getOrderId());
    }
    
    /**
     * Submit to vendor (PAYMENT_CONFIRMED → PENDING_ACCEPTANCE)
     */
    public void submitToVendor(Order order) {
        validateTransition(order, OrderTrigger.NOTIFY_VENDOR);
        log.info("Submitting order to vendor: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.PENDING_ACCEPTANCE);
        order.updateStateTimestamp(OrderState.PENDING_ACCEPTANCE);
        
        // Publish event
        publishStateChange(order, OrderState.PENDING_ACCEPTANCE);
        
        // TODO: Notify restaurant (need to add restaurantId to Order model)
        // notificationService.notifyRestaurant(
        //     order.getRestaurantId(),
        //     "New order received: #" + order.getOrderId()
        // );
    }
    
    /**
     * Accept order (PENDING_ACCEPTANCE → ACCEPTED)
     * Vendor accepts the order and starts preparation timer
     */
    public void acceptOrder(Order order) {
        validateTransition(order, OrderTrigger.ACCEPT_ORDER);
        log.info("Vendor accepting order: {}", order.getOrderId());
        
        // Cancel restaurant acceptance timeout
        timeoutService.cancelRestaurantAcceptanceTimeout(order.getOrderId());
        
        // Transition state
        order.setState(OrderState.ACCEPTED);
        order.updateStateTimestamp(OrderState.ACCEPTED);
        
        // Publish event
        publishStateChange(order, OrderState.ACCEPTED);
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Your order has been accepted by the restaurant!"
        );
        
        log.info("Order accepted by vendor: {}", order.getOrderId());
    }
    
    /**
     * Reject order (PENDING_ACCEPTANCE → REJECTED)
     * Vendor rejects the order - full refund issued
     */
    public void rejectOrder(Order order, String reason) {
        validateTransition(order, OrderTrigger.REJECT_ORDER);
        log.info("Vendor rejecting order: {} - Reason: {}", order.getOrderId(), reason);
        
        // Cancel restaurant acceptance timeout
        timeoutService.cancelRestaurantAcceptanceTimeout(order.getOrderId());
        
        // Transition state
        order.setState(OrderState.REJECTED);
        order.setCancellationReason(reason);
        order.updateStateTimestamp(OrderState.REJECTED);
        
        // Publish event
        publishStateChange(order, OrderState.REJECTED);
        
        // Release inventory via MenuService
        java.util.Map<Long, Integer> itemQuantities = buildItemQuantitiesMap(order);
        menuService.releaseStock(itemQuantities);
        
        // Process full refund (vendor rejection = full refund)
        try {
            BigDecimal refundAmount = order.getTotalAmount();
            String refundTxnId = paymentService.processRefund(order, refundAmount, "Vendor rejected: " + reason);
            log.info("Refund processed for rejected order: {}, refundTxnId: {}", order.getOrderId(), refundTxnId);
        } catch (Exception e) {
            log.error("Failed to process refund for rejected order: {}", order.getOrderId(), e);
        }
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Sorry, your order was rejected. Full refund will be processed. Reason: " + reason
        );
        
        log.info("Order rejected by vendor: {}", order.getOrderId());
    }
    
    /**
     * Handle timeout (PENDING_ACCEPTANCE → REJECTED)
     */
    public void handleTimeout(Order order) {
        validateTransition(order, OrderTrigger.TIMEOUT_ACCEPTANCE);
        log.warn("Order timeout: {}", order.getOrderId());
        order.setState(OrderState.REJECTED);
        order.setCancellationReason("Vendor acceptance timeout (2 minutes)");
        order.updateStateTimestamp(OrderState.REJECTED);
    }
    
    /**
     * Start preparing (ACCEPTED → PREPARING)
     */
    public void startPreparing(Order order) {
        validateTransition(order, OrderTrigger.START_PREPARATION);
        log.info("Starting preparation for order: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.PREPARING);
        order.updateStateTimestamp(OrderState.PREPARING);
        
        // Publish event
        publishStateChange(order, OrderState.PREPARING);
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Your food is being prepared!"
        );
    }
    
    /**
     * Mark ready (PREPARING → READY_FOR_PICKUP)
     * CRITICAL: This triggers Delivery FSM
     */
    public void markReady(Order order) {
        validateTransition(order, OrderTrigger.MARK_READY);
        log.info("Marking order ready for pickup: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.READY_FOR_PICKUP);
        order.updateStateTimestamp(OrderState.READY_FOR_PICKUP);
        
        // Publish event (CRITICAL - triggers Delivery FSM)
        publishStateChange(order, OrderState.READY_FOR_PICKUP);
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Your food is ready! Finding a delivery partner..."
        );
        
        // Trigger delivery assignment
        try {
            deliveryService.startRiderSearchByOrderId(order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to start rider search for order: {}", order.getOrderId(), e);
        }
    }
    
    /**
     * Assign rider (READY_FOR_PICKUP → ASSIGNED_TO_RIDER)
     * Called by DeliveryEventConsumer when rider accepts
     */
    public void assignRider(Order order) {
        validateTransition(order, OrderTrigger.ASSIGN_RIDER);
        log.info("Assigning rider to order: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.ASSIGNED_TO_RIDER);
        order.updateStateTimestamp(OrderState.ASSIGNED_TO_RIDER);
        
        // Publish event
        publishStateChange(order, OrderState.ASSIGNED_TO_RIDER);
        
        // Notify customer (rider details added by DeliveryEventConsumer)
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Delivery partner assigned! They're on their way to the restaurant."
        );
        
        // TODO: Notify restaurant (need to add restaurantId to Order model)
        // notificationService.notifyRestaurant(
        //     order.getRestaurantId(),
        //     "Delivery partner is coming to pick up order #" + order.getOrderId()
        // );
    }
    
    /**
     * Pickup order (ASSIGNED_TO_RIDER → PICKED_UP)
     * Called by DeliveryEventConsumer when rider picks up
     */
    public void pickupOrder(Order order) {
        validateTransition(order, OrderTrigger.RIDER_PICKUP);
        log.info("Rider picking up order: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.PICKED_UP);
        order.updateStateTimestamp(OrderState.PICKED_UP);
        
        // Publish event
        publishStateChange(order, OrderState.PICKED_UP);
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Your order has been picked up! It's on the way."
        );
    }
    
    /**
     * Deliver order (PICKED_UP → DELIVERED)
     * Called by DeliveryEventConsumer when order is delivered
     * Settles payments to vendor, rider, and platform
     */
    public void deliverOrder(Order order) {
        validateTransition(order, OrderTrigger.DELIVER_ORDER);
        log.info("Delivering order: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.DELIVERED);
        order.updateStateTimestamp(OrderState.DELIVERED);
        
        // Publish event
        publishStateChange(order, OrderState.DELIVERED);
        
        // Settle payments (distribute to vendor, rider, platform)
        try {
            paymentService.settlePayments(order);
            log.info("Payment settlement completed for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to settle payments for order: {}", order.getOrderId(), e);
            // Don't fail delivery - settlement can be retried
        }
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Your order has been delivered! Enjoy your meal! Please rate your experience."
        );
        
        // TODO: Trigger rating prompt after 5 minutes
        // TODO: Schedule auto-close after 24 hours
        
        log.info("Order delivered successfully: {}", order.getOrderId());
    }
    
    /**
     * Validation failed (CREATED → REJECTED)
     */
    public void validationFailed(Order order, String reason) {
        validateTransition(order, OrderTrigger.VALIDATION_FAILED);
        log.warn("Order validation failed: {} - Reason: {}", order.getOrderId(), reason);
        
        // Transition state
        order.setState(OrderState.REJECTED);
        order.setCancellationReason(reason);
        order.updateStateTimestamp(OrderState.REJECTED);
        
        // Publish event
        publishStateChange(order, OrderState.REJECTED);
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Order validation failed: " + reason
        );
        
        // TODO: Process full refund
    }
    
    /**
     * Payment failed (VALIDATED → REJECTED)
     */
    public void paymentFailed(Order order, String reason) {
        validateTransition(order, OrderTrigger.PAYMENT_FAILED);
        log.warn("Payment failed for order: {} - Reason: {}", order.getOrderId(), reason);
        
        // Transition state
        order.setState(OrderState.REJECTED);
        order.setCancellationReason(reason);
        order.updateStateTimestamp(OrderState.REJECTED);
        
        // Publish event
        publishStateChange(order, OrderState.REJECTED);
        
        // Notify customer
        notificationService.notifyCustomer(
            order.getOrderId(),
            "Payment failed: " + reason
        );
        
        // No refund needed - payment never succeeded
    }
    
    /**
     * Close order (DELIVERED → CLOSED)
     * Auto-triggered 24 hours after delivery or when customer rates
     */
    public void closeOrder(Order order) {
        validateTransition(order, OrderTrigger.CLOSE_ORDER);
        log.info("Closing order: {}", order.getOrderId());
        
        // Transition state
        order.setState(OrderState.CLOSED);
        order.updateStateTimestamp(OrderState.CLOSED);
        
        // Publish event
        publishStateChange(order, OrderState.CLOSED);
        
        // TODO: Archive order
        // TODO: Update analytics
    }
    
    /**
     * Cancel order (from any cancellable state → CANCELLED)
     * Applies cancellation fees based on current state and cancellation policy
     */
    public void cancelOrder(Order order, String cancelledBy, String reason) {
        if (!order.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled in state: " + order.getState());
        }
        
        validateTransition(order, OrderTrigger.CANCEL_ORDER);
        log.info("Cancelling order: {} by {} - Reason: {}", order.getOrderId(), cancelledBy, reason);
        
        // Calculate refund amount based on cancellation policy
        BigDecimal refundAmount = paymentService.calculateRefundAmount(order, cancelledBy);
        
        // Transition state
        order.setState(OrderState.CANCELLED);
        order.setCancelledBy(cancelledBy);
        order.setCancellationReason(reason);
        order.updateStateTimestamp(OrderState.CANCELLED);
        
        // Publish event
        publishStateChange(order, OrderState.CANCELLED);
        
        // Release inventory if order was validated
        if (order.getState() == OrderState.VALIDATED || 
            order.getState() == OrderState.PAYMENT_CONFIRMED ||
            order.getState() == OrderState.PENDING_ACCEPTANCE ||
            order.getState() == OrderState.ACCEPTED) {
            java.util.Map<Long, Integer> itemQuantities = buildItemQuantitiesMap(order);
            menuService.releaseStock(itemQuantities);
        }
        
        // Process refund
        try {
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                String refundTxnId = paymentService.processRefund(order, refundAmount, "Order cancelled: " + reason);
                log.info("Refund processed for cancelled order: {}, amount: {}, refundTxnId: {}", 
                    order.getOrderId(), refundAmount, refundTxnId);
                
                // Notify customer about refund
                notificationService.notifyCustomer(
                    order.getOrderId(),
                    String.format("Your order has been cancelled. Refund of ₹%s will be processed. %s", 
                        refundAmount, reason)
                );
            } else {
                notificationService.notifyCustomer(
                    order.getOrderId(),
                    "Your order has been cancelled. " + reason
                );
            }
        } catch (Exception e) {
            log.error("Failed to process refund for cancelled order: {}", order.getOrderId(), e);
            notificationService.notifyCustomer(
                order.getOrderId(),
                "Your order has been cancelled. Refund processing failed, please contact support."
            );
        }
        
        // Cancel any active timeouts
        try {
            timeoutService.cancelRestaurantAcceptanceTimeout(order.getOrderId());
            timeoutService.cancelPaymentProcessingTimeout(order.getOrderId());
            timeoutService.cancelRiderAssignmentTimeout(order.getOrderId());
        } catch (Exception e) {
            log.warn("Error cancelling timeouts for order: {}", order.getOrderId(), e);
        }
        
        // TODO: Notify vendor if order was accepted
        
        log.info("Order cancelled: {}, refund: ₹{}", order.getOrderId(), refundAmount);
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Build map of menuItemId -> quantity from order items
     * Used for inventory/stock operations via MenuService
     */
    private java.util.Map<Long, Integer> buildItemQuantitiesMap(Order order) {
        java.util.Map<Long, Integer> itemQuantities = new java.util.HashMap<>();
        
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
        for (OrderItem item : items) {
            itemQuantities.put(item.getMenuItemId(), item.getQuantity());
        }
        
        return itemQuantities;
    }
}
