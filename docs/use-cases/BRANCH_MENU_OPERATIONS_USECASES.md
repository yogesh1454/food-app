# Branch Menu Operations Use Cases

**Document Version:** 1.0  
**Last Updated:** November 8, 2025  
**Service:** Order Catalog Service (Port 8082)

---

## Table of Contents

1. [Menu Item Management Use Cases](#menu-item-management-use-cases)
2. [Menu Browsing Use Cases](#menu-browsing-use-cases)
3. [Menu Availability Use Cases](#menu-availability-use-cases)
4. [Menu Versioning Use Cases](#menu-versioning-use-cases)
5. [Error Handling Use Cases](#error-handling-use-cases)
6. [Authorization Use Cases](#authorization-use-cases)

---

## Menu Item Management Use Cases

### UC-M001: Create Menu Item

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists and is active
- User is authenticated and owns the vendor

**Main Flow:**
1. User provides menu item details:
   - Name (required, max 255 chars)
   - Description (optional, text)
   - Price (required, decimal 10,2)
   - Category (required, max 100 chars)
   - Preparation time in minutes (optional)
   - Images (optional, JSONB)
   - Metadata (optional, JSONB)
   - Tags (optional, array)
   - Is available (optional, default: true)

2. System validates:
   - Branch exists
   - Branch belongs to vendor
   - User owns vendor
   - Required fields present
   - Price is positive
   - Category is valid

3. System creates menu item with:
   - Generated menu_item_id (Long, auto-increment)
   - Branch association
   - is_available: true (default)
   - is_deleted: false
   - Timestamps (created_at, updated_at)

4. System increments branch menu_version
5. System returns menu item details

**Postconditions:**
- Menu item created and linked to branch
- Branch menu version incremented
- Item available for ordering (if is_available = true)

**Alternative Flows:**
- **A1:** Branch not found → Return 404 Not Found
- **A2:** User not authorized → Return 403 Forbidden
- **A3:** Invalid price (negative/zero) → Return 400 Bad Request
- **A4:** Missing required fields → Return 400 Bad Request

**API Endpoint:**
```
POST /api/v1/branches/{branchId}/menu-items
```

**Example Request:**
```json
{
  "name": "Masala Chai",
  "description": "Traditional Indian spiced tea with aromatic spices",
  "price": 20.00,
  "category": "Beverages",
  "preparationTimeMinutes": 5,
  "images": {
    "primary": "https://s3.amazonaws.com/menu/masala-chai-primary.jpg",
    "gallery": [
      "https://s3.amazonaws.com/menu/masala-chai-1.jpg",
      "https://s3.amazonaws.com/menu/masala-chai-2.jpg"
    ]
  },
  "metadata": {
    "spice_level": "medium",
    "serving_size": "150ml",
    "calories": 80,
    "allergens": ["milk"],
    "veg": true
  },
  "tags": ["hot", "popular", "traditional"],
  "isAvailable": true
}
```

**Example Response:**
```json
{
  "menuItemId": 1,
  "branchId": 101,
  "name": "Masala Chai",
  "description": "Traditional Indian spiced tea with aromatic spices",
  "price": 20.00,
  "category": "Beverages",
  "isAvailable": true,
  "preparationTimeMinutes": 5,
  "images": {
    "primary": "https://s3.amazonaws.com/menu/masala-chai-primary.jpg",
    "gallery": [...]
  },
  "metadata": {...},
  "tags": ["hot", "popular", "traditional"],
  "createdAt": "2025-11-08T14:30:00Z",
  "updatedAt": "2025-11-08T14:30:00Z"
}
```

---

### UC-M002: Get Menu Item Details

**Actor:** Restaurant Owner/Manager, Customer  
**Preconditions:**
- Menu item exists

**Main Flow:**
1. User requests menu item by menu_item_id
2. System retrieves menu item
3. System checks is_deleted flag
4. System returns menu item details

**Postconditions:**
- Menu item details returned

**Alternative Flows:**
- **A1:** Menu item not found → Return 404 Not Found
- **A2:** Menu item is deleted → Return 404 Not Found

**API Endpoint:**
```
GET /api/v1/menu-items/{menuItemId}
```

---

### UC-M003: Update Menu Item

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Menu item exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User provides updated fields (any combination):
   - Name
   - Description
   - Price
   - Category
   - Preparation time
   - Images
   - Metadata
   - Tags
   - Is available

2. System validates:
   - Menu item exists
   - Menu item not deleted
   - User owns vendor (via branch)
   - Data formats valid
   - Price positive (if provided)

3. System updates only provided fields
4. System increments branch menu_version
5. System invalidates cache
6. System returns updated menu item

**Postconditions:**
- Menu item updated
- Branch menu version incremented
- Cache invalidated
- updated_at timestamp refreshed

**Alternative Flows:**
- **A1:** Menu item not found → Return 404 Not Found
- **A2:** User not authorized → Return 403 Forbidden
- **A3:** Invalid price → Return 400 Bad Request

**API Endpoint:**
```
PUT /api/v1/menu-items/{menuItemId}
```

**Example Request:**
```json
{
  "price": 25.00,
  "isAvailable": false,
  "metadata": {
    "spice_level": "high",
    "seasonal": true
  }
}
```

---

### UC-M004: Delete Menu Item (Soft Delete)

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Menu item exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User requests to delete menu item
2. System validates:
   - Menu item exists
   - Menu item not already deleted
   - User owns vendor
3. System performs soft delete:
   - Sets is_deleted = true
   - Sets is_available = false
4. System increments branch menu_version
5. System invalidates cache
6. System returns success (204 No Content)

**Postconditions:**
- Menu item marked as deleted
- Item no longer visible in menu listings
- Item cannot be ordered
- Historical orders still reference item

**Alternative Flows:**
- **A1:** Menu item not found → Return 404 Not Found
- **A2:** User not authorized → Return 403 Forbidden

**API Endpoint:**
```
DELETE /api/v1/menu-items/{menuItemId}
```

**Note:** Soft delete preserves data integrity for historical orders

---

## Menu Browsing Use Cases

### UC-M005: Get Branch Menu (All Items)

**Actor:** Customer, Restaurant Owner/Manager  
**Preconditions:**
- Branch exists

**Main Flow:**
1. User requests menu for branch
2. System retrieves all menu items where:
   - branch_id matches
   - is_deleted = false
3. System optionally filters by:
   - Category (query param)
   - Availability (query param)
4. System applies pagination
5. System returns menu items list

**Postconditions:**
- Menu items list returned
- Pagination metadata included

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/menu-items?category=Beverages&availableOnly=true&page=0&size=20
```

**Query Parameters:**
- `category` (optional) - Filter by category
- `availableOnly` (optional, default: false) - Show only available items
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Items per page

**Example Response:**
```json
{
  "content": [
    {
      "menuItemId": 1,
      "name": "Masala Chai",
      "price": 20.00,
      "category": "Beverages",
      "isAvailable": true,
      ...
    },
    {
      "menuItemId": 2,
      "name": "Samosa",
      "price": 15.00,
      "category": "Snacks",
      "isAvailable": true,
      ...
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

---

### UC-M006: Get Menu by Category

**Actor:** Customer  
**Preconditions:**
- Branch exists

**Main Flow:**
1. User requests menu filtered by category
2. System retrieves items where:
   - branch_id matches
   - category matches
   - is_deleted = false
   - is_available = true (for customers)
3. System returns filtered list

**Postconditions:**
- Category-specific menu returned

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/menu-items?category=Beverages
```

**Common Categories:**
- Beverages
- Snacks
- Main Course
- Desserts
- Combos
- Specials

---

### UC-M007: Get Available Menu Items

**Actor:** Customer  
**Preconditions:**
- Branch exists and is open

**Main Flow:**
1. User requests available menu items
2. System retrieves items where:
   - branch_id matches
   - is_deleted = false
   - is_available = true
3. System returns available items

**Postconditions:**
- Only orderable items returned

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/menu-items?availableOnly=true
```

---

### UC-M008: Get Popular Menu Items

**Actor:** Customer  
**Preconditions:**
- Branch exists

**Main Flow:**
1. User requests popular items
2. System retrieves items:
   - Sorted by order count (from metadata)
   - Filtered by is_available = true
   - Limited to top N items
3. System returns popular items

**Postconditions:**
- Popular items list returned

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/menu-items/popular?limit=10
```

---

## Menu Availability Use Cases

### UC-M009: Toggle Menu Item Availability

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Menu item exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User requests to toggle availability
2. System validates authorization
3. System updates is_available flag
4. System increments menu_version
5. System invalidates cache
6. System returns updated item

**Postconditions:**
- Item availability updated
- Customers see updated availability

**API Endpoint:**
```
PUT /api/v1/menu-items/{menuItemId}
```

**Request Body:**
```json
{
  "isAvailable": false
}
```

**Use Cases:**
- Out of stock
- Seasonal items
- Time-based availability
- Temporary unavailability

---

### UC-M010: Bulk Update Availability

**Actor:** Restaurant Owner/Manager  
**Preconditions:**
- Branch exists
- User is authenticated and owns the vendor

**Main Flow:**
1. User provides list of item IDs and availability status
2. System validates:
   - All items belong to same branch
   - User owns vendor
3. System updates all items
4. System increments menu_version once
5. System invalidates cache
6. System returns success

**Postconditions:**
- Multiple items updated atomically
- Single menu version increment

**API Endpoint:**
```
PUT /api/v1/branches/{branchId}/menu-items/bulk-availability
```

**Request Body:**
```json
{
  "updates": [
    {"menuItemId": 1, "isAvailable": false},
    {"menuItemId": 2, "isAvailable": false},
    {"menuItemId": 3, "isAvailable": true}
  ]
}
```

---

## Menu Versioning Use Cases

### UC-M011: Get Menu Version

**Actor:** Customer App, System  
**Preconditions:**
- Branch exists

**Main Flow:**
1. Client requests current menu version
2. System returns branch menu_version
3. Client compares with cached version
4. If different, client fetches fresh menu

**Postconditions:**
- Version number returned
- Client can decide to refresh cache

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/menu-version
```

**Response:**
```json
{
  "branchId": 101,
  "menuVersion": 42,
  "lastUpdated": "2025-11-08T14:30:00Z"
}
```

**Use Case:**
- Cache invalidation strategy
- Optimistic UI updates
- Reduce unnecessary API calls

---

### UC-M012: Check Menu Updates

**Actor:** Customer App  
**Preconditions:**
- Client has cached menu with version

**Main Flow:**
1. Client sends current cached version
2. System compares with latest version
3. If versions match → Return 304 Not Modified
4. If versions differ → Return 200 with fresh menu

**Postconditions:**
- Efficient cache validation
- Reduced bandwidth usage

**API Endpoint:**
```
GET /api/v1/branches/{branchId}/menu-items
Headers: If-None-Match: "version-41"
```

**Response:**
- 304 Not Modified (if version matches)
- 200 OK with menu (if version changed)

---

## Error Handling Use Cases

### UC-E001: Handle Menu Item Not Found

**Trigger:** Request with non-existent menu_item_id  
**Response:** 404 Not Found  
**Message:** "Menu item not found"

---

### UC-E002: Handle Deleted Menu Item Access

**Trigger:** Request for soft-deleted menu item  
**Response:** 404 Not Found  
**Message:** "Menu item not found"

**Note:** Soft-deleted items treated as non-existent for API consumers

---

### UC-E003: Handle Invalid Price

**Trigger:** Create/update with negative or zero price  
**Response:** 400 Bad Request  
**Message:** "Price must be greater than zero"

---

### UC-E004: Handle Missing Required Fields

**Trigger:** Create menu item without name, price, or category  
**Response:** 400 Bad Request  
**Message:** "Validation failed"  
**Details:**
```json
{
  "validationErrors": {
    "name": "must not be blank",
    "price": "must not be null",
    "category": "must not be blank"
  }
}
```

---

### UC-E005: Handle Unauthorized Menu Modification

**Trigger:** User attempts to modify menu item of branch they don't own  
**Response:** 403 Forbidden  
**Message:** "Not authorized to modify this menu item"

---

### UC-E006: Handle Branch Not Found

**Trigger:** Create menu item for non-existent branch  
**Response:** 404 Not Found  
**Message:** "Branch not found"

---

## Authorization Use Cases

### UC-A001: Verify Menu Item Ownership

**Flow:**
1. System extracts user_id from authentication
2. System retrieves menu item
3. System retrieves associated branch
4. System retrieves associated vendor
5. System compares vendor.user_id with authenticated user_id
6. If match → Allow operation
7. If no match → Return 403 Forbidden

**Applied To:**
- Update menu item
- Delete menu item
- Toggle availability
- Bulk updates

---

### UC-A002: Public Menu Access

**Flow:**
1. Customer requests menu (no authentication required)
2. System returns only:
   - is_deleted = false
   - is_available = true (for customers)
3. Full menu visible to owners (including unavailable items)

**Applied To:**
- Get branch menu
- Get menu item details
- Browse by category

---

## Complete Menu Management Journey

### Happy Path:

1. **UC-M001:** Create "Masala Chai" → Get menuItemId: 1
2. **UC-M001:** Create "Samosa" → Get menuItemId: 2
3. **UC-M001:** Create "Vada Pav" → Get menuItemId: 3
4. **UC-M005:** Get full menu → Returns 3 items
5. **UC-M009:** Mark "Samosa" unavailable (out of stock)
6. **UC-M007:** Get available items → Returns 2 items (Chai, Vada Pav)
7. **UC-M003:** Update "Masala Chai" price to 25.00
8. **UC-M011:** Check menu version → Version incremented to 4
9. Customer places order for available items
10. **UC-M009:** Mark "Samosa" available again
11. **UC-M004:** Delete "Vada Pav" (discontinued)
12. **UC-M005:** Get full menu → Returns 2 items (Chai, Samosa)

### Total Operations: 12 menu management actions

---

## Testing Scenarios

### Scenario 1: Complete Menu Setup
```
1. POST /api/v1/branches/101/menu-items (Masala Chai) ✅
2. POST /api/v1/branches/101/menu-items (Ginger Tea) ✅
3. POST /api/v1/branches/101/menu-items (Samosa) ✅
4. POST /api/v1/branches/101/menu-items (Vada Pav) ✅
5. GET /api/v1/branches/101/menu-items ✅ (Returns 4 items)
6. GET /api/v1/branches/101/menu-version ✅ (Version: 4)
```

### Scenario 2: Menu Updates
```
1. PUT /api/v1/menu-items/1 (Update price) ✅
2. PUT /api/v1/menu-items/2 (Mark unavailable) ✅
3. GET /api/v1/branches/101/menu-items?availableOnly=true ✅ (3 items)
4. GET /api/v1/branches/101/menu-version ✅ (Version: 6)
```

### Scenario 3: Category Filtering
```
1. GET /api/v1/branches/101/menu-items?category=Beverages ✅
2. GET /api/v1/branches/101/menu-items?category=Snacks ✅
3. GET /api/v1/branches/101/menu-items?category=Invalid → Empty list ✅
```

### Scenario 4: Error Handling
```
1. POST /api/v1/branches/999/menu-items → 404 (Branch not found) ❌
2. POST /api/v1/branches/101/menu-items (price: -10) → 400 ❌
3. PUT /api/v1/menu-items/999 → 404 (Item not found) ❌
4. PUT /api/v1/menu-items/1 (unauthorized user) → 403 ❌
5. DELETE /api/v1/menu-items/1 (unauthorized) → 403 ❌
```

### Scenario 5: Soft Delete
```
1. DELETE /api/v1/menu-items/1 ✅ (Soft delete)
2. GET /api/v1/menu-items/1 → 404 ❌ (Appears deleted)
3. GET /api/v1/branches/101/menu-items ✅ (Item 1 not in list)
4. Database still has record with is_deleted=true ✅
```

---

## API Endpoint Summary

### Menu Item CRUD (4 endpoints)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| POST | `/api/v1/branches/{branchId}/menu-items` | UC-M001 |
| GET | `/api/v1/menu-items/{menuItemId}` | UC-M002 |
| PUT | `/api/v1/menu-items/{menuItemId}` | UC-M003, UC-M009 |
| DELETE | `/api/v1/menu-items/{menuItemId}` | UC-M004 |

### Menu Browsing (4 endpoints)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| GET | `/api/v1/branches/{branchId}/menu-items` | UC-M005, UC-M006, UC-M007 |
| GET | `/api/v1/branches/{branchId}/menu-items/popular` | UC-M008 |
| GET | `/api/v1/branches/{branchId}/menu-version` | UC-M011 |
| PUT | `/api/v1/branches/{branchId}/menu-items/bulk-availability` | UC-M010 |

**Total: 8 endpoints covering 12+ use cases**

---

## Data Model

### Menu Item Entity
```
menu_item_id: BIGSERIAL PRIMARY KEY (Long)
branch_id: BIGINT FOREIGN KEY → vendor_branches
name: VARCHAR(255) NOT NULL
description: TEXT
price: DECIMAL(10,2) NOT NULL
category: VARCHAR(100) NOT NULL
preparation_time_minutes: INTEGER
images: JSONB
metadata: JSONB
tags: TEXT[]
is_available: BOOLEAN DEFAULT true
is_deleted: BOOLEAN DEFAULT false
created_at: TIMESTAMP
updated_at: TIMESTAMP
```

### Images JSONB Structure
```json
{
  "primary": "https://s3.../item-primary.jpg",
  "gallery": [
    "https://s3.../item-1.jpg",
    "https://s3.../item-2.jpg"
  ]
}
```

### Metadata JSONB Structure
```json
{
  "spice_level": "medium",
  "serving_size": "150ml",
  "calories": 80,
  "allergens": ["milk", "nuts"],
  "veg": true,
  "vegan": false,
  "gluten_free": false,
  "order_count": 1250,
  "rating": 4.5,
  "seasonal": false
}
```

---

## Business Rules

1. **Price Validation:** Price must be > 0
2. **Soft Delete:** Items are never hard-deleted (preserves order history)
3. **Availability:** Unavailable items cannot be ordered but remain visible to owners
4. **Menu Version:** Increments on any menu change (create, update, delete, availability)
5. **Cache Invalidation:** Menu cache invalidated on version change
6. **Authorization:** Only vendor owners can modify menu
7. **Public Access:** Customers see only available, non-deleted items
8. **Category:** Free-form text, no predefined enum (flexibility)
9. **Images:** Optional, stored as JSONB for flexibility
10. **Metadata:** Extensible JSONB for future attributes

---

## Performance Considerations

1. **Caching Strategy:**
   - Cache full menu by branch_id
   - Cache popular items separately
   - Invalidate on menu_version change
   - TTL: 5 minutes (with version check)

2. **Database Indexes:**
   - `idx_menu_items_branch_id` on branch_id
   - `idx_menu_items_category` on category
   - `idx_menu_items_available` on is_available
   - Composite index on (branch_id, is_deleted, is_available)

3. **Query Optimization:**
   - Use pagination for large menus
   - Filter deleted items at query level
   - Eager load images/metadata only when needed

4. **API Response Times:**
   - Menu retrieval (cached): < 50ms
   - Menu retrieval (uncached): < 200ms
   - Menu item create: < 300ms
   - Menu item update: < 200ms

---

## Notes

- All IDs are Long (BIGSERIAL) for menu items
- Branch IDs are Long (BIGSERIAL)
- Vendor IDs are Long (BIGSERIAL)
- User IDs remain UUID (cross-service reference)
- All timestamps in UTC
- Prices in INR (Indian Rupees)
- JSONB allows schema evolution without migrations
- Soft deletes preserve referential integrity
- Menu versioning enables efficient caching
- Category is free-form for flexibility

---

**End of Use Cases Document**
