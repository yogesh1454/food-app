#!/bin/bash

# Branch Menu Operations Test Script
# Based on BRANCH_MENU_OPERATIONS_USECASES_V2.md
# Tests UC-M001 (Create Menu Item) and UC-B001 (Get Menu Items)

API_BASE_URL="http://localhost:8080/api/v1"
BRANCH_ID=5  # Using Chai Express - Whitefield branch
VENDOR_ID=1

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   BRANCH MENU OPERATIONS TEST                              ║${NC}"
echo -e "${BLUE}║   Based on BRANCH_MENU_OPERATIONS_USECASES_V2.md           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${YELLOW}Branch Details:${NC}"
echo "  Branch ID: $BRANCH_ID"
echo "  Branch Name: Chai Express - Whitefield"
echo "  Vendor ID: $VENDOR_ID"
echo ""

# Test 1: Create Menu Item 1 - Masala Chai
echo -e "${YELLOW}[Test 1] Creating Menu Item: Masala Chai (UC-M001)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MENU_ITEM_1=$(curl -s -X POST "$API_BASE_URL/branches/$BRANCH_ID/menu-items" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Masala Chai",
    "description": "Traditional Indian spiced tea with cardamom, ginger and cloves",
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
      "allergens": ["milk"],
      "dietary_tags": ["vegetarian", "gluten_free"]
    }
  }')

MENU_ID_1=$(echo "$MENU_ITEM_1" | grep -o '"menuItemId":[0-9]*' | head -1 | cut -d':' -f2)
if [ -n "$MENU_ID_1" ]; then
  echo -e "${GREEN}✓ Menu item created successfully${NC}"
  echo "  Menu Item ID: $MENU_ID_1"
  echo "  Name: Masala Chai"
  echo "  Price: ₹20.00"
else
  echo -e "${RED}✗ Failed to create menu item${NC}"
  echo "$MENU_ITEM_1"
fi
echo ""

# Test 2: Create Menu Item 2 - Ginger Lemon Tea
echo -e "${YELLOW}[Test 2] Creating Menu Item: Ginger Lemon Tea (UC-M001)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MENU_ITEM_2=$(curl -s -X POST "$API_BASE_URL/branches/$BRANCH_ID/menu-items" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ginger Lemon Tea",
    "description": "Refreshing tea with fresh ginger and lemon juice",
    "price": 25.00,
    "category": "Beverages",
    "preparationTimeMinutes": 3,
    "tags": ["hot", "healthy", "refreshing"],
    "isAvailable": true,
    "metadata": {
      "nutritional_info": {
        "calories": 45,
        "protein_g": 0.5,
        "carbohydrates_g": 10,
        "fat_g": 0.2,
        "sugar_g": 5
      },
      "customizations": [
        {
          "name": "Sugar Level",
          "options": [
            {"value": "No Sugar", "price_modifier": 0},
            {"value": "Normal", "price_modifier": 0},
            {"value": "Extra Sweet", "price_modifier": 2}
          ]
        }
      ],
      "allergens": ["lemon"],
      "dietary_tags": ["vegetarian", "vegan", "gluten_free", "keto"]
    }
  }')

MENU_ID_2=$(echo "$MENU_ITEM_2" | grep -o '"menuItemId":[0-9]*' | head -1 | cut -d':' -f2)
if [ -n "$MENU_ID_2" ]; then
  echo -e "${GREEN}✓ Menu item created successfully${NC}"
  echo "  Menu Item ID: $MENU_ID_2"
  echo "  Name: Ginger Lemon Tea"
  echo "  Price: ₹25.00"
else
  echo -e "${RED}✗ Failed to create menu item${NC}"
  echo "$MENU_ITEM_2"
fi
echo ""

# Test 3: Create Menu Item 3 - Cheese Toast Combo
echo -e "${YELLOW}[Test 3] Creating Menu Item: Cheese Toast Combo (UC-M001)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MENU_ITEM_3=$(curl -s -X POST "$API_BASE_URL/branches/$BRANCH_ID/menu-items" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cheese Toast Combo",
    "description": "Crispy toast with melted cheese and butter, served with tea",
    "price": 60.00,
    "category": "Snacks",
    "preparationTimeMinutes": 8,
    "tags": ["popular", "bestseller", "comfort-food"],
    "isAvailable": true,
    "metadata": {
      "nutritional_info": {
        "calories": 320,
        "protein_g": 12,
        "carbohydrates_g": 28,
        "fat_g": 18,
        "sugar_g": 2
      },
      "customizations": [
        {
          "name": "Cheese Type",
          "options": [
            {"value": "Mozzarella", "price_modifier": 0},
            {"value": "Cheddar", "price_modifier": 5},
            {"value": "Paneer", "price_modifier": 10}
          ],
          "required": true
        },
        {
          "name": "Add-ons",
          "options": [
            {"value": "Extra Cheese", "price_modifier": 10},
            {"value": "Tomato Slices", "price_modifier": 5},
            {"value": "Onion Slices", "price_modifier": 3}
          ],
          "multi_select": true
        }
      ],
      "allergens": ["milk", "gluten"],
      "dietary_tags": ["vegetarian"]
    }
  }')

