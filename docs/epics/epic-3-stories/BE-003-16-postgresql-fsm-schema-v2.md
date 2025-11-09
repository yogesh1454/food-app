# BE-003-16: PostgreSQL Schema for FSM Persistence

**Story ID:** BE-003-16  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 14  
**Epic:** BE-003  
**Dependencies:** BE-003-01 (Database Schema Infrastructure)

---

## 📖 User Story

**As a** backend developer  
**I want** to create PostgreSQL schema for Order and Delivery FSM  
**So that** the system can store order and delivery state with full audit trail

---

## ⚠️ Important: Existing Schema Cleanup

**Existing Tables:**
- `orders` table exists (V5__Create_orders_table.sql) - **WILL BE DROPPED**
- `order_items` table exists (V6__Create_order_items_table.sql) - **WILL BE DROPPED**

**Migration Strategy:**
Since the existing tables are **NOT in use**, this story will:
1. **DROP** existing `orders` and `order_items` tables
2. **CREATE** new `orders` table with FSM support
3. **CREATE** new `order_items` table with enhanced structure
4. **CREATE** new `sub_orders` table for multi-restaurant support
5. **CREATE** new `deliveries` table for delivery FSM
6. **CREATE** new audit tables for state tracking

**Approach:** Clean slate - Drop and recreate with new FSM-ready schema.

---

## ✅ Acceptance Criteria

1. **Orders Table**
   - [ ] Parent order table with aggregated state
   - [ ] Support for single and multi-restaurant orders
   - [ ] Payment and pricing fields
   - [ ] Delivery address and customer info
   - [ ] Timestamps for all state transitions

2. **Sub-Orders Table**
   - [ ] Sub-order table for multi-restaurant support
   - [ ] Independent FSM state per sub-order
   - [ ] Restaurant and item details
   - [ ] Estimated and actual ready times

3. **Deliveries Table**
   - [ ] Delivery table with FSM state
   - [ ] Rider assignment details
   - [ ] Route and location tracking
   - [ ] Pickup and delivery timestamps

4. **Order State Audit Table**
   - [ ] Complete audit trail of state transitions
   - [ ] Trigger, timestamp, and metadata
   - [ ] User/system actor tracking

5. **Indexes and Constraints**
   - [ ] Primary keys and foreign keys
   - [ ] Indexes on frequently queried fields
   - [ ] Check constraints for valid states
   - [ ] Unique constraints where applicable

6. **Flyway Migration**
   - [ ] Versioned migration scripts
   - [ ] Rollback scripts
   - [ ] Test data scripts for development

---

## 🔧 Technical Implementation

### **Flyway Migration Script**

