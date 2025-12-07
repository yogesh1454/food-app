package com.teadelivery.ordercatalog.order.fsm;

import com.teadelivery.ordercatalog.common.fsm.EventPublisher;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import com.teadelivery.ordercatalog.menu.service.MenuService;
import com.teadelivery.ordercatalog.notification.service.NotificationService;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.repository.OrderItemRepository;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.repository.OrderStateAuditRepository;
import com.teadelivery.ordercatalog.order.service.OrderTimeoutService;
import com.teadelivery.ordercatalog.order.service.OrderValidationService;
import com.teadelivery.ordercatalog.payment.service.PaymentService;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for creating OrderStateMachine instances
 * 
 * This factory injects all required dependencies and creates
 * a new FSM instance for each order. This ensures:
 * - Each order has its own FSM with proper state
 * - All dependencies are properly injected
 * - Thread-safe operations
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStateMachineFactory {

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

    /**
     * Create a new FSM for an order
     * 
     * @param order The order to manage
     * @return A new OrderStateMachine instance
     */
    public OrderStateMachine create(Order order) {
        log.debug("Creating FSM for order: {}", order.getOrderId());

        return new OrderStateMachine(
                order,
                orderRepository,
                auditRepository,
                orderItemRepository,
                eventPublisher,
                notificationService,
                timeoutService,
                deliveryService,
                paymentService,
                validationService,
                menuService,
                vendorBranchRepository);
    }

    /**
     * Create FSM for order by ID (loads from repository)
     * 
     * @param orderId The order ID
     * @return A new OrderStateMachine instance
     * @throws IllegalArgumentException if order not found
     */
    public OrderStateMachine createById(java.util.UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        return create(order);
    }
}
