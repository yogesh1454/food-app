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

## 🚀 Vendor Registration (Unified with First Branch)

### Phase 1: Initial Vendor Registration

**Actor:** Vendor (Restaurant Owner/Manager)  
**Goal:** Register as a vendor with their first branch

#### Step 1.1: Register Vendor with First Branch (UNIFIED)

```
POST /api/v1/vendors/onboard
Content-Type: application/json

Request Body:
{
  "vendor": {
    "companyName": "Chai Express",
    "brandName": "Chai Express - Premium Tea",
    "companyEmail": "contact@chaiexpress.com",
    "companyPhone": "+91-9876543210",
    "pan": "AAACR5055K",
    "gst": "18AABCT1234H1Z0",
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
  },
  "firstBranch": {
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
    }
  }
}

Expected Response (201 Created):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "firstBranchName": "Chai Express - Koramangala",
  "onboardingStatus": "PENDING",
  "message": "Vendor and first branch created successfully. Please upload documents to complete onboarding.",
  "createdAt": "2025-11-06T22:32:00Z"
}
```

**Validation Points:**
- ✅ Email format validation
- ✅ PAN format validation (10 alphanumeric)
- ✅ GST format validation (15 alphanumeric)
- ✅ Phone number validation
- ✅ Duplicate email check
- ✅ Latitude/Longitude format validation
- ✅ Duplicate branch code check

---

#### Step 1.2: Upload Vendor Company Logo & Brand Assets

```
POST /api/v1/vendors/{vendorId}/images
Content-Type: multipart/form-data

Request:
- imageType: "logo"
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

#### Step 1.3: Upload First Branch Documents (FSSAI, GST, SHOP_ACT, ID_PROOF)

```
POST /api/v1/branches/{branchId}/documents
Content-Type: multipart/form-data

Request 1 - FSSAI License:
- documentType: "FSSAI"
- file: <fssai_license.pdf>
- issueDate: "2023-01-15"
- expiryDate: "2026-01-15"

Expected Response (201 Created):
{
  "documentId": "doc-001",
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "documentType": "FSSAI",
  "documentUrl": "https://s3.amazonaws.com/tea-snacks/branches/be1bff1a-bab0-48cd-b758-200b43efd101/FSSAI.pdf",
  "verificationStatus": "PENDING",
  "issueDate": "2023-01-15",
  "expiryDate": "2026-01-15",
  "uploadedAt": "2025-11-06T22:36:00Z"
}

Request 2 - GST Certificate:
- documentType: "GST"
- file: <gst_certificate.pdf>
- issueDate: "2022-06-01"
- expiryDate: null

Request 3 - Shop Act License:
- documentType: "SHOP_ACT"
- file: <shop_act.pdf>
- issueDate: "2023-03-20"
- expiryDate: "2028-03-20"

Request 4 - ID Proof:
- documentType: "ID_PROOF"
- file: <id_proof.pdf>
- issueDate: "2020-05-10"
- expiryDate: "2030-05-10"
```

---

#### Step 1.4: Upload First Branch Images (Storefront, Interior, Kitchen)

```
POST /api/v1/branches/{branchId}/images
Content-Type: multipart/form-data

Request 1 - Storefront:
- imageType: "storefront"
- file: <storefront.jpg>

Request 2 - Interior:
- imageType: "interior"
- file: <interior.jpg>

Request 3 - Kitchen:
- imageType: "kitchen"
- file: <kitchen.jpg>

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "images": {
    "logo": null,
    "cover_photo": null,
    "storefront": "https://s3.amazonaws.com/.../storefront.jpg",
    "interior": ["https://s3.amazonaws.com/.../interior.jpg"],
    "kitchen": ["https://s3.amazonaws.com/.../kitchen.jpg"],
    "gallery": []
  }
}
```

---

#### Step 1.5: Set Operating Hours for First Branch

```
PUT /api/v1/branches/{branchId}/operating-hours
Content-Type: application/json

Request Body:
{
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

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "operatingHours": {
    "timeSlots": [...],
    "timezone": "Asia/Kolkata"
  },
  "updatedAt": "2025-11-06T22:42:00Z"
}
```

---

#### Step 1.6: Update Branch Preferences

```
PUT /api/v1/branches/{branchId}/preferences
Content-Type: application/json

Request Body:
{
  "auto_accept_orders": true,
  "max_orders_per_hour": 60,
  "delivery_radius_km": 7,
  "min_order_value": 150,
  "accepts_cash": true,
  "accepts_online_payment": true,
  "packing_time_minutes": 15,
  "commission_rate": 18.5,
  "priority_delivery": true
}

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "preferences": {...},
  "updatedAt": "2025-11-06T22:40:00Z"
}
```

---

#### Step 1.7: Get Vendor & First Branch Details

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
      "address": {...},
      "onboardingStatus": "DOCUMENTS_SUBMITTED",
      "isActive": true,
      "isOpen": false
    }
  ],
  "images": {...},
  "createdAt": "2025-11-06T22:32:00Z"
}
```

---

## 🏪 Adding Additional Branches

**Actor:** Vendor  
**Goal:** Add a new branch location to existing vendor

### Phase 2: Create Additional Branch

#### Step 2.1: Create Additional Branch

