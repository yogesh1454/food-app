#!/bin/bash

echo "=== Testing Authorization Framework ==="

# Test 1: Health check
echo "1. Testing health endpoint..."
curl -s -X GET http://localhost:8080/api/auth/health
echo -e "\n"

# Test 2: Authorization endpoint without auth (should fail)
echo "2. Testing authorization endpoint without authentication (should fail)..."
curl -s -X GET http://localhost:8080/api/auth/authorization/permissions -w "HTTP Status: %{http_code}\n"
echo -e "\n"

# Test 3: Test authorization endpoint without auth (should fail)
echo "3. Testing test authorization endpoint without authentication (should fail)..."
curl -s -X GET http://localhost:8080/api/test/auth/public -w "HTTP Status: %{http_code}\n"
echo -e "\n"

# Test 4: Admin-only endpoint without auth (should fail)
echo "4. Testing admin-only endpoint without authentication (should fail)..."
curl -s -X GET http://localhost:8080/api/test/auth/admin-only -w "HTTP Status: %{http_code}\n"
echo -e "\n"

echo "=== Authorization Framework Test Complete ===" 