```sql
-- V7__drop_and_recreate_orders_for_fsm.sql
-- Drop existing orders tables and recreate with FSM support

-- =====================================================
-- DROP EXISTING TABLES
-- =====================================================

DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- =====================================================
-- ORDERS TABLE (Parent Order with FSM Support)
-- =====================================================
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Order Type
    order_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE',
    parent_order_id UUID REFERENCES orders(order_id),
    
    -- Customer Info
    customer_id UUID NOT NULL,
    
    -- FSM State
    state VARCHAR(32) NOT NULL,
    
    -- Pricing
    item_total DECIMAL(10,2) NOT NULL,
    delivery_charges DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL,
    gst DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    
    -- Payment
    payment_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(32),
    payment_transaction_id VARCHAR(100),
    
    -- Delivery Address
    delivery_address JSONB NOT NULL,
    delivery_latitude DECIMAL(10,8),
    delivery_longitude DECIMAL(11,8),
    
    -- Special Instructions
    special_instructions TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    validated_at TIMESTAMP WITH TIME ZONE,
    payment_confirmed_at TIMESTAMP WITH TIME ZONE,
    accepted_at TIMESTAMP WITH TIME ZONE,
    preparing_started_at TIMESTAMP WITH TIME ZONE,
    ready_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    
    -- Cancellation
    cancellation_reason VARCHAR(100),
    cancelled_by VARCHAR(20),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT chk_order_state CHECK (state IN (
        'CREATED', 'VALIDATED', 'PAYMENT_CONFIRMED', 'PENDING_ACCEPTANCE',
        'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'ASSIGNED_TO_RIDER',
        'PICKED_UP', 'DELIVERED', 'CLOSED', 'CANCELLED', 'REJECTED'
    )),
    CONSTRAINT chk_payment_status CHECK (payment_status IN (
        'PENDING', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'
    )),
    CONSTRAINT chk_order_type CHECK (order_type IN ('SINGLE', 'MULTI_RESTAURANT'))
);

-- =====================================================
-- ORDER ITEMS TABLE (Enhanced Structure)
-- =====================================================
CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL,
    
    -- Item snapshot
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10,2) NOT NULL,
    
    -- Customizations
    notes TEXT,
    customizations JSONB DEFAULT '[]'::jsonb,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- =====================================================
-- SUB-ORDERS TABLE (Multi-Restaurant Support)
-- =====================================================
CREATE TABLE sub_orders (
    sub_order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    
    -- Restaurant Info
    restaurant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    
    -- FSM State (Independent)
    state VARCHAR(32) NOT NULL,
    
    -- Items
    items JSONB NOT NULL,
    item_total DECIMAL(10,2) NOT NULL,
    
    -- Preparation
    estimated_prep_time_minutes INT,
    actual_prep_time_minutes INT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    accepted_at TIMESTAMP WITH TIME ZONE,
    preparing_started_at TIMESTAMP WITH TIME ZONE,
    ready_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    
    -- Cancellation
    cancellation_reason VARCHAR(100),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT chk_sub_order_state CHECK (state IN (
        'CREATED', 'VALIDATED', 'PAYMENT_CONFIRMED', 'PENDING_ACCEPTANCE',
        'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'ASSIGNED_TO_RIDER',
        'PICKED_UP', 'DELIVERED', 'CANCELLED', 'REJECTED'
    ))
);

-- =====================================================
-- DELIVERIES TABLE
-- =====================================================
CREATE TABLE deliveries (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id),
    
    -- Sub-orders in this delivery (for multi-restaurant)
    sub_order_ids UUID[] NOT NULL,
    
    -- Rider Info
    rider_id UUID,
    rider_name VARCHAR(100),
    rider_phone VARCHAR(20),
    
    -- FSM State
    state VARCHAR(32) NOT NULL,
    
    -- Route
    pickup_locations JSONB NOT NULL,
    delivery_location JSONB NOT NULL,
    optimized_route JSONB,
    
    -- Distance and Fee
    distance_km DECIMAL(6,2),
    delivery_fee DECIMAL(10,2) NOT NULL,
    
    -- Assignment
    assignment_attempts INT DEFAULT 0,
    search_radius_km DECIMAL(6,2) DEFAULT 2.0,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    assigned_at TIMESTAMP WITH TIME ZONE,
    rider_accepted_at TIMESTAMP WITH TIME ZONE,
    at_restaurant_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    out_for_delivery_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    
    -- Failure
    failure_reason VARCHAR(100),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT chk_delivery_state CHECK (state IN (
        'PENDING', 'SEARCHING_RIDER', 'RIDER_ASSIGNED', 'RIDER_ACCEPTED',
        'AT_RESTAURANT', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED'
    ))
);

-- =====================================================
-- ORDER STATE AUDIT TABLE
-- =====================================================
CREATE TABLE order_state_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(order_id),
    
    -- State Transition
    previous_state VARCHAR(32),
    new_state VARCHAR(32) NOT NULL,
    trigger_name VARCHAR(50) NOT NULL,
    
    -- Actor
    actor_type VARCHAR(20) NOT NULL,
    actor_id UUID,
    
    -- Timestamp
    transitioned_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT chk_actor_type CHECK (actor_type IN ('SYSTEM', 'CUSTOMER', 'RESTAURANT', 'RIDER', 'ADMIN'))
);

-- =====================================================
-- DELIVERY STATE AUDIT TABLE
-- =====================================================
CREATE TABLE delivery_state_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    delivery_id UUID NOT NULL REFERENCES deliveries(delivery_id),
    
    -- State Transition
    previous_state VARCHAR(32),
    new_state VARCHAR(32) NOT NULL,
    trigger_name VARCHAR(50) NOT NULL,
    
    -- Actor
    actor_type VARCHAR(20) NOT NULL,
    actor_id UUID,
    
    -- Timestamp
    transitioned_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT chk_delivery_actor_type CHECK (actor_type IN ('SYSTEM', 'RIDER', 'ADMIN'))
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Orders indexes
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_state ON orders(state);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_state_created ON orders(state, created_at DESC);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);

-- Sub-orders indexes
CREATE INDEX idx_sub_orders_parent_id ON sub_orders(parent_order_id);
CREATE INDEX idx_sub_orders_restaurant_id ON sub_orders(restaurant_id);
CREATE INDEX idx_sub_orders_branch_id ON sub_orders(branch_id);
CREATE INDEX idx_sub_orders_state ON sub_orders(state);
CREATE INDEX idx_sub_orders_restaurant_state ON sub_orders(restaurant_id, state);

-- Deliveries indexes
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_rider_id ON deliveries(rider_id);
CREATE INDEX idx_deliveries_state ON deliveries(state);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at DESC);
CREATE INDEX idx_deliveries_rider_state ON deliveries(rider_id, state) WHERE rider_id IS NOT NULL;

-- Audit indexes
CREATE INDEX idx_order_audit_order_id ON order_state_audit(order_id);
CREATE INDEX idx_order_audit_transitioned_at ON order_state_audit(transitioned_at DESC);
CREATE INDEX idx_delivery_audit_delivery_id ON delivery_state_audit(delivery_id);
CREATE INDEX idx_delivery_audit_transitioned_at ON delivery_state_audit(transitioned_at DESC);

-- =====================================================
-- TRIGGERS FOR UPDATED_AT
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sub_orders_updated_at
    BEFORE UPDATE ON sub_orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_deliveries_updated_at
    BEFORE UPDATE ON deliveries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE orders IS 'Parent order table supporting single and multi-restaurant orders';
COMMENT ON TABLE sub_orders IS 'Sub-orders for multi-restaurant orders with independent FSM state';
COMMENT ON TABLE deliveries IS 'Delivery information with FSM state and rider assignment';
COMMENT ON TABLE order_state_audit IS 'Complete audit trail of order state transitions';
COMMENT ON TABLE delivery_state_audit IS 'Complete audit trail of delivery state transitions';
```

