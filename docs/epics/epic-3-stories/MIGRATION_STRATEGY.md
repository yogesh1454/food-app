# Database Migration Strategy for Order & Delivery FSM

**Date:** November 9, 2025  
**Story:** BE-003-16  
**Status:** ✅ Approved - Drop and Recreate Approach

---

## 🎯 Objective

Drop existing order tables and recreate with new Order & Delivery FSM design.

**Note:** Existing tables are **NOT in use**, so we can safely drop and recreate.

---

## 📊 Current State

### **Existing Tables:**

1. **orders** (V5__Create_orders_table.sql)
   - `order_id`: BIGSERIAL (needs to become UUID)
   - `order_status`: VARCHAR(50) (needs to become FSM `state`)
   - `branch_id`: BIGINT (single restaurant only)
   - Missing: FSM-specific columns, multi-restaurant support

2. **order_items** (V6__Create_order_items_table.sql)
   - Already compatible
   - No changes needed
   - Will continue to work with migrated orders table

### **Existing Entity:**

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;  // ← Needs to become UUID
    
    private String orderStatus;  // ← Needs to become FSM state enum
    private UUID customerId;
    private BigDecimal totalAmount;
    // ... other fields
}
```

---

## ✅ Approved Migration Approach

### **Strategy: Drop and Recreate (Clean Slate)**

**Why?**
- ✅ Tables are NOT in use (no data to preserve)
- ✅ Simpler migration script
- ✅ Clean schema without legacy columns
- ✅ No backward compatibility concerns
- ✅ Faster implementation

---

## 📝 Migration Script

### **V7__drop_and_recreate_orders_for_fsm.sql**

**Purpose:** Drop old tables and create new FSM-ready schema

**Actions:**
1. **DROP** `order_items` table (CASCADE)
2. **DROP** `orders` table (CASCADE)
3. **CREATE** new `orders` table with:
   - UUID primary key
   - FSM `state` column
   - Multi-restaurant support (`order_type`, `parent_order_id`)
   - Complete pricing breakdown
   - FSM-specific timestamps
4. **CREATE** new `order_items` table with UUID keys
5. **CREATE** new `sub_orders` table
6. **CREATE** new `deliveries` table
7. **CREATE** `order_state_audit` table
8. **CREATE** `delivery_state_audit` table
9. **CREATE** all indexes and constraints

**Key Schema Changes:**
```sql
-- OLD
order_id BIGSERIAL PRIMARY KEY
order_status VARCHAR(50)
total_amount DECIMAL(10,2)

-- NEW
order_id UUID PRIMARY KEY DEFAULT gen_random_uuid()
state VARCHAR(32) NOT NULL  -- FSM state
item_total DECIMAL(10,2) NOT NULL
delivery_charges DECIMAL(10,2) NOT NULL
platform_fee DECIMAL(10,2) NOT NULL
gst DECIMAL(10,2) NOT NULL
-- ... plus FSM timestamps
```

---

## 🔄 Implementation Plan

### **Single Migration (V7)**
```
1. DROP old tables:
   ├── order_items (CASCADE)
   └── orders (CASCADE)

2. CREATE new tables:
   ├── orders (with FSM support)
   ├── order_items (with UUID keys)
   ├── sub_orders (multi-restaurant)
   ├── deliveries (delivery FSM)
   ├── order_state_audit
   └── delivery_state_audit
```

### **Application Code**
```java
// Clean Order entity with new schema
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private UUID orderId;  // UUID primary key
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;  // FSM state
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;  // SINGLE or MULTI_RESTAURANT
    
    @Column(name = "item_total", nullable = false)
    private BigDecimal itemTotal;
    
    @Column(name = "delivery_charges", nullable = false)
    private BigDecimal deliveryCharges;
    
    // ... all FSM fields
}
```

### **No Future Cleanup Needed**
- ✅ Clean schema from day 1
- ✅ No deprecated columns
- ✅ No legacy code
- ✅ Ready for FSM implementation

---

## 📋 Migration Checklist

### **Before Migration:**
- [ ] Confirm tables are NOT in use (no production data)
- [ ] Test migration on development environment
- [ ] Review migration script
- [ ] Document rollback procedure

### **During Migration:**
- [ ] Run V7__drop_and_recreate_orders_for_fsm.sql
- [ ] Verify old tables dropped
- [ ] Verify all new tables created
- [ ] Verify all constraints and indexes created
- [ ] Test table structure matches design

### **After Migration:**
- [ ] Create Order entity with new schema
- [ ] Create SubOrder entity
- [ ] Create Delivery entity
- [ ] Create repository interfaces
- [ ] Test CRUD operations
- [ ] Begin FSM implementation

---

## 🧪 Testing Strategy

### **Schema Validation Tests:**
```sql
-- Verify orders table structure
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'orders';

-- Verify all tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_name IN ('orders', 'order_items', 'sub_orders', 'deliveries', 
                     'order_state_audit', 'delivery_state_audit');
-- Should return 6 rows

-- Verify constraints
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'orders';

-- Verify indexes
SELECT indexname 
FROM pg_indexes 
WHERE tablename = 'orders';
```

### **CRUD Tests:**
```java
// Test order creation
Order order = new Order();
order.setCustomerId(UUID.randomUUID());
order.setState(OrderState.CREATED);
order.setOrderType(OrderType.SINGLE);
order.setItemTotal(new BigDecimal("100.00"));
// ... set other fields
orderRepository.save(order);

// Verify UUID generation
assertNotNull(order.getOrderId());
assertTrue(order.getOrderId() instanceof UUID);
```

---

## ⚠️ Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Accidental data loss | Low | Confirm tables NOT in use before migration |
| Migration script errors | Medium | Test on development environment first |
| Application compatibility | Medium | Update entities and repositories immediately |
| Rollback complexity | Low | Simple rollback: restore V5 and V6 migrations |
| Performance issues | Low | Indexes included in migration script |

---

## 🎯 Success Criteria

✅ Old tables successfully dropped  
✅ All new tables created with correct schema  
✅ All constraints and indexes in place  
✅ UUID primary keys working  
✅ FSM state column ready  
✅ Multi-restaurant support enabled  
✅ Delivery FSM tables ready  
✅ Audit tables ready for state tracking  
✅ Clean schema with no legacy columns  

---

## 📚 References

- [BE-003-16: PostgreSQL Schema Migration](./BE-003-16-postgresql-fsm-schema-v2.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- Existing migrations:
  - V5__Create_orders_table.sql
  - V6__Create_order_items_table.sql
