package com.teadelivery.ordercatalog.event.consumer;

import com.teadelivery.ordercatalog.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.fsm.DeliveryState;
import com.teadelivery.ordercatalog.fsm.OrderTrigger;
import com.teadelivery.ordercatalog.fsm.order.OrderFSM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Delivery Event Consumer
 * Listens to delivery-events topic and triggers order FSM transitions
 * As per BE-004-26
 */
@Service
@Slf4j
public class DeliveryEventConsumer {
    
    private final OrderFSM orderFSM;
    
    public DeliveryEventConsumer(OrderFSM orderFSM) {
        this.orderFSM = orderFSM;
    }
    
    /**
     * Handle delivery state changed events
     * Updates order FSM based on delivery state
     */
    @KafkaListener(
        topics = "delivery-events",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryEvent(DeliveryStateChangedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received delivery event: deliveryId={}, orderId={}, fromState={}, toState={}, idempotencyKey={}", 
                     event.getDeliveryId(), event.getOrderId(), event.getFromState(), 
                     event.getToState(), event.getIdempotencyKey());
            
            // Map delivery state to order trigger
            DeliveryState toState = event.getToState();
            
            switch (toState) {
                case RIDER_ACCEPTED:
                    log.info("Rider accepted delivery, assigning to order: orderId={}, riderId={}", 
                             event.getOrderId(), event.getRiderId());
                    orderFSM.fire(event.getOrderId(), OrderTrigger.ASSIGN_RIDER);
                    break;
                    
                case PICKED_UP:
                    log.info("Rider picked up order: orderId={}, riderId={}", 
                             event.getOrderId(), event.getRiderId());
                    orderFSM.fire(event.getOrderId(), OrderTrigger.RIDER_PICKUP);
                    break;
                    
                case DELIVERED:
                    log.info("Order delivered successfully: orderId={}, riderId={}", 
                             event.getOrderId(), event.getRiderId());
                    orderFSM.fire(event.getOrderId(), OrderTrigger.DELIVER_ORDER);
                    break;
                    
                case FAILED:
                    log.error("Delivery failed, cancelling order: orderId={}, reason={}", 
                              event.getOrderId(), event.getFailureReason());
                    orderFSM.fire(event.getOrderId(), OrderTrigger.CANCEL);
                    break;
                    
                default:
                    log.debug("Delivery state change does not require order FSM update: state={}", toState);
            }
            
            // Acknowledge message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing delivery event: deliveryId={}, orderId={}, error={}", 
                     event.getDeliveryId(), event.getOrderId(), e.getMessage(), e);
            // Don't acknowledge - message will be retried or sent to DLQ
            throw e;
        }
    }
}
