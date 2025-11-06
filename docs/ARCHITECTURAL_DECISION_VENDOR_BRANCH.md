# Architectural Decision: Unified Vendor-Branch Onboarding

## 📌 Decision Summary

**Vendor Registration and First Branch Onboarding should be a UNIFIED process, not separate steps.**

---

## 🤔 Problem with Original Design

The original design treated vendor registration and branch onboarding as two completely separate processes:

```
Step 1: Register Vendor (POST /api/v1/vendors)
        ↓
Step 2: Create Branch (POST /api/v1/branches/vendors/{vendorId})
        ↓
Step 3-8: Upload documents, images, set hours, etc.
```

### Issues:
1. **Conceptual Confusion:** When a vendor registers, they ARE registering their first branch. Separating these creates confusion.
2. **Incomplete State:** After vendor registration, the vendor has no branch and cannot operate.
3. **Extra API Calls:** Users need to make 2 API calls just to start the onboarding process.
4. **Inconsistent Onboarding Status:** Vendor and branch have separate onboarding statuses, making it unclear what "ready to operate" means.

---

## ✅ Solution: Unified Onboarding

**Combine vendor registration and first branch creation into a single API call:**

```
POST /api/v1/vendors/onboard
{
  "vendor": { ... },
  "firstBranch": { ... }
}
```

### Benefits:
1. **Clarity:** Vendor registration = First branch onboarding. No confusion.
2. **Completeness:** After one API call, vendor + branch exist and are ready for next steps.
3. **Efficiency:** One API call instead of two.
4. **Consistency:** Single onboarding status for vendor-branch pair.

---

## 🏗️ Revised Data Model

### Before (Confusing):
```
Vendor
├── Company info (PAN, GST)
├── Brand assets
└── Status: PENDING (but what does this mean? No branch yet!)

Branch
├── Location info
├── Operating hours
├── Menu items
└── Status: PENDING
```

### After (Clear):
```
Vendor (Company)
├── Company info (PAN, GST, brand assets)
├── Status: PENDING (waiting for first branch documents)
└── Branches: [
    {
      Branch 1 (Location)
      ├── Location info, operating hours, menu
      ├── Status: DOCUMENTS_SUBMITTED
      └── Documents: [FSSAI, GST, SHOP_ACT, ID_PROOF]
    },
    {
      Branch 2 (Location)
      ├── Location info, operating hours, menu
      ├── Status: APPROVED
      └── Documents: [...]
    }
  ]
```

---

## 🔄 Workflow Comparison

### Original Workflow (9 API Calls)
```
1. POST /api/v1/vendors                           → Create vendor
2. POST /api/v1/vendors/{vendorId}/images         → Upload vendor logo
3. POST /api/v1/branches/vendors/{vendorId}       → Create branch
4. POST /api/v1/branches/{branchId}/documents     → Upload FSSAI
5. POST /api/v1/branches/{branchId}/documents     → Upload GST
6. POST /api/v1/branches/{branchId}/documents     → Upload SHOP_ACT
7. POST /api/v1/branches/{branchId}/documents     → Upload ID_PROOF
8. POST /api/v1/branches/{branchId}/images        → Upload branch images
9. PUT /api/v1/branches/{branchId}/operating-hours → Set hours
```

### Revised Workflow (8 API Calls)
```
1. POST /api/v1/vendors/onboard                   → Create vendor + first branch (UNIFIED)
2. POST /api/v1/vendors/{vendorId}/images         → Upload vendor logo
3. POST /api/v1/branches/{branchId}/documents     → Upload FSSAI
4. POST /api/v1/branches/{branchId}/documents     → Upload GST
5. POST /api/v1/branches/{branchId}/documents     → Upload SHOP_ACT
6. POST /api/v1/branches/{branchId}/documents     → Upload ID_PROOF
7. POST /api/v1/branches/{branchId}/images        → Upload branch images
8. PUT /api/v1/branches/{branchId}/operating-hours → Set hours
```

**Savings:** 1 fewer API call + clearer intent

---

## 📊 Multi-Branch Expansion

After initial onboarding, adding more branches is straightforward:

