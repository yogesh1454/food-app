# BE-003-21: Order Management APIs

**Story ID:** BE-003-21  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 16  
**Epic:** BE-003  
**Dependencies:** BE-003-18 (Order FSM), BE-003-19 (Validation)

---

## 📖 User Story

**As a** backend developer  
**I want** to implement REST APIs for order management  
**So that** customers, restaurants, and riders can interact with orders

---

## ✅ Acceptance Criteria

1. **Customer APIs**
   - [ ] POST /api/v1/orders - Create order
   - [ ] GET /api/v1/orders/{orderId} - Get order details
   - [ ] GET /api/v1/orders - List customer orders
   - [ ] POST /api/v1/orders/{orderId}/cancel - Cancel order

2. **Restaurant APIs**
   - [ ] GET /api/v1/restaurant/orders - List pending orders
   - [ ] POST /api/v1/restaurant/orders/{orderId}/accept - Accept order
   - [ ] POST /api/v1/restaurant/orders/{orderId}/reject - Reject order
   - [ ] POST /api/v1/restaurant/orders/{orderId}/ready - Mark ready

3. **Rider APIs**
   - [ ] GET /api/v1/rider/orders/{orderId} - Get order for pickup
   - [ ] POST /api/v1/rider/orders/{orderId}/pickup - Confirm pickup
   - [ ] POST /api/v1/rider/orders/{orderId}/deliver - Confirm delivery

4. **API Standards**
   - [ ] Follow REST API Standards document
   - [ ] Proper HTTP status codes
   - [ ] Comprehensive error handling
   - [ ] Input validation
   - [ ] Swagger/OpenAPI documentation

5. **Security**
   - [ ] Authentication required for all endpoints
   - [ ] Authorization based on user role
   - [ ] Rate limiting implemented
   - [ ] Input sanitization

---

## 🔧 Technical Implementation

### **Customer Order Controller**

```java
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@Validated
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderQueryService orderQueryService;
    
    /**
     * Create a new order
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order", description = "Create a new order with items from one or more restaurants")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody @Valid CreateOrderRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Creating order for customer: {}", userDetails.getUsername());
        
        UUID customerId = extractCustomerId(userDetails);
        OrderResponse response = orderService.createOrder(request, customerId);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
    
    /**
     * Get order details
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Get order details by ID")
    public ResponseEntity<OrderResponse> getOrder(
        @PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Getting order: orderId={}", orderId);
        
        UUID customerId = extractCustomerId(userDetails);
        OrderResponse response = orderQueryService.getOrder(orderId, customerId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * List customer orders
     */
    @GetMapping
    @Operation(summary = "List orders", description = "List orders for the authenticated customer")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) OrderState state,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Listing orders for customer: {}", userDetails.getUsername());
        
        UUID customerId = extractCustomerId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<OrderSummaryResponse> response = orderQueryService.listOrders(
            customerId, state, pageable
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order if it's in a cancellable state")
    public ResponseEntity<OrderResponse> cancelOrder(
        @PathVariable UUID orderId,
        @RequestBody @Valid CancelOrderRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Cancelling order: orderId={}", orderId);
        
        UUID customerId = extractCustomerId(userDetails);
        request.setCancelledBy("CUSTOMER");
        
        orderService.cancelOrder(orderId, request, customerId);
        OrderResponse response = orderQueryService.getOrder(orderId, customerId);
        
        return ResponseEntity.ok(response);
    }
    
    private UUID extractCustomerId(UserDetails userDetails) {
        // Extract customer ID from authenticated user
        return UUID.fromString(userDetails.getUsername());
    }
}
```

### **Restaurant Order Controller**

