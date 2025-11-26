package com.teadelivery.ordercatalog.common.fsm;

import com.teadelivery.ordercatalog.delivery.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.order.fsm.events.OrderStateChangedEvent;
import com.teadelivery.ordercatalog.delivery.fsm.events.RiderAssignmentRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event Publisher
 * Publishes FSM state change events to Kafka using proper event schemas
 * Falls back to no-op when Kafka is disabled
 */
@Service
@Slf4j
public class EventPublisher {
    
    @Value("${features.kafka.enabled:false}")
    private boolean kafkaEnabled;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Topic names as per BE-003-14
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String DELIVERY_EVENTS_TOPIC = "delivery-events";
    private static final String ASSIGNMENT_REQUESTS_TOPIC = "assignment-requests";
    
    /**
     * Publish order state change event
     * Uses orderId as partition key for consistent ordering
     */
    public void publishOrderStateChange(
        UUID orderId,
        String previousState,
        String newState,
        String trigger,
        UUID customerId,
        UUID restaurantId,
        Map<String, Object> metadata
    ) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.debug("Kafka disabled, skipping order state change event: orderId={}", orderId);
            return;
        }
        try {
            OrderStateChangedEvent event = OrderStateChangedEvent.builder()
                .orderId(orderId)
                .previousState(previousState)
                .newState(newState)
                .trigger(trigger)
                .customerId(customerId)
                .restaurantId(restaurantId)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
            
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, orderId.toString(), event);
            
            log.info("Published order state change: orderId={}, from={}, to={}", 
                orderId, previousState, newState);
                
        } catch (Exception e) {
            log.error("Failed to publish order state change: orderId={}", orderId, e);
            // Don't throw - event publishing is not critical for state transition
        }
    }
    
    /**
     * Publish delivery state change event
     * Uses deliveryId as partition key for consistent ordering
     */
    public void publishDeliveryStateChange(
        UUID deliveryId,
        UUID orderId,
        String previousState,
        String newState,
        String trigger,
        UUID riderId,
        Map<String, Object> metadata
    ) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.debug("Kafka disabled, skipping delivery state change event: deliveryId={}", deliveryId);
            return;
        }
        try {
            DeliveryStateChangedEvent event = DeliveryStateChangedEvent.builder()
                .deliveryId(deliveryId)
                .orderId(orderId)
                .previousState(previousState)
                .newState(newState)
                .trigger(trigger)
                .riderId(riderId)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
            
            kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, deliveryId.toString(), event);
            
            log.info("Published delivery state change: deliveryId={}, from={}, to={}", 
                deliveryId, previousState, newState);
                
        } catch (Exception e) {
            log.error("Failed to publish delivery state change: deliveryId={}", deliveryId, e);
        }
    }
    
    /**
     * Publish rider assignment request
     */
    public void publishRiderAssignmentRequest(RiderAssignmentRequestEvent event) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.debug("Kafka disabled, skipping rider assignment request: orderId={}", event.getOrderId());
            return;
        }
        try {
            kafkaTemplate.send(ASSIGNMENT_REQUESTS_TOPIC, event.getOrderId().toString(), event);
            
            log.info("Published rider assignment request: orderId={}, deliveryId={}", 
                event.getOrderId(), event.getDeliveryId());
                
        } catch (Exception e) {
            log.error("Failed to publish rider assignment request: orderId={}", 
                event.getOrderId(), e);
        }
    }
    
    /**
     * Legacy method - kept for backward compatibility
     * Delegates to new publishOrderStateChange method
     */
    @Deprecated
    public void publishStateChange(
        UUID entityId,
        String entityType,
        String fromState,
        String toState,
        String trigger
    ) {
        if ("ORDER".equals(entityType)) {
            publishOrderStateChange(entityId, fromState, toState, trigger, null, null, new HashMap<>());
        } else {
            log.warn("Legacy publishStateChange called with unsupported entityType: {}", entityType);
        }
    }
}
