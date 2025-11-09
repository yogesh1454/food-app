package com.teadelivery.ordercatalog.integration;

import com.teadelivery.ordercatalog.fsm.OrderState;
import com.teadelivery.ordercatalog.order.dto.CancelOrderRequest;
import com.teadelivery.ordercatalog.order.dto.CreateOrderRequest;
import com.teadelivery.ordercatalog.order.dto.OrderItemRequest;
import com.teadelivery.ordercatalog.order.dto.OrderResponse;
import com.teadelivery.ordercatalog.order.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests for Order Cancellation Flow
 * Tests scenarios 6.1 - 6.3 from the test plan
 */
@DisplayName("Order Cancellation Flow Integration Tests")
class OrderCancellationFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Scenario 6.1: Should cancel order in early stage (VALIDATED)")
    void shouldCancelOrderInEarlyStage() {
        // Given: Order in VALIDATED state
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrderInValidatedState(customerId);

        // When: POST /api/v1/orders/{orderId}/cancel
        CancelOrderRequest request = CancelOrderRequest.builder()
            .customerId(customerId)
            .reason("Changed my mind")
            .build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/orders/" + orderId + "/cancel"),
            request,
            OrderResponse.class
        );

        // Then: Order cancelled successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.CANCELLED.name());

        // Verify: Database updated
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);

        // Verify: Audit record created
        var auditRecords = auditRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        assertThat(auditRecords).isNotEmpty();
        assertThat(auditRecords.get(auditRecords.size() - 1).getNewState())
            .isEqualTo(OrderState.CANCELLED);
    }

    @Test
    @DisplayName("Scenario 6.2: Should cancel order after acceptance")
    void shouldCancelOrderAfterAcceptance() {
        // Given: Order in ACCEPTED state
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrderInAcceptedState(customerId);

        // When: POST /api/v1/orders/{orderId}/cancel
        CancelOrderRequest request = CancelOrderRequest.builder()
            .customerId(customerId)
            .reason("Emergency - need to cancel")
            .build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/orders/" + orderId + "/cancel"),
            request,
            OrderResponse.class
        );

        // Then: Order cancelled successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.CANCELLED.name());

        // Verify: Database updated
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
    }

    @Test
    @DisplayName("Scenario 6.3: Should not cancel order from non-cancellable state (DELIVERED)")
    void shouldNotCancelOrderFromNonCancellableState() {
        // Given: Order in DELIVERED state
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrderInDeliveredState(customerId);

        // When: Try to cancel order
        CancelOrderRequest request = CancelOrderRequest.builder()
            .customerId(customerId)
            .reason("Want to cancel")
            .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/orders/" + orderId + "/cancel"),
            request,
            String.class
        );

        // Then: Bad request - order not cancellable
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsAnyOf("cannot be cancelled", "not cancellable");

        // Verify: Order state unchanged
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.DELIVERED);
    }

    @Test
    @DisplayName("Scenario 6.4: Should cancel order in PREPARING state")
    void shouldCancelOrderInPreparingState() {
        // Given: Order in PREPARING state
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrderInPreparingState(customerId);

        // When: POST /api/v1/orders/{orderId}/cancel
        CancelOrderRequest request = CancelOrderRequest.builder()
            .customerId(customerId)
            .reason("Taking too long")
            .build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/orders/" + orderId + "/cancel"),
            request,
            OrderResponse.class
        );

        // Then: Order cancelled successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.CANCELLED.name());
    }

    @Test
    @DisplayName("Scenario 6.5: Should not cancel order without customer ID")
    void shouldNotCancelOrderWithoutCustomerId() {
        // Given: Order exists
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrderInValidatedState(customerId);

        // When: Try to cancel without customerId
        CancelOrderRequest request = CancelOrderRequest.builder()
            .customerId(null)
            .reason("Want to cancel")
            .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/orders/" + orderId + "/cancel"),
            request,
            String.class
        );

        // Then: Bad request - validation error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Scenario 6.6: Should not cancel order with wrong customer ID")
    void shouldNotCancelOrderWithWrongCustomerId() {
        // Given: Order exists for customer A
        UUID customerA = UUID.randomUUID();
        UUID orderId = createOrderInValidatedState(customerA);

        // When: Try to cancel with customer B's ID
        UUID customerB = UUID.randomUUID();
        CancelOrderRequest request = CancelOrderRequest.builder()
            .customerId(customerB)
            .reason("Want to cancel")
            .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/orders/" + orderId + "/cancel"),
            request,
            String.class
        );

        // Then: Forbidden or Bad Request
        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);

        // Verify: Order state unchanged
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.VALIDATED);
    }

    // ========== Helper Methods ==========

    private UUID createOrder(UUID customerId) {
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(customerId)
            .items(createValidOrderItems())
            .deliveryAddress(createValidDeliveryAddress())
            .build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/orders"),
            request,
            OrderResponse.class
        );

        return response.getBody().getOrderId();
    }

    private UUID createOrderInValidatedState(UUID customerId) {
        UUID orderId = createOrder(customerId);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.VALIDATED);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInAcceptedState(UUID customerId) {
        UUID orderId = createOrderInValidatedState(customerId);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.PAYMENT_CONFIRMED);
        order.setState(OrderState.PENDING_ACCEPTANCE);
        order.setState(OrderState.ACCEPTED);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInPreparingState(UUID customerId) {
        UUID orderId = createOrderInAcceptedState(customerId);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.PREPARING);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInDeliveredState(UUID customerId) {
        UUID orderId = createOrderInPreparingState(customerId);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.READY_FOR_PICKUP);
        order.setState(OrderState.ASSIGNED_TO_RIDER);
        order.setState(OrderState.PICKED_UP);
        order.setState(OrderState.DELIVERED);
        orderRepository.save(order);

        return orderId;
    }

    private List<OrderItemRequest> createValidOrderItems() {
        return Arrays.asList(
            OrderItemRequest.builder()
                .menuItemId(UUID.randomUUID())
                .itemName("Masala Chai")
                .quantity(2)
                .priceAtOrder(new BigDecimal("50.00"))
                .build(),
            OrderItemRequest.builder()
                .menuItemId(UUID.randomUUID())
                .itemName("Samosa")
                .quantity(5)
                .priceAtOrder(new BigDecimal("50.00"))
                .build()
        );
    }

    private Map<String, Object> createValidDeliveryAddress() {
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main Street");
        address.put("city", "Mumbai");
        address.put("state", "Maharashtra");
        address.put("pincode", "400001");
        return address;
    }
}
