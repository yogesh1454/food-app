# BE-003-01: Database Schema & Infrastructure Setup (Multi-Branch)

**Story ID:** BE-003-01  
**Story Points:** 5  
**Priority:** Critical (P0)  
**Sprint:** 5  
**Epic:** BE-003 (Order & Catalog Management Service)  
**Dependencies:** Epic 1 (Infrastructure)

---

## 📖 User Story

**As a** system architect  
**I want** to set up the database schema for multi-branch vendor management  
**So that** vendors can manage multiple branches with independent operations

---

## ✅ Acceptance Criteria

1. **Database Schema**
   - [ ] 6 core tables created: vendors, vendor_branches, branch_documents, menu_items, orders, order_items
   - [ ] All tables have proper indexes for query performance
   - [ ] JSONB columns for flexible data (preferences, images, metadata)
   - [ ] Foreign key constraints enforce referential integrity
   - [ ] Timestamps (created_at, updated_at) on all tables

2. **Flyway Migrations**
   - [ ] V1__Create_vendors_table.sql
   - [ ] V2__Create_vendor_branches_table.sql
   - [ ] V3__Create_branch_documents_table.sql
   - [ ] V4__Create_menu_items_table.sql
   - [ ] V5__Create_orders_table.sql
   - [ ] V6__Create_order_items_table.sql

3. **JPA Entities**
   - [ ] Vendor entity with relationships
   - [ ] VendorBranch entity with JSONB support
   - [ ] BranchDocument entity
   - [ ] MenuItem entity
   - [ ] Order and OrderItem entities

4. **Spring Data Repositories**
   - [ ] VendorRepository with custom queries
   - [ ] VendorBranchRepository with geospatial queries
   - [ ] BranchDocumentRepository
   - [ ] MenuItemRepository
   - [ ] OrderRepository and OrderItemRepository

5. **TestContainers Setup**
   - [ ] PostgreSQL container for integration tests
   - [ ] Test data fixtures
   - [ ] Database initialization for tests

---

## 🔧 Technical Tasks

### **Task 1: Create Flyway Migration for Vendors Table**
```sql
-- V1__Create_vendors_table.sql
CREATE TABLE vendors (
    vendor_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    
    -- Company details
    company_name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    legal_entity_name VARCHAR(255),
    
    -- Contact
    company_email VARCHAR(255),
    company_phone VARCHAR(20),
    
    -- Legal/Tax
    pan_number VARCHAR(20),
    gst_number VARCHAR(20),
    
    -- Company-level images (JSONB)
    images JSONB DEFAULT '{
        "logo": null,
        "cover_photo": null,
        "brand_assets": []
    }'::jsonb,
    
    -- Flexible metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Tags
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_company_email UNIQUE(company_email)
);

CREATE INDEX idx_vendors_user_id ON vendors(user_id);
CREATE INDEX idx_vendors_company_name ON vendors(company_name);
CREATE INDEX idx_vendors_tags ON vendors USING GIN(tags);
```

### **Task 2: Create Flyway Migration for Vendor Branches Table**
```sql
-- V2__Create_vendor_branches_table.sql
CREATE TABLE vendor_branches (
    branch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    
    -- Branch identification
    branch_name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(50) UNIQUE,
    display_name VARCHAR(255),
    
    -- Location
    address JSONB NOT NULL DEFAULT '{
        "street": "",
        "landmark": "",
        "area": "",
        "city": "",
        "state": "",
        "pincode": "",
        "country": "India"
    }'::jsonb,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    city VARCHAR(100) NOT NULL,
    
    -- Contact
    branch_phone VARCHAR(20),
    branch_email VARCHAR(255),
    branch_manager_name VARCHAR(255),
    branch_manager_phone VARCHAR(20),
    
    -- Onboarding & Verification
    onboarding_status VARCHAR(50) DEFAULT 'PENDING',
    verification_notes TEXT,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID,
    
    -- Branch Preferences (JSONB)
    preferences JSONB DEFAULT '{
        "auto_accept_orders": false,
        "max_orders_per_hour": 50,
        "delivery_radius_km": 5,
        "min_order_value": 0,
        "accepts_cash": true,
        "accepts_online_payment": true,
        "packing_time_minutes": 10,
        "commission_rate": 15.0,
        "priority_delivery": false
    }'::jsonb,
    
    -- Branch images (JSONB)
    images JSONB DEFAULT '{
        "logo": null,
        "cover_photo": null,
        "storefront": null,
        "interior": [],
        "kitchen": [],
        "gallery": []
    }'::jsonb,
    
    -- Operations
    is_active BOOLEAN DEFAULT FALSE,
    is_open BOOLEAN DEFAULT FALSE,
    operating_hours JSONB DEFAULT '{
        "MONDAY": [{"open": "09:00", "close": "21:00"}],
        "TUESDAY": [{"open": "09:00", "close": "21:00"}],
        "WEDNESDAY": [{"open": "09:00", "close": "21:00"}],
        "THURSDAY": [{"open": "09:00", "close": "21:00"}],
        "FRIDAY": [{"open": "09:00", "close": "21:00"}],
        "SATURDAY": [{"open": "09:00", "close": "21:00"}],
        "SUNDAY": [{"open": "09:00", "close": "21:00"}]
    }'::jsonb,
    
    -- Performance metrics
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_orders INTEGER DEFAULT 0,
    total_reviews INTEGER DEFAULT 0,
    
    -- Menu versioning
    menu_version INTEGER DEFAULT 1,
    
    -- Flexible metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Tags
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_branches_vendor_id ON vendor_branches(vendor_id);
CREATE INDEX idx_branches_city ON vendor_branches(city);
CREATE INDEX idx_branches_onboarding_status ON vendor_branches(onboarding_status);
CREATE INDEX idx_branches_active ON vendor_branches(is_active, is_open);
CREATE INDEX idx_branches_rating ON vendor_branches(rating DESC);
CREATE INDEX idx_branches_tags ON vendor_branches USING GIN(tags);
CREATE INDEX idx_branches_preferences ON vendor_branches USING GIN(preferences);
```

