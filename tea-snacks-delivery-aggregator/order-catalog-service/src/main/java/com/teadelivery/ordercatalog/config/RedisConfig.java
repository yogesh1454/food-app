package com.teadelivery.ordercatalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // JSON serializer for values with Java 8 date/time support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // Set key-value serialization
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        
        // Set hash key-value serialization
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        log.info("RedisTemplate configured with Java 8 date/time support");
        
        // Enable keyspace notifications for expired events
        try {
            template.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
                return null;
            });
            log.info("Redis keyspace notifications enabled for expired events");
        } catch (Exception e) {
            log.warn("Failed to enable Redis keyspace notifications: {}", e.getMessage());
        }
        
        return template;
    }
    
    /**
     * Redis message listener container for keyspace notifications
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
