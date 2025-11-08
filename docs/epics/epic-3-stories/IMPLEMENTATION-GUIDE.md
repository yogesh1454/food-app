# Epic 3: Implementation Guide

**Status:** ✅ All 13 stories created and ready for implementation  
**Total Story Points:** 121  
**Estimated Duration:** 5 Sprints (12-13 weeks)  
**Architecture:** Multi-branch vendor model with JSONB flexibility

---

## 🎯 Quick Start

### **Step 1: Review the Architecture**
1. Read `README-v2.md` for complete overview
2. Understand the 6-table database schema
3. Review the multi-branch vendor model

### **Step 2: Start with Sprint 5**
1. **BE-003-01**: Database Schema & Infrastructure
   - Create Flyway migrations (V1-V6)
   - Create JPA entities
   - Create repositories
   - Run integration tests

2. **BE-003-02**: Vendor Company Registration
   - Create VendorService
   - Create VendorController
   - Implement image upload to S3
   - Add Kafka event publishing

3. **BE-003-03**: Branch Onboarding & Document Verification
   - Create BranchOnboardingService
   - Implement document upload
   - Implement image upload
   - Create admin verification endpoints

### **Step 3: Continue with Sprints 6-9**
Follow the dependency chain in README-v2.md

---

## 📁 File Structure

```
docs/epics/epic-3-stories/
├── README-v2.md                                    # Main overview
├── IMPLEMENTATION-GUIDE.md                         # This file
├── BE-003-01-database-schema-infrastructure-v2.md  # Sprint 5
├── BE-003-02-vendor-company-registration-v2.md     # Sprint 5
├── BE-003-03-branch-onboarding-v2.md               # Sprint 5
└── BE-003-04-to-12-remaining-stories-v2.md         # Sprints 6-9
```

---

## 🗄️ Database Schema Summary

### **6 Core Tables**

| Table | Purpose | Key Columns |
|-------|---------|------------|
| `vendors` | Company/brand level | vendor_id, user_id, company_name, brand_name, images (JSONB), metadata (JSONB) |
| `vendor_branches` | Location level | branch_id, vendor_id, branch_name, address (JSONB), onboarding_status, preferences (JSONB), images (JSONB), operating_hours (JSONB) |
| `branch_documents` | Document verification | document_id, branch_id, document_type, document_url, verification_status, verified_by |
| `menu_items` | Branch menu | menu_item_id, branch_id, name, price, category, images (JSONB), metadata (JSONB), is_available, is_deleted |
| `orders` | Customer orders | order_id, customer_id, branch_id, order_status, payment_status, delivery_details (JSONB), total_amount |
| `order_items` | Order line items | order_item_id, order_id, menu_item_id, quantity, price_at_order, customizations (JSONB) |

### **JSONB Columns**

**vendors.images:**
```json
{
  "logo": "https://...",
  "cover_photo": "https://...",
  "brand_assets": []
}
```

**vendor_branches.preferences:**
```json
{
  "auto_accept_orders": false,
  "max_orders_per_hour": 50,
  "delivery_radius_km": 5,
  "min_order_value": 0,
  "accepts_cash": true,
  "accepts_online_payment": true,
  "packing_time_minutes": 10,
  "commission_rate": 15.0,
  "priority_delivery": false
}
```

**vendor_branches.images:**
```json
{
  "logo": "https://...",
  "cover_photo": "https://...",
  "storefront": "https://...",
  "interior": ["https://...", "https://..."],
  "kitchen": ["https://..."],
  "gallery": ["https://...", "https://..."]
}
```

**vendor_branches.operating_hours:**
```json
{
  "MONDAY": [{"open": "09:00", "close": "21:00"}],
  "TUESDAY": [{"open": "09:00", "close": "21:00"}],
  ...
}
```

**orders.delivery_details:**
```json
{
  "type": "STANDARD|TRAIN|BUS|FACTORY",
  "address": {...},
  "instructions": "...",
  "train_details": {
    "train_number": "...",
    "coach_number": "...",
    "seat_number": "...",
    "station_code": "...",
    "scheduled_arrival_time": "..."
  },
  "bus_details": {...},
  "factory_details": {...}
}
```

---

## 🔑 Key Features by Story

### **Sprint 5: Foundation (26 pts)**

**BE-003-01: Database Schema (5 pts)**
- 6 Flyway migrations (V1-V6)
- JPA entities with JSONB support
- Spring Data repositories
- TestContainers integration tests