```
POST /api/v1/branches/vendors/{vendorId}
Content-Type: application/json

Request Body:
{
  "branchName": "Chai Express - Indiranagar",
  "branchCode": "CE-IND-002",
  "address": {
    "street": "789 Indiranagar 100 Feet Road",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560038",
    "country": "India"
  },
  "latitude": 12.9716,
  "longitude": 77.6412,
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
  }
}

Expected Response (201 Created):
{
  "branchId": "cf2cgg2b-cbc1-49de-c869-301c54fge202",
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "branchName": "Chai Express - Indiranagar",
  "branchCode": "CE-IND-002",
  "onboardingStatus": "PENDING",
  "isActive": true,
  "isOpen": false,
  "createdAt": "2025-11-06T23:00:00Z"
}
```

#### Step 2.2: Upload Documents for New Branch

Same as Step 1.3 - Upload FSSAI, GST, SHOP_ACT, ID_PROOF

#### Step 2.3: Upload Images for New Branch

Same as Step 1.4 - Upload storefront, interior, kitchen images

#### Step 2.4: Set Operating Hours for New Branch

Same as Step 1.5 - Set operating hours

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

## 🧪 Testing Scenarios

### Scenario 1: Complete Vendor Onboarding (Happy Path)
```
1. POST /api/v1/vendors/onboard ✅ (Create vendor + first branch)
2. POST /api/v1/vendors/{vendorId}/images ✅ (Upload vendor logo)
3. POST /api/v1/branches/{branchId}/documents ✅ (Upload 4 documents)
4. POST /api/v1/branches/{branchId}/images ✅ (Upload branch images)
5. PUT /api/v1/branches/{branchId}/operating-hours ✅ (Set hours)
6. PUT /api/v1/branches/{branchId}/preferences ✅ (Update preferences)
7. GET /api/v1/vendors/{vendorId} ✅ (Verify complete setup)
```

### Scenario 2: Add Additional Branch
```
1. POST /api/v1/branches/vendors/{vendorId} ✅ (Create new branch)
2. POST /api/v1/branches/{branchId}/documents ✅ (Upload documents)
3. POST /api/v1/branches/{branchId}/images ✅ (Upload images)
4. PUT /api/v1/branches/{branchId}/operating-hours ✅ (Set hours)
5. GET /api/v1/vendors/{vendorId} ✅ (Verify 2 branches)
```

### Scenario 3: Menu Management (Per Branch)
```
1. POST /api/v1/menu-items/branches/{branchId1} ✅ (Create items for branch 1)
2. POST /api/v1/menu-items/branches/{branchId2} ✅ (Create items for branch 2)
3. GET /api/v1/menu-items/branches/{branchId1} ✅ (Get branch 1 menu)
4. GET /api/v1/menu-items/branches/{branchId2} ✅ (Get branch 2 menu)
5. PUT /api/v1/menu-items/{menuItemId} ✅ (Update item in branch 1)
6. DELETE /api/v1/menu-items/{menuItemId} ✅ (Delete item from branch 1)
```

### Scenario 4: Daily Operations
```
1. GET /api/v1/branches/{branchId}/availability ✅ (Check status)
2. PUT /api/v1/branches/{branchId}/status ✅ (Toggle online)
3. GET /api/v1/branches/{branchId}/availability ✅ (Verify status)
4. PUT /api/v1/branches/{branchId}/operating-hours ✅ (Update hours)
5. GET /api/v1/branches/{branchId}/operating-hours ✅ (Get hours)
```

### Scenario 5: Error Cases
```
1. POST /api/v1/vendors/onboard with invalid email ❌
2. POST /api/v1/vendors/onboard with duplicate email ❌
3. POST /api/v1/branches with invalid coordinates ❌
4. POST /api/v1/menu-items with negative price ❌
5. POST /api/v1/menu-items with duplicate name in same branch ❌
6. PUT /api/v1/branches/{branchId}/operating-hours with overlapping times ❌
7. DELETE /api/v1/menu-items/{invalidId} ❌
8. GET /api/v1/branches/{invalidId} ❌
```

### Scenario 6: Authorization Checks
```
1. User A registers vendor ✅
2. User B tries to update vendor ❌ (Unauthorized)
3. User A creates branch ✅
4. User B tries to update branch ❌ (Unauthorized)
5. User A creates menu item ✅
6. User B tries to delete menu item ❌ (Unauthorized)
```

---

## 📊 API Endpoint Reference

### Vendor Management
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/vendors/onboard` | Register vendor with first branch (UNIFIED) |
| GET | `/api/v1/vendors/{vendorId}` | Get vendor details with all branches |
| POST | `/api/v1/vendors/{vendorId}/images` | Upload vendor logo/brand assets |

### Branch Management
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/branches/vendors/{vendorId}` | Create additional branch |
| GET | `/api/v1/branches/{branchId}` | Get branch details |
| PUT | `/api/v1/branches/{branchId}` | Update branch |
| POST | `/api/v1/branches/{branchId}/documents` | Upload branch documents |
| GET | `/api/v1/branches/{branchId}/documents` | Get branch documents |
| POST | `/api/v1/branches/{branchId}/images` | Upload branch images |
| PUT | `/api/v1/branches/{branchId}/preferences` | Update branch preferences |
| PUT | `/api/v1/branches/{branchId}/operating-hours` | Set operating hours |
| GET | `/api/v1/branches/{branchId}/operating-hours` | Get operating hours |
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
