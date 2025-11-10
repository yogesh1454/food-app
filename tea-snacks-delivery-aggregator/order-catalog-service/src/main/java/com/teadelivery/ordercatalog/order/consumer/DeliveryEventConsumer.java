package com.teadelivery.ordercatalog.order.consumer;

import com.teadelivery.ordercatalog.delivery.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.order.fsm.OrderTrigger;
import com.teadelivery.ordercatalog.order.fsm.OrderFSM;
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
            
            // Note: OrderFSM methods require Order object, not just orderId
            // This consumer should be refactored to fetch Order and call appropriate FSM methods
            // For now, logging the events
            switch (toState) {
                case RIDER_ACCEPTED:
                    log.info("Rider accepted delivery, order should transition to ASSIGNED_TO_RIDER: orderId={}, riderId={}", 
                             event.getOrderId(), event.getRiderId());
                    // TODO: Fetch order and call orderFSM.assignRider(order)
                    break;
                    
                case PICKED_UP:
                    log.info("Rider picked up order, order should transition to PICKED_UP: orderId={}, riderId={}", 
                             event.getOrderId(), event.getRiderId());
                    // TODO: Fetch order and call orderFSM.pickupOrder(order)
                    break;
                    
                case DELIVERED:
                    log.info("Order delivered successfully, order should transition to DELIVERED: orderId={}, riderId={}", 
                             event.getOrderId(), event.getRiderId());
                    // TODO: Fetch order and call orderFSM.deliverOrder(order)
                    break;
                    
                case FAILED:
                    log.error("Delivery failed, order should be cancelled: orderId={}, reason={}", 
                              event.getOrderId(), event.getFailureReason());
                    // TODO: Fetch order and call orderFSM.cancelOrder(order, "SYSTEM", failureReason)
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
