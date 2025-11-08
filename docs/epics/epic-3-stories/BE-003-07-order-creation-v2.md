# BE-003-07: Order Creation & Validation (Branch-Specific)

**Story ID:** BE-003-07  
**Story Points:** 13  
**Priority:** Critical (P0)  
**Sprint:** 7  
**Epic:** BE-003  
**Dependencies:** BE-003-05 (Menu Items)

---

## 📖 User Story

**As a** customer  
**I want** to place orders with validation and confirmation  
**So that** I can purchase items from branches with confidence

---

## ✅ Acceptance Criteria

1. **Order Creation**
   - [ ] Customer can create order with multiple items
   - [ ] Order validates branch is active and open
   - [ ] Order validates all items are available
   - [ ] Order calculates total amount correctly
   - [ ] Order captures delivery address
   - [ ] Order creates with PENDING status

2. **Validation Rules**
   - [ ] Branch must be ACTIVE and is_open = true
   - [ ] All menu items must exist and be available
   - [ ] Quantities must be positive integers
   - [ ] Delivery address must be valid
   - [ ] Minimum order amount validation (if applicable)
   - [ ] Maximum items per order limit

3. **Price Calculation**
   - [ ] Use current menu item prices
   - [ ] Store price_at_order for each item (price snapshot)
   - [ ] Calculate subtotal, taxes, delivery fee
   - [ ] Apply discounts/coupons if applicable
   - [ ] Total amount = sum of (quantity × price_at_order)

4. **Order Items**
   - [ ] Each item has quantity and price snapshot
   - [ ] Special instructions per item supported
   - [ ] Unique constraint: one entry per item per order
   - [ ] Linked to menu_item for reference

5. **Event Publishing**
   - [ ] order.created event published
   - [ ] Includes customer, branch, items, total
   - [ ] Consumed by Payment, Delivery, Notification services

6. **API Endpoints**
   - [ ] POST /api/v1/orders - Create order
   - [ ] GET /api/v1/orders/{orderId} - Get order details
   - [ ] GET /api/v1/orders/customer/{customerId} - Customer orders
   - [ ] GET /api/v1/branches/{branchId}/orders - Branch orders

---

## 🔧 Technical Implementation

### **Order Service**
```java
@Service
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderEventPublisher eventPublisher;
    
    public OrderResponse createOrder(OrderCreateRequest request, UUID customerId) {
        log.info("Creating order for customer: {} at branch: {}", customerId, request.getBranchId());
        
        // Validate branch
        VendorBranch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getIsActive()) {
            throw new BranchNotActiveException("Branch is not active");
        }
        
        if (!branch.getIsOpen()) {
            throw new BranchClosedException("Branch is currently closed");
        }
        
        // Create order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setBranch(branch);
        order.setDeliveryDetails(request.getDeliveryDetails());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("PENDING");
        
        // Process order items
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
            
            if (!menuItem.getIsAvailable()) {
                throw new MenuItemNotAvailableException(
                    "Item not available: " + menuItem.getName());
            }
            
            if (!menuItem.getBranch().getBranchId().equals(branch.getBranchId())) {
                throw new ValidationException("Item does not belong to this branch");
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrder(menuItem.getPrice()); // Price snapshot
            orderItem.setNotes(itemRequest.getNotes());
            
            order.addOrderItem(orderItem);
        }
        
        // Calculate total
        order.calculateTotalAmount();
        
        // Estimate delivery time
        int totalPrepTime = order.getOrderItems().stream()
            .mapToInt(item -> item.getMenuItem().getPreparationTimeMinutes() != null 
                ? item.getMenuItem().getPreparationTimeMinutes() : 15)
            .max()
            .orElse(30);
        
        order.setEstimatedDeliveryTime(
            LocalDateTime.now().plusMinutes(totalPrepTime + 30)); // prep + delivery
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Publish event
        eventPublisher.publishOrderCreated(savedOrder);
        
        log.info("Order created: {} with total: {}", savedOrder.getOrderId(), savedOrder.getTotalAmount());
        return OrderMapper.toResponse(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID requestingUserId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        
        // Authorization: customer or vendor can view
        if (!order.getCustomerId().equals(requestingUserId) &&
            !order.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to view this order");
        }
        
        return OrderMapper.toResponse(order);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByCustomerIdOrderByOrderedAtDesc(
            customerId, pageable);
        return orders.map(OrderMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getBranchOrders(UUID branchId, 
                                               String status,
                                               Pageable pageable) {
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByBranchAndOrderStatusOrderByOrderedAtDesc(
                branch, status, pageable);
        } else {
            orders = orderRepository.findByBranchOrderByOrderedAtDesc(branch, pageable);
        }
        
        return orders.map(OrderMapper::toResponse);
    }
}
```

### **Controller**
```java
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        OrderResponse response = orderService.createOrder(request, userPrincipal.getUserId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Order created successfully"));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        OrderResponse response = orderService.getOrder(orderId, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getCustomerOrders(
            @PathVariable UUID customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<OrderResponse> response = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getBranchOrders(
            @PathVariable UUID branchId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<OrderResponse> response = orderService.getBranchOrders(branchId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## 🧪 Testing Requirements

- [ ] Order creation with valid data
- [ ] Branch closed validation
- [ ] Item availability validation
- [ ] Price calculation accuracy
- [ ] Total amount calculation
- [ ] Estimated delivery time
- [ ] Kafka events published
- [ ] Authorization checks

---

## 📋 Definition of Done

- [ ] OrderService implemented
- [ ] All endpoints working
- [ ] Validation complete
- [ ] Price snapshot working
- [ ] Total calculation correct
- [ ] Kafka events published
- [ ] Unit tests passing (>80%)
- [ ] Integration tests passing
- [ ] Code reviewed

---

## 📚 References

- [Database Schema](/docs/architecture/9-database-schema.md)
- [Core Workflows](/docs/architecture/7-core-workflows.md)
