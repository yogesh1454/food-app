# BE-003-19: Order Validation & Pre-Acceptance Logic

**Story ID:** BE-003-19  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 15  
**Epic:** BE-003  
**Dependencies:** BE-003-18 (Order FSM Implementation)

---

## 📖 User Story

**As a** backend developer  
**I want** to implement comprehensive order validation logic  
**So that** only valid orders are sent to restaurants for acceptance

---

## ✅ Acceptance Criteria

1. **Restaurant Validation**
   - [ ] Restaurant exists and is active
   - [ ] Restaurant is currently open (operating hours)
   - [ ] Restaurant accepts orders (not paused)
   - [ ] Branch is operational

2. **Menu Item Validation**
   - [ ] All menu items exist
   - [ ] All menu items are available
   - [ ] Menu items belong to the restaurant
   - [ ] Prices match current menu prices
   - [ ] Customizations are valid

3. **Delivery Zone Validation**
   - [ ] Customer address is within delivery zone
   - [ ] Calculate distance to customer
   - [ ] Verify delivery is possible
   - [ ] Estimate delivery time

4. **Order Amount Validation**
   - [ ] Minimum order amount met
   - [ ] Maximum order amount not exceeded
   - [ ] Item quantities are valid (> 0)
   - [ ] Total amount calculation correct

5. **Payment Validation**
   - [ ] Payment method is valid
   - [ ] Payment amount matches order total
   - [ ] Payment authorization successful
   - [ ] Fraud detection checks

6. **Business Rules**
   - [ ] Maximum items per order limit
   - [ ] Restaurant capacity check
   - [ ] Peak hour restrictions
   - [ ] Customer order frequency limits

---

## 🔧 Technical Implementation

### **Validation Service**

