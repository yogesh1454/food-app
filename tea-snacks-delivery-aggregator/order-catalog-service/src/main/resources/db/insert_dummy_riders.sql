-- Dummy Riders Insert Script
-- Insert test riders for delivery system testing
-- Run this script against nastto_db

-- First, let's clear any existing test data (optional)
-- DELETE FROM riders WHERE rider_id IN (
--   'a1111111-1111-1111-1111-111111111111',
--   'a2222222-2222-2222-2222-222222222222',
--   'a3333333-3333-3333-3333-333333333333',
--   'a4444444-4444-4444-4444-444444444444',
--   'a5555555-5555-5555-5555-555555555555',
--   'a6666666-6666-6666-6666-666666666666'
-- );

-- Insert 6 dummy riders across Bangalore
INSERT INTO riders (
    rider_id, 
    name, 
    phone, 
    email, 
    current_location, 
    is_online, 
    is_on_break, 
    current_deliveries, 
    rating, 
    total_deliveries, 
    completed_deliveries_today,
    acceptance_rate, 
    total_assignments,
    accepted_assignments,
    created_at, 
    updated_at, 
    version, 
    last_location_update
)
VALUES 
-- Rider 1: Near Koramangala area (Central Bangalore)
(
    'a1111111-1111-1111-1111-111111111111', 
    'Ravi Kumar', 
    '+919876543210', 
    'ravi@example.com', 
    ST_SetSRID(ST_MakePoint(77.6245, 12.9352), 4326), 
    true,   -- is_online
    false,  -- is_on_break
    0,      -- current_deliveries
    4.85,   -- rating
    120,    -- total_deliveries
    5,      -- completed_deliveries_today
    95.50,  -- acceptance_rate
    130,    -- total_assignments
    124,    -- accepted_assignments
    NOW(), 
    NOW(), 
    0, 
    NOW()
),

-- Rider 2: Near Indiranagar
(
    'a2222222-2222-2222-2222-222222222222', 
    'Suresh Reddy', 
    '+919876543211', 
    'suresh@example.com', 
    ST_SetSRID(ST_MakePoint(77.6408, 12.9784), 4326), 
    true,   -- is_online
    false,  -- is_on_break
    0,      -- current_deliveries
    4.92,   -- rating
    210,    -- total_deliveries
    8,      -- completed_deliveries_today
    98.00,  -- acceptance_rate
    220,    -- total_assignments
    216,    -- accepted_assignments
    NOW(), 
    NOW(), 
    0, 
    NOW()
),

-- Rider 3: Near Whitefield (has 1 ongoing delivery)
(
    'a3333333-3333-3333-3333-333333333333', 
    'Manoj Singh', 
    '+919876543212', 
    'manoj@example.com', 
    ST_SetSRID(ST_MakePoint(77.7509, 12.9698), 4326), 
    true,   -- is_online
    false,  -- is_on_break
    1,      -- current_deliveries (already has 1)
    4.70,   -- rating
    85,     -- total_deliveries
    3,      -- completed_deliveries_today
    90.00,  -- acceptance_rate
    95,     -- total_assignments
    86,     -- accepted_assignments
    NOW(), 
    NOW(), 
    0, 
    NOW()
),

-- Rider 4: Near HSR Layout (Top rated rider)
(
    'a4444444-4444-4444-4444-444444444444', 
    'Prakash Gowda', 
    '+919876543213', 
    'prakash@example.com', 
    ST_SetSRID(ST_MakePoint(77.6446, 12.9121), 4326), 
    true,   -- is_online
    false,  -- is_on_break
    0,      -- current_deliveries
    4.95,   -- rating (high rating)
    350,    -- total_deliveries
    12,     -- completed_deliveries_today
    99.00,  -- acceptance_rate
    360,    -- total_assignments
    356,    -- accepted_assignments
    NOW(), 
    NOW(), 
    0, 
    NOW()
),

-- Rider 5: Near MG Road (On break - should be excluded from search)
(
    'a5555555-5555-5555-5555-555555555555', 
    'Venkat Rao', 
    '+919876543214', 
    'venkat@example.com', 
    ST_SetSRID(ST_MakePoint(77.6069, 12.9756), 4326), 
    true,   -- is_online
    true,   -- is_on_break (ON BREAK!)
    0,      -- current_deliveries
    4.60,   -- rating
    45,     -- total_deliveries
    2,      -- completed_deliveries_today
    88.00,  -- acceptance_rate
    52,     -- total_assignments
    46,     -- accepted_assignments
    NOW(), 
    NOW(), 
    0, 
    NOW()
),

-- Rider 6: Near Electronic City
(
    'a6666666-6666-6666-6666-666666666666', 
    'Arjun Prasad', 
    '+919876543215', 
    'arjun@example.com', 
    ST_SetSRID(ST_MakePoint(77.6704, 12.8441), 4326), 
    true,   -- is_online
    false,  -- is_on_break
    0,      -- current_deliveries
    4.88,   -- rating
    180,    -- total_deliveries
    7,      -- completed_deliveries_today
    96.50,  -- acceptance_rate
    190,    -- total_assignments
    183,    -- accepted_assignments
    NOW(), 
    NOW(), 
    0, 
    NOW()
)

ON CONFLICT (rider_id) DO UPDATE SET 
    is_online = EXCLUDED.is_online,
    is_on_break = EXCLUDED.is_on_break,
    current_deliveries = EXCLUDED.current_deliveries,
    current_location = EXCLUDED.current_location,
    last_location_update = EXCLUDED.last_location_update,
    updated_at = NOW();

-- Verify the insert
SELECT 
    rider_id,
    name,
    phone,
    is_online,
    is_on_break,
    current_deliveries,
    rating,
    ST_AsText(current_location) as location
FROM riders
ORDER BY name;
