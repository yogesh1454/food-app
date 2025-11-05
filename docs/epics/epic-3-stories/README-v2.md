# Epic 3: Order & Catalog Management Service - User Stories (Multi-Branch Architecture)

**Epic ID:** BE-003  
**Total Stories:** 13  
**Total Story Points:** 121  
**Estimated Duration:** 5 Sprints (12-13 weeks)  
**Priority:** Critical (P0)  
**Architecture:** Multi-branch vendor model with JSONB flexibility

---

## 📚 Story Index

### **Sprint 5: Foundation & Vendor Management (26 points)**

| Story ID | Title | Points | Status | Document |
|----------|-------|--------|--------|----------|
| BE-003-01 | Database Schema & Infrastructure | 5 | ✅ Created | [View](./BE-003-01-database-schema-infrastructure-v2.md) |
| BE-003-02 | Vendor Company Registration | 8 | ✅ Created | [View](./BE-003-02-vendor-company-registration-v2.md) |
| BE-003-03 | Branch Onboarding & Document Verification | 13 | ✅ Created | [View](./BE-003-03-branch-onboarding-v2.md) |

### **Sprint 6: Menu Management**

| Story ID | Title | Points | Status | Document |
|----------|-------|--------|--------|----------|
| BE-003-04 | Branch Operating Hours & Status | 5 | ✅ Created | [View](./BE-003-04-branch-operating-hours-v2.md) |
| BE-003-05 | Menu Item CRUD Operations | 8 | ✅ Created | [View](./BE-003-05-menu-item-crud-v2.md) |
| BE-003-06 | Menu Versioning & Cache Management | 5 | ✅ Created | [View](./BE-003-06-menu-versioning-cache-v2.md) |

### **Sprint 7: Order Processing Core**

| Story ID | Title | Points | Status | Document |
|----------|-------|--------|--------|----------|
| BE-003-07 | Order Creation & Validation | 13 | ✅ Created | [View](./BE-003-07-order-creation-v2.md) |
| BE-003-08 | Order Status Lifecycle Management | 8 | ✅ Created | [View](./BE-003-08-order-status-lifecycle-v2.md) |

### **Sprint 8: Specialized Delivery & Reporting**

| Story ID | Title | Points | Status | Document |
|----------|-------|--------|--------|----------|
| BE-003-09 | Specialized Delivery Scenarios | 13 | ✅ Created | [View](./BE-003-09-specialized-delivery-v2.md) |
| BE-003-10 | B2B Order Processing | 8 | ✅ Created | [View](./BE-003-10-b2b-orders-v2.md) |
| BE-003-11 | Sales Reporting & Analytics | 8 | ✅ Created | [View](./BE-003-11-sales-reporting-v2.md) |

### **Sprint 9: Integration & Testing**

| Story ID | Title | Points | Status | Document |
|----------|-------|--------|--------|----------|
| BE-003-12 | Kafka Event Publishing Integration | 8 | ✅ Created | [View](./BE-003-12-kafka-integration-v2.md) |
| BE-003-13 | End-to-End Testing & Performance Optimization | 13 | ✅ Created | [View](./BE-003-13-e2e-testing-v2.md) |

---

## 🏗️ Architecture Overview

### **Multi-Branch Vendor Model**

```
Vendor (Company/Brand)
├── Branch 1 (Location A)
│   ├── Menu Items (Branch-specific)
│   ├── Operating Hours (Branch-specific)
│   ├── Orders (Branch-specific)
│   └── Documents (Branch-specific)
├── Branch 2 (Location B)
│   ├── Menu Items (Branch-specific)
│   ├── Operating Hours (Branch-specific)
│   └── Orders (Branch-specific)
└── Branch N (Location N)
```

### **Database Schema (6 Core Tables)**

```sql
vendors              -- Company/brand level
vendor_branches      -- Location level (main entity)
branch_documents     -- Documents for verification
menu_items          -- Branch-specific menu
orders              -- Customer orders
order_items         -- Order line items
```

### **Key Design Decisions**

✅ **Flat Structure:** 6 tables instead of 10+  
✅ **JSONB Flexibility:** preferences, images, metadata, operating_hours  
✅ **ACID Compliance:** Foreign keys, constraints, transactions  
✅ **NoSQL Flexibility:** JSON columns for evolving requirements  
✅ **Performance:** Indexes on frequently queried columns  
✅ **Scalability:** Geospatial queries for branch search  

---

## 📊 Database Schema Details

### **Vendors Table**
- Company-level information
- Brand assets (logo, cover photo)
- Legal/tax details (PAN, GST)
- Flexible metadata and tags

