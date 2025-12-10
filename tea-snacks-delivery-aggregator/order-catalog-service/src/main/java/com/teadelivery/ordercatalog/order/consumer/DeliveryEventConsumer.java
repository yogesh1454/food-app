package com.teadelivery.ordercatalog.order.consumer;

import com.teadelivery.ordercatalog.delivery.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.order.fsm.OrderStateMachineFactory;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Delivery Event Consumer
 * Listens to delivery-events topic and triggers order FSM transitions
 * As per BE-004-26
 * Only active when features.kafka.enabled=true
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class DeliveryEventConsumer {

    private final OrderStateMachineFactory fsmFactory;
    private final OrderRepository orderRepository;

    /**
     * Handle delivery state changed events
     * Updates order FSM based on delivery state
     */
    @KafkaListener(topics = "delivery-events", groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleDeliveryEvent(DeliveryStateChangedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received delivery event: deliveryId={}, orderId={}, fromState={}, toState={}, idempotencyKey={}",
                    event.getDeliveryId(), event.getOrderId(), event.getFromState(),
                    event.getToState(), event.getIdempotencyKey());

            // Fetch order
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Order not found: " + event.getOrderId()));

            // Map delivery state to order FSM transition
            DeliveryState toState = event.getToState();

            switch (toState) {
                case RIDER_ACCEPTED:
                    log.info(
                            "Rider accepted delivery, transitioning order to ASSIGNED_TO_RIDER: orderId={}, riderId={}",
                            event.getOrderId(), event.getRiderId());
                    fsmFactory.create(order)
                            .withActor(event.getRiderId(), "SYSTEM")
                            .assignRider();
                    break;

                case PICKED_UP:
                    log.info("Rider picked up order, transitioning to PICKED_UP: orderId={}, riderId={}",
                            event.getOrderId(), event.getRiderId());
                    fsmFactory.create(order)
                            .withActor(event.getRiderId(), "RIDER")
                            .pickup();
                    break;

                case DELIVERED:
                    log.info("Order delivered successfully, transitioning to DELIVERED: orderId={}, riderId={}",
                            event.getOrderId(), event.getRiderId());
                    fsmFactory.create(order)
                            .withActor(event.getRiderId(), "RIDER")
                            .deliver();
                    break;

                case FAILED:
                    String failureReason = event.getMetadata() != null
                            ? (String) event.getMetadata().get("failureReason")
                            : "Delivery failed";
                    log.error("Delivery failed, cancelling order: orderId={}, reason={}",
                            event.getOrderId(), failureReason);

                    // Only cancel if order is still cancellable
                    if (order.isCancellable()) {
                        fsmFactory.create(order)
                                .withActor(null, "SYSTEM")
                                .cancel("SYSTEM", "Delivery failed: " + failureReason);
                    } else {
                        log.warn("Cannot cancel order in state: {}", order.getState());
                    }
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
