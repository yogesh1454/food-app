package com.teadelivery.ordercatalog.fsm;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.teadelivery.ordercatalog.fsm.base.InvalidStateTransitionException;
import com.teadelivery.ordercatalog.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Order Finite State Machine
 * Manages the complete lifecycle of an order with 13 states and 12 triggers
 */
@Slf4j
@Component
public class OrderFSM {
    
    private final StateMachineConfig<OrderState, OrderTrigger> config;
    
    public OrderFSM() {
        this.config = configureStateMachine();
    }
    
    private StateMachineConfig<OrderState, OrderTrigger> configureStateMachine() {
        StateMachineConfig<OrderState, OrderTrigger> config = new StateMachineConfig<>();
        
        // ========== CREATED → VALIDATED ==========
        config.configure(OrderState.CREATED)
            .permit(OrderTrigger.VALIDATE_ORDER, OrderState.VALIDATED)
            .onEntry(this::onOrderCreated);
        
        // ========== VALIDATED → PAYMENT_CONFIRMED ==========
        config.configure(OrderState.VALIDATED)
            .permit(OrderTrigger.CONFIRM_PAYMENT, OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onOrderValidated);
        
        // ========== PAYMENT_CONFIRMED → PENDING_ACCEPTANCE ==========
        config.configure(OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.NOTIFY_RESTAURANT, OrderState.PENDING_ACCEPTANCE)
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
        
        // ========== PREPARING → READY_FOR_PICKUP ==========
        config.configure(OrderState.PREPARING)
            .permit(OrderTrigger.MARK_READY, OrderState.READY_FOR_PICKUP)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onPreparingStarted);
        
