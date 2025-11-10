package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.order.model.OrderStateAudit;
import com.teadelivery.ordercatalog.order.repository.OrderStateAuditRepository;
import com.teadelivery.ordercatalog.order.fsm.OrderFSM;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.OrderType;
import com.teadelivery.ordercatalog.order.fsm.PaymentStatus;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.model.SubOrder;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.repository.SubOrderRepository;
import com.teadelivery.ordercatalog.order.timeout.OrderTimeoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Order Service
 * Business logic layer for order management with FSM integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final OrderStateAuditRepository auditRepository;
    private final OrderFSM orderFSM;
    private final OrderTimeoutService timeoutService;
    
    // ========== Order Creation ==========
    
    /**
     * Create a new order
     */
    @Transactional
    public Order createOrder(UUID customerId, List<OrderItem> items, Map<String, Object> deliveryAddress, 
                            String specialInstructions) {
        log.info("Creating order for customer: {}", customerId);
        
        // Determine order type
        OrderType orderType = determineOrderType(items);
        
        // Create order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setState(OrderState.CREATED);
        order.setOrderType(orderType);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);
        order.setSpecialInstructions(specialInstructions);
        order.setCreatedAt(LocalDateTime.now());
        order.updateStateTimestamp(OrderState.CREATED);
        
        // Add items
        for (OrderItem item : items) {
            order.addOrderItem(item);
        }
        
        // Calculate totals
        order.calculateTotalAmount();
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Create audit record
        createAuditRecord(savedOrder, null, OrderState.CREATED, "ORDER_CREATED", customerId, "CUSTOMER");
        
        log.info("Order created successfully: {}", savedOrder.getOrderId());
        return savedOrder;
    }
    
    /**
     * Determine if order is single or multi-vendor
     */
    private OrderType determineOrderType(List<OrderItem> items) {
        // In real implementation, check if items are from different vendors
        // For now, default to SINGLE
        return OrderType.SINGLE;
    }
    
    // ========== Order Validation ==========
    
    /**
     * Validate order (CREATED → VALIDATED)
     */
    @Transactional
    public Order validateOrder(UUID orderId) {
        log.info("Validating order: {}", orderId);
        
        Order order = getOrderById(orderId);
        
        // Validate order state
        if (order.getState() != OrderState.CREATED) {
            throw new IllegalStateException("Order must be in CREATED state to validate");
        }
        
        // Business validations
        validateOrderItems(order);
        validateDeliveryAddress(order);
        
        // Transition state
        orderFSM.validateOrder(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.CREATED, OrderState.VALIDATED, 
            "VALIDATE_ORDER", null, "SYSTEM");
        
        log.info("Order validated successfully: {}", orderId);
        return savedOrder;
    }
    
    private void validateOrderItems(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        for (OrderItem item : order.getOrderItems()) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive");
            }
            if (item.getPriceAtOrder().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Item price must be positive");
            }
        }
    }
    
    private void validateDeliveryAddress(Order order) {
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
    }
    
    // ========== Payment Processing ==========
    
    /**
     * Confirm payment (VALIDATED → PAYMENT_CONFIRMED)
     */
    @Transactional
    public Order confirmPayment(UUID orderId, String paymentId, String paymentMethod) {
        log.info("Confirming payment for order: {}", orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.VALIDATED) {
            throw new IllegalStateException("Order must be in VALIDATED state to confirm payment");
        }
        
        // Update payment info
        order.setPaymentStatus(PaymentStatus.CAPTURED);
        Map<String, Object> metadata = order.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("paymentId", paymentId);
        metadata.put("paymentMethod", paymentMethod);
        metadata.put("paymentConfirmedAt", LocalDateTime.now().toString());
        order.setMetadata(metadata);
        
        // Transition state
        orderFSM.confirmPayment(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.VALIDATED, OrderState.PAYMENT_CONFIRMED,
            "CONFIRM_PAYMENT", null, "PAYMENT_GATEWAY");
        
        log.info("Payment confirmed for order: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Submit order to vendor (PAYMENT_CONFIRMED → PENDING_ACCEPTANCE)
     */
    @Transactional
    public Order submitToVendor(UUID orderId) {
        log.info("Submitting order to vendor: {}", orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.PAYMENT_CONFIRMED) {
            throw new IllegalStateException("Order must be in PAYMENT_CONFIRMED state to submit to vendor");
        }
        
        // Transition state
        orderFSM.submitToVendor(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.PAYMENT_CONFIRMED, OrderState.PENDING_ACCEPTANCE,
            "NOTIFY_RESTAURANT", null, "SYSTEM");
        
        // Schedule restaurant acceptance timeout (2 minutes)
        timeoutService.scheduleRestaurantAcceptanceTimeout(orderId);
        
        // TODO: Send notification to vendor
        
        log.info("Order submitted to vendor: {}", orderId);
        return savedOrder;
    }
    
    // ========== Vendor Actions ==========
    
    /**
     * Vendor accepts order (PENDING_ACCEPTANCE → ACCEPTED)
     */
    @Transactional
    public Order acceptOrder(UUID orderId, UUID vendorId) {
        log.info("Vendor {} accepting order: {}", vendorId, orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order must be in PENDING_ACCEPTANCE state to accept");
        }
        
        // Cancel timeout since restaurant accepted
        timeoutService.cancelRestaurantAcceptanceTimeout(orderId);
        
        // Transition state
        orderFSM.acceptOrder(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.PENDING_ACCEPTANCE, OrderState.ACCEPTED,
            "ACCEPT_ORDER", vendorId, "VENDOR");
        
        log.info("Order accepted by vendor: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Vendor rejects order (PENDING_ACCEPTANCE → REJECTED)
     */
    @Transactional
    public Order rejectOrder(UUID orderId, UUID vendorId, String reason) {
        log.info("Vendor {} rejecting order: {} - Reason: {}", vendorId, orderId, reason);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order must be in PENDING_ACCEPTANCE state to reject");
        }
        
        // Cancel timeout since restaurant rejected
        timeoutService.cancelRestaurantAcceptanceTimeout(orderId);
        
        // Transition state
        orderFSM.rejectOrder(order, reason);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.PENDING_ACCEPTANCE, OrderState.REJECTED,
            "REJECT_ORDER", vendorId, "VENDOR");
        
        // TODO: Initiate refund process
        
        log.info("Order rejected by vendor: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Handle vendor acceptance timeout (PENDING_ACCEPTANCE → REJECTED)
     */
    @Transactional
    public Order handleTimeout(UUID orderId) {
        log.warn("Handling timeout for order: {}", orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.PENDING_ACCEPTANCE) {
            log.warn("Order {} is not in PENDING_ACCEPTANCE state, skipping timeout", orderId);
            return order;
        }
        
        // Transition state
        orderFSM.handleTimeout(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.PENDING_ACCEPTANCE, OrderState.REJECTED,
            "TIMEOUT_ACCEPTANCE", null, "SYSTEM");
        
        // TODO: Initiate refund process
        // TODO: Send notification to customer
        
        log.info("Order timeout handled: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Start preparing order (ACCEPTED → PREPARING)
     */
    @Transactional
    public Order startPreparing(UUID orderId, UUID vendorId) {
        log.info("Vendor {} starting preparation for order: {}", vendorId, orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.ACCEPTED) {
            throw new IllegalStateException("Order must be in ACCEPTED state to start preparing");
        }
        
        // Transition state
        orderFSM.startPreparing(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.ACCEPTED, OrderState.PREPARING,
            "START_PREPARATION", vendorId, "VENDOR");
        
        log.info("Order preparation started: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Mark order ready for pickup (PREPARING → READY_FOR_PICKUP)
     */
    @Transactional
    public Order markReady(UUID orderId, UUID vendorId) {
        log.info("Vendor {} marking order ready: {}", vendorId, orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.PREPARING) {
            throw new IllegalStateException("Order must be in PREPARING state to mark ready");
        }
        
        // Transition state
        orderFSM.markReady(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.PREPARING, OrderState.READY_FOR_PICKUP,
            "MARK_READY", vendorId, "VENDOR");
        
        // TODO: Notify delivery system to assign rider
        
        log.info("Order marked ready for pickup: {}", orderId);
        return savedOrder;
    }
    
    // ========== Delivery Actions ==========
    
    /**
     * Assign rider to order (READY_FOR_PICKUP → ASSIGNED_TO_RIDER)
     */
    @Transactional
    public Order assignRider(UUID orderId, UUID riderId) {
        log.info("Assigning rider {} to order: {}", riderId, orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.READY_FOR_PICKUP) {
            throw new IllegalStateException("Order must be in READY_FOR_PICKUP state to assign rider");
        }
        
        // Transition state
        orderFSM.assignRider(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.READY_FOR_PICKUP, OrderState.ASSIGNED_TO_RIDER,
            "ASSIGN_RIDER", riderId, "SYSTEM");
        
        log.info("Rider assigned to order: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Rider picks up order (ASSIGNED_TO_RIDER → PICKED_UP)
     */
    @Transactional
    public Order pickupOrder(UUID orderId, UUID riderId) {
        log.info("Rider {} picking up order: {}", riderId, orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.ASSIGNED_TO_RIDER) {
            throw new IllegalStateException("Order must be in ASSIGNED_TO_RIDER state to pickup");
        }
        
        // Transition state
        orderFSM.pickupOrder(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.ASSIGNED_TO_RIDER, OrderState.PICKED_UP,
            "RIDER_PICKUP", riderId, "RIDER");
        
        log.info("Order picked up by rider: {}", orderId);
        return savedOrder;
    }
    
    /**
     * Deliver order to customer (PICKED_UP → DELIVERED)
     */
    @Transactional
    public Order deliverOrder(UUID orderId, UUID riderId) {
        log.info("Rider {} delivering order: {}", riderId, orderId);
        
        Order order = getOrderById(orderId);
        
        if (order.getState() != OrderState.PICKED_UP) {
            throw new IllegalStateException("Order must be in PICKED_UP state to deliver");
        }
        
        // Transition state
        orderFSM.deliverOrder(order);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, OrderState.PICKED_UP, OrderState.DELIVERED,
            "DELIVER_ORDER", riderId, "RIDER");
        
        log.info("Order delivered successfully: {}", orderId);
        return savedOrder;
    }
    
    // ========== Cancellation ==========
    
    /**
     * Cancel order (from any cancellable state → CANCELLED)
     */
    @Transactional
    public Order cancelOrder(UUID orderId, UUID userId, String cancelledBy, String reason) {
        log.info("Cancelling order: {} by {} - Reason: {}", orderId, cancelledBy, reason);
        
        Order order = getOrderById(orderId);
        
        if (!order.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled in state: " + order.getState());
        }
        
        OrderState previousState = order.getState();
        
        // Transition state
        orderFSM.cancelOrder(order, cancelledBy, reason);
        
        // Save and audit
        Order savedOrder = orderRepository.save(order);
        createAuditRecord(savedOrder, previousState, OrderState.CANCELLED,
            "CANCEL_ORDER", userId, cancelledBy);
        
        // TODO: Handle refund based on cancellation stage
        
        log.info("Order cancelled: {}", orderId);
        return savedOrder;
    }
    
    // ========== Query Methods ==========
    
    /**
     * Get order by ID
     */
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
    
    /**
     * Get orders by customer
     */
    public List<Order> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    /**
     * Get orders by state
     */
    public List<Order> getOrdersByState(OrderState state) {
        return orderRepository.findByState(state);
    }
    
    /**
     * Get customer orders by state
     */
    public List<Order> getCustomerOrdersByState(UUID customerId, OrderState state) {
        return orderRepository.findByCustomerIdAndState(customerId, state);
    }
    
    /**
     * Get order audit trail
     */
    public List<OrderStateAudit> getOrderAuditTrail(UUID orderId) {
        return auditRepository.findByOrderIdOrderByTransitionedAtDesc(orderId);
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Create audit record for state transition
     */
    private void createAuditRecord(Order order, OrderState fromState, OrderState toState,
                                  String triggerName, UUID triggeredBy, String triggeredByRole) {
        OrderStateAudit audit = OrderStateAudit.create(
            order.getOrderId(),
            fromState != null ? fromState.name() : null,
            toState.name(),
            triggerName,
            triggeredBy,
            triggeredByRole
        );
        
        auditRepository.save(audit);
    }
}