```
POST /api/v1/branches/vendors/{vendorId}
{
  "branchName": "Chai Express - Indiranagar",
  "branchCode": "CE-IND-002",
  "address": { ... },
  "latitude": 12.9716,
  "longitude": 77.6412,
  "preferences": { ... }
}
```

Then repeat document upload, image upload, and hours setup for the new branch.

---

## 🎯 Key Principles

1. **Vendor = Company-level entity**
   - PAN, GST, brand name, logo
   - Created once per business
   - Shared across all branches

2. **Branch = Operational unit**
   - Address, coordinates, operating hours
   - Menu items (branch-specific)
   - Documents (location-specific)
   - Can be created multiple times per vendor

3. **First Branch = Vendor Registration**
   - When registering a vendor, you're registering their first branch
   - Unified API call for clarity and efficiency

4. **Additional Branches = Branch Creation**
   - Simpler process, no vendor data needed
   - Same document and image upload process

---

## 🔧 Implementation Changes Required

### 1. Create New Unified Endpoint
```java
@PostMapping("/onboard")
public ResponseEntity<VendorOnboardingResponse> onboardVendor(
    @Valid @RequestBody VendorOnboardingRequest request) {
    // Create vendor
    // Create first branch
    // Return both IDs
}
```

### 2. Update Vendor Response DTO
```java
@Data
public class VendorResponse {
    private UUID vendorId;
    private String companyName;
    private String brandName;
    private List<BranchResponse> branches;  // NEW: Include all branches
    private String onboardingStatus;
    // ... other fields
}
```

### 3. Update Branch Response DTO
```java
@Data
public class BranchResponse {
    private UUID branchId;
    private UUID vendorId;
    private String branchName;
    private String onboardingStatus;
    // ... other fields
}
```

### 4. Service Layer Changes
```java
public VendorOnboardingResponse onboardVendor(
    VendorOnboardingRequest request, UUID userId) {
    
    // 1. Create vendor
    Vendor vendor = vendorService.registerVendor(request.getVendor(), userId);
    
    // 2. Create first branch
    VendorBranch firstBranch = branchService.createBranch(
        vendor.getVendorId(), 
        request.getFirstBranch(), 
        userId
    );
    
    // 3. Return combined response
    return VendorOnboardingResponse.builder()
        .vendorId(vendor.getVendorId())
        .branchId(firstBranch.getBranchId())
        .companyName(vendor.getCompanyName())
        .firstBranchName(firstBranch.getBranchName())
        .onboardingStatus("PENDING")
        .build();
}
```

---

## 📋 API Endpoint Changes

### Removed/Deprecated
- None (backward compatibility maintained)

### New
- `POST /api/v1/vendors/onboard` - Unified vendor + first branch registration

### Unchanged
- `GET /api/v1/vendors/{vendorId}` - Now returns branches array
- `POST /api/v1/branches/vendors/{vendorId}` - For additional branches
- All branch, menu, document endpoints - Unchanged

---

## ✨ Benefits Summary

| Aspect | Original | Revised | Benefit |
|--------|----------|---------|---------|
| **Clarity** | Confusing separation | Unified process | Users understand vendor = first branch |
| **Efficiency** | 9 API calls | 8 API calls | 11% fewer calls |
| **Completeness** | Vendor without branch | Vendor with branch | Immediately operational |
| **Consistency** | Separate statuses | Single status | Clear onboarding progress |
| **Scalability** | Same for all branches | Different for first/additional | Simpler multi-branch expansion |

---

## 🚀 Migration Path

1. **Phase 1:** Keep old endpoints, add new unified endpoint
2. **Phase 2:** Update documentation and examples
3. **Phase 3:** Deprecate old vendor registration endpoint
4. **Phase 4:** Remove old endpoint in next major version

---

## 📚 Related Documents

- `VENDOR_ONBOARDING_REVISED.md` - Complete revised workflow with examples
- `VENDOR_BRANCH_BUSINESS_FLOW.md` - Original (deprecated) business flow

---

## ✅ Decision Status

**APPROVED** - Implement unified vendor-branch onboarding

**Rationale:**
- Clearer business logic
- Better user experience
- More efficient API usage
- Maintains backward compatibility