```java
@RestController
@RequestMapping("/api/v1/restaurant/orders")
@Slf4j
@Validated
public class RestaurantOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderQueryService orderQueryService;
    
    /**
     * List pending orders for restaurant
     */
    @GetMapping
    @Operation(summary = "List pending orders", description = "List orders pending acceptance for the restaurant")
    public ResponseEntity<Page<OrderResponse>> listPendingOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Listing pending orders for restaurant: {}", userDetails.getUsername());
        
        UUID restaurantId = extractRestaurantId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        
        Page<OrderResponse> response = orderQueryService.listRestaurantOrders(
            restaurantId,
            OrderState.PENDING_ACCEPTANCE,
            pageable
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Accept order
     */
    @PostMapping("/{orderId}/accept")
    @Operation(summary = "Accept order", description = "Accept an order and start preparation")
    public ResponseEntity<OrderResponse> acceptOrder(
        @PathVariable UUID orderId,
        @RequestBody @Valid AcceptOrderRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Accepting order: orderId={}", orderId);
        
        UUID restaurantId = extractRestaurantId(userDetails);
        orderService.acceptOrder(orderId, request, restaurantId);
        
        OrderResponse response = orderQueryService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reject order
     */
    @PostMapping("/{orderId}/reject")
    @Operation(summary = "Reject order", description = "Reject an order with reason")
    public ResponseEntity<OrderResponse> rejectOrder(
        @PathVariable UUID orderId,
        @RequestBody @Valid RejectOrderRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Rejecting order: orderId={}", orderId);
        
        UUID restaurantId = extractRestaurantId(userDetails);
        orderService.rejectOrder(orderId, request.getReason(), restaurantId);
        
        OrderResponse response = orderQueryService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mark order ready
     */
    @PostMapping("/{orderId}/ready")
    @Operation(summary = "Mark order ready", description = "Mark order as ready for pickup")
    public ResponseEntity<OrderResponse> markOrderReady(
        @PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Marking order ready: orderId={}", orderId);
        
        UUID restaurantId = extractRestaurantId(userDetails);
        orderService.markOrderReady(orderId, restaurantId);
        
        OrderResponse response = orderQueryService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    private UUID extractRestaurantId(UserDetails userDetails) {
        // Extract restaurant ID from authenticated user
        return UUID.fromString(userDetails.getUsername());
    }
}
```

### **Rider Order Controller**

```java
@RestController
@RequestMapping("/api/v1/rider/orders")
@Slf4j
@Validated
public class RiderOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderQueryService orderQueryService;
    
    /**
     * Get order for pickup
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order for pickup", description = "Get order details for pickup")
    public ResponseEntity<OrderResponse> getOrderForPickup(
        @PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Getting order for pickup: orderId={}", orderId);
        
        UUID riderId = extractRiderId(userDetails);
        OrderResponse response = orderQueryService.getOrderForRider(orderId, riderId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Confirm pickup
     */
    @PostMapping("/{orderId}/pickup")
    @Operation(summary = "Confirm pickup", description = "Confirm order pickup from restaurant")
    public ResponseEntity<OrderResponse> confirmPickup(
        @PathVariable UUID orderId,
        @RequestBody @Valid ConfirmPickupRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Confirming pickup: orderId={}", orderId);
        
        UUID riderId = extractRiderId(userDetails);
        orderService.confirmPickup(orderId, riderId, request);
        
        OrderResponse response = orderQueryService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Confirm delivery
     */
    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Confirm delivery", description = "Confirm order delivery to customer")
    public ResponseEntity<OrderResponse> confirmDelivery(
        @PathVariable UUID orderId,
        @RequestBody @Valid ConfirmDeliveryRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Confirming delivery: orderId={}", orderId);
        
        UUID riderId = extractRiderId(userDetails);
        orderService.confirmDelivery(orderId, riderId, request);
        
        OrderResponse response = orderQueryService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    private UUID extractRiderId(UserDetails userDetails) {
        // Extract rider ID from authenticated user
        return UUID.fromString(userDetails.getUsername());
    }
}
```

### **Request DTOs**