**BE-003-02: Vendor Registration (8 pts)**
- Company registration endpoint
- Logo and cover photo upload to S3
- Company profile management
- Kafka event publishing

**BE-003-03: Branch Onboarding (13 pts)**
- Branch creation with location details
- Document upload (FSSAI, GST, Shop Act, ID proof)
- Branch image uploads (logo, cover, storefront, interior, kitchen, gallery)
- Preferences management (JSONB)
- Onboarding status workflow
- Admin verification endpoints

### **Sprint 6: Menu Management (18 pts)**

**BE-003-04: Operating Hours (5 pts)**
- Set operating hours per day
- Multiple time slots per day support
- Toggle online/offline status
- Scheduled auto-status updates
- Redis caching (5-min TTL)

**BE-003-05: Menu CRUD (8 pts)**
- Create menu items with images
- Update item details
- Soft delete items
- Filter by category, availability, price
- Pagination support

**BE-003-06: Menu Versioning (5 pts)**
- Menu version incremented on changes
- Cache key includes version
- Cache invalidation on updates
- Popular items caching (15-min TTL)
- Cache hit rate target: >80%

### **Sprint 7: Order Processing (21 pts)**

**BE-003-07: Order Creation (13 pts)**
- Create order with multiple items
- Validate branch is active and open
- Validate item availability
- Price snapshot at order time
- Estimated delivery time calculation
- Support specialized delivery types

**BE-003-08: Order Status (8 pts)**
- Status lifecycle: PENDING → ACCEPTED → PREPARING → READY → ON_THE_WAY → DELIVERED
- Status transition validation
- Role-based authorization
- Kafka event publishing

### **Sprint 8: Advanced Features (29 pts)**

**BE-003-09: Specialized Delivery (13 pts)**
- Train delivery (train number, coach, seat, station, arrival time)
- Bus delivery (operator, bus number, stop time)
- Factory delivery (company ID, internal delivery point)
- Delivery window validation

**BE-003-10: B2B Orders (8 pts)**
- Company association
- Bulk order support
- Approval workflow for orders above threshold
- Company billing
- Credit limit checking

**BE-003-11: Sales Reporting (8 pts)**
- Daily sales reports
- Vendor performance metrics
- Order analytics
- Top selling items
- Revenue trends

### **Sprint 9: Integration & Quality (27 pts)**

**BE-003-12: Kafka Integration (8 pts)**
- Event topics: vendor.events, branch.events, menu.events, order.events
- Event types: created, updated, status.changed, deleted
- Transactional publishing
- Retry mechanism
- Dead letter queue

**BE-003-13: E2E Testing (13 pts)**
- Complete order flow tests
- Load testing (1000 concurrent orders)
- Performance optimization
- Query optimization (prevent N+1)
- Cache effectiveness verification

---

## 🚀 Implementation Checklist

### **Sprint 5**

- [ ] **BE-003-01: Database Schema**
  - [ ] Create V1__Create_vendors_table.sql
  - [ ] Create V2__Create_vendor_branches_table.sql
  - [ ] Create V3__Create_branch_documents_table.sql
  - [ ] Create V4__Create_menu_items_table.sql
  - [ ] Create V5__Create_orders_table.sql
  - [ ] Create V6__Create_order_items_table.sql
  - [ ] Create Vendor entity
  - [ ] Create VendorBranch entity
  - [ ] Create BranchDocument entity
  - [ ] Create MenuItem entity
  - [ ] Create Order and OrderItem entities
  - [ ] Create all repositories
  - [ ] Write integration tests
  - [ ] Verify migrations run successfully

- [ ] **BE-003-02: Vendor Registration**
  - [ ] Create VendorService
  - [ ] Create VendorController
  - [ ] Implement S3 image upload
  - [ ] Create VendorEventPublisher
  - [ ] Write unit tests
  - [ ] Write integration tests
  - [ ] Verify Kafka events published

- [ ] **BE-003-03: Branch Onboarding**
  - [ ] Create BranchOnboardingService
  - [ ] Implement branch creation
  - [ ] Implement document upload
  - [ ] Implement image upload
  - [ ] Create DocumentVerificationService
  - [ ] Create admin verification endpoints
  - [ ] Write unit tests
  - [ ] Write integration tests

### **Sprint 6**

- [ ] **BE-003-04: Operating Hours**
  - [ ] Create OperatingHoursService
  - [ ] Implement status toggle
  - [ ] Create scheduled job for auto-update
  - [ ] Implement Redis caching
  - [ ] Write tests

