package com.teadelivery.ordercatalog.integration;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.events.OrderStateChangedEvent;
import com.teadelivery.ordercatalog.order.dto.AcceptOrderRequest;
import com.teadelivery.ordercatalog.order.dto.CreateOrderRequest;
import com.teadelivery.ordercatalog.order.dto.OrderItemRequest;
import com.teadelivery.ordercatalog.order.dto.OrderResponse;
import com.teadelivery.ordercatalog.order.model.Order;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration Tests for Redis Cache and Kafka Event Publishing
 * Tests scenarios 7.1 - 8.3 from the test plan
 */
@DisplayName("Redis Cache & Kafka Event Integration Tests")
class RedisCacheKafkaIntegrationTest extends BaseIntegrationTest {

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    @Test
    @DisplayName("Scenario 7.1: Should cache order state on transition")
    void shouldCacheOrderStateOnTransition() throws Exception {
        // Given: Order in PENDING_ACCEPTANCE state
        UUID orderId = createOrderInPendingAcceptanceState();

        // When: Accept order (state transition)
        AcceptOrderRequest request = AcceptOrderRequest.builder()
            .restaurantId(UUID.randomUUID())
            .estimatedPreparationTime(20)
            .build();

        restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/accept"),
            request,
            OrderResponse.class
        );

