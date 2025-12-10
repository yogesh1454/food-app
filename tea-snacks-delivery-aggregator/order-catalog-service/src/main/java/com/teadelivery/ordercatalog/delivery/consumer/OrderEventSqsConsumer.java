package com.teadelivery.ordercatalog.delivery.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.events.OrderStateChangedEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Order Event Consumer (SQS)
 * Listens to order-events SQS queue and triggers delivery creation.
 * 
 * Replaces the Kafka-based OrderEventConsumer.
 * Only active when features.sqs.order-delivery-events.enabled=true
 * 
 * As per BE-004-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.sqs.order-delivery-events.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventSqsConsumer {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    /**
     * Handle order state changed events from SQS
     * Creates delivery when order is READY_FOR_PICKUP
     */
    @SqsListener(value = "${aws.sqs.queues.order-events-for-delivery}")
    public void handleOrderEvent(String messageBody) {
        try {
            OrderStateChangedEvent event = objectMapper.readValue(messageBody, OrderStateChangedEvent.class);

            log.info("Received order event from SQS: orderId={}, fromState={}, toState={}, idempotencyKey={}",
                    event.getOrderId(), event.getFromState(), event.getToState(),
                    event.getIdempotencyKey());

            // Check if order is ready for pickup
            if (event.getToState() == OrderState.READY_FOR_PICKUP) {
                log.info("Order ready for pickup, creating delivery: orderId={}", event.getOrderId());

                // Check idempotency - prevent duplicate delivery creation
                if (deliveryService.deliveryExistsForOrder(event.getOrderId())) {
                    log.warn("Delivery already exists for order: orderId={}", event.getOrderId());
                    return;
                }

                // Create delivery
                deliveryService.createDelivery(
                        event.getOrderId(),
                        event.getPickupLocation(),
                        event.getDeliveryLocation(),
                        event.getDeliveryFee() != null ? event.getDeliveryFee() : new BigDecimal("50.00"));

                // Start rider search
                deliveryService.startRiderSearchByOrderId(event.getOrderId());

                log.info("Delivery created and rider search started: orderId={}", event.getOrderId());
            } else {
                log.debug("Order state change does not require delivery action: state={}", event.getToState());
            }

        } catch (Exception e) {
            log.error("Error processing order event from SQS: error={}", e.getMessage(), e);
            // Throwing exception will cause message to be retried or sent to DLQ
            throw new RuntimeException("Failed to process order event", e);
        }
    }
}
