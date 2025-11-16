-- Complete Test Data Setup Script for Integration Tests
-- This script creates comprehensive test data needed for all integration tests to pass

-- Clean up any existing test data (optional - comment out if you want to keep existing data)
-- DELETE FROM menu_items WHERE menu_item_id BETWEEN 1 AND 10;
-- DELETE FROM vendor_branches WHERE branch_id BETWEEN 1 AND 10;
-- DELETE FROM vendors WHERE vendor_id BETWEEN 1001 AND 1010;

-- Step 1: Create test vendors
INSERT INTO vendors (vendor_id, user_id, company_name, brand_name, company_email, company_phone, created_at, updated_at)
VALUES 
  (1001, '11111111-1111-1111-1111-111111111111'::uuid, 'Test Cafe Company', 'Test Cafe Mumbai', 'testcafe@test.com', '+919876543210', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (1002, '22222222-2222-2222-2222-222222222222'::uuid, 'Test Restaurant Company', 'Test Restaurant Delhi', 'testrestaurant@test.com', '+919876543211', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (1003, '33333333-3333-3333-3333-333333333333'::uuid, 'Test Snacks Company', 'Test Snacks Bangalore', 'testsnacks@test.com', '+919876543212', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (vendor_id) DO UPDATE SET updated_at = CURRENT_TIMESTAMP;

-- Step 2: Create test vendor branches (with various IDs that tests might use)
INSERT INTO vendor_branches (
  branch_id, vendor_id, branch_name, city, is_active, is_open, 
  latitude, longitude, rating, 
  created_at, updated_at
)
VALUES 
  -- Branch ID 1 (used by PlaceOrder tests)
  (1, 1001, 'Test Cafe Branch 1', 'Mumbai', true, true, 19.0760, 72.8777, 4.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Branch ID 2
  (2, 1001, 'Test Cafe Branch 2', 'Mumbai', true, true, 19.0800, 72.8800, 4.3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Branch ID 1001 (used by some tests)
  (1001, 1001, 'Test Cafe Mumbai Main', 'Mumbai', true, true, 19.0760, 72.8777, 4.7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Branch ID 1002
  (1002, 1002, 'Test Restaurant Delhi', 'Delhi', true, true, 28.7041, 77.1025, 4.6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Branch ID 1003
  (1003, 1003, 'Test Snacks Bangalore', 'Bangalore', true, true, 12.9716, 77.5946, 4.4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Closed branch for testing
  (999, 1001, 'Test Closed Branch', 'Mumbai', true, false, 19.0760, 72.8777, 3.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (branch_id) DO UPDATE SET 
  is_active = EXCLUDED.is_active, 
  is_open = EXCLUDED.is_open,
  updated_at = CURRENT_TIMESTAMP;

-- Step 3: Create comprehensive menu items
INSERT INTO menu_items (
  menu_item_id, branch_id, name, description, price, category, 
  is_available, preparation_time_minutes, 
  created_at, updated_at
)
VALUES 
  -- Branch 1 items (IDs 1-5)
  (1, 1, 'Masala Chai', 'Traditional Indian tea with spices', 20.00, 'BEVERAGES', true, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 'Samosa', 'Crispy fried potato snack', 15.00, 'SNACKS', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1, 'Pakora', 'Deep fried vegetable fritters', 25.00, 'SNACKS', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 1, 'Vada Pav', 'Mumbai street food special', 30.00, 'SNACKS', true, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 1, 'Filter Coffee', 'South Indian style coffee', 25.00, 'BEVERAGES', true, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Branch 1001 items (IDs 10-15)
  (10, 1001, 'Masala Chai', 'Traditional Indian tea', 20.00, 'BEVERAGES', true, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (11, 1001, 'Samosa', 'Crispy fried snack', 15.00, 'SNACKS', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (12, 1001, 'Pakora', 'Deep fried fritters', 25.00, 'SNACKS', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (13, 1001, 'Vada', 'Crispy lentil donuts', 20.00, 'SNACKS', true, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (14, 1001, 'Cutting Chai', 'Half cup of chai', 10.00, 'BEVERAGES', true, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (15, 1001, 'Bread Pakora', 'Bread fritters', 30.00, 'SNACKS', true, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Branch 1002 items (IDs 20-25)
  (20, 1002, 'Filter Coffee', 'South Indian filter coffee', 30.00, 'BEVERAGES', true, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (21, 1002, 'Masala Dosa', 'Crispy rice crepe with potato filling', 50.00, 'MAIN_COURSE', true, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (22, 1002, 'Idli', 'Steamed rice cakes', 40.00, 'MAIN_COURSE', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (23, 1002, 'Vada', 'Crispy lentil donuts', 20.00, 'SNACKS', true, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (24, 1002, 'Medu Vada', 'Crispy fried lentil donuts', 25.00, 'SNACKS', true, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Unavailable item for testing
  (99, 1, 'Out of Stock Item', 'This item is not available', 50.00, 'SNACKS', false, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- High price item for testing
  (100, 1, 'Premium Thali', 'Full course meal', 500.00, 'MAIN_COURSE', true, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (menu_item_id) DO UPDATE SET 
  is_available = EXCLUDED.is_available,
  price = EXCLUDED.price,
  updated_at = CURRENT_TIMESTAMP;

-- Verify setup
SELECT '=== VENDORS ===' as info;
SELECT vendor_id, company_name, brand_name FROM vendors WHERE vendor_id BETWEEN 1001 AND 1010 ORDER BY vendor_id;

SELECT '=== BRANCHES ===' as info;
SELECT branch_id, branch_name, vendor_id, is_active, is_open FROM vendor_branches WHERE branch_id IN (1, 2, 999, 1001, 1002, 1003) ORDER BY branch_id;

SELECT '=== MENU ITEMS ===' as info;
SELECT menu_item_id, branch_id, name, price, is_available FROM menu_items WHERE menu_item_id < 110 ORDER BY menu_item_id;

SELECT '=== SUMMARY ===' as info;
SELECT 
  (SELECT COUNT(*) FROM vendors WHERE vendor_id BETWEEN 1001 AND 1010) as vendors_count,
  (SELECT COUNT(*) FROM vendor_branches WHERE branch_id IN (1, 2, 999, 1001, 1002, 1003)) as branches_count,
  (SELECT COUNT(*) FROM menu_items WHERE menu_item_id < 110) as menu_items_count;