### **Task 3: Create Flyway Migration for Branch Documents Table**
```sql
-- V3__Create_branch_documents_table.sql
CREATE TABLE branch_documents (
    document_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL REFERENCES vendor_branches(branch_id) ON DELETE CASCADE,
    
    -- Document details
    document_type VARCHAR(50) NOT NULL,
    document_url VARCHAR(500) NOT NULL,
    document_number VARCHAR(100),
    
    -- Validity
    issue_date DATE,
    expiry_date DATE,
    
    -- Verification
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    verification_notes TEXT,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_branch_documents_branch_id ON branch_documents(branch_id);
CREATE INDEX idx_branch_documents_type ON branch_documents(document_type);
CREATE INDEX idx_branch_documents_status ON branch_documents(verification_status);
CREATE INDEX idx_branch_documents_expiry ON branch_documents(expiry_date) 
    WHERE expiry_date IS NOT NULL;
```

### **Task 4: Create Flyway Migration for Menu Items Table**
```sql
-- V4__Create_menu_items_table.sql
CREATE TABLE menu_items (
    menu_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL REFERENCES vendor_branches(branch_id) ON DELETE CASCADE,
    
    -- Item details
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    category VARCHAR(100) NOT NULL,
    
    -- Images (JSONB)
    images JSONB DEFAULT '{
        "primary": null,
        "gallery": []
    }'::jsonb,
    
    -- Availability
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INTEGER DEFAULT 15,
    
    -- Flexible metadata
    metadata JSONB DEFAULT '{
        "nutritional_info": {},
        "customizations": [],
        "allergens": [],
        "dietary_tags": []
    }'::jsonb,
    
    -- Tags
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    
    -- Soft delete
    is_deleted BOOLEAN DEFAULT FALSE,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_items_branch_id ON menu_items(branch_id);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_available ON menu_items(is_available) 
    WHERE is_deleted = FALSE;
CREATE INDEX idx_menu_items_tags ON menu_items USING GIN(tags);
```

### **Task 5: Create Flyway Migration for Orders Table**
```sql
-- V5__Create_orders_table.sql
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    branch_id UUID NOT NULL REFERENCES vendor_branches(branch_id),
    delivery_partner_id UUID,
    
    -- Order status
    order_status VARCHAR(50) DEFAULT 'PENDING',
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    
    -- Pricing
    total_amount DECIMAL(10,2) NOT NULL,
    
    -- Delivery details (JSONB)
    delivery_details JSONB DEFAULT '{
        "type": "STANDARD",
        "address": {},
        "instructions": "",
        "train_details": null,
        "bus_details": null,
        "factory_details": null
    }'::jsonb,
    
    -- Timestamps
    ordered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    
    -- Special instructions
    special_instructions TEXT,
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_branch_id ON orders(branch_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_ordered_at ON orders(ordered_at DESC);
```

### **Task 6: Create Flyway Migration for Order Items Table**
```sql
-- V6__Create_order_items_table.sql
CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL REFERENCES menu_items(menu_item_id),
    
    -- Item snapshot
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10,2) NOT NULL,
    
    -- Customizations
    notes TEXT,
    customizations JSONB DEFAULT '[]'::jsonb,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
```

