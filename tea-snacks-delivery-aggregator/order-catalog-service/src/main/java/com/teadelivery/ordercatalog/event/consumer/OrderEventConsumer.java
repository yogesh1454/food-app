package com.teadelivery.ordercatalog.event.consumer;

import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import com.teadelivery.ordercatalog.fsm.events.OrderStateChangedEvent;
import com.teadelivery.ordercatalog.fsm.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Order Event Consumer
 * Listens to order-events topic and triggers delivery creation
 * As per BE-004-26
 */
@Service
@Slf4j
public class OrderEventConsumer {
    
    private final DeliveryService deliveryService;
    
    public OrderEventConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    /**
     * Handle order state changed events
     * Creates delivery when order is READY_FOR_PICKUP
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "delivery-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(OrderStateChangedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received order event: orderId={}, fromState={}, toState={}, idempotencyKey={}", 
                     event.getOrderId(), event.getFromState(), event.getToState(), 
                     event.getIdempotencyKey());
            
            // Check if order is ready for pickup
            if (event.getToState() == OrderState.READY_FOR_PICKUP) {
                log.info("Order ready for pickup, creating delivery: orderId={}", event.getOrderId());
                
                // Check idempotency - prevent duplicate delivery creation
                if (deliveryService.deliveryExistsForOrder(event.getOrderId())) {
                    log.warn("Delivery already exists for order: orderId={}", event.getOrderId());
                    acknowledgment.acknowledge();
                    return;
                }
                
                // Create delivery
                deliveryService.createDelivery(
                    event.getOrderId(),
                    event.getPickupLocation(),
                    event.getDeliveryLocation(),
                    event.getDeliveryFee() != null ? event.getDeliveryFee() : new BigDecimal("50.00")
                );
                
                // Start rider search
                deliveryService.startRiderSearchByOrderId(event.getOrderId());
                
                log.info("Delivery created and rider search started: orderId={}", event.getOrderId());
            }
            
            // Acknowledge message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing order event: orderId={}, error={}", 
                     event.getOrderId(), e.getMessage(), e);
            // Don't acknowledge - message will be retried or sent to DLQ
            throw e;
        }
    }
}