### **Vendor Branches Table** (Most Complex)
- Location details (address, lat/long, city)
- Onboarding status (PENDING → DOCUMENTS_SUBMITTED → UNDER_VERIFICATION → APPROVED)
- Preferences (JSONB): auto-accept, max orders, delivery radius, etc.
- Images (JSONB): logo, cover, storefront, interior, kitchen, gallery
- Operating hours (JSONB): per-day time slots
- Performance metrics: rating, total orders, reviews
- Menu versioning: incremented on any menu change

### **Branch Documents Table**
- Document types: FSSAI_LICENSE, SHOP_ACT, GST_CERTIFICATE, OWNER_ID_PROOF, MENU_CARD
- Verification status: PENDING, APPROVED, REJECTED, EXPIRED
- Issue/expiry dates for compliance tracking
- Verified by admin user ID

### **Menu Items Table**
- Branch-specific (not vendor-level)
- Images (JSONB): primary + gallery
- Metadata (JSONB): nutritional info, customizations, allergens, dietary tags
- Soft delete support
- Category and availability filtering

### **Orders Table**
- Branch-specific (not vendor-level)
- Delivery details (JSONB): type (STANDARD/TRAIN/BUS/FACTORY), address, instructions
- Status tracking: PENDING → ACCEPTED → PREPARING → READY → ON_THE_WAY → DELIVERED
- Payment status: PENDING, PAID, FAILED, REFUNDED
- Metadata (JSONB): extensible for future needs

### **Order Items Table**
- Price snapshot at order time
- Customizations (JSONB): special requests per item
- Linked to menu item for reference

---

## 🔑 Key Features by Story

### **Sprint 5: Foundation**
- ✅ 6-table database schema with JSONB support
- ✅ Vendor company registration with logo upload
- ✅ Branch creation with location details
- ✅ Document upload and verification workflow
- ✅ Branch image uploads (logo, cover, storefront, interior, kitchen, gallery)
- ✅ Preferences management (JSONB)
- ✅ Onboarding status tracking

### **Sprint 6: Menu Management**
- ✅ Branch-specific operating hours
- ✅ Online/offline status toggle
- ✅ Menu item CRUD (create, read, update, soft delete)
- ✅ Menu item images (primary + gallery)
- ✅ Category filtering and pagination
- ✅ Menu versioning and cache invalidation
- ✅ Popular items caching

### **Sprint 7: Order Processing**
- ✅ Order creation with validation
- ✅ Price snapshot at order time
- ✅ Estimated delivery time calculation
- ✅ Order status lifecycle (PENDING → DELIVERED)
- ✅ Status transition validation
- ✅ Role-based authorization (vendor, delivery partner, customer)

### **Sprint 8: Advanced Features**
- ✅ Specialized delivery (train, bus, factory)
- ✅ B2B order processing with approval workflows
- ✅ Company billing and credit limits
- ✅ Sales reporting and analytics
- ✅ Daily sales reports
- ✅ Vendor performance metrics
- ✅ Top selling items analysis

### **Sprint 9: Integration & Quality**
- ✅ Kafka event publishing (vendor, branch, menu, order events)
- ✅ Transactional event publishing
- ✅ Retry mechanism and dead letter queue
- ✅ E2E testing (complete order flow)
- ✅ Load testing (1000 concurrent orders)
- ✅ Performance optimization
- ✅ Query optimization (prevent N+1)

---

## 🎯 API Endpoints Summary

### **Vendor Management**
```
POST   /api/v1/vendors                    # Register company
GET    /api/v1/vendors/{vendorId}         # Get company profile
PUT    /api/v1/vendors/{vendorId}         # Update company
POST   /api/v1/vendors/{vendorId}/logo    # Upload logo
```

### **Branch Management**
```
POST   /api/v1/vendors/{vendorId}/branches           # Create branch
GET    /api/v1/vendors/{vendorId}/branches           # List branches
GET    /api/v1/branches/{branchId}                   # Get branch details
PUT    /api/v1/branches/{branchId}                   # Update branch
PUT    /api/v1/branches/{branchId}/status            # Toggle online/offline
GET    /api/v1/branches/{branchId}/onboarding-status # Check status
```

### **Document Management**
```
POST   /api/v1/branches/{branchId}/documents    # Upload document
GET    /api/v1/branches/{branchId}/documents    # List documents
POST   /api/v1/branches/{branchId}/images       # Upload image
```

### **Menu Management**
```
POST   /api/v1/branches/{branchId}/menu-items   # Create item
GET    /api/v1/branches/{branchId}/menu         # Get menu
GET    /api/v1/menu-items/{menuItemId}          # Get item
PUT    /api/v1/menu-items/{menuItemId}          # Update item
DELETE /api/v1/menu-items/{menuItemId}          # Delete item
```