```java
@Service
@Slf4j
public class OrderValidationService {
    
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final DeliveryZoneService deliveryZoneService;
    private final PricingService pricingService;
    
    public ValidationResult validateOrder(CreateOrderRequest request) {
        log.info("Validating order for customer: {}", request.getCustomerId());
        
        ValidationResult result = new ValidationResult();
        
        // Run all validations
        validateRestaurant(request, result);
        validateMenuItems(request, result);
        validateDeliveryZone(request, result);
        validateOrderAmount(request, result);
        validateBusinessRules(request, result);
        
        return result;
    }
    
    private void validateRestaurant(CreateOrderRequest request, ValidationResult result) {
        for (RestaurantOrder restaurantOrder : request.getRestaurantOrders()) {
            UUID restaurantId = restaurantOrder.getRestaurantId();
            UUID branchId = restaurantOrder.getBranchId();
            
            // Check restaurant exists and is active
            Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
            if (restaurant == null) {
                result.addError("restaurant", "Restaurant not found: " + restaurantId);
                continue;
            }
            
            if (!restaurant.isActive()) {
                result.addError("restaurant", 
                    "Restaurant is not active: " + restaurant.getName());
                continue;
            }
            
            // Check branch exists and is operational
            VendorBranch branch = restaurantService.getBranch(branchId);
            if (branch == null) {
                result.addError("branch", "Branch not found: " + branchId);
                continue;
            }
            
            if (!branch.isActive()) {
                result.addError("branch", 
                    "Branch is not active: " + branch.getName());
                continue;
            }
            
            // Check operating hours
            if (!isRestaurantOpen(branch)) {
                result.addError("operating_hours", 
                    "Restaurant is currently closed: " + restaurant.getName());
            }
            
            // Check if accepting orders
            if (branch.isPaused()) {
                result.addError("restaurant_paused", 
                    "Restaurant is temporarily not accepting orders: " + restaurant.getName());
            }
        }
    }
    
    private void validateMenuItems(CreateOrderRequest request, ValidationResult result) {
        for (RestaurantOrder restaurantOrder : request.getRestaurantOrders()) {
            UUID restaurantId = restaurantOrder.getRestaurantId();
            
            for (OrderItemRequest item : restaurantOrder.getItems()) {
                UUID menuItemId = item.getMenuItemId();
                
                // Check menu item exists
                MenuItem menuItem = menuItemService.getMenuItem(menuItemId);
                if (menuItem == null) {
                    result.addError("menu_item", 
                        "Menu item not found: " + menuItemId);
                    continue;
                }
                
                // Check menu item belongs to restaurant
                if (!menuItem.getRestaurantId().equals(restaurantId)) {
                    result.addError("menu_item", 
                        "Menu item does not belong to restaurant: " + menuItem.getName());
                    continue;
                }
                
                // Check menu item is available
                if (!menuItem.isAvailable()) {
                    result.addError("menu_item_availability", 
                        "Menu item is not available: " + menuItem.getName());
                    continue;
                }
                
                // Check price matches
                if (!item.getPrice().equals(menuItem.getPrice())) {
                    result.addError("price_mismatch", 
                        "Price mismatch for item: " + menuItem.getName() + 
                        ". Expected: " + menuItem.getPrice() + ", Got: " + item.getPrice());
                }
                
                // Validate customizations
                if (item.getCustomizations() != null) {
                    validateCustomizations(item.getCustomizations(), menuItem, result);
                }
            }
        }
    }
    
    private void validateDeliveryZone(CreateOrderRequest request, ValidationResult result) {
        DeliveryAddress address = request.getDeliveryAddress();
        
        for (RestaurantOrder restaurantOrder : request.getRestaurantOrders()) {
            UUID branchId = restaurantOrder.getBranchId();
            VendorBranch branch = restaurantService.getBranch(branchId);
            
            // Calculate distance
            double distance = deliveryZoneService.calculateDistance(
                branch.getLocation(),
                new Location(address.getLatitude(), address.getLongitude())
            );
            
            // Check if within delivery zone
            if (distance > branch.getDeliveryRadiusKm()) {
                result.addError("delivery_zone", 
                    "Delivery address is outside service area for: " + branch.getName() +
                    ". Distance: " + String.format("%.2f", distance) + " km, " +
                    "Max: " + branch.getDeliveryRadiusKm() + " km");
            }
        }
    }
    
    private void validateOrderAmount(CreateOrderRequest request, ValidationResult result) {
        for (RestaurantOrder restaurantOrder : request.getRestaurantOrders()) {
            UUID branchId = restaurantOrder.getBranchId();
            VendorBranch branch = restaurantService.getBranch(branchId);
            
            // Calculate item total
            BigDecimal itemTotal = calculateItemTotal(restaurantOrder.getItems());
            
            // Check minimum order amount
            if (branch.getMinimumOrderAmount() != null && 
                itemTotal.compareTo(branch.getMinimumOrderAmount()) < 0) {
                result.addError("minimum_order", 
                    "Order amount below minimum for " + branch.getName() + ". " +
                    "Minimum: ₹" + branch.getMinimumOrderAmount() + ", " +
                    "Current: ₹" + itemTotal);
            }
            
            // Check maximum order amount (if applicable)
            BigDecimal maxOrderAmount = new BigDecimal("10000"); // ₹10,000
            if (itemTotal.compareTo(maxOrderAmount) > 0) {
                result.addError("maximum_order", 
                    "Order amount exceeds maximum: ₹" + maxOrderAmount);
            }
            
            // Validate quantities
            for (OrderItemRequest item : restaurantOrder.getItems()) {
                if (item.getQuantity() <= 0) {
                    result.addError("quantity", 
                        "Invalid quantity for item: " + item.getMenuItemId());
                }
                
                if (item.getQuantity() > 50) {
                    result.addError("quantity", 
                        "Quantity exceeds maximum (50) for item: " + item.getMenuItemId());
                }
            }
        }
    }
    
    private void validateBusinessRules(CreateOrderRequest request, ValidationResult result) {
        // Maximum items per order
        int totalItems = request.getRestaurantOrders().stream()
            .mapToInt(ro -> ro.getItems().size())
            .sum();
        
        if (totalItems > 100) {
            result.addError("max_items", 
                "Order exceeds maximum items limit (100). Current: " + totalItems);
        }
        
        // Check restaurant capacity
        for (RestaurantOrder restaurantOrder : request.getRestaurantOrders()) {
            UUID branchId = restaurantOrder.getBranchId();
            int currentOrders = restaurantService.getCurrentOrderCount(branchId);
            int maxCapacity = 50; // Maximum concurrent orders
            
            if (currentOrders >= maxCapacity) {
                result.addError("restaurant_capacity", 
                    "Restaurant is at full capacity. Please try again later.");
            }
        }
        
        // Peak hour restrictions (optional)
        if (isPeakHour()) {
            // Could add additional validations or warnings
            result.addWarning("peak_hour", 
                "This is a peak hour. Delivery may take longer than usual.");
        }
    }
    
    private boolean isRestaurantOpen(VendorBranch branch) {
        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        
        OperatingHours hours = branch.getOperatingHours(today);
        if (hours == null) {
            return false;
        }
        
        return now.isAfter(hours.getOpenTime()) && 
               now.isBefore(hours.getCloseTime());
    }
    
    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(14, 0))) ||
               (now.isAfter(LocalTime.of(19, 0)) && now.isBefore(LocalTime.of(22, 0)));
    }
    
    private BigDecimal calculateItemTotal(List<OrderItemRequest> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void validateCustomizations(
        List<Customization> customizations, 
        MenuItem menuItem, 
        ValidationResult result
    ) {
        // Validate each customization is allowed for this menu item
        for (Customization customization : customizations) {
            if (!menuItem.getAllowedCustomizations().contains(customization.getName())) {
                result.addError("customization", 
                    "Invalid customization for " + menuItem.getName() + ": " + 
                    customization.getName());
            }
        }
    }
}
```

