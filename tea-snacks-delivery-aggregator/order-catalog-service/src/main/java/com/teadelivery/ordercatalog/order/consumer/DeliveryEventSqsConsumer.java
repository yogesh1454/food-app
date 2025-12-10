package com.teadelivery.ordercatalog.order.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.delivery.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.order.fsm.OrderStateMachineFactory;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Delivery Event Consumer (SQS)
 * Listens to delivery-events SQS queue and triggers Order FSM transitions.
 * 
 * Replaces the Kafka-based DeliveryEventConsumer.
 * Only active when features.sqs.order-delivery-events.enabled=true
 * 
 * As per BE-004-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.sqs.order-delivery-events.enabled", havingValue = "true", matchIfMissing = false)
public class DeliveryEventSqsConsumer {

    private final OrderStateMachineFactory fsmFactory;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    /**
     * Handle delivery state changed events from SQS
     * Updates order FSM based on delivery state
     */
    @SqsListener(value = "${aws.sqs.queues.delivery-events-for-order}")
    public void handleDeliveryEvent(String messageBody) {
        try {
            DeliveryStateChangedEvent event = objectMapper.readValue(messageBody, DeliveryStateChangedEvent.class);

            log.info(
                    "Received delivery event from SQS: deliveryId={}, orderId={}, fromState={}, toState={}, idempotencyKey={}",
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

            log.info("Successfully processed delivery event: deliveryId={}, orderId={}",
                    event.getDeliveryId(), event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing delivery event from SQS: error={}", e.getMessage(), e);
            // Throwing exception will cause message to be retried or sent to DLQ
            throw new RuntimeException("Failed to process delivery event", e);
        }
    }
}