        // Then: Redis cache updated with new state
        await().atMost(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                String cachedState = (String) redisTemplate.opsForValue()
                    .get("order:state:" + orderId);
                assertThat(cachedState).isEqualTo(OrderState.ACCEPTED.name());
            });
    }

    @Test
    @DisplayName("Scenario 7.2: Should retrieve state from cache")
    void shouldRetrieveStateFromCache() {
        // Given: Order state cached in Redis
        UUID orderId = UUID.randomUUID();
        String cacheKey = "order:state:" + orderId;
        redisTemplate.opsForValue().set(cacheKey, OrderState.PREPARING.name(), Duration.ofHours(1));

        // When: Get cached state
        String cachedState = (String) redisTemplate.opsForValue().get(cacheKey);

        // Then: State retrieved from Redis
        assertThat(cachedState).isEqualTo(OrderState.PREPARING.name());
    }

    @Test
    @DisplayName("Scenario 7.3: Should invalidate cache on state change")
    void shouldInvalidateCacheOnStateChange() throws Exception {
        // Given: Order with cached state
        UUID orderId = createOrderInPendingAcceptanceState();
        String cacheKey = "order:state:" + orderId;

        // Verify cache exists
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
        String initialState = (String) redisTemplate.opsForValue().get(cacheKey);
        assertThat(initialState).isEqualTo(OrderState.PENDING_ACCEPTANCE.name());

        // When: State changes (accept order)
        AcceptOrderRequest request = AcceptOrderRequest.builder()
            .restaurantId(UUID.randomUUID())
            .estimatedPreparationTime(20)
            .build();

        restTemplate.postForEntity(
            getApiUrl("/restaurant/orders/" + orderId + "/accept"),
            request,
            OrderResponse.class
        );

        // Then: Cache updated with new state
        await().atMost(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                String newState = (String) redisTemplate.opsForValue().get(cacheKey);
                assertThat(newState).isEqualTo(OrderState.ACCEPTED.name());
            });
    }

    @Test
    @DisplayName("Scenario 7.4: Should handle cache TTL expiration")
    void shouldHandleCacheTTLExpiration() throws InterruptedException {
        // Given: Order state cached with short TTL
        UUID orderId = UUID.randomUUID();
        String cacheKey = "order:state:" + orderId;
        redisTemplate.opsForValue().set(cacheKey, OrderState.CREATED.name(), Duration.ofSeconds(2));

        // Verify cache exists
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // When: Wait for TTL to expire
        Thread.sleep(3000);

        // Then: Cache expired
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    @DisplayName("Scenario 8.1: Should publish OrderStateChangedEvent to Kafka")
    void shouldPublishOrderStateChangedEvent() throws Exception {
        // Given: Kafka consumer listening to order-events topic
        BlockingQueue<ConsumerRecord<String, OrderStateChangedEvent>> records = 
            new LinkedBlockingQueue<>();
        
        KafkaMessageListenerContainer<String, OrderStateChangedEvent> container = 
            createKafkaConsumer(records);
        container.start();

        try {
            // When: Order state changes
            UUID orderId = createOrderInPendingAcceptanceState();
            
            AcceptOrderRequest request = AcceptOrderRequest.builder()
                .restaurantId(UUID.randomUUID())
                .estimatedPreparationTime(20)
                .build();

            restTemplate.postForEntity(
                getApiUrl("/restaurant/orders/" + orderId + "/accept"),
                request,
                OrderResponse.class
            );

            // Then: Event published to Kafka
            ConsumerRecord<String, OrderStateChangedEvent> record = 
                records.poll(10, TimeUnit.SECONDS);
            
            assertThat(record).isNotNull();
            assertThat(record.topic()).isEqualTo(ORDER_EVENTS_TOPIC);
            
            OrderStateChangedEvent event = record.value();
            assertThat(event.getOrderId()).isEqualTo(orderId);
            assertThat(event.getNewState()).isEqualTo(OrderState.ACCEPTED.name());
            assertThat(event.getPreviousState()).isEqualTo(OrderState.PENDING_ACCEPTANCE.name());
            
        } finally {
            container.stop();
        }
    }

    @Test
    @DisplayName("Scenario 8.2: Should use orderId as partition key for consistent ordering")
    void shouldUseOrderIdAsPartitionKey() throws Exception {
        // Given: Kafka consumer
        BlockingQueue<ConsumerRecord<String, OrderStateChangedEvent>> records = 
            new LinkedBlockingQueue<>();
        
        KafkaMessageListenerContainer<String, OrderStateChangedEvent> container = 
            createKafkaConsumer(records);
        container.start();

        try {
            // When: Multiple state changes for same order
            UUID orderId = createOrderInPendingAcceptanceState();
            
            // Accept order
            AcceptOrderRequest acceptRequest = AcceptOrderRequest.builder()
                .restaurantId(UUID.randomUUID())
                .estimatedPreparationTime(20)
                .build();

            restTemplate.postForEntity(
                getApiUrl("/restaurant/orders/" + orderId + "/accept"),
                acceptRequest,
                OrderResponse.class
            );

            // Start preparing
            restTemplate.postForEntity(
                getApiUrl("/restaurant/orders/" + orderId + "/start-preparing"),
                null,
                OrderResponse.class
            );

            // Then: All events use orderId as key
            ConsumerRecord<String, OrderStateChangedEvent> record1 = 
                records.poll(10, TimeUnit.SECONDS);
            ConsumerRecord<String, OrderStateChangedEvent> record2 = 
                records.poll(10, TimeUnit.SECONDS);

            assertThat(record1).isNotNull();
            assertThat(record2).isNotNull();
            
            // Both events should have orderId as key
            assertThat(record1.key()).isEqualTo(orderId.toString());
            assertThat(record2.key()).isEqualTo(orderId.toString());
            
            // Events should be in order (same partition)
            assertThat(record1.partition()).isEqualTo(record2.partition());
            
        } finally {
            container.stop();
        }
    }

    @Test
    @DisplayName("Scenario 8.3: Should include all required fields in event")
    void shouldIncludeAllRequiredFieldsInEvent() throws Exception {
        // Given: Kafka consumer
        BlockingQueue<ConsumerRecord<String, OrderStateChangedEvent>> records = 
            new LinkedBlockingQueue<>();
        
        KafkaMessageListenerContainer<String, OrderStateChangedEvent> container = 
            createKafkaConsumer(records);
        container.start();

        try {
            // When: Order state changes
            UUID customerId = UUID.randomUUID();
            UUID orderId = createOrderInPendingAcceptanceState();
            
            AcceptOrderRequest request = AcceptOrderRequest.builder()
                .restaurantId(UUID.randomUUID())
                .estimatedPreparationTime(20)
                .build();

            restTemplate.postForEntity(
                getApiUrl("/restaurant/orders/" + orderId + "/accept"),
                request,
                OrderResponse.class
            );

            // Then: Event contains all required fields
            ConsumerRecord<String, OrderStateChangedEvent> record = 
                records.poll(10, TimeUnit.SECONDS);
            
            assertThat(record).isNotNull();
            OrderStateChangedEvent event = record.value();
            
            // Verify all required fields
            assertThat(event.getOrderId()).isNotNull();
            assertThat(event.getPreviousState()).isNotNull();
            assertThat(event.getNewState()).isNotNull();
            assertThat(event.getTrigger()).isNotNull();
            assertThat(event.getTimestamp()).isNotNull();
            // customerId and restaurantId may be null depending on implementation
            
        } finally {
            container.stop();
        }
    }

    // ========== Helper Methods ==========

    private KafkaMessageListenerContainer<String, OrderStateChangedEvent> createKafkaConsumer(
        BlockingQueue<ConsumerRecord<String, OrderStateChangedEvent>> records
    ) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderStateChangedEvent.class.getName());

        DefaultKafkaConsumerFactory<String, OrderStateChangedEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProps = new ContainerProperties(ORDER_EVENTS_TOPIC);
        containerProps.setMessageListener((MessageListener<String, OrderStateChangedEvent>) records::add);

        return new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
    }

    private UUID createOrderInPendingAcceptanceState() {
        UUID customerId = UUID.randomUUID();
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

        UUID orderId = response.getBody().getOrderId();

        // Transition to PENDING_ACCEPTANCE
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setState(OrderState.VALIDATED);
        order.setState(OrderState.PAYMENT_CONFIRMED);
        order.setState(OrderState.PENDING_ACCEPTANCE);
        orderRepository.save(order);

        // Schedule timeout
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
                .build()
        );
    }

    private Map<String, Object> createValidDeliveryAddress() {
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main Street");
        address.put("city", "Mumbai");
        address.put("pincode", "400001");
        return address;
    }
}
