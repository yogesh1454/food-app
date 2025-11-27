-- Test Data Setup Script for Integration Tests
-- This script creates minimal test data needed for integration tests to pass

-- Step 1: Create test vendors
INSERT INTO vendors (vendor_id, user_id, company_name, brand_name, created_at, updated_at)
VALUES 
  (1001, '11111111-1111-1111-1111-111111111111'::uuid, 'Test Cafe Company', 'Test Cafe Mumbai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (1002, '22222222-2222-2222-2222-222222222222'::uuid, 'Test Restaurant Company', 'Test Restaurant Delhi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (vendor_id) DO UPDATE SET updated_at = CURRENT_TIMESTAMP;

-- Step 2: Create test vendor branches
INSERT INTO vendor_branches (branch_id, vendor_id, branch_name, city, is_active, is_open, latitude, longitude, created_at, updated_at)
VALUES 
  (1001, 1001, 'Test Cafe Mumbai', 'Mumbai', true, true, 19.0760, 72.8777, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (1002, 1002, 'Test Restaurant Delhi', 'Delhi', true, true, 28.7041, 77.1025, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (branch_id) DO UPDATE SET 
  is_active = true, 
  is_open = true,
  updated_at = CURRENT_TIMESTAMP;

-- Step 3: Create test menu items
INSERT INTO menu_items (menu_item_id, branch_id, item_name, description, price, category, is_available, created_at, updated_at)
VALUES 
  (1, 1001, 'Masala Chai', 'Traditional Indian tea', 20.00, 'BEVERAGES', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1001, 'Samosa', 'Crispy fried snack', 15.00, 'SNACKS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1001, 'Pakora', 'Deep fried fritters', 25.00, 'SNACKS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 1002, 'Filter Coffee', 'South Indian filter coffee', 30.00, 'BEVERAGES', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 1002, 'Vada', 'Crispy lentil donuts', 20.00, 'SNACKS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (menu_item_id) DO UPDATE SET 
  is_available = true,
  updated_at = CURRENT_TIMESTAMP;

-- Verify setup
SELECT 'Vendors:' as info, COUNT(*) as count FROM vendors WHERE vendor_id IN (1001, 1002)
UNION ALL
SELECT 'Branches:', COUNT(*) FROM vendor_branches WHERE branch_id IN (1001, 1002)
UNION ALL
SELECT 'Menu Items:', COUNT(*) FROM menu_items WHERE menu_item_id BETWEEN 1 AND 5;