```java
@Data
@Builder
public class CreateOrderRequest {
    
    @NotNull(message = "Restaurant orders are required")
    @Size(min = 1, max = 5, message = "Must have 1-5 restaurant orders")
    private List<RestaurantOrder> restaurantOrders;
    
    @NotNull(message = "Delivery address is required")
    @Valid
    private DeliveryAddress deliveryAddress;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Valid
    private PaymentDetails paymentDetails;
    
    @Size(max = 500, message = "Special instructions must be less than 500 characters")
    private String specialInstructions;
}

@Data
public class RestaurantOrder {
    
    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;
    
    @NotNull(message = "Branch ID is required")
    private UUID branchId;
    
    @NotNull(message = "Items are required")
    @Size(min = 1, message = "Must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
}

@Data
public class AcceptOrderRequest {
    
    @NotNull(message = "Estimated prep time is required")
    @Min(value = 5, message = "Minimum prep time is 5 minutes")
    @Max(value = 120, message = "Maximum prep time is 120 minutes")
    private Integer estimatedPrepTime;
}

@Data
public class CancelOrderRequest {
    
    @NotNull(message = "Reason is required")
    @NotBlank(message = "Reason cannot be blank")
    private String reason;
    
    private String cancelledBy;
}
```

### **Response DTOs**

```java
@Data
@Builder
public class OrderResponse {
    private UUID orderId;
    private OrderType orderType;
    private OrderState state;
    private String customerStatus;
    
    private List<SubOrderResponse> subOrders;
    
    private PricingDetails pricing;
    private DeliveryAddress deliveryAddress;
    private String specialInstructions;
    
    private List<StatusTimeline> timeline;
    private DeliveryInfo delivery;
    
    private Instant createdAt;
    private Instant estimatedDeliveryTime;
    
    private boolean canCancel;
    private boolean canTrack;
    
    public static OrderResponse from(Order order) {
        // Map Order entity to OrderResponse
        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .orderType(order.getOrderType())
            .state(order.getState())
            // ... map other fields
            .build();
    }
}
```

### **Global Exception Handler**

```java
@RestControllerAdvice
@Slf4j
public class OrderExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        log.error("Order not found", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .error("ORDER_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(OrderValidationException ex) {
        log.error("Order validation failed", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .error("VALIDATION_FAILED")
            .message(ex.getMessage())
            .details(ex.getValidationErrors())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(OrderCancellationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleCancellationNotAllowed(
        OrderCancellationNotAllowedException ex
    ) {
        log.error("Order cancellation not allowed", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .error("CANCELLATION_NOT_ALLOWED")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex
    ) {
        log.error("Method argument validation failed", ex);
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .error("INVALID_REQUEST")
            .message("Request validation failed")
            .details(errors)
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test all controller endpoints
- [ ] Test request validation
- [ ] Test error handling
- [ ] Test authentication/authorization
- [ ] Test DTO mapping

### **Integration Tests**
- [ ] Test complete order creation flow
- [ ] Test order acceptance flow
- [ ] Test order rejection flow
- [ ] Test order cancellation flow
- [ ] Test order pickup and delivery flow
- [ ] Test pagination
- [ ] Test filtering

### **API Tests**
- [ ] Test all endpoints with Postman/REST Assured
- [ ] Test error scenarios
- [ ] Test rate limiting
- [ ] Test concurrent requests

---

## 📚 References

- [REST API Standards](../../REST_API_STANDARDS.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [BE-003-18: Order FSM Implementation](./BE-003-18-order-fsm-implementation-v2.md)

---

## 🎯 Definition of Done

- [ ] All customer APIs implemented
- [ ] All restaurant APIs implemented
- [ ] All rider APIs implemented
- [ ] Request/Response DTOs implemented
- [ ] Input validation implemented
- [ ] Error handling implemented
- [ ] Swagger/OpenAPI documentation complete
- [ ] Authentication/authorization working
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] API tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