- [ ] **BE-003-05: Menu CRUD**
  - [ ] Create MenuService
  - [ ] Implement CRUD operations
  - [ ] Implement soft delete
  - [ ] Implement image upload
  - [ ] Write tests

- [ ] **BE-003-06: Menu Versioning**
  - [ ] Implement version increment logic
  - [ ] Implement cache invalidation
  - [ ] Implement popular items caching
  - [ ] Write performance tests

### **Sprint 7**

- [ ] **BE-003-07: Order Creation**
  - [ ] Create OrderService
  - [ ] Implement validation
  - [ ] Implement price snapshot
  - [ ] Implement delivery time calculation
  - [ ] Write tests

- [ ] **BE-003-08: Order Status**
  - [ ] Create OrderStatusService
  - [ ] Implement status transitions
  - [ ] Implement authorization
  - [ ] Write tests

### **Sprint 8**

- [ ] **BE-003-09: Specialized Delivery**
  - [ ] Implement train delivery
  - [ ] Implement bus delivery
  - [ ] Implement factory delivery
  - [ ] Write tests

- [ ] **BE-003-10: B2B Orders**
  - [ ] Implement company association
  - [ ] Implement approval workflow
  - [ ] Implement credit limit checking
  - [ ] Write tests

- [ ] **BE-003-11: Sales Reporting**
  - [ ] Create ReportingService
  - [ ] Implement daily sales report
  - [ ] Implement vendor metrics
  - [ ] Implement order analytics
  - [ ] Write tests

### **Sprint 9**

- [ ] **BE-003-12: Kafka Integration**
  - [ ] Configure Kafka producer
  - [ ] Create event publishers
  - [ ] Implement retry mechanism
  - [ ] Implement dead letter queue
  - [ ] Write tests

- [ ] **BE-003-13: E2E Testing**
  - [ ] Write E2E test suite
  - [ ] Write load tests
  - [ ] Optimize queries
  - [ ] Verify performance targets
  - [ ] Verify cache hit rate

---

## 📊 Performance Targets

| Metric | Target | How to Verify |
|--------|--------|---------------|
| Menu retrieval (cached) | < 50ms | JMeter load test |
| Menu retrieval (uncached) | < 200ms | JMeter load test |
| Order creation | < 500ms | JMeter load test |
| Branch search | < 100ms | JMeter load test |
| Load test | 1000 concurrent orders | JMeter test |
| Cache hit rate | > 80% | Redis monitoring |

---

## 🧪 Testing Strategy

### **Unit Tests**
- Entity validation
- Service business logic
- Repository queries
- Mapper transformations
- Target: >80% code coverage

### **Integration Tests**
- TestContainers with PostgreSQL
- Flyway migrations
- JPA relationships
- Transaction handling
- Kafka event publishing

### **E2E Tests**
- Complete order flow
- Multi-branch scenarios
- Concurrent order processing
- Error handling

### **Performance Tests**
- Load test: 1000 concurrent orders
- Menu retrieval benchmarks
- Query optimization verification
- Cache effectiveness

---

## 🔗 Dependencies

### **External Services**
- User Management Service (for user validation)
- S3/CDN (for image storage)
- Kafka (for event publishing)
- Redis (for caching)
- PostgreSQL (for data persistence)

### **Internal Dependencies**
- Shared library (common utilities, exceptions, DTOs)
- Architecture standards (coding standards, patterns)

---

## 📚 Key References

- [Architecture - Database Schema](/docs/architecture/9-database-schema.md)
- [Architecture - REST API Spec](/docs/architecture/8-rest-api-spec.md)
- [Architecture - Core Workflows](/docs/architecture/7-core-workflows.md)
- [Architecture - Components](/docs/architecture/5-components.md)
- [Coding Standards](/docs/architecture/13-coding-standards.md)
- [Test Strategy](/docs/architecture/14-test-strategy-and-standards.md)

---

## ✅ Success Criteria

- ✅ All 13 stories implemented
- ✅ All tests passing (>80% coverage)
- ✅ Performance targets met
- ✅ Kafka events publishing correctly
- ✅ Redis caching working (>80% hit rate)
- ✅ Code reviewed and approved
- ✅ Documentation complete

---

## 🎯 Next Steps

1. **Review** this implementation guide with the team
2. **Assign** stories to developers
3. **Start** with Sprint 5 (BE-003-01)
4. **Follow** the dependency chain
5. **Test** each story before moving to next
6. **Deploy** incrementally after each sprint

**Ready to start implementation!** 🚀
