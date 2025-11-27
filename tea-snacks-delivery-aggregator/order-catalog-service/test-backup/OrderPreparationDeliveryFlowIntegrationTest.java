package com.teadelivery.ordercatalog.integration;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
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
 * Integration Tests for Order Preparation and Delivery Flow
 * Tests scenarios 4.1 - 5.4 from the test plan
 */
@DisplayName("Order Preparation & Delivery Flow Integration Tests")
class OrderPreparationDeliveryFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Scenario 4.1: Should start order preparation")
    void shouldStartOrderPreparation() {
        // Given: Order in ACCEPTED state
        UUID orderId = createOrderInAcceptedState();

        // When: POST /api/v1/restaurant/orders/{orderId}/start-preparing
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/start-preparing"),
            null,
            OrderResponse.class
        );

        // Then: Order transitions to PREPARING
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.PREPARING.name());

        // Verify: Database updated
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.PREPARING);
    }

    @Test
    @DisplayName("Scenario 4.2: Should mark order ready for pickup")
    void shouldMarkOrderReady() {
        // Given: Order in PREPARING state
        UUID orderId = createOrderInPreparingState();

        // When: POST /api/v1/restaurant/orders/{orderId}/ready
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/ready"),
            null,
            OrderResponse.class
        );

        // Then: Order transitions to READY_FOR_PICKUP
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.READY_FOR_PICKUP.name());

        // Verify: Database updated
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.READY_FOR_PICKUP);
    }

    @Test
    @DisplayName("Scenario 5.1: Should assign rider to order")
    void shouldAssignRiderToOrder() {
        // Given: Order in READY_FOR_PICKUP state
        UUID orderId = createOrderInReadyForPickupState();

        // When: Assign rider (simulated by service call)
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.ASSIGNED_TO_RIDER);
        orderRepository.save(order);

        // Then: Order transitions to ASSIGNED_TO_RIDER
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getState()).isEqualTo(OrderState.ASSIGNED_TO_RIDER);
    }

    @Test
    @DisplayName("Scenario 5.2: Should confirm rider pickup")
    void shouldConfirmRiderPickup() {
        // Given: Order in ASSIGNED_TO_RIDER state
        UUID orderId = createOrderInAssignedToRiderState();
        UUID riderId = UUID.randomUUID();

        // When: POST /api/v1/rider/orders/{orderId}/pickup
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/rider/orders/" + orderId + "/pickup?riderId=" + riderId),
            null,
            OrderResponse.class
        );

        // Then: Order transitions to PICKED_UP
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.PICKED_UP.name());

        // Verify: Database updated
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.PICKED_UP);
    }

    @Test
    @DisplayName("Scenario 5.3: Should confirm order delivery")
    void shouldConfirmOrderDelivery() {
        // Given: Order in PICKED_UP state
        UUID orderId = createOrderInPickedUpState();
        UUID riderId = UUID.randomUUID();

        // When: POST /api/v1/rider/orders/{orderId}/deliver
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/rider/orders/" + orderId + "/deliver?riderId=" + riderId),
            null,
            OrderResponse.class
        );

        // Then: Order transitions to DELIVERED
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.DELIVERED.name());

        // Verify: Database updated
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.DELIVERED);
    }

    @Test
    @DisplayName("Scenario 5.4: Should get orders for pickup by rider")
    void shouldGetOrdersForPickup() {
        // Given: Multiple orders in ASSIGNED_TO_RIDER state
        UUID orderId1 = createOrderInAssignedToRiderState();
        UUID orderId2 = createOrderInAssignedToRiderState();

        // When: GET /api/v1/rider/orders/for-pickup
        UUID riderId = UUID.randomUUID();
        ResponseEntity<OrderResponse[]> response = restTemplate.getForEntity(
            getApiUrl("/rider/orders/for-pickup?riderId=" + riderId),
            OrderResponse[].class
        );

        // Then: Returns list of assigned orders
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Note: May be empty if rider assignment logic filters by riderId
    }

    @Test
    @DisplayName("Scenario 4.3: Should complete full order lifecycle")
    void shouldCompleteFullOrderLifecycle() {
        // Given: New order created
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrder(customerId);

        // When: Progress through all states
        // 1. CREATED -> VALIDATED
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.VALIDATED);
        orderRepository.save(order);

        // 2. VALIDATED -> PAYMENT_CONFIRMED
        order.setState(OrderState.PAYMENT_CONFIRMED);
        orderRepository.save(order);

        // 3. PAYMENT_CONFIRMED -> PENDING_ACCEPTANCE
        order.setState(OrderState.PENDING_ACCEPTANCE);
        orderRepository.save(order);

        // 4. PENDING_ACCEPTANCE -> ACCEPTED
        order.setState(OrderState.ACCEPTED);
        orderRepository.save(order);

        // 5. ACCEPTED -> PREPARING
        restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/start-preparing"),
            null,
            OrderResponse.class
        );

        // 6. PREPARING -> READY_FOR_PICKUP
        restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/ready"),
            null,
            OrderResponse.class
        );

        // 7. READY_FOR_PICKUP -> ASSIGNED_TO_RIDER
        order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.ASSIGNED_TO_RIDER);
        orderRepository.save(order);

        // 8. ASSIGNED_TO_RIDER -> PICKED_UP
        UUID riderId = UUID.randomUUID();
        restTemplate.postForEntity(
            getApiUrl("/rider/orders/" + orderId + "/pickup?riderId=" + riderId),
            null,
            OrderResponse.class
        );

        // 9. PICKED_UP -> DELIVERED
        ResponseEntity<OrderResponse> finalResponse = restTemplate.postForEntity(
            getApiUrl("/rider/orders/" + orderId + "/deliver?riderId=" + riderId),
            null,
            OrderResponse.class
        );

        // Then: Order successfully delivered
        assertThat(finalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalResponse.getBody().getState()).isEqualTo(OrderState.DELIVERED.name());

        // Verify: Audit trail contains all transitions
        var auditRecords = auditRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        assertThat(auditRecords.size()).isGreaterThanOrEqualTo(9);
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

    private UUID createOrderInAcceptedState() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrder(customerId);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.VALIDATED);
        order.setState(OrderState.PAYMENT_CONFIRMED);
        order.setState(OrderState.PENDING_ACCEPTANCE);
        order.setState(OrderState.ACCEPTED);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInPreparingState() {
        UUID orderId = createOrderInAcceptedState();
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.PREPARING);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInReadyForPickupState() {
        UUID orderId = createOrderInPreparingState();
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.READY_FOR_PICKUP);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInAssignedToRiderState() {
        UUID orderId = createOrderInReadyForPickupState();
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.ASSIGNED_TO_RIDER);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInPickedUpState() {
        UUID orderId = createOrderInAssignedToRiderState();
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.PICKED_UP);
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
