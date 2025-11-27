package com.teadelivery.ordercatalog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.order.repository.OrderStateAuditRepository;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.repository.SubOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base Integration Test
 * Provides common setup for all integration tests with Testcontainers
 * Uses real PostgreSQL, Redis, and Kafka containers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local-integration")
// Note: NOT using @Transactional on class level because it prevents the test from seeing
// data changes made by the API (different transactions). Instead, tests should create 
// unique data that won't conflict with other tests.
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected SubOrderRepository subOrderRepository;

    @Autowired
    protected OrderStateAuditRepository auditRepository;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    protected TestDataBuilder testDataBuilder;

    // Note: Using local Docker containers (docker-compose)
    // No Testcontainers - connects to localhost:5432, localhost:6379, localhost:9092
    // Containers must be started with: docker-compose up -d
    
    @BeforeEach
    void setUp() {
        // Clean Redis before each test (Redis data doesn't rollback with @Transactional)
        cleanRedis();
        // Note: Database cleanup handled automatically by @Transactional rollback
    }

    protected void cleanRedis() {
        try {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        } catch (Exception e) {
            // Ignore if Redis is not available
        }
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    protected String getApiUrl(String path) {
        return getBaseUrl() + "/api/v1" + path;
    }
}
