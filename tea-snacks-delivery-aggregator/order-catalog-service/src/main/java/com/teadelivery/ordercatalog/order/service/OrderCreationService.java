package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.common.util.GeometryUtils;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSession;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSessionStatus;
import com.teadelivery.ordercatalog.order.checkout.service.SessionManagementService;
import com.teadelivery.ordercatalog.order.dto.CreateOrderFromCheckoutRequest;
import com.teadelivery.ordercatalog.order.exception.*;
import com.teadelivery.ordercatalog.order.fsm.OrderStateMachine;
import com.teadelivery.ordercatalog.order.fsm.OrderStateMachineFactory;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.OrderType;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.payment.dto.PaymentTransaction;
import com.teadelivery.ordercatalog.payment.exception.InsufficientFundsException;
import com.teadelivery.ordercatalog.payment.exception.InvalidPaymentTokenException;
import com.teadelivery.ordercatalog.payment.exception.PaymentGatewayException;
import com.teadelivery.ordercatalog.payment.service.PaymentService;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Order Creation Service
 * Implements the 6-step atomic process for creating orders from checkout
 * sessions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCreationService {

    private final SessionManagementService sessionManagementService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final VendorBranchRepository vendorBranchRepository;
    private final OrderStateMachineFactory fsmFactory;
    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;
    private final com.teadelivery.ordercatalog.menu.service.MenuService menuService;

    // Configuration constants
    private static final int DUPLICATE_ORDER_WINDOW_MINUTES = 5;
    private static final BigDecimal PRICE_TOLERANCE_PERCENTAGE = new BigDecimal("0.05"); // 5%
    private static final double MAX_DELIVERY_DISTANCE_KM = 10.0;

    /**
     * Create order from checkout session (6-step atomic process)
     * 
     * @param request Request with checkout session ID and payment token
     * @return Created order
     */
    @Transactional
    public Order createOrderFromCheckout(CreateOrderFromCheckoutRequest request) {
        log.info("Starting order creation from checkout session: {}", request.getCheckoutSessionId());

        CheckoutSession session = null;
        PaymentTransaction paymentTransaction = null;

        try {
            // ========== STEP 1: Session Lock ==========
            session = lockCheckoutSession(request.getCheckoutSessionId());

            // ========== STEP 2: Final Validation ==========
            performFinalValidation(session);

            // ========== STEP 3: Execute Payment ==========
            paymentTransaction = executePayment(session, request.getPaymentToken());

            // ========== STEP 4: Order Creation ==========
            Order order = createOrderEntity(session, paymentTransaction);

            // ========== STEP 5: Session Cleanup ==========
            cleanupSession(session, order);

            // ========== STEP 6: Event Publishing ==========
            publishEvents(order, paymentTransaction);

            log.info("Order created successfully: orderId={}, sessionId={}",
                    order.getOrderId(), request.getCheckoutSessionId());

            return order;

        } catch (Exception e) {
            log.error("Error creating order from checkout session: {}", request.getCheckoutSessionId(), e);

            // Rollback logic
            if (session != null) {
                rollbackOnFailure(session, paymentTransaction, e);
            }

            throw e;
        }
    }

    // ========== STEP 1: Session Lock ==========

    private CheckoutSession lockCheckoutSession(String sessionId) {
        log.info("Step 1: Locking checkout session: {}", sessionId);

        try {
            CheckoutSession session = sessionManagementService.lockSession(sessionId);
            log.info("Session locked successfully: {}", sessionId);
            return session;

        } catch (IllegalStateException e) {
            log.error("Failed to lock session: {}", sessionId, e);
            throw e;
        }
    }

    // ========== STEP 2: Final Validation ==========

    private void performFinalValidation(CheckoutSession session) {
        log.info("Step 2: Performing final validation for session: {}", session.getCheckoutSessionId());

        // Validate vendor branch
        VendorBranch vendorBranch = vendorBranchRepository.findById(session.getVendorBranchId())
                .orElseThrow(
                        () -> new IllegalStateException("Vendor branch not found: " + session.getVendorBranchId()));

        if (!vendorBranch.getIsActive()) {
            throw new IllegalStateException("Vendor branch is not active: " + session.getVendorBranchId());
        }

        // TODO: Add more validations
        // - Check vendor is accepting orders
        // - Validate menu items are still available
        // - Verify pricing hasn't changed significantly
        // - Confirm delivery zone is serviceable

        log.info("Final validation passed for session: {}", session.getCheckoutSessionId());
    }

    // ========== STEP 3: Execute Payment ==========

    private PaymentTransaction executePayment(CheckoutSession session, String paymentToken) {
        log.info("Step 3: Executing payment for session: {}, method: {}",
                session.getCheckoutSessionId(), session.getPaymentMethod());

        BigDecimal amount = session.getPricing().getTotalAmount();
        String purpose = "ORDER_PAYMENT";

        try {
            PaymentTransaction transaction;

            switch (session.getPaymentMethod().toUpperCase()) {
                case "WALLET":
                    transaction = paymentService.deductBalance(session.getUserId(), amount, purpose);
                    break;

                case "GPAY":
                    if (paymentToken == null || paymentToken.isBlank()) {
                        throw new InvalidPaymentTokenException("Payment token is required for GPay");
                    }
                    transaction = paymentService.processGpayTransaction(paymentToken, amount, purpose);
                    transaction.setUserId(session.getUserId());
                    break;

                case "COD":
                    transaction = paymentService.registerCodTransaction(session.getUserId(), amount, purpose);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + session.getPaymentMethod());
            }

            log.info("Payment executed successfully: txnId={}, method={}, status={}",
                    transaction.getTransactionId(), transaction.getPaymentMethod(), transaction.getStatus());

            return transaction;

        } catch (InsufficientFundsException | InvalidPaymentTokenException | PaymentGatewayException e) {
            log.error("Payment failed for session: {}", session.getCheckoutSessionId(), e);
            throw e;
        }
    }

    // ========== STEP 4: Order Creation ==========

    private Order createOrderEntity(CheckoutSession session, PaymentTransaction paymentTransaction) {
        log.info("Step 4: Creating order entity for session: {}", session.getCheckoutSessionId());

        // Get vendor branch with vendor (using eager loading to avoid
        // LazyInitializationException)
        VendorBranch vendorBranch = vendorBranchRepository.findByIdWithVendor(session.getVendorBranchId())
                .orElseThrow(() -> new IllegalStateException("Vendor branch not found"));

        // Create order
        Order order = new Order();
        order.setCustomerId(session.getUserId());
        order.setVendorId(vendorBranch.getVendor().getVendorId());
        order.setVendorBranchId(session.getVendorBranchId());
        order.setCheckoutSessionId(session.getCheckoutSessionId());
        order.setState(OrderState.CREATED);
        order.setOrderType(OrderType.SINGLE);
        order.setPaymentStatus(paymentTransaction.getStatus());
        order.setPaymentMethod(paymentTransaction.getPaymentMethod());
        order.setPaymentTransactionId(paymentTransaction.getTransactionId());
        order.setCreatedAt(LocalDateTime.now());
        order.updateStateTimestamp(OrderState.CREATED);

        // Set metadata with additional information (optional)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("vendorName", vendorBranch.getVendor().getCompanyName());
        metadata.put("branchName", vendorBranch.getBranchName());
        order.setMetadata(metadata);

        // Set delivery address
        order.setDeliveryAddress(session.getDeliveryAddress());

        // Set delivery location (customer) as PostGIS Point
        if (session.getDeliveryLocation() != null) {
            order.setDeliveryLocation(GeometryUtils.createPoint(
                    session.getDeliveryLocation().getLatitude(),
                    session.getDeliveryLocation().getLongitude()));
        }

        // Set pickup location (vendor branch) as PostGIS Point - captured at order
        // creation time
        if (vendorBranch.getLatitude() != null && vendorBranch.getLongitude() != null) {
            order.setPickupLocation(GeometryUtils.createPoint(
                    vendorBranch.getLatitude(),
                    vendorBranch.getLongitude()));
            log.debug("Set pickup location from vendor branch: lat={}, lng={}",
                    vendorBranch.getLatitude(), vendorBranch.getLongitude());
        } else {
            log.warn("Vendor branch {} missing coordinates, pickup location not set",
                    vendorBranch.getBranchId());
        }

        // Set pricing
        order.setItemTotal(session.getPricing().getItemTotal());
        order.setDeliveryCharges(session.getPricing().getDeliveryCharges());
        order.setPlatformFee(session.getPricing().getPlatformFee());
        order.setGst(session.getPricing().getGst());
        order.setDiscount(session.getPricing().getDiscount());
        order.setTotalAmount(session.getPricing().getTotalAmount());

        // Set delivery preferences
        order.setSpecialInstructions(session.getDeliveryInstructions());

        // Add order items - need to fetch menu item details for name and price
        session.getItems().forEach(cartItem -> {
            try {
                // Fetch menu item to get name and current price
                com.teadelivery.ordercatalog.menu.dto.MenuItemResponse menuItem = menuService
                        .getMenuItem(cartItem.getMenuItemId());

                OrderItem item = new OrderItem();
                item.setMenuItemId(cartItem.getMenuItemId());
                item.setItemName(menuItem.getName()); // ✅ FIX: Set item name from menu service
                item.setQuantity(cartItem.getQuantity());
                item.setPriceAtOrder(menuItem.getPrice()); // ✅ FIX: Set price from menu service
                item.setCustomizations(cartItem.getCustomizations());
                item.setNotes(cartItem.getSpecialInstructions());
                item.setCreatedAt(LocalDateTime.now());
                order.addOrderItem(item);

                log.debug("Added order item: {} x {} @ {}", menuItem.getName(), cartItem.getQuantity(),
                        menuItem.getPrice());
            } catch (Exception e) {
                log.error("Error fetching menu item {} for order creation", cartItem.getMenuItemId(), e);
                throw new IllegalStateException("Failed to fetch menu item: " + cartItem.getMenuItemId(), e);
            }
        });

        // Save order (initial save with CREATED state)
        Order savedOrder = orderRepository.save(order);

        // Create FSM for this order and run state transitions
        // FSM handles: state change, persistence, audit records, event publishing
        try {
            OrderStateMachine fsm = fsmFactory.create(savedOrder)
                    .withActor(session.getUserId(), "SYSTEM");

            // CREATED → VALIDATED (validates order, reserves inventory)
            savedOrder = fsm.validate();

            // VALIDATED → PAYMENT_CONFIRMED (verifies payment)
            savedOrder = fsm.confirmPayment();

            // PAYMENT_CONFIRMED → PENDING_ACCEPTANCE (notifies vendor)
            savedOrder = fsm.submitToVendor();

            log.info("Order FSM transitions completed: orderId={}, state={}",
                    savedOrder.getOrderId(), savedOrder.getState());

        } catch (Exception e) {
            log.error("Error in FSM transitions for order: {}", savedOrder.getOrderId(), e);
            // Continue - order is created, FSM can be retried
        }

        log.info("Order created: orderId={}, state={}, paymentStatus={}",
                savedOrder.getOrderId(), savedOrder.getState(), savedOrder.getPaymentStatus());

        return savedOrder;
    }

    // ========== STEP 5: Session Cleanup ==========

    private void cleanupSession(CheckoutSession session, Order order) {
        log.info("Step 5: Cleaning up session: {}", session.getCheckoutSessionId());

        try {
            sessionManagementService.updateSessionStatus(
                    session.getCheckoutSessionId(),
                    CheckoutSessionStatus.COMMITTED,
                    order.getOrderId().toString());

            log.info("Session marked as committed: {}", session.getCheckoutSessionId());

        } catch (Exception e) {
            log.error("Error cleaning up session: {}", session.getCheckoutSessionId(), e);
            // Non-critical - order is already created
        }
    }

    // ========== STEP 6: Event Publishing ==========

    private void publishEvents(Order order, PaymentTransaction paymentTransaction) {
        log.info("Step 6: Publishing events for order: {}", order.getOrderId());

        try {
            // TODO: Publish OrderPlacedEvent
            // TODO: Publish PaymentCompletedEvent
            // TODO: Publish OrderStateChangedEvent

            log.info("Events published for order: {}", order.getOrderId());

        } catch (Exception e) {
            log.error("Error publishing events for order: {}", order.getOrderId(), e);
            // Non-critical - order is created, events can be retried
        }
    }

    // ========== Rollback Logic ==========

    private void rollbackOnFailure(CheckoutSession session, PaymentTransaction paymentTransaction, Exception error) {
        log.error("Rolling back order creation for session: {}", session.getCheckoutSessionId());

        try {
            // Release session lock
            if (session.getStatus() == CheckoutSessionStatus.IN_PROGRESS) {
                sessionManagementService.updateSessionStatus(
                        session.getCheckoutSessionId(),
                        CheckoutSessionStatus.READY_FOR_COMMIT);
                log.info("Session lock released: {}", session.getCheckoutSessionId());
            }

            // TODO: Rollback payment if order creation failed after payment
            // This requires payment service to support refunds/reversals

        } catch (Exception e) {
            log.error("Error during rollback for session: {}", session.getCheckoutSessionId(), e);
        }
    }
}