        // ========== READY_FOR_PICKUP → ASSIGNED_TO_RIDER ==========
        config.configure(OrderState.READY_FOR_PICKUP)
            .permit(OrderTrigger.ASSIGN_RIDER, OrderState.ASSIGNED_TO_RIDER)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onReadyForPickup);
        
        // ========== ASSIGNED_TO_RIDER → PICKED_UP ==========
        config.configure(OrderState.ASSIGNED_TO_RIDER)
            .permit(OrderTrigger.RIDER_PICKUP, OrderState.PICKED_UP)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onRiderAssigned);
        
        // ========== PICKED_UP → DELIVERED ==========
        config.configure(OrderState.PICKED_UP)
            .permit(OrderTrigger.DELIVER_ORDER, OrderState.DELIVERED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED)
            .onEntry(this::onPickedUp);
        
        // ========== DELIVERED (Terminal State) ==========
        config.configure(OrderState.DELIVERED)
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
            throw new InvalidStateTransitionException(
                String.format("Cannot fire trigger %s in state %s for order %s",
                    trigger, order.getState(), order.getOrderId())
            );
        }
    }
    
    // ========== State Entry Actions ==========
    
    private void onOrderCreated() {
        log.info("Order created - awaiting validation");
    }
    
    private void onOrderValidated() {
        log.info("Order validated - awaiting payment confirmation");
    }
    
    private void onPaymentConfirmed() {
        log.info("Payment confirmed - submitting to vendor");
    }
    
    private void onPendingAcceptance() {
        log.info("Order pending vendor acceptance - timeout in 2 minutes");
        // Timeout will be handled by TimeoutService
    }
    
    private void onOrderAccepted() {
        log.info("Order accepted by vendor - ready to start preparation");
    }
    
    private void onPreparingStarted() {
        log.info("Order preparation started");
    }
    
    private void onReadyForPickup() {
        log.info("Order ready for pickup - assigning rider");
    }
    
    private void onRiderAssigned() {
        log.info("Rider assigned - awaiting pickup");
    }
    
    private void onPickedUp() {
        log.info("Order picked up by rider - in transit");
    }
    
    private void onDelivered() {
        log.info("Order delivered successfully");
    }
    
    private void onCancelled() {
        log.info("Order cancelled");
    }
    
    private void onRejected() {
        log.info("Order rejected by vendor");
    }
    
    private void onClosed() {
        log.info("Order closed - lifecycle complete");
    }
    
    // ========== Public API Methods ==========
    
    /**
     * Validate order (CREATED → VALIDATED)
     */
    public void validateOrder(Order order) {
        validateTransition(order, OrderTrigger.VALIDATE_ORDER);
        log.info("Validating order: {}", order.getOrderId());
        order.setState(OrderState.VALIDATED);
        order.updateStateTimestamp(OrderState.VALIDATED);
    }
    
    /**
     * Confirm payment (VALIDATED → PAYMENT_CONFIRMED)
     */
    public void confirmPayment(Order order) {
        validateTransition(order, OrderTrigger.CONFIRM_PAYMENT);
        log.info("Confirming payment for order: {}", order.getOrderId());
        order.setState(OrderState.PAYMENT_CONFIRMED);
        order.updateStateTimestamp(OrderState.PAYMENT_CONFIRMED);
    }
    
    /**
     * Submit to vendor (PAYMENT_CONFIRMED → PENDING_ACCEPTANCE)
     */
    public void submitToVendor(Order order) {
        validateTransition(order, OrderTrigger.NOTIFY_RESTAURANT);
        log.info("Submitting order to vendor: {}", order.getOrderId());
        order.setState(OrderState.PENDING_ACCEPTANCE);
        order.updateStateTimestamp(OrderState.PENDING_ACCEPTANCE);
    }
    
    /**
     * Accept order (PENDING_ACCEPTANCE → ACCEPTED)
     */
    public void acceptOrder(Order order) {
        validateTransition(order, OrderTrigger.ACCEPT_ORDER);
        log.info("Vendor accepting order: {}", order.getOrderId());
        order.setState(OrderState.ACCEPTED);
        order.updateStateTimestamp(OrderState.ACCEPTED);
    }
    
    /**
     * Reject order (PENDING_ACCEPTANCE → REJECTED)
     */
    public void rejectOrder(Order order, String reason) {
        validateTransition(order, OrderTrigger.REJECT_ORDER);
        log.info("Vendor rejecting order: {} - Reason: {}", order.getOrderId(), reason);
        order.setState(OrderState.REJECTED);
        order.setCancellationReason(reason);
        order.updateStateTimestamp(OrderState.REJECTED);
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
        order.setState(OrderState.PREPARING);
        order.updateStateTimestamp(OrderState.PREPARING);
    }
    
    /**
     * Mark ready (PREPARING → READY_FOR_PICKUP)
     */
    public void markReady(Order order) {
        validateTransition(order, OrderTrigger.MARK_READY);
        log.info("Marking order ready for pickup: {}", order.getOrderId());
        order.setState(OrderState.READY_FOR_PICKUP);
        order.updateStateTimestamp(OrderState.READY_FOR_PICKUP);
    }
    
    /**
     * Assign rider (READY_FOR_PICKUP → ASSIGNED_TO_RIDER)
     */
    public void assignRider(Order order) {
        validateTransition(order, OrderTrigger.ASSIGN_RIDER);
        log.info("Assigning rider to order: {}", order.getOrderId());
        order.setState(OrderState.ASSIGNED_TO_RIDER);
        order.updateStateTimestamp(OrderState.ASSIGNED_TO_RIDER);
    }
    
    /**
     * Pickup order (ASSIGNED_TO_RIDER → PICKED_UP)
     */
    public void pickupOrder(Order order) {
        validateTransition(order, OrderTrigger.RIDER_PICKUP);
        log.info("Rider picking up order: {}", order.getOrderId());
        order.setState(OrderState.PICKED_UP);
        order.updateStateTimestamp(OrderState.PICKED_UP);
    }
    
    /**
     * Deliver order (PICKED_UP → DELIVERED)
     */
    public void deliverOrder(Order order) {
        validateTransition(order, OrderTrigger.DELIVER_ORDER);
        log.info("Delivering order: {}", order.getOrderId());
        order.setState(OrderState.DELIVERED);
        order.updateStateTimestamp(OrderState.DELIVERED);
    }
    
    /**
     * Cancel order (from any cancellable state → CANCELLED)
     */
    public void cancelOrder(Order order, String cancelledBy, String reason) {
        if (!order.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled in state: " + order.getState());
        }
        
        validateTransition(order, OrderTrigger.CANCEL_ORDER);
        log.info("Cancelling order: {} by {} - Reason: {}", order.getOrderId(), cancelledBy, reason);
        order.setState(OrderState.CANCELLED);
        order.setCancelledBy(cancelledBy);
        order.setCancellationReason(reason);
        order.updateStateTimestamp(OrderState.CANCELLED);
    }
}
