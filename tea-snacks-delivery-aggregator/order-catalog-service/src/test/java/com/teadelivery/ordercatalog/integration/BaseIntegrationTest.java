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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base Integration Test
 * Provides common setup for all integration tests with Testcontainers
 * Uses real PostgreSQL, Redis, and Kafka containers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
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

    // PostgreSQL Container
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("test_order_catalog_db")
        .withUsername("test_user")
        .withPassword("test_password")
        .withReuse(true);

    // Redis Container
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .withReuse(true);

    // Kafka Container
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    ).withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        
        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        
        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        
        // Timeout configuration for tests (shorter timeouts)
        registry.add("order.timeout.restaurant-acceptance", () -> "5s");
        registry.add("order.timeout.payment-processing", () -> "10s");
        registry.add("order.timeout.rider-assignment", () -> "10s");
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test
        cleanDatabase();
        cleanRedis();
    }

    protected void cleanDatabase() {
        auditRepository.deleteAll();
        subOrderRepository.deleteAll();
        orderRepository.deleteAll();
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
