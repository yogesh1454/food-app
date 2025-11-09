package com.teadelivery.ordercatalog.fsm.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event Publisher
 * Publishes FSM state change events to Kafka
 */
@Service
@Slf4j
public class EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String STATE_CHANGE_TOPIC = "order.state.changed";
    
    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publish state change event
     */
    public void publishStateChange(
        UUID entityId,
        String entityType,
        String fromState,
        String toState,
        String trigger
    ) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("entityId", entityId.toString());
            event.put("entityType", entityType);
            event.put("fromState", fromState);
            event.put("toState", toState);
            event.put("trigger", trigger);
            event.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send(STATE_CHANGE_TOPIC, entityId.toString(), event);
            
            log.info("Published state change event: entityId={}, entityType={}, from={}, to={}",
                entityId, entityType, fromState, toState);
                
        } catch (Exception e) {
            log.error("Failed to publish state change event: entityId={}", entityId, e);
            // Don't throw - event publishing is not critical for state transition
        }
    }
    
    /**
     * Publish order ready for pickup event
     */
    public void publishOrderReadyForPickup(UUID orderId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("orderId", orderId.toString());
            event.put("eventType", "ORDER_READY_FOR_PICKUP");
            event.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("order.ready.pickup", orderId.toString(), event);
            
            log.info("Published order ready for pickup event: orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to publish order ready event: orderId={}", orderId, e);
        }
    }
}
