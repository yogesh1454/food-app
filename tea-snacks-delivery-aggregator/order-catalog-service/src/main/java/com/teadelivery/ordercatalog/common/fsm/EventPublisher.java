package com.teadelivery.ordercatalog.common.fsm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.delivery.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.delivery.fsm.events.RiderAssignmentRequestEvent;
import com.teadelivery.ordercatalog.order.fsm.events.OrderStateChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event Publisher
 * Publishes FSM state change events to AWS SNS topics.
 * Events are consumed by SQS queues for Order ↔ Delivery integration.
 * 
 * Architecture:
 * - Order Events SNS Topic → Delivery Service SQS Queue
 * - Delivery Events SNS Topic → Order Service SQS Queue
 * 
 * As per BE-004-26
 */
@Service
@Slf4j
public class EventPublisher {

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private SnsClient snsClient;

    @Value("${aws.sns.topics.order-events:}")
    private String orderEventsTopicArn;

    @Value("${aws.sns.topics.delivery-events:}")
    private String deliveryEventsTopicArn;

    @Value("${features.sns.order-delivery-events.enabled:false}")
    private boolean snsEnabled;

    public EventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Publish order state change event to SNS
     */
    public void publishOrderStateChange(
            UUID orderId,
            String previousState,
            String newState,
            String trigger,
            UUID customerId,
            UUID restaurantId,
            Map<String, Object> metadata) {
        if (!snsEnabled || snsClient == null || orderEventsTopicArn == null || orderEventsTopicArn.isEmpty()) {
            log.debug("SNS disabled or topic not configured, skipping order event: orderId={}", orderId);
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
                    .idempotencyKey(UUID.randomUUID())
                    .metadata(metadata != null ? metadata : new HashMap<>())
                    // Location info for delivery creation (from metadata if available)
                    .pickupLocation(metadata != null ? (String) metadata.get("pickupLocation") : null)
                    .deliveryLocation(metadata != null ? (String) metadata.get("deliveryLocation") : null)
                    .deliveryFee(metadata != null ? (BigDecimal) metadata.get("deliveryFee") : null)
                    .build();

            String messageBody = objectMapper.writeValueAsString(event);

            // Message attributes for filtering
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue("ORDER_STATE_CHANGED")
                    .build());
            messageAttributes.put("toState", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(newState)
                    .build());
            messageAttributes.put("orderId", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(orderId.toString())
                    .build());

            PublishRequest request = PublishRequest.builder()
                    .topicArn(orderEventsTopicArn)
                    .message(messageBody)
                    .messageAttributes(messageAttributes)
                    .build();

            PublishResponse response = snsClient.publish(request);

            log.info("Published order state change to SNS: orderId={}, from={}, to={}, messageId={}",
                    orderId, previousState, newState, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order event: orderId={}", orderId, e);
        } catch (Exception e) {
            log.error("Failed to publish order state change to SNS: orderId={}", orderId, e);
            // Don't throw - event publishing is not critical for state transition
        }
    }

    /**
     * Publish delivery state change event to SNS
     */
    public void publishDeliveryStateChange(
            UUID deliveryId,
            UUID orderId,
            String previousState,
            String newState,
            String trigger,
            UUID riderId,
            Map<String, Object> metadata) {
        if (!snsEnabled || snsClient == null || deliveryEventsTopicArn == null || deliveryEventsTopicArn.isEmpty()) {
            log.debug("SNS disabled or topic not configured, skipping delivery event: deliveryId={}", deliveryId);
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
                    .idempotencyKey(UUID.randomUUID())
                    .metadata(metadata != null ? metadata : new HashMap<>())
                    .build();

            String messageBody = objectMapper.writeValueAsString(event);

            // Message attributes for filtering
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue("DELIVERY_STATE_CHANGED")
                    .build());
            messageAttributes.put("toState", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(newState)
                    .build());
            messageAttributes.put("deliveryId", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(deliveryId.toString())
                    .build());
            messageAttributes.put("orderId", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(orderId.toString())
                    .build());

            PublishRequest request = PublishRequest.builder()
                    .topicArn(deliveryEventsTopicArn)
                    .message(messageBody)
                    .messageAttributes(messageAttributes)
                    .build();

            PublishResponse response = snsClient.publish(request);

            log.info("Published delivery state change to SNS: deliveryId={}, orderId={}, from={}, to={}, messageId={}",
                    deliveryId, orderId, previousState, newState, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize delivery event: deliveryId={}", deliveryId, e);
        } catch (Exception e) {
            log.error("Failed to publish delivery state change to SNS: deliveryId={}", deliveryId, e);
            // Don't throw - event publishing is not critical for state transition
        }
    }

    /**
     * Publish rider assignment request
     */
    public void publishRiderAssignmentRequest(RiderAssignmentRequestEvent event) {
        // For now, rider assignment is handled directly by RiderAssignmentService
        // This is a placeholder for future async rider matching
        log.debug("Rider assignment request not published to SNS (handled synchronously): orderId={}",
                event.getOrderId());
    }

    /**
     * Legacy method - kept for backward compatibility
     */
    @Deprecated
    public void publishStateChange(
            UUID entityId,
            String entityType,
            String fromState,
            String toState,
            String trigger) {
        if ("ORDER".equals(entityType)) {
            publishOrderStateChange(entityId, fromState, toState, trigger, null, null, new HashMap<>());
        } else {
            log.warn("Legacy publishStateChange called with unsupported entityType: {}", entityType);
        }
    }
}
