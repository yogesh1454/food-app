package com.teadelivery.ordercatalog.integration;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.dto.*;
import com.teadelivery.ordercatalog.order.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration Tests for Restaurant Acceptance Flow
 * Tests scenarios 3.1 - 3.5 from the test plan
 * Includes timeout handling with Redis keyspace notifications
 */
@DisplayName("Restaurant Acceptance Flow Integration Tests")
class RestaurantAcceptanceFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Scenario 3.1: Should submit order to restaurant with timeout scheduling")
    void shouldSubmitOrderToRestaurantWithTimeout() throws Exception {
        // Given: Order in PAYMENT_CONFIRMED state
        UUID orderId = createOrderInPaymentConfirmedState();

        // When: Submit to vendor (this happens automatically after payment confirmation)
        // The order service should transition to PENDING_ACCEPTANCE
        
        // Wait for async processing
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                Order order = orderRepository.findById(orderId).orElseThrow();
                assertThat(order.getState()).isEqualTo(OrderState.PENDING_ACCEPTANCE);
            });

        // Then: Order in PENDING_ACCEPTANCE state
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.PENDING_ACCEPTANCE);

        // Verify: Timeout key created in Redis
        String timeoutKey = "timeout:restaurant_acceptance:" + orderId;
        Boolean hasKey = redisTemplate.hasKey(timeoutKey);
        assertThat(hasKey).isTrue();

        // Verify: TTL is approximately 5 seconds (configured for tests)
        Long ttl = redisTemplate.getExpire(timeoutKey, TimeUnit.SECONDS);
        assertThat(ttl).isBetween(1L, 6L);

        // Verify: Redis cache updated with order state
        String cachedState = (String) redisTemplate.opsForValue()
            .get("order:state:" + orderId);
        assertThat(cachedState).isEqualTo(OrderState.PENDING_ACCEPTANCE.name());
    }

    @Test
    @DisplayName("Scenario 3.2: Should accept order and cancel timeout")
    void shouldAcceptOrderAndCancelTimeout() throws Exception {
        // Given: Order in PENDING_ACCEPTANCE state
        UUID orderId = createOrderInPendingAcceptanceState();
        UUID restaurantId = UUID.randomUUID();

        // Verify timeout key exists
        String timeoutKey = "timeout:restaurant_acceptance:" + orderId;
        assertThat(redisTemplate.hasKey(timeoutKey)).isTrue();

        // When: POST /api/v1/restaurant/orders/{orderId}/accept
        AcceptOrderRequest request = AcceptOrderRequest.builder()
            .restaurantId(restaurantId)
            .estimatedPreparationTime(20)
            .build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/accept"),
            request,
            OrderResponse.class
        );

        // Then: Order accepted successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.ACCEPTED.name());

        // Verify: Order state updated in database
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.ACCEPTED);

        // Verify: Timeout key deleted from Redis
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                Boolean hasKey = redisTemplate.hasKey(timeoutKey);
                assertThat(hasKey).isFalse();
            });

        // Verify: Redis cache updated
        String cachedState = (String) redisTemplate.opsForValue()
            .get("order:state:" + orderId);
        assertThat(cachedState).isEqualTo(OrderState.ACCEPTED.name());
    }

    @Test
    @DisplayName("Scenario 3.3: Should reject order and cancel timeout")
    void shouldRejectOrderAndCancelTimeout() throws Exception {
        // Given: Order in PENDING_ACCEPTANCE state
        UUID orderId = createOrderInPendingAcceptanceState();
        UUID restaurantId = UUID.randomUUID();

        // Verify timeout key exists
        String timeoutKey = "timeout:restaurant_acceptance:" + orderId;
        assertThat(redisTemplate.hasKey(timeoutKey)).isTrue();

        // When: POST /api/v1/restaurant/orders/{orderId}/reject
        RejectOrderRequest request = RejectOrderRequest.builder()
            .restaurantId(restaurantId)
            .reason("Out of ingredients")
            .build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/reject"),
            request,
            OrderResponse.class
        );

        // Then: Order rejected successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getState()).isEqualTo(OrderState.REJECTED.name());

        // Verify: Order state updated in database
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getState()).isEqualTo(OrderState.REJECTED);

        // Verify: Timeout key deleted from Redis
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                Boolean hasKey = redisTemplate.hasKey(timeoutKey);
                assertThat(hasKey).isFalse();
            });
    }

    @Test
    @DisplayName("Scenario 3.4: Should auto-reject order on restaurant timeout")
    void shouldAutoRejectOrderOnTimeout() throws Exception {
        // Given: Order in PENDING_ACCEPTANCE state
        UUID orderId = createOrderInPendingAcceptanceState();

        // Verify initial state
        Order initialOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(initialOrder.getState()).isEqualTo(OrderState.PENDING_ACCEPTANCE);

        // When: Wait for timeout (5 seconds configured for tests)
        // Redis keyspace notification should trigger auto-rejection
        
        // Then: Order auto-rejected after timeout
        await().atMost(Duration.ofSeconds(8))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                Order order = orderRepository.findById(orderId).orElseThrow();
                assertThat(order.getState()).isEqualTo(OrderState.REJECTED);
            });

        // Verify: Order state is REJECTED
        Order rejectedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(rejectedOrder.getState()).isEqualTo(OrderState.REJECTED);

        // Verify: Timeout key no longer exists
        String timeoutKey = "timeout:restaurant_acceptance:" + orderId;
        assertThat(redisTemplate.hasKey(timeoutKey)).isFalse();
    }

    @Test
    @DisplayName("Scenario 3.5: Should list pending orders for restaurant")
    void shouldListPendingOrdersForRestaurant() throws Exception {
        // Given: Multiple orders in PENDING_ACCEPTANCE state
        UUID orderId1 = createOrderInPendingAcceptanceState();
        UUID orderId2 = createOrderInPendingAcceptanceState();
        UUID orderId3 = createOrderInPendingAcceptanceState();

        // When: GET /api/v1/restaurant/orders/pending
        ResponseEntity<OrderResponse[]> response = restTemplate.getForEntity(
            getApiUrl("/restaurant/orders/pending"),
            OrderResponse[].class
        );

        // Then: Returns list of pending orders
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(3);

        // Verify: All returned orders are in PENDING_ACCEPTANCE state
        Arrays.stream(response.getBody())
            .forEach(order -> assertThat(order.getState())
                .isEqualTo(OrderState.PENDING_ACCEPTANCE.name()));
    }

    @Test
    @DisplayName("Scenario 3.6: Should not accept order from invalid state")
    void shouldNotAcceptOrderFromInvalidState() {
        // Given: Order in CREATED state (not PENDING_ACCEPTANCE)
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrder(customerId);

        // When: Try to accept order
        AcceptOrderRequest request = AcceptOrderRequest.builder()
            .restaurantId(UUID.randomUUID())
            .estimatedPreparationTime(20)
            .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/accept"),
            request,
            String.class
        );

        // Then: Bad request - invalid state transition
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("state");
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

    private UUID createOrderInPaymentConfirmedState() {
        // Create order and manually transition to PAYMENT_CONFIRMED
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrder(customerId);

        // Transition through states
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.VALIDATED);
        order.setState(OrderState.PAYMENT_CONFIRMED);
        orderRepository.save(order);

        return orderId;
    }

    private UUID createOrderInPendingAcceptanceState() throws Exception {
        // Create order and transition to PENDING_ACCEPTANCE
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrder(customerId);

        // Transition through states
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.VALIDATED);
        order.setState(OrderState.PAYMENT_CONFIRMED);
        order.setState(OrderState.PENDING_ACCEPTANCE);
        orderRepository.save(order);

        // Schedule timeout manually
        String timeoutKey = "timeout:restaurant_acceptance:" + orderId;
        redisTemplate.opsForValue().set(timeoutKey, orderId.toString(), Duration.ofSeconds(5));

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
