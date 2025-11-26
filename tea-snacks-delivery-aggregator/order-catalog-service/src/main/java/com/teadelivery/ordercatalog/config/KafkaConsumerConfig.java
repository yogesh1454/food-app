package com.teadelivery.ordercatalog.config;

import com.teadelivery.ordercatalog.delivery.fsm.events.DeliveryStateChangedEvent;
import com.teadelivery.ordercatalog.order.fsm.events.OrderStateChangedEvent;
import com.teadelivery.ordercatalog.delivery.fsm.events.RiderAssignmentRequestEvent;
import com.teadelivery.ordercatalog.delivery.fsm.events.RiderAssignmentResponseEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration
 * Configures consumers with manual commit and JSON deserialization
 * Only active when features.kafka.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "features.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    /**
     * Consumer factory for OrderStateChangedEvent
     */
    @Bean
    public ConsumerFactory<String, OrderStateChangedEvent> orderEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-fsm-consumers");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderStateChangedEvent.class);
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new JsonDeserializer<>(OrderStateChangedEvent.class, false)
        );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStateChangedEvent> 
        orderEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderStateChangedEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
    
    /**
     * Consumer factory for DeliveryStateChangedEvent
     */
    @Bean
    public ConsumerFactory<String, DeliveryStateChangedEvent> deliveryEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "delivery-fsm-consumers");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DeliveryStateChangedEvent.class);
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new JsonDeserializer<>(DeliveryStateChangedEvent.class, false)
        );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStateChangedEvent> 
        deliveryEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DeliveryStateChangedEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(deliveryEventConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
    
    /**
     * Consumer factory for RiderAssignmentRequestEvent
     */
    @Bean
    public ConsumerFactory<String, RiderAssignmentRequestEvent> assignmentRequestConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "rider-assignment-consumers");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RiderAssignmentRequestEvent.class);
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new JsonDeserializer<>(RiderAssignmentRequestEvent.class, false)
        );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RiderAssignmentRequestEvent> 
        assignmentRequestKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RiderAssignmentRequestEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentRequestConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
    
    /**
     * Consumer factory for RiderAssignmentResponseEvent
     */
    @Bean
    public ConsumerFactory<String, RiderAssignmentResponseEvent> assignmentResponseConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-fsm-consumers");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RiderAssignmentResponseEvent.class);
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new JsonDeserializer<>(RiderAssignmentResponseEvent.class, false)
        );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RiderAssignmentResponseEvent> 
        assignmentResponseKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RiderAssignmentResponseEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentResponseConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
