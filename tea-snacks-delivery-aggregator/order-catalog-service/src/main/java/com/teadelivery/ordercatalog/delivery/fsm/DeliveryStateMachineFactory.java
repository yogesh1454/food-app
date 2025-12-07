package com.teadelivery.ordercatalog.delivery.fsm;

import com.teadelivery.ordercatalog.common.fsm.EventPublisher;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryStateAuditRepository;
import com.teadelivery.ordercatalog.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for creating DeliveryStateMachine instances
 * 
 * This factory injects all required dependencies and creates a new FSM instance
 * for each Delivery. The FSM instance has direct access to the Delivery object.
 * 
 * Usage:
 * DeliveryStateMachine fsm = fsmFactory.create(delivery);
 * Delivery saved = fsm.withActor(riderId, "RIDER").riderAccept();
 * 
 * As per BE-003-22
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryStateMachineFactory {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStateAuditRepository auditRepository;
    private final EventPublisher eventPublisher;
    private final NotificationService notificationService;

    /**
     * Create a new DeliveryStateMachine for the given delivery
     * 
     * @param delivery The delivery entity (must have state set)
     * @return A new DeliveryStateMachine instance
     */
    public DeliveryStateMachine create(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }

        // Ensure delivery has a state (default to PENDING for new deliveries)
        if (delivery.getState() == null) {
            delivery.setState(DeliveryState.PENDING);
        }

        log.debug("Creating DeliveryStateMachine for delivery: {} (state: {})",
                delivery.getDeliveryId(), delivery.getState());

        return new DeliveryStateMachine(
                delivery,
                deliveryRepository,
                auditRepository,
                eventPublisher,
                notificationService);
    }
}
