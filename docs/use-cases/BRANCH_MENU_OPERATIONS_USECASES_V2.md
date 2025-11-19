# Branch Menu Operations Use Cases

**Document Version:** 2.0  
**Last Updated:** November 8, 2025  
**Service:** Order Catalog Service (Port 8082)

---

## Key Changes in V2.0

1. ✅ **No separate bulk-availability endpoint** - Single PUT endpoint handles all updates
2. ✅ **Comprehensive metadata fields** documented (nutritional_info, customizations, allergens, dietary_tags)
3. ✅ **Image upload functionality** added for menu items
4. ✅ **Complete schema reference** from V4 migration
5. ✅ **Partial update support** - update any combination of fields

---

## Table of Contents

1. [Complete Use Case List](#complete-use-case-list-for-e2e-testing)
2. [Menu Item CRUD Operations](#menu-item-crud-operations)
3. [Menu Item Image Management](#menu-item-image-management)
4. [Nutritional Information Management](#nutritional-information-management)
5. [Customization Management](#customization-management)
6. [Allergen Management](#allergen-management)
7. [Dietary Tags Management](#dietary-tags-management)
8. [Menu Browsing & Filtering](#menu-browsing--filtering)
9. [Menu Availability Management](#menu-availability-management)
10. [Menu Versioning](#menu-versioning)
11. [Error Handling Scenarios](#error-handling-scenarios)
12. [Authorization & Security](#authorization--security)
13. [Metadata Structure Reference](#metadata-structure-reference)

---

## Complete Use Case List for E2E Testing

### Menu Item CRUD Operations (10 use cases)
1. **UC-M001:** Create menu item with basic fields only
2. **UC-M002:** Create menu item with complete metadata (nutritional info, customizations, allergens, dietary tags)
3. **UC-M003:** Get menu item by ID
4. **UC-M004:** Update menu item - price only
5. **UC-M005:** Update menu item - availability toggle
6. **UC-M006:** Update menu item - multiple fields at once
7. **UC-M007:** Update menu item - partial metadata update
8. **UC-M008:** Soft delete menu item
9. **UC-M009:** Attempt to get deleted menu item (should return 404)
10. **UC-M010:** List all menu items for a branch

### Image Management (8 use cases)
11. **UC-I001:** Upload primary image for menu item
12. **UC-I002:** Upload multiple gallery images
13. **UC-I003:** Replace existing primary image
14. **UC-I004:** Add more gallery images to existing item
15. **UC-I005:** Upload image with invalid format (error case)
16. **UC-I006:** Upload image exceeding size limit (error case)
17. **UC-I007:** Upload image for non-existent menu item (error case)
18. **UC-I008:** Upload image without authorization (error case)

### Nutritional Information (6 use cases)
19. **UC-N001:** Add nutritional info to existing menu item
20. **UC-N002:** Update specific nutritional fields (e.g., calories only)
21. **UC-N003:** Add complete nutritional profile (all fields)
22. **UC-N004:** Remove nutritional info
23. **UC-N005:** Browse menu items with nutritional info filter
24. **UC-N006:** Get menu item with detailed nutritional breakdown

### Customization Management (10 use cases)
25. **UC-C001:** Add single customization option (e.g., Size)
26. **UC-C002:** Add multiple customization groups (Size, Add-ons, Sugar level)
27. **UC-C003:** Add customization with price modifiers
28. **UC-C004:** Add required customization
29. **UC-C005:** Add optional customization
30. **UC-C006:** Add multi-select customization (e.g., multiple toppings)
31. **UC-C007:** Update existing customization options
32. **UC-C008:** Remove specific customization group
33. **UC-C009:** Update price modifiers for customization options
34. **UC-C010:** Get menu item with all customization options

### Allergen Management (8 use cases)
35. **UC-A001:** Add allergens to menu item
36. **UC-A002:** Update allergen list
37. **UC-A003:** Remove specific allergen
38. **UC-A004:** Clear all allergens
39. **UC-A005:** Browse menu excluding specific allergens
40. **UC-A006:** Browse menu with multiple allergen exclusions
41. **UC-A007:** Get all menu items safe for nut allergy
42. **UC-A008:** Get all menu items safe for dairy allergy

### Dietary Tags Management (10 use cases)
43. **UC-D001:** Add dietary tags (vegetarian, vegan, etc.)
44. **UC-D002:** Update dietary tags
45. **UC-D003:** Remove specific dietary tag
46. **UC-D004:** Browse menu by single dietary tag (vegetarian)
47. **UC-D005:** Browse menu by multiple dietary tags (vegan + gluten_free)
48. **UC-D006:** Get all vegetarian items
49. **UC-D007:** Get all vegan items
50. **UC-D008:** Get all gluten-free items
51. **UC-D009:** Get all keto-friendly items
52. **UC-D010:** Get all halal items

### Menu Browsing & Filtering (15 use cases)
53. **UC-B001:** Get all menu items for a branch
54. **UC-B002:** Get menu items with pagination
55. **UC-B003:** Filter menu by category (Beverages)
56. **UC-B004:** Filter menu by category (Snacks)
57. **UC-B005:** Filter menu by category (Main Course)
58. **UC-B006:** Filter menu by price range (min-max)
59. **UC-B007:** Filter menu by availability (available only)
60. **UC-B008:** Filter menu by tags (popular, bestseller)
61. **UC-B009:** Combined filters (category + dietary tags)
62. **UC-B010:** Combined filters (price range + allergen exclusion)
63. **UC-B011:** Search menu by name (partial match)
64. **UC-B012:** Sort menu by price (ascending)
65. **UC-B013:** Sort menu by price (descending)
66. **UC-B014:** Sort menu by popularity (order count)
67. **UC-B015:** Get menu with images only

### Availability Management (6 use cases)
68. **UC-V001:** Mark menu item as unavailable
69. **UC-V002:** Mark menu item as available
70. **UC-V003:** Toggle availability multiple times
71. **UC-V004:** Get only available menu items
72. **UC-V005:** Get unavailable menu items (owner view)
73. **UC-V006:** Update availability with other fields simultaneously

### Menu Versioning (5 use cases)
74. **UC-MV001:** Get current menu version
75. **UC-MV002:** Verify version increments after create
76. **UC-MV003:** Verify version increments after update
77. **UC-MV004:** Verify version increments after delete
78. **UC-MV005:** Verify version increments after image upload

### Price Management (5 use cases)
79. **UC-P001:** Update menu item price
80. **UC-P002:** Set price to zero (free item)
81. **UC-P003:** Update price with customization modifiers
82. **UC-P004:** Attempt negative price (error case)
83. **UC-P005:** Update multiple item prices

### Tags Management (5 use cases)
84. **UC-T001:** Add tags to menu item
85. **UC-T002:** Update tags
86. **UC-T003:** Remove specific tag
87. **UC-T004:** Clear all tags
88. **UC-T005:** Browse menu by tags

### Error Handling (15 use cases)
89. **UC-E001:** Create menu item with missing required fields
90. **UC-E002:** Create menu item with invalid price
91. **UC-E003:** Create menu item for non-existent branch
92. **UC-E004:** Get non-existent menu item
93. **UC-E005:** Update non-existent menu item
94. **UC-E006:** Delete non-existent menu item
95. **UC-E007:** Update menu item with invalid metadata structure
96. **UC-E008:** Create menu item with malformed JSON
97. **UC-E009:** Update with negative price
98. **UC-E010:** Exceed maximum description length
99. **UC-E011:** Invalid category format
100. **UC-E012:** Invalid preparation time (negative)
101. **UC-E013:** Duplicate menu item name in same branch
102. **UC-E014:** Invalid image URL format
103. **UC-E015:** Empty required fields

### Authorization & Security (10 use cases)
104. **UC-S001:** Create menu item without authentication
105. **UC-S002:** Create menu item for branch not owned
106. **UC-S003:** Update menu item without authorization
107. **UC-S004:** Delete menu item without authorization
108. **UC-S005:** Upload image without authorization
109. **UC-S006:** Access menu item from different vendor
110. **UC-S007:** Customer view (only available items)
111. **UC-S008:** Owner view (all items including unavailable)
112. **UC-S009:** Verify authorization chain (User → Vendor → Branch → MenuItem)
113. **UC-S010:** Cross-branch menu item access attempt

### Complex Scenarios (12 use cases)
114. **UC-X001:** Create complete menu item with all fields populated
115. **UC-X002:** Update all metadata fields in single request
116. **UC-X003:** Create item → Upload images → Update metadata → Toggle availability
117. **UC-X004:** Create multiple items in sequence
118. **UC-X005:** Update multiple items with different fields
119. **UC-X006:** Delete multiple items
120. **UC-X007:** Create item with customizations → Customer orders with selections
121. **UC-X008:** Seasonal menu item workflow (create → available → unavailable → delete)
122. **UC-X009:** Menu item lifecycle (create → update → images → availability → delete)
123. **UC-X010:** Browse menu with multiple complex filters
124. **UC-X011:** Cache validation after menu changes
125. **UC-X012:** Menu version tracking across multiple operations

**Total: 125 Use Cases for Comprehensive E2E Testing**

---

## Menu Item CRUD Operations

### UC-M001: Create Menu Item (Comprehensive)

**API Endpoint:** `POST /api/v1/branches/{branchId}/menu-items`

**Complete Request Example:**
```json
{
  "name": "Masala Chai",
  "description": "Traditional Indian spiced tea",
  "price": 20.00,
  "category": "Beverages",
  "preparationTimeMinutes": 5,
  "tags": ["hot", "popular", "traditional"],
  "isAvailable": true,
  "metadata": {
    "nutritional_info": {
      "calories": 80,
      "protein_g": 2.5,
      "carbohydrates_g": 12,
      "fat_g": 3
    },
    "customizations": [
      {
        "name": "Size",
        "options": [
          {"value": "Small", "price_modifier": 0},
          {"value": "Medium", "price_modifier": 5},
          {"value": "Large", "price_modifier": 10}
        ],
        "required": true
      }
    ],
    "allergens": ["milk", "cardamom"],
    "dietary_tags": ["vegetarian", "gluten_free"]
  }
}
```

---

### UC-M003: Update Menu Item (Single Endpoint for All Updates)

**API Endpoint:** `PUT /api/v1/menu-items/{menuItemId}`

**Important:** This single endpoint handles ALL update scenarios:
- ✅ Price changes
- ✅ Availability toggles  
- ✅ Metadata updates
- ✅ Customization changes
- ✅ Allergen updates
- ✅ Any combination

**Update Examples:**

**1. Simple Price Update:**
```json
{
  "price": 25.00
}
```

**2. Toggle Availability:**
```json
{
  "isAvailable": false
}
```

**3. Add Customization:**
```json
{
  "metadata": {
    "customizations": [
      {
        "name": "Sugar Level",
        "options": [
          {"value": "No Sugar", "price_modifier": 0},
          {"value": "Normal", "price_modifier": 0},
          {"value": "Extra Sweet", "price_modifier": 2}
        ]
      }
    ]
  }
}
```

**4. Update Multiple Items:**
```json
{
  "price": 22.00,
  "isAvailable": true,
  "preparationTimeMinutes": 7,
  "metadata": {
    "dietary_tags": ["vegetarian", "gluten_free", "low_calorie"],
    "spice_level": "high"
  }
}
```

---

## Menu Item Image Upload Use Cases

### UC-I001: Upload Primary Image

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/upload?target=menu_item&branchId={branchId}&menuItemId={menuItemId}&fileType=primary
```

**Query Parameters:**
- `target=menu_item` (required) - Upload target type
- `branchId={branchId}` (required) - Branch ID for authorization
- `menuItemId={menuItemId}` (required) - Menu item ID
- `fileType=primary` (required) - Image type

**Request:** multipart/form-data with image file

**Authorization Flow:**
1. System validates vendor ownership via vendorId
2. System validates branch belongs to vendor via branchId
3. System validates menu item belongs to branch via menuItemId
4. If all checks pass → Upload allowed

### UC-I002: Upload Gallery Images

**API Endpoint:**
```
POST /api/v1/vendors/{vendorId}/upload?target=menu_item&branchId={branchId}&menuItemId={menuItemId}&fileType=gallery
```

**Query Parameters:**
- `target=menu_item` (required)
- `branchId={branchId}` (required) - For authorization chain
- `menuItemId={menuItemId}` (required)
- `fileType=gallery` (required)

**Result:** Images appended to gallery array

**Why branchId is needed:**
- Validates authorization chain: User → Vendor → Branch → MenuItem
- Prevents unauthorized access to menu items
- Ensures menu item belongs to correct branch

---

## Metadata Structure Reference

### Complete metadata JSONB Structure (from V4 migration):

```json
{
  "nutritional_info": {},
  "customizations": [],
  "allergens": [],
  "dietary_tags": []
}
```

### Detailed Examples:

**nutritional_info:**
```json
{
  "calories": 80,
  "protein_g": 2.5,
  "carbohydrates_g": 12,
  "fat_g": 3,
  "sugar_g": 8,
  "sodium_mg": 45,
  "fiber_g": 0.5
}
```

**customizations:**
```json
[
  {
    "name": "Size",
    "options": [
      {"value": "Small", "price_modifier": 0},
      {"value": "Medium", "price_modifier": 5},
      {"value": "Large", "price_modifier": 10}
    ],
    "required": true,
    "multi_select": false
  }
]
```

**allergens:**
```json
["milk", "nuts", "gluten", "eggs"]
```

**dietary_tags:**
```json
["vegetarian", "vegan", "gluten_free", "keto", "halal"]
```

---

## API Endpoint Summary

### CRUD Operations (4 endpoints)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/branches/{branchId}/menu-items` | Create item |
| GET | `/api/v1/menu-items/{menuItemId}` | Get item |
| PUT | `/api/v1/menu-items/{menuItemId}` | **Update ANY fields** |
| DELETE | `/api/v1/menu-items/{menuItemId}` | Soft delete |

### Image Upload (2 endpoints)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/vendors/{vendorId}/upload?target=menu_item&branchId={branchId}&menuItemId={menuItemId}&fileType=primary` | Primary image |
| POST | `/api/v1/vendors/{vendorId}/upload?target=menu_item&branchId={branchId}&menuItemId={menuItemId}&fileType=gallery` | Gallery images |

### Browsing (2 endpoints)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/branches/{branchId}/menu-items` | List with filters |
| GET | `/api/v1/branches/{branchId}/menu-version` | Version check |

**Total: 8 endpoints (No bulk-availability endpoint needed)**

---

## Database Schema (from V4 migration)

```sql
CREATE TABLE menu_items (
    menu_item_id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    category VARCHAR(100) NOT NULL,
    images JSONB DEFAULT '{"primary": null, "gallery": []}'::jsonb,
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INTEGER DEFAULT 15,
    metadata JSONB DEFAULT '{
        "nutritional_info": {},
        "customizations": [],
        "allergens": [],
        "dietary_tags": []
    }'::jsonb,
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

---

## Key Business Rules

1. ✅ **Single PUT endpoint** handles all update scenarios
2. ✅ **Partial updates** supported - send only fields to change
3. ✅ **Metadata is flexible** - JSONB allows schema evolution
4. ✅ **Images via unified upload** - same endpoint as vendor/branch
5. ✅ **Soft delete** preserves order history
6. ✅ **Menu versioning** on any change
7. ✅ **Price >= 0** (free items allowed)
8. ✅ **Comprehensive nutritional info** for transparency
9. ✅ **Customizations** support dynamic pricing
10. ✅ **Allergen safety** clearly documented

---

---

## E2E Test Implementation Priority

### Phase 1: Core CRUD (Priority: HIGH)
- UC-M001 to UC-M010: Basic menu item operations
- Must pass before moving to Phase 2

### Phase 2: Metadata Management (Priority: HIGH)
- UC-N001 to UC-N006: Nutritional information
- UC-C001 to UC-C010: Customizations
- UC-A001 to UC-A008: Allergens
- UC-D001 to UC-D010: Dietary tags

### Phase 3: Image & Availability (Priority: MEDIUM)
- UC-I001 to UC-I008: Image uploads
- UC-V001 to UC-V006: Availability management

### Phase 4: Browsing & Filtering (Priority: MEDIUM)
- UC-B001 to UC-B015: All filtering scenarios

### Phase 5: Error Handling (Priority: HIGH)
- UC-E001 to UC-E015: All error scenarios
- Critical for robustness

### Phase 6: Security & Authorization (Priority: HIGH)
- UC-S001 to UC-S010: Authorization checks
- Must be tested thoroughly

### Phase 7: Complex Workflows (Priority: LOW)
- UC-X001 to UC-X012: End-to-end workflows
- Integration scenarios

---

## Test Data Requirements

### Minimum Test Data Needed:
1. **1 Vendor** (registered and approved)
2. **2 Branches** (for cross-branch testing)
3. **10+ Menu Items** with varied configurations:
   - 3 Beverages (with customizations)
   - 3 Snacks (with allergens)
   - 2 Main Course (with nutritional info)
   - 2 Desserts (with dietary tags)
4. **Multiple Images** (primary + gallery)
5. **Various Metadata** combinations

### Test Users:
1. **Owner User** (owns vendor)
2. **Unauthorized User** (different vendor)
3. **Customer User** (public access)

---

## Expected Test Outcomes

### Success Metrics:
- ✅ All 125 use cases documented
- ✅ 100% API endpoint coverage
- ✅ All CRUD operations tested
- ✅ All metadata fields validated
- ✅ All error scenarios handled
- ✅ All authorization checks passed
- ✅ Menu versioning verified
- ✅ Cache invalidation working

### Test Coverage Goals:
- **Unit Tests:** 80%+ coverage
- **Integration Tests:** All API endpoints
- **E2E Tests:** All 125 use cases
- **Performance Tests:** < 200ms response time

---

**End of Document - Ready for E2E Test Implementation**

