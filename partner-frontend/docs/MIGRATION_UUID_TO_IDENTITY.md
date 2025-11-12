# Migration from UUID to IDENTITY (BIGSERIAL) - Vendor Module

**Date:** November 8, 2025  
**Reason:** Human-readable IDs for finite vendor/branch data  
**Status:** IN PROGRESS

---

## Rationale

For vendor and branch entities, the volume is finite and predictable. Human-readable sequential IDs (1, 2, 3...) provide better:
- **Operational visibility** - Easy to reference in support tickets
- **Debugging** - Simple to track in logs
- **Communication** - Easy to share between teams
- **URL friendliness** - Clean URLs like `/vendors/123` instead of `/vendors/550e8400-e29b-41d4-a716-446655440000`

---

## ✅ Completed Changes

### 1. Database Migrations
- ✅ **V1__Create_vendors_table.sql**
  - Changed `vendor_id UUID` → `vendor_id BIGSERIAL`
  - user_id remains UUID (references user-management-service)

- ✅ **V2__Create_vendor_branches_table.sql**
  - Changed `branch_id UUID` → `branch_id BIGSERIAL`
  - Changed `vendor_id UUID` → `vendor_id BIGINT` (FK reference)
  - verified_by remains UUID (references users)

### 2. Entity Models
- ✅ **Vendor.java**
  - Changed `private UUID vendorId` → `private Long vendorId`
  - Changed `@GeneratedValue(strategy = GenerationType.UUID)` → `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - user_id remains UUID

- ✅ **VendorBranch.java**
  - Changed `private UUID branchId` → `private Long branchId`
  - Changed `@GeneratedValue(strategy = GenerationType.UUID)` → `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - verified_by remains UUID

### 3. DTOs
- ✅ **VendorResponse.java**
  - Changed `private UUID vendorId` → `private Long vendorId`
  - Removed unused UUID import

- ✅ **BranchResponse.java**
  - Changed `private UUID branchId` → `private Long branchId`
  - Changed `private UUID vendorId` → `private Long vendorId`
  - Removed unused UUID import

### 4. Controllers
- ✅ **VendorController.java**
  - Changed all `@PathVariable UUID vendorId` → `@PathVariable Long vendorId`
  - Changed `@RequestParam(required = false) UUID branchId` → `@RequestParam(required = false) Long branchId`

- ✅ **BranchController.java**
  - Changed all `@PathVariable UUID vendorId` → `@PathVariable Long vendorId`
  - Changed all `@PathVariable UUID branchId` → `@PathVariable Long branchId`
  - Fixed endpoint: `/branches/{branchId}` → `/branches/{branchId}` (already correct)

---

## ⏳ Remaining Changes (TO BE COMPLETED)

### 5. Services (CRITICAL - BLOCKING COMPILATION)
- ❌ **VendorService.java**
  - Update `getVendor(UUID vendorId)` → `getVendor(Long vendorId)`
  - Update `updateVendor(UUID vendorId, ...)` → `updateVendor(Long vendorId, ...)`
  - Update `uploadVendorImage(UUID vendorId, ...)` → `uploadVendorImage(Long vendorId, ...)`

- ❌ **BranchOnboardingService.java**
  - Update `createBranch(UUID vendorId, ...)` → `createBranch(Long vendorId, ...)`
  - Update `getBranch(UUID branchId)` → `getBranch(Long branchId)`
  - Update `updateBranch(UUID vendorId, UUID branchId, ...)` → `updateBranch(Long vendorId, Long branchId, ...)`
  - Update `uploadBranchImage(UUID branchId, ...)` → `uploadBranchImage(Long branchId, ...)`
  - Update `uploadBranchDocument(UUID branchId, ...)` → `uploadBranchDocument(Long branchId, ...)`
  - Update `generateBranchCode(UUID vendorId, ...)` → `generateBranchCode(Long vendorId, ...)`

- ❌ **BranchAvailabilityService.java**
  - Update `toggleBranchStatus(UUID branchId, ...)` → `toggleBranchStatus(Long branchId, ...)`
  - Update `checkAvailability(UUID branchId)` → `checkAvailability(Long branchId)`
  - Update `updateOperatingHours(UUID branchId, ...)` → `updateOperatingHours(Long branchId, ...)`