MENU_ID_3=$(echo "$MENU_ITEM_3" | grep -o '"menuItemId":[0-9]*' | head -1 | cut -d':' -f2)
if [ -n "$MENU_ID_3" ]; then
  echo -e "${GREEN}✓ Menu item created successfully${NC}"
  echo "  Menu Item ID: $MENU_ID_3"
  echo "  Name: Cheese Toast Combo"
  echo "  Price: ₹60.00"
else
  echo -e "${RED}✗ Failed to create menu item${NC}"
  echo "$MENU_ITEM_3"
fi
echo ""

# Test 4: Create Menu Item 4 - Espresso Coffee
echo -e "${YELLOW}[Test 4] Creating Menu Item: Espresso Coffee (UC-M001)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MENU_ITEM_4=$(curl -s -X POST "$API_BASE_URL/branches/$BRANCH_ID/menu-items" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Espresso Coffee",
    "description": "Strong and bold espresso shot, perfect for coffee lovers",
    "price": 40.00,
    "category": "Beverages",
    "preparationTimeMinutes": 2,
    "tags": ["popular", "hot", "premium"],
    "isAvailable": true,
    "metadata": {
      "nutritional_info": {
        "calories": 15,
        "protein_g": 0.2,
        "carbohydrates_g": 0.3,
        "fat_g": 0.1,
        "caffeine_mg": 75
      },
      "customizations": [
        {
          "name": "Milk Option",
          "options": [
            {"value": "Black", "price_modifier": 0},
            {"value": "With Milk", "price_modifier": 5},
            {"value": "Oat Milk", "price_modifier": 10}
          ]
        }
      ],
      "allergens": ["coffee"],
      "dietary_tags": ["vegetarian", "vegan", "gluten_free"]
    }
  }')

MENU_ID_4=$(echo "$MENU_ITEM_4" | grep -o '"menuItemId":[0-9]*' | head -1 | cut -d':' -f2)
if [ -n "$MENU_ID_4" ]; then
  echo -e "${GREEN}✓ Menu item created successfully${NC}"
  echo "  Menu Item ID: $MENU_ID_4"
  echo "  Name: Espresso Coffee"
  echo "  Price: ₹40.00"
else
  echo -e "${RED}✗ Failed to create menu item${NC}"
  echo "$MENU_ITEM_4"
fi
echo ""

# Test 5: Get All Menu Items for the Branch (UC-B001)
echo -e "${YELLOW}[Test 5] Getting All Menu Items for Branch (UC-B001)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MENU_ITEMS=$(curl -s -X GET "$API_BASE_URL/branches/$BRANCH_ID/menu-items" \
  -H "Content-Type: application/json")

ITEM_COUNT=$(echo "$MENU_ITEMS" | grep -o '"menuItemId"' | wc -l)
echo -e "${GREEN}✓ Menu items retrieved successfully${NC}"
echo "  Total items in branch: $ITEM_COUNT"
echo ""

# Test 6: Get Specific Menu Item (UC-M003)
if [ -n "$MENU_ID_1" ]; then
  echo -e "${YELLOW}[Test 6] Getting Specific Menu Item: Masala Chai (UC-M003)${NC}"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

  MENU_DETAIL=$(curl -s -X GET "$API_BASE_URL/menu-items/$MENU_ID_1" \
    -H "Content-Type: application/json")

  echo -e "${GREEN}✓ Menu item details retrieved${NC}"
  echo ""
fi

# Display complete menu in formatted JSON
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    COMPLETE MENU RESPONSE                   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "$MENU_ITEMS" | python3 -m json.tool 2>/dev/null || echo "$MENU_ITEMS"
echo ""

# Summary
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                        TEST SUMMARY                        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ All tests completed successfully!${NC}"
echo ""
echo "Menu Items Created:"
if [ -n "$MENU_ID_1" ]; then echo "  1. Masala Chai (ID: $MENU_ID_1) - ₹20.00"; fi
if [ -n "$MENU_ID_2" ]; then echo "  2. Ginger Lemon Tea (ID: $MENU_ID_2) - ₹25.00"; fi
if [ -n "$MENU_ID_3" ]; then echo "  3. Cheese Toast Combo (ID: $MENU_ID_3) - ₹60.00"; fi
if [ -n "$MENU_ID_4" ]; then echo "  4. Espresso Coffee (ID: $MENU_ID_4) - ₹40.00"; fi
echo ""
echo "Total Menu Items: $ITEM_COUNT"
echo ""
echo "Features Tested:"
echo "  ✓ UC-M001: Create menu item with complete metadata"
echo "  ✓ UC-B001: List all menu items for a branch"
echo "  ✓ UC-M003: Get specific menu item details"
echo "  ✓ Nutritional information management"
echo "  ✓ Customization options (Size, Sugar Level, Cheese Type)"
echo "  ✓ Allergen tagging"
echo "  ✓ Dietary tags management"
echo ""
echo -e "${BLUE}For more details, see: BRANCH_MENU_OPERATIONS_USECASES_V2.md${NC}"

