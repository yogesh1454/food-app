package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.order.event.OrderPlacedEvent;
import com.teadelivery.ordercatalog.order.event.PaymentCompletedEvent;
import com.teadelivery.ordercatalog.order.fsm.events.OrderStateChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing order-related events to Kafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_PLACED_TOPIC = "order-placed-events";
    private static final String PAYMENT_COMPLETED_TOPIC = "payment-completed-events";
    private static final String ORDER_STATE_CHANGED_TOPIC = "order-state-changed-events";
    
    /**
     * Publish OrderPlacedEvent
     */
    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            log.info("Publishing OrderPlacedEvent: orderId={}", event.getOrderId());
            kafkaTemplate.send(ORDER_PLACED_TOPIC, event.getOrderId().toString(), event);
            log.info("OrderPlacedEvent published successfully: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent: orderId={}", event.getOrderId(), e);
            throw new RuntimeException("Failed to publish OrderPlacedEvent", e);
        }
    }
    
    /**
     * Publish PaymentCompletedEvent
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("Publishing PaymentCompletedEvent: orderId={}, txnId={}", 
                event.getOrderId(), event.getTransactionId());
            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, event.getOrderId().toString(), event);
            log.info("PaymentCompletedEvent published successfully: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompletedEvent: orderId={}", event.getOrderId(), e);
            throw new RuntimeException("Failed to publish PaymentCompletedEvent", e);
        }
    }
    
    /**
     * Publish OrderStateChangedEvent
     */
    public void publishOrderStateChanged(OrderStateChangedEvent event) {
        try {
            log.info("Publishing OrderStateChangedEvent: orderId={}, {} -> {}", 
                event.getOrderId(), event.getPreviousState(), event.getNewState());
            kafkaTemplate.send(ORDER_STATE_CHANGED_TOPIC, event.getOrderId().toString(), event);
            log.info("OrderStateChangedEvent published successfully: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderStateChangedEvent: orderId={}", event.getOrderId(), e);
            throw new RuntimeException("Failed to publish OrderStateChangedEvent", e);
        }
    }
}
