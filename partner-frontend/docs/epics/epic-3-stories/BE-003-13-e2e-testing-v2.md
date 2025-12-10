# BE-003-13: End-to-End Testing & Performance Optimization

**Story ID:** BE-003-13  
**Story Points:** 13  
**Priority:** High  
**Sprint:** 9  
**Epic:** BE-003  
**Dependencies:** All previous stories

---

## 📖 User Story

**As a** development team  
**I want** comprehensive E2E tests and performance optimization  
**So that** the service is production-ready with guaranteed quality and performance

---

## ✅ Acceptance Criteria

1. **End-to-End Test Scenarios**
   - [ ] Complete order flow: vendor registration → branch onboarding → menu creation → order placement → status updates → delivery
   - [ ] Multi-branch order scenarios
   - [ ] Concurrent order processing
   - [ ] Error handling and recovery
   - [ ] Cache invalidation scenarios

2. **Performance Testing**
   - [ ] Load test: 1000 concurrent orders
   - [ ] Menu retrieval < 50ms (cached)
   - [ ] Order creation < 500ms
   - [ ] Database query optimization
   - [ ] N+1 query prevention

3. **Integration Testing**
   - [ ] Kafka event publishing verified
   - [ ] Redis caching verified
   - [ ] Database transactions verified
   - [ ] API endpoint integration tests

4. **Performance Optimization**
   - [ ] Database indexes optimized
   - [ ] Query optimization (use @Query with joins)
   - [ ] Lazy loading configured correctly
   - [ ] Connection pooling tuned
   - [ ] Cache hit rate > 80%

---

## 🔧 Key Implementation

### **E2E Test**
```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderCatalogE2ETest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);
    
    @Test
    void completeOrderFlow_Success() throws Exception {
        // 1. Register vendor
        VendorRegistrationRequest vendorRequest = createVendorRequest();
        String vendorResponse = mockMvc.perform(post("/api/v1/vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vendorRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        UUID vendorId = extractVendorId(vendorResponse);
        
        // 2. Create branch
        BranchCreateRequest branchRequest = createBranchRequest();
        String branchResponse = mockMvc.perform(post("/api/v1/vendors/" + vendorId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branchRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        UUID branchId = extractBranchId(branchResponse);
        
        // 3. Add menu items
        MenuItemCreateRequest menuRequest = createMenuItemRequest();
        mockMvc.perform(post("/api/v1/branches/" + branchId + "/menu-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(menuRequest)))
            .andExpect(status().isCreated());
        
        // 4. Set branch online
        mockMvc.perform(put("/api/v1/branches/" + branchId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isOpen\": true}"))
            .andExpect(status().isOk());
        
        // 5. Create order
        OrderCreateRequest orderRequest = createOrderRequest(branchId);
        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        UUID orderId = extractOrderId(orderResponse);
        
        // 6. Update order status
        mockMvc.perform(put("/api/v1/orders/" + orderId + "/status")
                .param("status", "ACCEPTED"))
            .andExpect(status().isOk());
        
        // 7. Verify order
        mockMvc.perform(get("/api/v1/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("ACCEPTED"));
    }
}
```

### **Performance Test**
```java
@SpringBootTest
class PerformanceTest {
    
    @Test
    void loadTest_1000ConcurrentOrders() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    createOrder();
                } catch (Exception e) {
                    fail("Order creation failed: " + e.getMessage());
                }
            }, executor);
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        
        long orderCount = orderRepository.count();
        assertEquals(1000, orderCount);
    }
    
    @Test
    void menuRetrieval_Performance() {
        UUID branchId = createBranchWithMenu(100);
        
        // First call (uncached)
        long start1 = System.currentTimeMillis();
        menuService.getBranchMenu(branchId, null, null, Pageable.unpaged());
        long duration1 = System.currentTimeMillis() - start1;
        
        assertTrue(duration1 < 200, "Uncached menu retrieval should be < 200ms");
        
        // Second call (cached)
        long start2 = System.currentTimeMillis();
        menuService.getBranchMenu(branchId, null, null, Pageable.unpaged());
        long duration2 = System.currentTimeMillis() - start2;
        
        assertTrue(duration2 < 50, "Cached menu retrieval should be < 50ms");
    }
}
```

### **Query Optimization**
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    // Optimized query with JOIN FETCH to prevent N+1
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.branch b " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.menuItem " +
           "WHERE o.customerId = :customerId " +
           "ORDER BY o.orderedAt DESC")
    List<Order> findCustomerOrdersOptimized(@Param("customerId") UUID customerId);
}
```

---

## 📋 Definition of Done

- [ ] E2E test suite implemented
- [ ] Performance tests passing
- [ ] Load test: 1000 concurrent orders successful
- [ ] All performance SLAs met
- [ ] Database queries optimized
- [ ] Cache hit rate > 80%
- [ ] N+1 queries eliminated
- [ ] Connection pooling tuned
- [ ] Code coverage > 80%
- [ ] All tests passing
- [ ] Code reviewed
- [ ] Documentation updated

---

## 📚 References

- [Test Strategy](/docs/architecture/14-test-strategy-and-standards.md)