### **Validation Result**

```java
@Data
@Builder
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;
    private List<ValidationWarning> warnings;
    
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public void addError(String field, String message) {
        this.valid = false;
        this.errors.add(new ValidationError(field, message));
    }
    
    public void addWarning(String field, String message) {
        this.warnings.add(new ValidationWarning(field, message));
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

@Data
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}

@Data
@AllArgsConstructor
public class ValidationWarning {
    private String field;
    private String message;
}
```

### **Integration with Order FSM**

```java
@Service
public class OrderService {
    
    private final OrderValidationService validationService;
    private final OrderFSM orderFSM;
    
    public OrderResponse createOrder(CreateOrderRequest request, UUID customerId) {
        // Validate order
        ValidationResult validation = validationService.validateOrder(request);
        
        if (!validation.isValid()) {
            throw new OrderValidationException(
                "Order validation failed",
                validation.getErrors()
            );
        }
        
        // Create order
        Order order = buildOrder(request, customerId);
        order.setState(OrderState.CREATED);
        order = orderRepository.save(order);
        
        // Trigger FSM: CREATED → VALIDATED
        orderFSM.fire(order.getOrderId(), OrderTrigger.VALIDATE_ORDER);
        
        // Continue with payment and restaurant notification...
        
        return OrderResponse.from(order);
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test restaurant validation (active, open, not paused)
- [ ] Test menu item validation (exists, available, price match)
- [ ] Test delivery zone validation (within radius)
- [ ] Test order amount validation (min/max)
- [ ] Test quantity validation
- [ ] Test customization validation
- [ ] Test business rules (capacity, max items)

### **Integration Tests**
- [ ] Test complete validation flow
- [ ] Test validation with invalid restaurant
- [ ] Test validation with unavailable items
- [ ] Test validation with out-of-zone address
- [ ] Test validation with below minimum amount
- [ ] Test validation during closed hours

### **Edge Case Tests**
- [ ] Test validation at boundary times (opening/closing)
- [ ] Test validation with exactly minimum amount
- [ ] Test validation at delivery zone boundary
- [ ] Test validation with maximum items
- [ ] Test validation during peak hours

---

## 📚 References

- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [BE-003-18: Order FSM Implementation](./BE-003-18-order-fsm-implementation-v2.md)
- [REST API Standards](../../REST_API_STANDARDS.md)

---

## 🎯 Definition of Done

- [ ] OrderValidationService implemented
- [ ] All validation rules implemented
- [ ] ValidationResult class implemented
- [ ] Restaurant validation working
- [ ] Menu item validation working
- [ ] Delivery zone validation working
- [ ] Order amount validation working
- [ ] Business rules validation working
- [ ] Integration with Order FSM complete
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Error messages are clear and actionable
- [ ] Code reviewed and approved
- [ ] Documentation updated