### **JPA Entities**

```java
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private UUID orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;
    
    @ManyToOne
    @JoinColumn(name = "parent_order_id")
    private Order parentOrder;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;
    
    @Column(name = "item_total", nullable = false)
    private BigDecimal itemTotal;
    
    @Column(name = "delivery_charges", nullable = false)
    private BigDecimal deliveryCharges;
    
    @Column(name = "platform_fee", nullable = false)
    private BigDecimal platformFee;
    
    @Column(name = "gst", nullable = false)
    private BigDecimal gst;
    
    @Column(name = "discount")
    private BigDecimal discount;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;
    
    @Type(JsonBinaryType.class)
    @Column(name = "delivery_address", columnDefinition = "jsonb", nullable = false)
    private DeliveryAddress deliveryAddress;
    
    @Column(name = "delivery_latitude")
    private BigDecimal deliveryLatitude;
    
    @Column(name = "delivery_longitude")
    private BigDecimal deliveryLongitude;
    
    @Column(name = "special_instructions")
    private String specialInstructions;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "validated_at")
    private Instant validatedAt;
    
    @Column(name = "payment_confirmed_at")
    private Instant paymentConfirmedAt;
    
    @Column(name = "accepted_at")
    private Instant acceptedAt;
    
    @Column(name = "preparing_started_at")
    private Instant preparingStartedAt;
    
    @Column(name = "ready_at")
    private Instant readyAt;
    
    @Column(name = "picked_up_at")
    private Instant pickedUpAt;
    
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Column(name = "cancelled_by")
    private String cancelledBy;
    
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @OneToMany(mappedBy = "parentOrder", cascade = CascadeType.ALL)
    private List<SubOrder> subOrders;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### **Repository Interfaces**

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByCustomerId(UUID customerId);
    
    List<Order> findByState(OrderState state);
    
    List<Order> findByCustomerIdAndState(UUID customerId, OrderState state);
    
    @Query("SELECT o FROM Order o WHERE o.state = :state AND o.createdAt < :before")
    List<Order> findStaleOrders(
        @Param("state") OrderState state, 
        @Param("before") Instant before
    );
    
    @Query("SELECT o FROM Order o WHERE o.state IN :states ORDER BY o.createdAt DESC")
    Page<Order> findByStates(
        @Param("states") List<OrderState> states, 
        Pageable pageable
    );
}

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder, UUID> {
    
    List<SubOrder> findByParentOrderId(UUID parentOrderId);
    
    List<SubOrder> findByRestaurantIdAndState(UUID restaurantId, OrderState state);
    
    @Query("SELECT so FROM SubOrder so WHERE so.restaurantId = :restaurantId " +
           "AND so.state IN ('PENDING_ACCEPTANCE', 'ACCEPTED', 'PREPARING')")
    List<SubOrder> findActiveSubOrdersByRestaurant(@Param("restaurantId") UUID restaurantId);
}

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    
    Optional<Delivery> findByOrderId(UUID orderId);
    
    List<Delivery> findByRiderIdAndState(UUID riderId, DeliveryState state);
    
    @Query("SELECT d FROM Delivery d WHERE d.state = :state AND d.createdAt < :before")
    List<Delivery> findStaleDeliveries(
        @Param("state") DeliveryState state,
        @Param("before") Instant before
    );
}
```