### 6. Repositories
- ❌ **VendorRepository.java**
  - Change `JpaRepository<Vendor, UUID>` → `JpaRepository<Vendor, Long>`
  - Update all method signatures using UUID → Long

- ❌ **VendorBranchRepository.java**
  - Change `JpaRepository<VendorBranch, UUID>` → `JpaRepository<VendorBranch, Long>`
  - Update all method signatures using UUID → Long

### 7. Mappers
- ❌ **VendorMapper.java**
  - Update any UUID type handling → Long

- ❌ **BranchMapper.java**
  - Update any UUID type handling → Long

### 8. Other DTOs
- ❌ **DocumentResponse.java**
  - Change `private UUID branchId` → `private Long branchId`

- ❌ **BranchAvailabilityResponse.java**
  - Change `private UUID branchId` → `private Long branchId`

### 9. Menu Module Integration
- ❌ **MenuItemResponse.java**
  - Change `private UUID branchId` → `private Long branchId`

- ❌ **MenuService.java**
  - Update cache eviction methods to use Long instead of UUID

- ❌ **MenuCacheService.java**
  - Update `evictBranchMenu(UUID branchId)` → `evictBranchMenu(Long branchId)`
  - Update `evictPopularItems(UUID branchId)` → `evictPopularItems(Long branchId)`

### 10. Testing
- ❌ Update all test files to use Long instead of UUID
- ❌ Update test data fixtures
- ❌ Update integration tests

---

## Breaking Changes

### API Endpoints (BREAKING)
**Before:**
```
GET /api/v1/vendors/550e8400-e29b-41d4-a716-446655440000
POST /api/v1/vendors/550e8400-e29b-41d4-a716-446655440000/branches
```

**After:**
```
GET /api/v1/vendors/1
POST /api/v1/vendors/1/branches
```

### Database Schema (BREAKING)
- Requires database migration/recreation
- Existing UUID data cannot be directly converted to BIGSERIAL
- **Action Required:** Drop and recreate tables OR create migration script to preserve data

---

## Migration Strategy

### Option 1: Fresh Start (RECOMMENDED for Development)
1. Drop existing vendor/branch tables
2. Run updated Flyway migrations
3. Data will start from ID 1

### Option 2: Data Preservation (Production)
1. Create new tables with BIGSERIAL
2. Migrate data with ID mapping
3. Update foreign key references
4. Switch over atomically

---

## Current Compilation Errors

**Total Errors:** ~15-20 compilation errors

**Categories:**
1. Service method signatures (UUID → Long)
2. Repository type parameters (UUID → Long)
3. Mapper type handling
4. Cache service method signatures
5. Menu module integration

---

## Next Steps

1. ✅ Update all service methods (VendorService, BranchOnboardingService, BranchAvailabilityService)
2. ✅ Update all repositories
3. ✅ Update all mappers
4. ✅ Update remaining DTOs
5. ✅ Update menu module integration
6. ✅ Run full build and fix any remaining errors
7. ✅ Update tests
8. ✅ Test end-to-end functionality

---

## Benefits After Migration

### Human-Readable IDs
```
Vendor ID: 1, 2, 3, 4...
Branch ID: 1, 2, 3, 4...
```

### Cleaner URLs
```
GET /api/v1/vendors/123
GET /api/v1/vendors/123/branches
POST /api/v1/vendors/123/branches
```

### Better Logging
```
[INFO] Creating branch for vendor: 123
[INFO] Branch created: 456
```

### Easier Debugging
```
Support: "Check vendor 123, branch 456"
Developer: *immediately finds it*
```

### Storage Savings
- UUID: 16 bytes per ID
- BIGINT: 8 bytes per ID
- **50% reduction** in ID storage
- Smaller indexes, faster queries

---

## Estimated Completion Time

- Remaining work: 2-3 hours
- Testing: 1 hour
- **Total:** 3-4 hours

---

## Notes

- user_id remains UUID (references external user-management-service)
- verified_by remains UUID (references users)
- Only vendor_id and branch_id changed to BIGSERIAL
- This is a one-way migration (cannot easily revert)

---

**Status:** Paused at service layer updates
**Next Action:** Update all service method signatures to use Long instead of UUID
