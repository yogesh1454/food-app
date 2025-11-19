# Vendor & Branch Onboarding Use Cases

**Document Version:** 1.0  
**Last Updated:** November 8, 2025  
**Service:** Order Catalog Service (Port 8082)

---

## Table of Contents

1. [Vendor Onboarding Use Cases](#vendor-onboarding-use-cases)
2. [Branch Onboarding Use Cases](#branch-onboarding-use-cases)
3. [Document Management Use Cases](#document-management-use-cases)
4. [Branch Operations Use Cases](#branch-operations-use-cases)
5. [Error Handling Use Cases](#error-handling-use-cases)
6. [Authorization Use Cases](#authorization-use-cases)

---

## Vendor Onboarding Use Cases

### UC-V001: Register New Vendor

**Actor:** Restaurant Owner/Manager  
**Preconditions:** 
- User is authenticated
- User does not have an existing vendor account

**Main Flow:**
1. User provides company details:
   - Company name (required)
   - Brand name (optional)
   - Legal entity name (optional)
   - Company email (required, unique)
   - Company phone (required, 10 digits)
   - PAN number (optional, format: ABCDE1234F)
   - GST number (optional, format: 29ABCDE1234F1Z5)

2. System validates input:
   - Email format and uniqueness
   - Phone number format (10 digits)
   - PAN format (if provided)
   - GST format (if provided)

3. System creates vendor record with:
   - Generated vendor_id (UUID)
   - User_id from authenticated session
   - Status: PENDING
   - Empty images map
   - Empty metadata map
   - Empty tags array

4. System returns vendor details with vendor_id

**Postconditions:**
- Vendor record created in database
- Vendor status is PENDING
- User can proceed to upload brand assets

**Alternative Flows:**
- **A1:** Email already exists → Return 409 Conflict
- **A2:** Invalid PAN format → Return 400 Bad Request
- **A3:** Invalid GST format → Return 400 Bad Request
- **A4:** Invalid phone format → Return 400 Bad Request

**API Endpoint:**
```
POST /api/v1/vendors
```

**Example Request:**
```json
{
  "companyName": "Chai Express",
  "brandName": "Chai Express - Premium Tea",
  "legalEntityName": "Chai Express Private Limited",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "9876543210",
  "panNumber": "ABCDE1234F",
  "gstNumber": "29ABCDE1234F1Z5"
}
```

---

### UC-V002: Upload Vendor Brand Assets

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Vendor account exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User selects file type (logo, cover_photo, brand_assets)
2. User uploads file
3. System validates:
   - Vendor exists
   - User owns vendor
   - File type is valid
4. System uploads file to S3 (mocked in current implementation)
5. System updates vendor images JSONB with file URL
6. System returns updated vendor details

**Postconditions:**
- Vendor images map updated with new file URL
- File accessible via returned URL

**Alternative Flows:**
- **A1:** Vendor not found → Return 404 Not Found
- **A2:** User not authorized → Return 403 Forbidden
- **A3:** Invalid file type → Return 400 Bad Request

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/upload?target=vendor&fileType=logo
```

**Supported File Types:**
- `logo` - Company logo
- `cover_photo` - Cover/banner image
- `brand_assets` - Additional brand materials

---

### UC-V003: Get Vendor Details

**Actor:** Restaurant Owner/Manager, Admin  
**Preconditions:**
- Vendor exists

**Main Flow:**
1. User requests vendor details by vendor_id
2. System retrieves vendor record
3. System includes all associated branches
4. System returns complete vendor information

**Postconditions:**
- Vendor details returned with all branches

**Alternative Flows:**
- **A1:** Vendor not found → Return 404 Not Found

**API Endpoint:**
```
GET /api/v1/vendors/{vendorId}
```

**Response Includes:**
- Vendor details
- All branches (summary)
- Images map
- Metadata
- Tags
- Timestamps

---

### UC-V004: Update Vendor Information

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Vendor exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User provides updated fields:
   - Company name
   - Brand name
   - Company phone
   - Metadata
   - Tags
2. System validates authorization
3. System updates only provided fields
4. System returns updated vendor details

**Postconditions:**
- Vendor record updated
- updated_at timestamp refreshed

**Alternative Flows:**
- **A1:** Vendor not found → Return 404 Not Found
- **A2:** User not authorized → Return 403 Forbidden
- **A3:** Invalid data format → Return 400 Bad Request

**API Endpoint:**
```
PUT /api/v1/vendors/{vendorId}
```

---

## Branch Onboarding Use Cases

### UC-B001: Create First Branch for Vendor

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Vendor account exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User provides branch details:
   - Branch name (required)
   - Branch code (optional, auto-generated if not provided)
   - Display name (optional)
   - City (required)
   - Address (required, JSONB)
   - Latitude (optional)
   - Longitude (optional)
   - Branch phone (optional)
   - Branch email (optional)
   - Branch manager name (optional)
   - Branch manager phone (optional)
   - Preferences (optional, uses defaults if not provided)
   - Operating hours (optional, uses defaults if not provided)

2. System validates:
   - Vendor exists
   - User owns vendor
   - Required fields present
   - Address is valid JSONB
   - Lat/Long in valid range (if provided)
   - Phone format (if provided)
   - Email format (if provided)

3. System creates branch with:
   - Generated branch_id (UUID)
   - Generated or provided branch_code (unique)
   - Onboarding status: PENDING
   - is_active: false
   - is_open: false
   - Default preferences (if not provided)
   - Default operating hours (if not provided)
   - Empty images map
   - Empty metadata map
   - Menu version: 1

4. System returns branch details

**Postconditions:**
- Branch created and linked to vendor
- Branch status is PENDING
- Branch is inactive and closed
- Ready for document upload

**Alternative Flows:**
- **A1:** Vendor not found → Return 404 Not Found
- **A2:** User not authorized → Return 403 Forbidden
- **A3:** Branch code already exists → Return 409 Conflict
- **A4:** Invalid address format → Return 400 Bad Request
- **A5:** Invalid coordinates → Return 400 Bad Request

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/branches
```

**Example Request:**
```json
{
  "branchName": "Chai Express - Koramangala",
  "branchCode": "CE-KOR-001",
  "displayName": "Chai Express Koramangala",
  "city": "Bangalore",
  "address": {
    "street": "456 Koramangala 1st Block",
    "landmark": "Near Sony Signal",
    "area": "Koramangala",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560034",
    "country": "India"
  },
  "latitude": 12.9352,
  "longitude": 77.6245,
  "branchPhone": "9876543211",
  "branchEmail": "koramangala@chaiexpress.com",
  "branchManagerName": "Rajesh Kumar",
  "branchManagerPhone": "9876543212",
  "preferences": {
    "auto_accept_orders": true,
    "max_orders_per_hour": 60,
    "delivery_radius_km": 5,
    "min_order_value": 100,
    "accepts_cash": true,
    "accepts_online_payment": true,
    "packing_time_minutes": 15,
    "commission_rate": 18.0,
    "priority_delivery": false
  },
  "operatingHours": {
    "MONDAY": [{"open": "08:00", "close": "22:00"}],
    "TUESDAY": [{"open": "08:00", "close": "22:00"}],
    "WEDNESDAY": [{"open": "08:00", "close": "22:00"}],
    "THURSDAY": [{"open": "08:00", "close": "22:00"}],
    "FRIDAY": [{"open": "08:00", "close": "23:00"}],
    "SATURDAY": [{"open": "09:00", "close": "23:00"}],
    "SUNDAY": [{"open": "09:00", "close": "22:00"}]
  }
}
```

---

### UC-B002: Create Additional Branch for Vendor

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Vendor account exists
- At least one branch already exists
- User is authenticated and owns the vendor

**Main Flow:**
- Same as UC-B001 (Create First Branch)
- System uses same endpoint for consistency

**Key Point:**
- **Same API endpoint** for 1st, 2nd, 3rd... branches
- No special handling needed for "first" vs "additional"
- UI can use same component/form

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/branches
```

---

### UC-B003: Update Branch Information

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User provides updated fields (any combination):
   - Branch name
   - Branch code
   - Display name
   - Address
   - Latitude/Longitude
   - City
   - Contact details
   - Manager details
   - Preferences
   - Operating hours

2. System validates:
   - Branch exists
   - Branch belongs to specified vendor
   - User owns vendor
   - Data formats are valid

3. System updates only provided fields
4. System returns updated branch details

**Postconditions:**
- Branch record updated
- updated_at timestamp refreshed

**Alternative Flows:**
- **A1:** Branch not found → Return 404 Not Found
- **A2:** Branch doesn't belong to vendor → Return 403 Forbidden
- **A3:** User not authorized → Return 403 Forbidden
- **A4:** Invalid data format → Return 400 Bad Request

**API Endpoint:**
```
PUT /api/v1/vendors/{vendorId}/branches/{branchId}
```

---

### UC-B004: Get Branch Details

**Actor:** Restaurant Owner/Manager, Customer, Admin  
**Preconditions:**
- Branch exists

**Main Flow:**
1. User requests branch details by branch_id
2. System retrieves branch record
3. System returns complete branch information

**Postconditions:**
- Branch details returned

**Alternative Flows:**
- **A1:** Branch not found → Return 404 Not Found

**API Endpoint:**
```
GET /api/v1/branches/{branchId}
```

**Response Includes:**
- Branch details
- Address (JSONB)
- Preferences (JSONB)
- Operating hours (JSONB)
- Images map
- Documents list
- Metrics (rating, orders, reviews)
- Status flags (is_active, is_open)

---

## Document Management Use Cases

### UC-D001: Upload Branch Image

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User selects image type (logo, cover_photo, storefront, interior, kitchen, gallery)
2. User uploads image file
3. System validates:
   - Branch exists
   - User owns vendor
   - Image type is valid
4. System uploads file to S3 (mocked)
5. System updates branch images JSONB
6. System returns updated branch details

**Postconditions:**
- Branch images map updated
- Image accessible via URL

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/upload?target=branch&branchId={branchId}&fileType=storefront
```

**Supported Image Types:**
- `logo` - Branch logo
- `cover_photo` - Cover image
- `storefront` - Store exterior photo
- `interior` - Interior photos (array)
- `kitchen` - Kitchen photos (array)
- `gallery` - General gallery (array)

---

### UC-D002: Upload Branch Document

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User selects document type (fssai, gst, shop_act, id_proof, trade_license)
2. User provides document details:
   - Document number
   - Issue date (optional)
   - Expiry date (optional)
3. User uploads document file
4. System validates:
   - Branch exists
   - User owns vendor
   - Document type is valid
5. System uploads file to S3 (mocked)
6. System creates BranchDocument record:
   - Document type
   - Document URL
   - Document number
   - Issue/expiry dates
   - Verification status: PENDING
7. System checks if all required documents uploaded
8. If all required docs uploaded, updates branch onboarding_status to DOCUMENTS_SUBMITTED
9. System returns updated branch details

**Postconditions:**
- Document record created
- Branch onboarding status may be updated
- Document pending verification

**Required Documents:**
- FSSAI License
- GST Certificate
- Shop Act License
- ID Proof

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/upload?target=branch&branchId={branchId}&fileType=fssai&documentNumber=12345678901234&issueDate=2023-01-15&expiryDate=2026-01-15
```

---

### UC-D003: View Branch Documents

**Actor:** Restaurant Owner/Manager, Admin  
**Preconditions:**
- Branch exists
- User is authenticated and authorized

**Main Flow:**
1. User requests documents for branch
2. System validates authorization
3. System retrieves all documents for branch
4. System returns document list with:
   - Document type
   - Document URL
   - Verification status
   - Issue/expiry dates

**Postconditions:**
- Document list returned

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/documents
```

---

### UC-D004: Check Onboarding Status

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User requests onboarding status
2. System validates authorization
3. System calculates:
   - Total documents uploaded
   - Approved documents count
   - Missing required documents
   - Current onboarding status
4. System returns status summary

**Postconditions:**
- Status information returned

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/onboarding-status
```

**Response Example:**
```json
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "onboardingStatus": "DOCUMENTS_SUBMITTED",
  "isActive": false,
  "totalDocuments": 4,
  "approvedDocuments": 0,
  "documents": [
    {"type": "FSSAI", "status": "PENDING"},
    {"type": "GST", "status": "PENDING"},
    {"type": "SHOP_ACT", "status": "PENDING"},
    {"type": "ID_PROOF", "status": "PENDING"}
  ]
}
```

---

## Branch Operations Use Cases

### UC-O001: Toggle Branch Online/Offline Status

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists and is active
- User is authenticated and owns the vendor

**Main Flow:**
1. User requests to toggle branch status (open/close)
2. System validates:
   - Branch exists
   - Branch is active (approved)
   - User owns vendor
3. System toggles is_open flag
4. System returns updated branch status

**Postconditions:**
- Branch is_open status updated
- Customers can/cannot place orders

**API Endpoint:**
```
PUT /api/v1/branches/{branchId}/status
```

**Request Body:**
```json
{
  "isOpen": true
}
```

---

### UC-O002: Check Branch Availability

**Actor:** Customer, System  
**Preconditions:**
- Branch exists

**Main Flow:**
1. System checks:
   - Branch is_active flag
   - Branch is_open flag
   - Current time vs operating hours
   - Current day of week
2. System calculates:
   - Is branch currently accepting orders?
   - Next opening time (if closed)
   - Closing time (if open)
3. System returns availability status

**Postconditions:**
- Availability status returned

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/availability
```

**Response Example:**
```json
{
  "branchId": "be1bff1a-bab0-48cd-b758-200b43efd101",
  "isAvailable": true,
  "isActive": true,
  "isOpen": true,
  "currentTime": "2025-11-08T14:30:00Z",
  "opensAt": "08:00",
  "closesAt": "22:00",
  "message": "Open now, closes at 22:00"
}
```

---

## Error Handling Use Cases

### UC-E001: Handle Duplicate Vendor Email

**Trigger:** User attempts to register with existing email  
**Response:** 409 Conflict  
**Message:** "Email already registered"

---

### UC-E002: Handle Duplicate Branch Code

**Trigger:** User provides branch code that already exists  
**Response:** 409 Conflict  
**Message:** "Branch code already exists"

---

### UC-E003: Handle Invalid PAN Format

**Trigger:** User provides invalid PAN number  
**Response:** 400 Bad Request  
**Message:** "Invalid PAN format (e.g., ABCDE1234F)"

---

### UC-E004: Handle Invalid GST Format

**Trigger:** User provides invalid GST number  
**Response:** 400 Bad Request  
**Message:** "Invalid GST format (e.g., 29ABCDE1234F1Z5)"

---

### UC-E005: Handle Vendor Not Found

**Trigger:** Request with non-existent vendor_id  
**Response:** 404 Not Found  
**Message:** "Vendor not found"

---

### UC-E006: Handle Branch Not Found

**Trigger:** Request with non-existent branch_id  
**Response:** 404 Not Found  
**Message:** "Branch not found"

---

### UC-E007: Handle Unauthorized Access

**Trigger:** User attempts to modify vendor/branch they don't own  
**Response:** 403 Forbidden  
**Message:** "Not authorized to update this vendor/branch"

---

### UC-E008: Handle Branch-Vendor Mismatch

**Trigger:** Branch doesn't belong to specified vendor  
**Response:** 403 Forbidden  
**Message:** "Branch does not belong to this vendor"

---

## Authorization Use Cases

### UC-A001: Verify Vendor Ownership

**Flow:**
1. System extracts user_id from authentication token
2. System retrieves vendor record
3. System compares vendor.user_id with authenticated user_id
4. If match → Allow operation
5. If no match → Return 403 Forbidden

**Applied To:**
- Update vendor
- Upload vendor images
- Create branch
- Update branch
- Upload branch files

---

### UC-A002: Verify Branch Ownership (via Vendor)

**Flow:**
1. System extracts user_id from authentication token
2. System retrieves branch record
3. System retrieves associated vendor
4. System compares vendor.user_id with authenticated user_id
5. If match → Allow operation
6. If no match → Return 403 Forbidden

**Applied To:**
- Update branch
- Upload branch files
- Toggle branch status
- View documents

---

## Onboarding Status Flow

### Status Progression:

```
PENDING
   ↓
DOCUMENTS_SUBMITTED (all 4 required docs uploaded)
   ↓
UNDER_VERIFICATION (admin reviewing)
   ↓
APPROVED / REJECTED
   ↓
ACTIVE (if approved, is_active = true)
   ↓
SUSPENDED (if violations occur)
```

### Required Documents for DOCUMENTS_SUBMITTED:
1. FSSAI License
2. GST Certificate
3. Shop Act License
4. ID Proof

---

## Complete Onboarding Journey

### Happy Path:

1. **UC-V001:** Register vendor → Get vendor_id
2. **UC-V002:** Upload vendor logo
3. **UC-B001:** Create first branch → Get branch_id
4. **UC-D002:** Upload FSSAI document
5. **UC-D002:** Upload GST document
6. **UC-D002:** Upload Shop Act document
7. **UC-D002:** Upload ID Proof document
   - Status auto-updates to DOCUMENTS_SUBMITTED
8. **UC-D001:** Upload branch storefront image
9. **UC-D001:** Upload branch interior images
10. **UC-D004:** Check onboarding status
11. **Admin:** Reviews and approves documents
    - Status updates to APPROVED
    - is_active set to true
12. **UC-O001:** Owner opens branch (is_open = true)
13. **Branch is live!** Customers can place orders

### Total API Calls: 10-12 calls for complete onboarding

---

## Testing Scenarios

### Scenario 1: Complete Vendor Onboarding
```
1. POST /api/v1/vendors ✅
2. POST /api/v1/vendors/{id}/upload?target=vendor&fileType=logo ✅
3. POST /api/v1/vendors/{id}/branches ✅
4. POST /api/v1/vendors/{id}/upload?target=branch&branchId={id}&fileType=fssai&... ✅
5. POST /api/v1/vendors/{id}/upload?target=branch&branchId={id}&fileType=gst&... ✅
6. POST /api/v1/vendors/{id}/upload?target=branch&branchId={id}&fileType=shop_act&... ✅
7. POST /api/v1/vendors/{id}/upload?target=branch&branchId={id}&fileType=id_proof&... ✅
8. GET /api/v1/branches/{id}/onboarding-status ✅
```

### Scenario 2: Multi-Branch Vendor
```
1. POST /api/v1/vendors ✅
2. POST /api/v1/vendors/{id}/branches (Branch 1) ✅
3. POST /api/v1/vendors/{id}/branches (Branch 2) ✅ (Same endpoint!)
4. POST /api/v1/vendors/{id}/branches (Branch 3) ✅ (Same endpoint!)
5. GET /api/v1/vendors/{id} (Returns all 3 branches) ✅
```

### Scenario 3: Error Handling
```
1. POST /api/v1/vendors (duplicate email) → 409 ❌
2. POST /api/v1/vendors (invalid PAN) → 400 ❌
3. POST /api/v1/vendors (invalid GST) → 400 ❌
4. POST /api/v1/vendors/{invalid-id}/branches → 404 ❌
5. PUT /api/v1/vendors/{id} (unauthorized user) → 403 ❌
```

### Scenario 4: Authorization
```
1. User A creates vendor ✅
2. User B tries to update vendor → 403 ❌
3. User A creates branch ✅
4. User B tries to update branch → 403 ❌
```

---

## API Endpoint Summary

### Vendor Endpoints (3)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| POST | `/api/v1/vendors` | UC-V001 |
| GET | `/api/v1/vendors/{vendorId}` | UC-V003 |
| PUT | `/api/v1/vendors/{vendorId}` | UC-V004 |

### Branch Endpoints (4)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| POST | `/api/v1/vendors/{vendorId}/branches` | UC-B001, UC-B002 |
| GET | `/api/v1/branches/{branchId}` | UC-B004 |
| PUT | `/api/v1/vendors/{vendorId}/branches/{branchId}` | UC-B003 |
| GET | `/api/v1/branches/{branchId}/availability` | UC-O002 |

### Upload Endpoint (1 - Unified)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| POST | `/api/v1/vendors/{vendorId}/upload` | UC-V002, UC-D001, UC-D002 |

### Operations Endpoints (1)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| PUT | `/api/v1/branches/{branchId}/status` | UC-O001 |

**Total: 9 endpoints covering 20+ use cases**

---

## Notes

- All timestamps are in UTC with timezone
- All IDs are UUIDs
- JSONB fields allow flexible schema evolution
- Soft deletes not implemented (hard deletes via CASCADE)
- File uploads are mocked (S3 URLs generated, not actual uploads)
- Authentication uses hardcoded userId (to be replaced with JWT in production)
- All monetary values in INR (Indian Rupees)
- Phone numbers are 10-digit Indian format
- PAN and GST follow Indian government formats

---

**End of Use Cases Document**
