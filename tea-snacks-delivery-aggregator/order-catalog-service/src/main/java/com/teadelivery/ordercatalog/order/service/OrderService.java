package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSessionStatus;
import com.teadelivery.ordercatalog.order.dto.CreateOrderRequest;
import com.teadelivery.ordercatalog.order.model.OrderStateAudit;
import com.teadelivery.ordercatalog.order.repository.OrderStateAuditRepository;
import com.teadelivery.ordercatalog.order.fsm.OrderFSM;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.OrderType;
import com.teadelivery.ordercatalog.order.fsm.PaymentStatus;
import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.repository.SubOrderRepository;
import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
     * Create a new order from CreateOrderRequest
     * Performs comprehensive validation and mapping
     */
    @Transactional
    public Order createOrder(UUID customerId, CreateOrderRequest request) {
        log.info("Creating order for customer: {}, vendor: {}, branch: {}",
                customerId, request.getVendorId(), request.getVendorBranchId());

        // TODO: Add validation here
        // - Validate vendor branch exists
        // - Validate menu items belong to branch
        // - Validate pricing
        // - Validate delivery zone

        // Create order entity
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setState(OrderState.CREATED);
        order.setOrderType(OrderType.SINGLE); // TODO: Determine from items
        order.setVendorId(request.getVendorId()); // Set vendorId from request
        order.setVendorBranchId(request.getVendorBranchId()); // Set vendorBranchId from request
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setCreatedAt(LocalDateTime.now());
        order.updateStateTimestamp(OrderState.CREATED);

        // Set payment info in proper columns (not just metadata)
        if (request.getPayment() != null) {
            order.setPaymentMethod(request.getPayment().getMethod());
            order.setPaymentTransactionId(request.getPayment().getTransactionId());
        }

        // Set delivery address (embedded object)
        order.setDeliveryAddress(buildDeliveryAddress(request.getDeliveryAddress()));

        // Set delivery location
        if (request.getDeliveryLocation() != null) {
            order.setDeliveryLatitude(BigDecimal.valueOf(request.getDeliveryLocation().getLatitude()));
            order.setDeliveryLongitude(BigDecimal.valueOf(request.getDeliveryLocation().getLongitude()));
        }

        // Set pricing fields
        if (request.getPricing() != null) {
            order.setItemTotal(request.getPricing().getItemTotal());
            order.setDeliveryCharges(request.getPricing().getDeliveryCharges());
            order.setPlatformFee(request.getPricing().getPlatformFee());
            order.setGst(request.getPricing().getGst());
            order.setDiscount(request.getPricing().getDiscount());
            order.setTotalAmount(request.getPricing().getTotalAmount());
        }

        // Build and set metadata
        order.setMetadata(buildOrderMetadata(request));

        // Add order items with all details
        request.getItems().forEach(itemReq -> {
            OrderItem item = new OrderItem();
            item.setMenuItemId(itemReq.getMenuItemId());
            item.setItemName(itemReq.getItemName());
            item.setItemDescription(itemReq.getItemDescription());
            item.setImageUrl(itemReq.getImageUrl());
            item.setCategoryName(itemReq.getCategoryName());
            item.setQuantity(itemReq.getQuantity());
            item.setPriceAtOrder(itemReq.getUnitPrice());
            // Calculate subtotal = quantity * unit price
            item.setSubtotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            item.setCustomizations(itemReq.getCustomizations());
            item.setNotes(itemReq.getSpecialInstructions());
            order.addOrderItem(item);
        });

        // Save order (single save)
        Order savedOrder = orderRepository.save(order);

        // Create audit record
        createAuditRecord(savedOrder, null, OrderState.CREATED, "ORDER_CREATED", customerId, "CUSTOMER");

        log.info("Order created: orderId={}, state={}", savedOrder.getOrderId(), savedOrder.getState());
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

    /**
     * Build delivery address from DTO
     */
    private DeliveryAddress buildDeliveryAddress(CreateOrderRequest.DeliveryAddressRequest addressReq) {
        if (addressReq == null) {
            return null;
        }

        return DeliveryAddress.builder()
                .addressLine1(addressReq.getAddressLine1())
                .addressLine2(addressReq.getAddressLine2())
                .landmark(addressReq.getLandmark())
                .city(addressReq.getCity())
                .state(addressReq.getState())
                .pincode(addressReq.getPincode())
                .addressType(addressReq.getAddressType())
                .label(addressReq.getLabel())
                .build();
    }

    /**
     * Build order metadata from CreateOrderRequest
     */
    private Map<String, Object> buildOrderMetadata(CreateOrderRequest request) {
        Map<String, Object> metadata = new HashMap<>();

        // Vendor information
        metadata.put("vendorId", request.getVendorId());
        metadata.put("vendorName", request.getVendorName());
        metadata.put("vendorBranchId", request.getVendorBranchId());

        // Payment metadata
        if (request.getPayment() != null) {
            metadata.put("paymentMethod", request.getPayment().getMethod());
            metadata.put("paymentInstrumentId", request.getPayment().getInstrumentId());
            metadata.put("paymentTransactionId", request.getPayment().getTransactionId());
            if (request.getPayment().getMetadata() != null) {
                metadata.put("paymentMetadata", request.getPayment().getMetadata());
            }
        }

        // Pricing metadata
        if (request.getPricing() != null && request.getPricing().getCouponCode() != null) {
            metadata.put("couponCode", request.getPricing().getCouponCode());
        }

        // Delivery preferences
        metadata.put("contactlessDelivery", request.getContactlessDelivery());
        metadata.put("leaveAtDoor", request.getLeaveAtDoor());
        metadata.put("deliveryInstructions", request.getDeliveryInstructions());

        // Device info
        metadata.put("deviceId", request.getDeviceId());
        metadata.put("appVersion", request.getAppVersion());
        metadata.put("platform", request.getPlatform());

        // Additional custom metadata
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }

        return metadata;
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
        DeliveryAddress address = order.getDeliveryAddress();
        if (address == null || address.getAddressLine1() == null || address.getAddressLine1().isBlank()) {
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
    public void createAuditRecord(Order order, OrderState fromState, OrderState toState,
            String triggerName, UUID triggeredBy, String triggeredByRole) {
        OrderStateAudit audit = OrderStateAudit.create(
                order.getOrderId(),
                fromState != null ? fromState.name() : null,
                toState.name(),
                triggerName,
                triggeredBy,
                triggeredByRole);

        auditRepository.save(audit);
    }

    /**
     * Convert Order to unified OrderDetailsResponse
     * This ensures consistent response format across /commit and /orders endpoints
     */
    public com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse toCheckoutResponse(
            Order order,
            String vendorName,
            String branchName) {

        // Convert order items
        List<com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.CheckoutItem> items = order
                .getOrderItems().stream()
                .map(item -> com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.CheckoutItem.builder()
                        .orderItemId(item.getOrderItemId())
                        .menuItemId(item.getMenuItemId())
                        .name(item.getItemName())
                        .description(item.getItemDescription())
                        .imageUrl(item.getImageUrl())
                        .categoryName(item.getCategoryName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getPriceAtOrder())
                        .subtotal(item.getSubtotal())
                        .specialInstructions(item.getNotes())
                        .customizations(item.getCustomizations())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        int totalItems = items.stream().mapToInt(
                com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.CheckoutItem::getQuantity).sum();
        int prepTime = order.getEstimatedPrepTimeMinutes() != null ? order.getEstimatedPrepTimeMinutes() : 25;
        int deliveryDuration = 20;
        int totalTime = prepTime + deliveryDuration;

        // Determine status based on order state
        CheckoutSessionStatus status;
        String message;
        if (order.getState() == OrderState.CANCELLED || order.getState() == OrderState.REJECTED) {
            status = CheckoutSessionStatus.VALIDATION_FAILED;
            message = "Order " + order.getState().name().toLowerCase();
        } else {
            status = CheckoutSessionStatus.COMMITTED;
            message = getOrderStateMessage(order.getState());
        }

        return com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.builder()
                // Session info
                .checkoutSessionId(order.getCheckoutSessionId())
                .status(status)
                .statusDisplayName(status.getDisplayName())
                // Order info
                .orderId(order.getOrderId())
                .orderNumber("ORD-" + order.getOrderId().toString().substring(0, 8).toUpperCase())
                .orderPlacedAt(order.getCreatedAt())
                .isSuccess(order.getState() != OrderState.CANCELLED && order.getState() != OrderState.REJECTED)
                .message(message)
                // Customer
                .customerId(order.getCustomerId())
                // Vendor
                .vendor(com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.VendorInfo.builder()
                        .vendorId(order.getVendorId())
                        .vendorName(vendorName)
                        .vendorBranchId(order.getVendorBranchId())
                        .branchName(branchName)
                        .estimatedPrepTime(prepTime)
                        .build())
                // Items
                .items(items)
                .totalItemCount(totalItems)
                // Pricing
                .pricing(com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.PricingDetails.builder()
                        .itemTotal(order.getItemTotal())
                        .deliveryCharges(order.getDeliveryCharges())
                        .platformFee(order.getPlatformFee())
                        .gst(order.getGst())
                        .discount(order.getDiscount())
                        .totalAmount(order.getTotalAmount())
                        .currency("INR")
                        .itemTotalLabel("Item Total")
                        .deliveryLabel("Delivery Charges")
                        .taxesLabel("Taxes & Fees")
                        .discountLabel("Discount")
                        .totalLabel("Total Amount")
                        .build())
                // Delivery info
                .delivery(com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.DeliveryInfo.builder()
                        .address(order.getDeliveryAddress())
                        .latitude(order.getDeliveryLatitude())
                        .longitude(order.getDeliveryLongitude())
                        .specialInstructions(order.getSpecialInstructions())
                        .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                        .estimatedPrepTime(prepTime)
                        .estimatedDeliveryDuration(deliveryDuration)
                        .totalEstimatedTime(totalTime)
                        .deliveryTimeRange(totalTime + "-" + (totalTime + 15) + " mins")
                        .build())
                // Payment info
                .payment(com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse.PaymentInfo.builder()
                        .status(order.getPaymentStatus().name())
                        .statusDisplayName(getPaymentStatusDisplayName(order.getPaymentStatus()))
                        .method(order.getPaymentMethod())
                        .methodDisplayName(getPaymentMethodDisplayName(order.getPaymentMethod()))
                        .transactionId(order.getPaymentTransactionId())
                        .amountPaid(order.getTotalAmount())
                        .paidAt(order.getPaymentConfirmedAt())
                        .build())
                .build();
    }

    private String getOrderStateMessage(OrderState state) {
        return switch (state) {
            case CREATED -> "Order placed successfully!";
            case VALIDATED -> "Order validated";
            case PAYMENT_CONFIRMED -> "Payment confirmed";
            case PENDING_ACCEPTANCE -> "Waiting for restaurant to accept";
            case ACCEPTED -> "Restaurant accepted your order";
            case PREPARING -> "Your order is being prepared";
            case READY_FOR_PICKUP -> "Order is ready for pickup";
            case ASSIGNED_TO_RIDER -> "Rider assigned";
            case PICKED_UP -> "Order picked up - on the way!";
            case DELIVERED -> "Order delivered";
            case CANCELLED -> "Order cancelled";
            case REJECTED -> "Order rejected by restaurant";
            default -> "Order " + state.name().toLowerCase().replace('_', ' ');
        };
    }

    public String getPaymentStatusDisplayName(PaymentStatus status) {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case PENDING -> "Pending";
            case AUTHORIZED -> "Authorized";
            case CAPTURED -> "Paid";
            case FAILED -> "Failed";
            case REFUNDED -> "Refunded";
            case PARTIALLY_REFUNDED -> "Partially Refunded";
            default -> status.name();
        };
    }

    public String getPaymentMethodDisplayName(String method) {
        if (method == null)
            return "Unknown";
        return switch (method.toUpperCase()) {
            case "UPI" -> "UPI";
            case "CARD" -> "Credit/Debit Card";
            case "WALLET" -> "Wallet";
            case "COD" -> "Cash on Delivery";
            case "NETBANKING" -> "Net Banking";
            default -> method;
        };
    }
}