### **Order Management**
```
POST   /api/v1/orders                           # Create order
GET    /api/v1/orders/{orderId}                 # Get order
GET    /api/v1/orders/customer/{customerId}     # Customer orders
GET    /api/v1/branches/{branchId}/orders       # Branch orders
PUT    /api/v1/orders/{orderId}/status          # Update status
```

### **Reporting**
```
GET    /api/v1/reports/daily-sales              # Daily sales
GET    /api/v1/reports/vendor-performance/{id}  # Vendor metrics
GET    /api/v1/reports/branch-performance/{id}  # Branch metrics
GET    /api/v1/reports/order-analytics          # Order analytics
```

---

## 🔗 Kafka Topics & Events

### **Topics**
- `vendor.events` - Vendor lifecycle events
- `branch.events` - Branch lifecycle events
- `menu.events` - Menu update events
- `order.events` - Order lifecycle events

### **Event Types**
```
vendor.created, vendor.updated
branch.created, branch.approved, branch.rejected
menu.updated, menu.item.created, menu.item.updated, menu.item.deleted
order.created, order.status.changed, order.completed, order.cancelled
```

---

## 💾 Redis Cache Keys

```
vendor:{vendorId}:menu:v{version}           # Full menu (1 hour TTL)
vendor:{vendorId}:popular-items             # Popular items (15 min TTL)
branch:{branchId}:status                    # Branch status (5 min TTL)
branch:{branchId}:availability              # Availability (5 min TTL)
```

---

## 📈 Performance Targets

| Operation | Target | Notes |
|-----------|--------|-------|
| Menu retrieval (cached) | < 50ms | Redis hit |
| Menu retrieval (uncached) | < 200ms | Database query |
| Order creation | < 500ms | Validation + DB insert |
| Branch search | < 100ms | Geospatial query |
| Load test | 1000 concurrent orders | All successful |
| Cache hit rate | > 80% | Menu and popular items |

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
- Complete order flow: registration → onboarding → menu → order → delivery
- Multi-branch scenarios
- Concurrent order processing
- Error handling and recovery

### **Performance Tests**
- Load test: 1000 concurrent orders
- Menu retrieval benchmarks
- Query optimization verification
- Cache effectiveness

---

## 🚀 Implementation Sequence

1. **BE-003-01** → Database schema and migrations
2. **BE-003-02** → Vendor registration
3. **BE-003-03** → Branch onboarding
4. **BE-003-04** → Operating hours
5. **BE-003-05** → Menu CRUD
6. **BE-003-06** → Menu versioning
7. **BE-003-07** → Order creation
8. **BE-003-08** → Order status
9. **BE-003-09** → Specialized delivery
10. **BE-003-10** → B2B orders
11. **BE-003-11** → Reporting
12. **BE-003-12** → Kafka integration
13. **BE-003-13** → E2E testing

---

## 📋 Story Dependencies

```
BE-003-01 (Database)
├── BE-003-02 (Vendor Registration)
│   └── BE-003-03 (Branch Onboarding)
│       ├── BE-003-04 (Operating Hours)
│       │   └── BE-003-05 (Menu CRUD)
│       │       └── BE-003-06 (Menu Versioning)
│       │           └── BE-003-07 (Order Creation)
│       │               ├── BE-003-08 (Order Status)
│       │               ├── BE-003-09 (Specialized Delivery)
│       │               └── BE-003-10 (B2B Orders)
│       └── BE-003-11 (Reporting)
└── BE-003-12 (Kafka Integration)
    └── BE-003-13 (E2E Testing)
```

---

## 📚 References

- [Architecture - Database Schema](/docs/architecture/9-database-schema.md)
- [Architecture - REST API Spec](/docs/architecture/8-rest-api-spec.md)
- [Architecture - Core Workflows](/docs/architecture/7-core-workflows.md)
- [Architecture - Components](/docs/architecture/5-components.md)
- [Coding Standards](/docs/architecture/13-coding-standards.md)
- [Test Strategy](/docs/architecture/14-test-strategy-and-standards.md)

---

## ✅ Status

**All 13 stories created and ready for implementation!**

- ✅ Multi-branch architecture finalized
- ✅ Database schema optimized (6 tables, JSONB flexibility)
- ✅ API endpoints specified
- ✅ Kafka events defined
- ✅ Performance targets set
- ✅ Testing strategy documented

**Next Steps:** Begin implementation with BE-003-01 (Database Schema)
