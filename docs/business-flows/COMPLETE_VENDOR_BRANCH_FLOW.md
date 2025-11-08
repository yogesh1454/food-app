# Complete Vendor & Branch Onboarding & Day-to-Day Activity Flow

**Single Comprehensive Guide for Vendor Registration, Branch Onboarding, and Daily Operations**

---

## 📋 Table of Contents

1. [Architectural Overview](#architectural-overview)
2. [Vendor Registration (Unified with First Branch)](#vendor-registration-unified-with-first-branch)
3. [Adding Additional Branches](#adding-additional-branches)
4. [Day-to-Day Operations](#day-to-day-operations)
5. [Testing Scenarios](#testing-scenarios)
6. [API Endpoint Reference](#api-endpoint-reference)

---

## 🏗️ Architectural Overview

### Key Principle: Vendor = Company, Branch = Location

```
┌─────────────────────────────────────────────────────────────┐
│                    VENDOR (Company)                         │
│  - Company Name, PAN, GST, Brand Assets                    │
│  - Company-level metadata and images                        │
│  - Created once per business                                │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 1:N Relationship
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    BRANCH (Location)                        │
│  - Branch Name, Address, Coordinates                        │
│  - Operating Hours, Preferences, Status                     │
│  - Menu Items (specific to this branch)                     │
│  - Documents (FSSAI, GST, etc. for this location)          │
│  - Can be created multiple times per vendor                 │
└─────────────────────────────────────────────────────────────┘
```

### Why Unified Onboarding?

**When a vendor registers, they ARE registering their first branch.** Separating these creates confusion and leaves the vendor in an incomplete state. The unified approach:
- ✅ Clearer business logic
- ✅ 1 fewer API call
- ✅ Vendor immediately operational
- ✅ Consistent onboarding status

---

## 🚀 Vendor & Branch Onboarding (Streamlined)

### Phase 1: Vendor Registration

**Actor:** Vendor (Restaurant Owner/Manager)  
**Goal:** Register as a vendor

#### Step 1.1: Search for Existing Vendor (Optional)

```
GET /api/v1/vendors/{vendorId}

Expected Response (200 OK):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "+91-9876543210",
  "address": {...},
  "branches": [
    {
      "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
      "branchName": "Chai Express - Koramangala",
      "onboardingStatus": "APPROVED"
    }
  ],
  "images": {...},
  "createdAt": "2025-11-06T22:32:00Z"
}
```

---

#### Step 1.2: Create New Vendor

```
POST /api/v1/vendors
Content-Type: application/json

Request Body:
{
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "+91-9876543210",
  "address": {
    "street": "123 Business Park",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560001",
    "country": "India"
  },
  "metadata": {
    "businessType": "QSR",
    "cuisineType": ["Tea", "Snacks"],
    "averageOrderValue": 250,
    "yearsInBusiness": 5
  },
  "tags": ["premium", "tea-specialist", "quick-service"]
}

Expected Response (201 Created):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "companyEmail": "contact@chaiexpress.com",
  "onboardingStatus": "PENDING",
  "createdAt": "2025-11-06T22:32:00Z"
}
```

**Validation Points:**
- ✅ Email format validation
- ✅ Phone number validation
- ✅ Duplicate email check

---

#### Step 1.3: Upload Vendor Logo & Brand Assets

**Unified Upload Endpoint** - Handles both vendor and branch files

```
POST /api/v1/vendors/{vendorId}/upload?target=vendor&fileType=logo
Content-Type: multipart/form-data

Request:
- target: "vendor"
- fileType: "logo"
- file: <logo.png>

Expected Response (200 OK):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "images": {
    "logo": "https://s3.amazonaws.com/tea-snacks/vendors/4d8373d6-b727-4649-8685-3de1e6ca3f99/logo.png",
    "cover_photo": null,
    "brand_assets": []
  }
}
```

---

### Phase 2: Branch Onboarding

**Actor:** Vendor  
**Goal:** Add a branch for the vendor

#### Step 2.1: Create Branch with Preferences & Operating Hours

```
POST /api/v1/vendors/{vendorId}/branches
Content-Type: application/json

Request Body:
{
  "branchName": "Chai Express - Koramangala",
  "branchCode": "CE-KOR-001",
  "address": {
    "street": "456 Koramangala 1st Block",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560034",
    "country": "India"
  },
  "latitude": 12.9352,
  "longitude": 77.6245,
  "preferences": {
    "auto_accept_orders": false,
    "max_orders_per_hour": 50,
    "delivery_radius_km": 5,
    "min_order_value": 100,
    "accepts_cash": true,
    "accepts_online_payment": true,
    "packing_time_minutes": 10,
    "commission_rate": 18.5,
    "priority_delivery": false
  },
  "operatingHours": {
    "timeSlots": [
      {
        "day": "MONDAY",
        "openTime": "06:00",
        "closeTime": "22:00"
      },
      {
        "day": "TUESDAY",
        "openTime": "06:00",
        "closeTime": "22:00"
      },
      {
        "day": "WEDNESDAY",
        "openTime": "06:00",
        "closeTime": "22:00"
      },
      {
        "day": "THURSDAY",
        "openTime": "06:00",
        "closeTime": "22:00"
      },
      {
        "day": "FRIDAY",
        "openTime": "06:00",
        "closeTime": "23:00"
      },
      {
        "day": "SATURDAY",
        "openTime": "07:00",
        "closeTime": "23:00"
      },
      {
        "day": "SUNDAY",
        "openTime": "08:00",
        "closeTime": "22:00"
      }
    ]
  }
}

Expected Response (201 Created):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "branchName": "Chai Express - Koramangala",
  "branchCode": "CE-KOR-001",
  "onboardingStatus": "PENDING",
  "isActive": true,
  "isOpen": false,
  "createdAt": "2025-11-06T22:35:00Z"
}
```

**Validation Points:**
- ✅ Vendor exists check
- ✅ Authorization (user owns vendor)
- ✅ Latitude/Longitude format validation
- ✅ Duplicate branch code check
- ✅ Preferences validation
- ✅ Operating hours validation

---

#### Step 2.2: Upload Branch Images & Documents

**Unified Upload Endpoint** - Same endpoint for vendor and branch files

```
POST /api/v1/vendors/{vendorId}/upload?target=branch&branchId={branchId}&fileType=storefront
Content-Type: multipart/form-data

Request 1 - Storefront Image:
- target: "branch"
- branchId: "be1bff1a-bab0-48cd-b758-200b43efd101"
- fileType: "storefront"
- file: <storefront.jpg>

Request 2 - FSSAI Document:
- target: "branch"
- branchId: "be1bff1a-bab0-48cd-b758-200b43efd101"
- fileType: "fssai"
- documentNumber: "12345678901234"
- issueDate: "2023-01-15"
- expiryDate: "2026-01-15"
- file: <fssai_license.pdf>

Request 3 - GST Document:
- target: "branch"
- branchId: "be1bff1a-bab0-48cd-b758-200b43efd101"
- fileType: "gst"
- documentNumber: "29ABCDE1234F1Z5"
- file: <gst_certificate.pdf>

Request 4 - Shop Act Document:
- target: "branch"
- branchId: "be1bff1a-bab0-48cd-b758-200b43efd101"
- fileType: "shop_act"
- file: <shop_act.pdf>

Request 5 - ID Proof Document:
- target: "branch"
- branchId: "be1bff1a-bab0-48cd-b758-200b43efd101"
- fileType: "id_proof"
- file: <id_proof.pdf>

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "images": {
    "storefront": "https://s3.amazonaws.com/.../storefront.jpg",
    "interior": [],
    "kitchen": [],
    "gallery": []
  },
  "documents": [
    {
      "documentId": "doc-001",
      "documentType": "FSSAI",
      "documentUrl": "https://s3.amazonaws.com/.../FSSAI.pdf",
      "verificationStatus": "PENDING"
    },
    {
      "documentId": "doc-002",
      "documentType": "GST",
      "documentUrl": "https://s3.amazonaws.com/.../GST.pdf",
      "verificationStatus": "PENDING"
    },
    {
      "documentId": "doc-003",
      "documentType": "SHOP_ACT",
      "documentUrl": "https://s3.amazonaws.com/.../SHOP_ACT.pdf",
      "verificationStatus": "PENDING"
    },
    {
      "documentId": "doc-004",
      "documentType": "ID_PROOF",
      "documentUrl": "https://s3.amazonaws.com/.../ID_PROOF.pdf",
      "verificationStatus": "PENDING"
    }
  ]
}
```

---

#### Step 2.3: Update Branch (Future Changes)

```
PUT /api/v1/vendors/{vendorId}/branches/{branchId}
Content-Type: application/json

Request Body (Any combination of these):
{
  "branchName": "Chai Express - Koramangala Updated",
  "preferences": {
    "auto_accept_orders": true,
    "max_orders_per_hour": 60,
    "delivery_radius_km": 7,
    "min_order_value": 150,
    "accepts_cash": true,
    "accepts_online_payment": true,
    "packing_time_minutes": 15,
    "commission_rate": 18.5,
    "priority_delivery": true
  },
  "operatingHours": {
    "timeSlots": [...]
  }
}

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "branchName": "Chai Express - Koramangala Updated",
  "preferences": {...},
  "operatingHours": {...},
  "updatedAt": "2025-11-06T22:50:00Z"
}
```

---

#### Step 2.4: Get Vendor with All Branches

```
GET /api/v1/vendors/{vendorId}

Expected Response (200 OK):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "companyEmail": "contact@chaiexpress.com",
  "onboardingStatus": "DOCUMENTS_SUBMITTED",
  "branches": [
    {
      "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
      "branchName": "Chai Express - Koramangala",
      "branchCode": "CE-KOR-001",
      "address": {...},
      "latitude": 12.9352,
      "longitude": 77.6245,
      "onboardingStatus": "DOCUMENTS_SUBMITTED",
      "isActive": true,
      "isOpen": false,
      "preferences": {...},
      "operatingHours": {...},
      "images": {...},
      "documents": [...]
    }
  ],
  "images": {...},
  "createdAt": "2025-11-06T22:32:00Z"
}
```

---

### Phase 3: Adding Additional Branches

**Actor:** Vendor  
**Goal:** Add more branch locations

**Process:** Repeat Phase 2 (Steps 2.1 - 2.4) for each additional branch

---

## 🍽️ Day-to-Day Operations

### Phase 3: Menu Management (Per Branch)

#### Step 3.1: Create Menu Item for a Branch

```
POST /api/v1/menu-items/branches/{branchId}
Content-Type: application/json

Request Body:
{
  "name": "Masala Tea",
  "description": "Aromatic tea with Indian spices",
  "price": 50.00,
  "category": "Beverages",
  "preparationTimeMinutes": 5,
  "metadata": {
    "servingSize": "250ml",
    "spiceLevel": "medium",
    "vegetarian": true,
    "vegan": false
  },
  "tags": ["tea", "hot", "popular"]
}

Expected Response (201 Created):
{
  "menuItemId": "5f85a293-e29f-4212-a093-a047a39bcaf3",
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "name": "Masala Tea",
  "price": 50.00,
  "isAvailable": true,
  "preparationTimeMinutes": 5,
  "createdAt": "2025-11-06T22:45:00Z"
}
```

---

#### Step 3.2: Upload Menu Item Image

```
POST /api/v1/menu-items/{menuItemId}/images
Content-Type: multipart/form-data

Request:
- imageType: "primary"
- file: <masala_tea.jpg>

Expected Response (200 OK):
{
  "menuItemId": "5f85a293-e29f-4212-a093-a047a39bcaf3",
  "images": {
    "primary": "https://s3.amazonaws.com/.../masala_tea.jpg",
    "gallery": []
  }
}
```

---

#### Step 3.3: Get Branch Menu

```
GET /api/v1/menu-items/branches/{branchId}?category=Beverages&page=0&size=50

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "menuVersion": 1,
  "items": [
    {
      "menuItemId": "5f85a293-e29f-4212-a093-a047a39bcaf3",
      "name": "Masala Tea",
      "price": 50.00,
      "category": "Beverages",
      "isAvailable": true,
      "preparationTimeMinutes": 5,
      "images": {...},
      "tags": ["tea", "hot", "popular"]
    },
    ...
  ],
  "totalItems": 15,
  "page": 0,
  "size": 50
}
```

---

#### Step 3.4: Update Menu Item

```
PUT /api/v1/menu-items/{menuItemId}
Content-Type: application/json

Request Body:
{
  "price": 60.00,
  "isAvailable": false,
  "preparationTimeMinutes": 7
}

Expected Response (200 OK):
{
  "menuItemId": "5f85a293-e29f-4212-a093-a047a39bcaf3",
  "name": "Masala Tea",
  "price": 60.00,
  "isAvailable": false,
  "preparationTimeMinutes": 7,
  "updatedAt": "2025-11-06T22:50:00Z"
}
```

---

#### Step 3.5: Delete Menu Item

```
DELETE /api/v1/menu-items/{menuItemId}

Expected Response (204 No Content)
```

---

### Phase 4: Branch Operations

#### Step 4.1: Check Branch Availability

```
GET /api/v1/branches/{branchId}/availability

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "isOpen": true,
  "isActive": true,
  "status": "OPEN",
  "currentTime": "2025-11-06T22:55:00+05:30",
  "nextOpenTime": null,
  "nextCloseTime": "2025-11-06T23:00:00+05:30",
  "operatingHours": {...}
}
```

**Status Values:**
- `OPEN` - Branch is open and accepting orders
- `OFFLINE` - Branch is manually set to offline
- `CLOSED` - Branch is closed based on operating hours

---

#### Step 4.2: Toggle Branch Online/Offline Status

```
PUT /api/v1/branches/{branchId}/status
Content-Type: application/json

Request Body:
{
  "isOpen": false
}

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "isOpen": false,
  "status": "OFFLINE",
  "updatedAt": "2025-11-06T22:56:00Z"
}
```

---

#### Step 4.3: Get Operating Hours

```
GET /api/v1/branches/{branchId}/operating-hours

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "operatingHours": {
    "timeSlots": [
      {
        "day": "MONDAY",
        "openTime": "06:00",
        "closeTime": "22:00"
      },
      ...
    ],
    "timezone": "Asia/Kolkata"
  }
}
```

---

## 🧪 Testing Scenarios (Streamlined)

### Scenario 1: Complete Vendor & Branch Onboarding (Happy Path)
```
1. POST /api/v1/vendors ✅ (Create vendor)
2. POST /api/v1/vendors/{vendorId}/upload ✅ (Upload vendor logo)
3. POST /api/v1/vendors/{vendorId}/branches ✅ (Create branch with preferences & hours)
4. POST /api/v1/branches/{branchId}/upload ✅ (Upload images & documents)
5. GET /api/v1/vendors/{vendorId} ✅ (Verify complete setup)
```

### Scenario 2: Add Additional Branch
```
1. POST /api/v1/vendors/{vendorId}/branches ✅ (Create new branch)
2. POST /api/v1/branches/{branchId}/upload ✅ (Upload images & documents)
3. GET /api/v1/vendors/{vendorId} ✅ (Verify 2 branches)
```

### Scenario 3: Update Existing Branch
```
1. PUT /api/v1/vendors/{vendorId}/branches/{branchId} ✅ (Update preferences/hours)
2. GET /api/v1/vendors/{vendorId} ✅ (Verify updates)
```

### Scenario 4: Menu Management (Per Branch)
```
1. POST /api/v1/menu-items/branches/{branchId1} ✅ (Create items for branch 1)
2. POST /api/v1/menu-items/branches/{branchId2} ✅ (Create items for branch 2)
3. GET /api/v1/menu-items/branches/{branchId1} ✅ (Get branch 1 menu)
4. GET /api/v1/menu-items/branches/{branchId2} ✅ (Get branch 2 menu)
5. PUT /api/v1/menu-items/{menuItemId} ✅ (Update item)
6. DELETE /api/v1/menu-items/{menuItemId} ✅ (Delete item)
```

### Scenario 5: Daily Operations
```
1. GET /api/v1/branches/{branchId}/availability ✅ (Check status)
2. PUT /api/v1/branches/{branchId}/status ✅ (Toggle online/offline)
3. GET /api/v1/branches/{branchId}/availability ✅ (Verify status change)
```

### Scenario 6: Error Cases
```
1. POST /api/v1/vendors with invalid email ❌
2. POST /api/v1/vendors with duplicate email ❌
3. POST /api/v1/vendors/{vendorId}/branches with invalid coordinates ❌
4. POST /api/v1/vendors/{vendorId}/branches with overlapping operating hours ❌
5. POST /api/v1/menu-items with negative price ❌
6. DELETE /api/v1/menu-items/{invalidId} ❌
7. GET /api/v1/vendors/{invalidId} ❌
```

### Scenario 7: Authorization Checks
```
1. User A creates vendor ✅
2. User B tries to update vendor ❌ (Unauthorized)
3. User A creates branch ✅
4. User B tries to update branch ❌ (Unauthorized)
5. User A creates menu item ✅
6. User B tries to delete menu item ❌ (Unauthorized)
```

---

## 📊 API Endpoint Reference (Unified Approach)

### Vendor Management
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/vendors/{vendorId}` | Search for existing vendor |
| POST | `/api/v1/vendors` | Create new vendor (lightweight) |
| PUT | `/api/v1/vendors/{vendorId}` | Update vendor details |

### Unified Upload Endpoint
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/vendors/{vendorId}/upload?target=vendor&fileType=logo` | Upload vendor logo/brand assets |
| POST | `/api/v1/vendors/{vendorId}/upload?target=branch&branchId={id}&fileType=storefront` | Upload branch images |
| POST | `/api/v1/vendors/{vendorId}/upload?target=branch&branchId={id}&fileType=fssai&documentNumber=...` | Upload branch documents |

### Branch Management
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/vendors/{vendorId}/branches` | Create branch (1st, 2nd, 3rd... same endpoint!) |
| PUT | `/api/v1/vendors/{vendorId}/branches/{branchId}` | Update branch details |
| GET | `/api/v1/branches/{branchId}/availability` | Check branch availability |
| PUT | `/api/v1/branches/{branchId}/status` | Toggle online/offline |

### Menu Management (Per Branch)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/menu-items/branches/{branchId}` | Create menu item |
| GET | `/api/v1/menu-items/{menuItemId}` | Get menu item |
| GET | `/api/v1/menu-items/branches/{branchId}` | Get branch menu |
| PUT | `/api/v1/menu-items/{menuItemId}` | Update menu item |
| DELETE | `/api/v1/menu-items/{menuItemId}` | Delete menu item |
| POST | `/api/v1/menu-items/{menuItemId}/images` | Upload menu item images |

---

## 📝 Key Points

- **Vendor Registration:** Unified with first branch creation via `/api/v1/vendors/onboard`
- **Multi-Branch:** Add additional branches using `/api/v1/branches/vendors/{vendorId}`
- **Menu Management:** Each branch has its own menu items
- **Operating Hours:** Set per branch, not vendor-wide
- **Documents:** Uploaded per branch for location-specific verification
- **Authorization:** All modification endpoints require user ownership validation
- **Caching:** Menu items are cached with version tracking for performance
- **Soft Deletes:** Menu items use soft delete (is_deleted flag)

---

## 🚀 Quick Test Commands

```bash
# Register vendor with first branch
curl -X POST http://localhost:8082/api/v1/vendors/onboard \
  -H "Content-Type: application/json" \
  -d '{"vendor":{...},"firstBranch":{...}}'

# Add additional branch
curl -X POST http://localhost:8082/api/v1/branches/vendors/{vendorId} \
  -H "Content-Type: application/json" \
  -d '{"branchName":"...",...}'

# Create menu item
curl -X POST http://localhost:8082/api/v1/menu-items/branches/{branchId} \
  -H "Content-Type: application/json" \
  -d '{"name":"Masala Tea","price":50.00,...}'

# View Swagger UI
open http://localhost:8082/swagger-ui.html
```