### **Task 7: Create JPA Entities**
```java
// Vendor.java
@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vendor_id")
    private UUID vendorId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @Column(length = 255)
    private String brandName;
    
    @Column(length = 255)
    private String legalEntityName;
    
    @Column(length = 255)
    private String companyEmail;
    
    @Column(length = 20)
    private String companyPhone;
    
    @Column(length = 20)
    private String panNumber;
    
    @Column(length = 20)
    private String gstNumber;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> images;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(columnDefinition = "text[]")
    private String[] tags;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBranch> branches = new ArrayList<>();
}

// VendorBranch.java
@Entity
@Table(name = "vendor_branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorBranch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id")
    private UUID branchId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Branch name is required")
    private String branchName;
    
    @Column(length = 50, unique = true)
    private String branchCode;
    
    @Column(length = 255)
    private String displayName;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    @NotNull(message = "Address is required")
    private Map<String, Object> address;
    
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "City is required")
    private String city;
    
    @Column(length = 20)
    private String branchPhone;
    
    @Column(length = 255)
    private String branchEmail;
    
    @Column(length = 255)
    private String branchManagerName;
    
    @Column(length = 20)
    private String branchManagerPhone;
    
    @Column(length = 50, nullable = false)
    private String onboardingStatus = "PENDING";
    
    @Column(columnDefinition = "TEXT")
    private String verificationNotes;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "verified_by")
    private UUID verifiedBy;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> preferences;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> images;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "is_open")
    private Boolean isOpen = false;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> operatingHours;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Column(name = "menu_version")
    private Integer menuVersion = 1;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(columnDefinition = "text[]")
    private String[] tags;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BranchDocument> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();
    
    public void incrementMenuVersion() {
        this.menuVersion++;
    }
}

// BranchDocument.java
@Entity
@Table(name = "branch_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BranchDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    private UUID documentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private VendorBranch branch;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Document type is required")
    private String documentType;
    
    @Column(nullable = false, length = 500)
    @NotBlank(message = "Document URL is required")
    private String documentUrl;
    
    @Column(length = 100)
    private String documentNumber;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(length = 50, nullable = false)
    private String verificationStatus = "PENDING";
    
    @Column(columnDefinition = "TEXT")
    private String verificationNotes;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "verified_by")
    private UUID verifiedBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

// MenuItem.java
@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_item_id")
    private UUID menuItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private VendorBranch branch;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Item name is required")
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Category is required")
    private String category;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> images;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "preparation_time_minutes")
    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    private Integer preparationTimeMinutes = 15;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(columnDefinition = "text[]")
    private String[] tags;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### **Task 8: Create Spring Data Repositories**
```java
// VendorRepository.java
@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByUserId(UUID userId);
    List<Vendor> findByCompanyNameContainingIgnoreCase(String companyName);
}

// VendorBranchRepository.java
@Repository
public interface VendorBranchRepository extends JpaRepository<VendorBranch, UUID> {
    List<VendorBranch> findByVendor(Vendor vendor);
    List<VendorBranch> findByCity(String city);
    List<VendorBranch> findByOnboardingStatus(String status);
    List<VendorBranch> findByIsActiveTrue();
    
    @Query("SELECT b FROM VendorBranch b WHERE b.isActive = true AND b.isOpen = true")
    List<VendorBranch> findOpenBranches();
}

// BranchDocumentRepository.java
@Repository
public interface BranchDocumentRepository extends JpaRepository<BranchDocument, UUID> {
    List<BranchDocument> findByBranch(VendorBranch branch);
    List<BranchDocument> findByBranchAndDocumentType(VendorBranch branch, String documentType);
    List<BranchDocument> findByVerificationStatus(String status);
}

// MenuItemRepository.java
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByBranchAndIsDeletedFalse(VendorBranch branch);
    List<MenuItem> findByBranchAndCategoryAndIsDeletedFalse(VendorBranch branch, String category);
    List<MenuItem> findByBranchAndIsAvailableTrueAndIsDeletedFalse(VendorBranch branch);
}

// OrderRepository.java
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerId(UUID customerId);
    List<Order> findByBranch(VendorBranch branch);
    List<Order> findByBranchAndOrderStatus(VendorBranch branch, String status);
}

// OrderItemRepository.java
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder(Order order);
}
```

---

## 🧪 Testing Requirements

- [ ] Database schema validates with Flyway
- [ ] All entities map correctly to tables
- [ ] Relationships work (vendor → branches → menu items)
- [ ] JSONB columns serialize/deserialize correctly
- [ ] Indexes created successfully
- [ ] TestContainers PostgreSQL integration tests pass

---

## 📋 Definition of Done

- [ ] All 6 Flyway migrations created
- [ ] All JPA entities implemented
- [ ] All repositories created
- [ ] JSONB support configured
- [ ] Relationships tested
- [ ] Indexes verified
- [ ] Integration tests passing
- [ ] Code reviewed

---

## 📚 References

- [Architecture - Database Schema](/docs/architecture/9-database-schema.md)
- [Coding Standards](/docs/architecture/13-coding-standards.md)