---

## 📝 Migration Summary

### **What Gets Created:**

| Component | Action | Description |
|-----------|--------|-------------|
| `orders` table | DROP & CREATE | New FSM-ready orders table with UUID primary key |
| `order_items` table | DROP & CREATE | Enhanced order items with UUID foreign key |
| `sub_orders` table | CREATE NEW | Multi-restaurant support |
| `deliveries` table | CREATE NEW | Delivery FSM |
| Audit tables | CREATE NEW | State transition tracking |

### **Migration Script:**

**V7__drop_and_recreate_orders_for_fsm.sql** - Single comprehensive migration:
1. **DROP** existing `order_items` table (CASCADE)
2. **DROP** existing `orders` table (CASCADE)
3. **CREATE** new `orders` table with:
   - UUID primary key (not BIGSERIAL)
   - FSM `state` column (not `order_status`)
   - Multi-restaurant support (`order_type`, `parent_order_id`)
   - Complete pricing breakdown
   - FSM-specific timestamps
4. **CREATE** new `order_items` table with UUID keys
5. **CREATE** new `sub_orders` table
6. **CREATE** new `deliveries` table
7. **CREATE** audit tables

### **Key Changes from Old Schema:**

| Old Schema | New Schema | Reason |
|------------|------------|--------|
| `order_id` BIGSERIAL | `order_id` UUID | Better for distributed systems |
| `order_status` VARCHAR | `state` VARCHAR | FSM state naming |
| Single `total_amount` | Breakdown: `item_total`, `delivery_charges`, `platform_fee`, `gst` | Transparent pricing |
| No parent support | `parent_order_id`, `order_type` | Multi-restaurant orders |
| Limited timestamps | FSM-specific timestamps | Track each state transition |
| No audit trail | Separate audit tables | Complete history |  

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test entity creation and persistence
- [ ] Test repository queries
- [ ] Test cascade operations (DELETE CASCADE)
- [ ] Test constraint validations (state, payment_status, order_type)
- [ ] Test UUID generation

### **Integration Tests**
- [ ] Test Flyway migration execution (V7)
- [ ] Test complete order lifecycle persistence
- [ ] Test multi-restaurant order creation
- [ ] Test sub-order creation and linking
- [ ] Test delivery creation and linking
- [ ] Test audit trail recording
- [ ] Test JSONB columns (delivery_address, metadata, customizations)

### **Migration Tests**
- [ ] Test migration on clean database
- [ ] Test migration drops old tables successfully
- [ ] Test all new tables created with correct schema
- [ ] Test all indexes created
- [ ] Test all constraints working
- [ ] Test foreign key integrity

---

## 📚 References

- [Architecture Decisions](../../business-flows/01_ARCHITECTURE_DECISIONS.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)
- [Multi-Restaurant Design](../../business-flows/05_MULTI_RESTAURANT_DESIGN.md)
- [REST API Standards](../../REST_API_STANDARDS.md)
- **Existing Migrations:**
  - V5__Create_orders_table.sql
  - V6__Create_order_items_table.sql

---

## 🎯 Definition of Done

- [ ] Flyway migration script created (V7__drop_and_recreate_orders_for_fsm.sql)
- [ ] Migration tested on clean database
- [ ] Old tables (`orders`, `order_items`) successfully dropped
- [ ] All new tables created with proper constraints:
  - [ ] `orders` with FSM support
  - [ ] `order_items` with UUID keys
  - [ ] `sub_orders` for multi-restaurant
  - [ ] `deliveries` for delivery FSM
  - [ ] `order_state_audit` for order tracking
  - [ ] `delivery_state_audit` for delivery tracking
- [ ] All indexes created for performance
- [ ] JPA entities implemented for all tables
- [ ] Repository interfaces implemented
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Migration rollback tested
- [ ] Code reviewed and approved
- [ ] Documentation updated
