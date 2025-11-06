# Vendor & Branch Onboarding & Day-to-Day Activity Flow

This document outlines the complete business flow for vendor registration, branch onboarding, and daily operational activities. Use this as a reference for testing API endpoints.

---

## 📋 Table of Contents

1. [Vendor Onboarding Flow](#vendor-onboarding-flow)
2. [Branch Onboarding Flow](#branch-onboarding-flow)
3. [Day-to-Day Operations](#day-to-day-operations)
4. [Testing Scenarios](#testing-scenarios)

---

## 🏢 Vendor Onboarding Flow

### Phase 1: Vendor Registration

**Actor:** Vendor (Restaurant Owner/Manager)  
**Goal:** Register as a vendor in the platform

#### Step 1.1: Register Vendor Company
```
POST /api/v1/vendors
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
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
}

Expected Response (201 Created):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "companyEmail": "contact@chaiexpress.com",
  "onboardingStatus": "PENDING",
  "createdAt": "2025-11-06T22:32:00Z",
  "updatedAt": "2025-11-06T22:32:00Z"
}
```

**Validation Points:**
- ✅ Email format validation
- ✅ PAN format validation (10 alphanumeric)
- ✅ GST format validation (15 alphanumeric)
- ✅ Phone number validation
- ✅ Duplicate email check
- ✅ User exists check

---

#### Step 1.2: Upload Vendor Logo & Brand Assets
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

**Validation Points:**
- ✅ File type validation (PNG, JPG, JPEG)
- ✅ File size validation (max 5MB)
- ✅ S3 upload success
- ✅ URL generation

---

#### Step 1.3: Get Vendor Details
```
GET /api/v1/vendors/{vendorId}

Expected Response (200 OK):
{
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "+91-9876543210",
  "pan": "AAACR5055K",
  "gst": "18AABCT1234H1Z0",
  "address": {...},
  "images": {...},
  "metadata": {...},
  "tags": [...],
  "onboardingStatus": "PENDING",
  "createdAt": "2025-11-06T22:32:00Z",
  "updatedAt": "2025-11-06T22:32:00Z"
}
```

---

## 🏪 Branch Onboarding Flow

### Phase 2: Create First Branch

**Actor:** Vendor  
**Goal:** Add a physical branch location

#### Step 2.1: Create Branch
```
POST /api/v1/branches/vendors/{vendorId}
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
  }
}

Expected Response (201 Created):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "branchName": "Chai Express - Koramangala",
  "branchCode": "CE-KOR-001",
  "address": {...},
  "latitude": 12.9352,
  "longitude": 77.6245,
  "onboardingStatus": "PENDING",
  "isActive": true,
  "isOpen": false,
  "menuVersion": 0,
  "createdAt": "2025-11-06T22:35:00Z",
  "updatedAt": "2025-11-06T22:35:00Z"
}
```

**Validation Points:**
- ✅ Vendor exists check
- ✅ Authorization (user owns vendor)
- ✅ Latitude/Longitude format validation
- ✅ Duplicate branch code check
- ✅ Preferences validation

---

#### Step 2.2: Upload Branch Documents (FSSAI, GST, SHOP_ACT, ID_PROOF)
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

**Validation Points:**
- ✅ Document type validation (FSSAI, GST, SHOP_ACT, ID_PROOF)
- ✅ File type validation (PDF, JPG, PNG)
- ✅ File size validation
- ✅ Expiry date validation (not expired)
- ✅ All required documents uploaded
- ✅ S3 upload success

---

#### Step 2.3: Get Branch Documents
```
GET /api/v1/branches/{branchId}/documents

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "documents": [
    {
      "documentId": "doc-001",
      "documentType": "FSSAI",
      "documentUrl": "...",
      "verificationStatus": "PENDING",
      "issueDate": "2023-01-15",
      "expiryDate": "2026-01-15"
    },
    {
      "documentId": "doc-002",
      "documentType": "GST",
      "documentUrl": "...",
      "verificationStatus": "PENDING",
      "issueDate": "2022-06-01",
      "expiryDate": null
    },
    ...
  ]
}
```

---

#### Step 2.4: Upload Branch Images
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

#### Step 2.5: Update Branch Preferences
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

#### Step 2.6: Set Operating Hours
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

**Validation Points:**
- ✅ Time format validation (HH:MM)
- ✅ No overlapping time slots
- ✅ Close time > open time
- ✅ All days covered

---

#### Step 2.7: Get Branch Details
```
GET /api/v1/branches/{branchId}

Expected Response (200 OK):
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "vendorId": "4d8373d6-b727-4649-8685-3de1e6ca3f99",
  "branchName": "Chai Express - Koramangala",
  "branchCode": "CE-KOR-001",
  "address": {...},
  "latitude": 12.9352,
  "longitude": 77.6245,
  "onboardingStatus": "DOCUMENTS_SUBMITTED",
  "isActive": true,
  "isOpen": false,
  "menuVersion": 0,
  "operatingHours": {...},
  "preferences": {...},
  "images": {...},
  "createdAt": "2025-11-06T22:35:00Z",
  "updatedAt": "2025-11-06T22:42:00Z"
}
```

---

## 🍽️ Day-to-Day Operations

### Phase 3: Menu Management

#### Step 3.1: Create Menu Item
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
  "description": "Aromatic tea with Indian spices",
  "price": 50.00,
  "category": "Beverages",
  "isAvailable": true,
  "preparationTimeMinutes": 5,
  "images": {},
  "metadata": {...},
  "tags": ["tea", "hot", "popular"],
  "createdAt": "2025-11-06T22:45:00Z",
  "updatedAt": "2025-11-06T22:45:00Z"
}
```

**Validation Points:**
- ✅ Branch exists check
- ✅ Authorization (user owns branch)
- ✅ Price validation (> 0)
- ✅ Category validation
- ✅ Preparation time validation (1-240 minutes)
- ✅ Name uniqueness per branch

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

**Validation Points:**
- ✅ Pagination working
- ✅ Category filtering
- ✅ Menu version tracking
- ✅ Only non-deleted items returned

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

**Validation Points:**
- ✅ Menu version incremented
- ✅ Cache invalidated
- ✅ Authorization check
- ✅ Partial update support

---

#### Step 3.5: Delete Menu Item
```
DELETE /api/v1/menu-items/{menuItemId}

Expected Response (204 No Content)
```

**Validation Points:**
- ✅ Soft delete (is_deleted = true)
- ✅ Menu version incremented
- ✅ Cache invalidated
- ✅ Authorization check

---

### Phase 4: Daily Operations

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
1. POST /api/v1/vendors ✅
2. POST /api/v1/vendors/{vendorId}/images ✅
3. GET /api/v1/vendors/{vendorId} ✅
4. POST /api/v1/branches/vendors/{vendorId} ✅
5. POST /api/v1/branches/{branchId}/documents (4 times) ✅
6. POST /api/v1/branches/{branchId}/images ✅
7. PUT /api/v1/branches/{branchId}/preferences ✅
8. PUT /api/v1/branches/{branchId}/operating-hours ✅
9. GET /api/v1/branches/{branchId} ✅
```

### Scenario 2: Menu Management
```
1. POST /api/v1/menu-items/branches/{branchId} (Create 5 items) ✅
2. POST /api/v1/menu-items/{menuItemId}/images (Upload images) ✅
3. GET /api/v1/menu-items/branches/{branchId} ✅
4. GET /api/v1/menu-items/branches/{branchId}?category=Beverages ✅
5. PUT /api/v1/menu-items/{menuItemId} (Update price) ✅
6. DELETE /api/v1/menu-items/{menuItemId} ✅
7. GET /api/v1/menu-items/branches/{branchId} (Verify deleted item not shown) ✅
```

### Scenario 3: Daily Operations
```
1. GET /api/v1/branches/{branchId}/availability ✅
2. PUT /api/v1/branches/{branchId}/status (Toggle online) ✅
3. GET /api/v1/branches/{branchId}/availability (Verify status) ✅
4. PUT /api/v1/branches/{branchId}/operating-hours (Update hours) ✅
5. GET /api/v1/branches/{branchId}/operating-hours ✅
```

### Scenario 4: Error Cases
```
1. POST /api/v1/vendors with invalid email ❌
2. POST /api/v1/vendors with duplicate email ❌
3. POST /api/v1/branches with invalid coordinates ❌
4. POST /api/v1/menu-items with negative price ❌
5. POST /api/v1/menu-items with duplicate name in same branch ❌
6. PUT /api/v1/branches/{branchId}/operating-hours with overlapping times ❌
7. DELETE /api/v1/menu-items/{invalidId} ❌
8. GET /api/v1/branches/{invalidId} ❌
```

### Scenario 5: Authorization Checks
```
1. User A creates vendor ✅
2. User B tries to update vendor ❌ (Unauthorized)
3. User A creates branch ✅
4. User B tries to update branch ❌ (Unauthorized)
5. User A creates menu item ✅
6. User B tries to delete menu item ❌ (Unauthorized)
```

---

## 📊 Data Flow Summary

```
┌─────────────────────────────────────────────────────────────┐
│                    VENDOR REGISTRATION                      │
│  (Vendor Details → Logo Upload → Get Vendor)               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   BRANCH CREATION                           │
│  (Create Branch → Upload Documents → Upload Images)        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              BRANCH CONFIGURATION                           │
│  (Set Preferences → Set Operating Hours → Get Details)     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  MENU MANAGEMENT                            │
│  (Create Items → Upload Images → Get Menu → Update/Delete) │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              DAILY OPERATIONS                               │
│  (Check Availability → Toggle Status → Update Hours)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔗 API Endpoint Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/vendors` | Register vendor |
| GET | `/api/v1/vendors/{vendorId}` | Get vendor details |
| POST | `/api/v1/vendors/{vendorId}/images` | Upload vendor images |
| POST | `/api/v1/branches/vendors/{vendorId}` | Create branch |
| GET | `/api/v1/branches/{branchId}` | Get branch details |
| PUT | `/api/v1/branches/{branchId}` | Update branch |
| POST | `/api/v1/branches/{branchId}/documents` | Upload documents |
| GET | `/api/v1/branches/{branchId}/documents` | Get documents |
| POST | `/api/v1/branches/{branchId}/images` | Upload branch images |
| PUT | `/api/v1/branches/{branchId}/preferences` | Update preferences |
| PUT | `/api/v1/branches/{branchId}/operating-hours` | Set operating hours |
| GET | `/api/v1/branches/{branchId}/operating-hours` | Get operating hours |
| GET | `/api/v1/branches/{branchId}/availability` | Check availability |
| PUT | `/api/v1/branches/{branchId}/status` | Toggle online/offline |
| POST | `/api/v1/menu-items/branches/{branchId}` | Create menu item |
| GET | `/api/v1/menu-items/{menuItemId}` | Get menu item |
| GET | `/api/v1/menu-items/branches/{branchId}` | Get branch menu |
| PUT | `/api/v1/menu-items/{menuItemId}` | Update menu item |
| DELETE | `/api/v1/menu-items/{menuItemId}` | Delete menu item |
| POST | `/api/v1/menu-items/{menuItemId}/images` | Upload menu item images |

---

## 📝 Notes

- All timestamps are in ISO 8601 format with timezone
- Timezone for all operations: `Asia/Kolkata` (IST)
- Menu version increments on every create/update/delete operation
- Cache is automatically invalidated on menu changes
- Soft deletes are used for menu items (is_deleted flag)
- Authorization is enforced at all modification endpoints
- All endpoints require valid authentication (hardcoded userId for testing)

---

## 🚀 Quick Test Command Examples

```bash
# Test Vendor Registration
curl -X POST http://localhost:8082/api/v1/vendors \
  -H "Content-Type: application/json" \
  -d '{"userId":"550e8400-e29b-41d4-a716-446655440000","companyName":"Chai Express",...}'

# Test Branch Creation
curl -X POST http://localhost:8082/api/v1/branches/vendors/{vendorId} \
  -H "Content-Type: application/json" \
  -d '{"branchName":"Chai Express - Koramangala",...}'

# Test Menu Item Creation
curl -X POST http://localhost:8082/api/v1/menu-items/branches/{branchId} \
  -H "Content-Type: application/json" \
  -d '{"name":"Masala Tea","price":50.00,...}'

# Test Swagger UI
open http://localhost:8082/swagger-ui.html
```
