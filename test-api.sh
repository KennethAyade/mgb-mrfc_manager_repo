#!/bin/bash

# ================================================
# MGB MRFC Manager - API Testing Script
# ================================================
# This script tests all implemented endpoints
# Run: bash test-api.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# API Configuration
BASE_URL="http://localhost:3000/api/v1"
TOKEN=""
TEST_USER_ID=""
TEST_MRFC_ID=""

# Test counters
PASSED=0
FAILED=0
TOTAL=0

# ================================================
# Helper Functions
# ================================================

print_header() {
  echo -e "${BLUE}================================${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}================================${NC}"
}

print_test() {
  TOTAL=$((TOTAL + 1))
  echo -e "\n${YELLOW}[Test $TOTAL] $1${NC}"
}

print_success() {
  PASSED=$((PASSED + 1))
  echo -e "${GREEN}✓ PASS${NC}: $1"
}

print_error() {
  FAILED=$((FAILED + 1))
  echo -e "${RED}✗ FAIL${NC}: $1"
}

check_server() {
  echo -e "${YELLOW}Checking if server is running...${NC}"
  if ! curl -s "${BASE_URL}/health" > /dev/null; then
    echo -e "${RED}ERROR: Server is not running on ${BASE_URL}${NC}"
    echo "Please start the server first with: npm run dev"
    exit 1
  fi
  echo -e "${GREEN}✓ Server is running${NC}"
}

# ================================================
# PHASE 1: HEALTH CHECK
# ================================================

test_health_check() {
  print_test "Health Check"

  response=$(curl -s "${BASE_URL}/health")

  if echo "$response" | grep -q '"success":true'; then
    print_success "Health check passed"
    echo "Response: $response"
  else
    print_error "Health check failed"
    echo "Response: $response"
  fi
}

# ================================================
# PHASE 2: AUTHENTICATION
# ================================================

test_login() {
  print_test "POST /auth/login - Super Admin Login"

  response=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
      "username": "superadmin",
      "password": "Change@Me#2025"
    }')

  if echo "$response" | grep -q '"success":true'; then
    TOKEN=$(echo "$response" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    print_success "Login successful"
    echo "Token: ${TOKEN:0:30}..."
    return 0
  else
    print_error "Login failed"
    echo "Response: $response"
    exit 1
  fi
}

# ================================================
# PHASE 3: USER MANAGEMENT
# ================================================

test_list_users() {
  print_test "GET /users - List all users"

  response=$(curl -s -X GET "${BASE_URL}/users" \
    -H "Authorization: Bearer $TOKEN")

  if echo "$response" | grep -q '"success":true'; then
    user_count=$(echo "$response" | grep -o '"id":' | wc -l)
    print_success "Listed $user_count users"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "List users failed"
    echo "Response: $response"
  fi
}

test_create_user() {
  print_test "POST /users - Create test user"

  timestamp=$(date +%s)
  username="testuser_${timestamp}"
  email="testuser${timestamp}@mgb.gov.ph"

  response=$(curl -s -X POST "${BASE_URL}/users" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$username\",
      \"email\": \"$email\",
      \"full_name\": \"Test User\",
      \"password\": \"TestPass@123\",
      \"role\": \"USER\"
    }")

  if echo "$response" | grep -q '"success":true'; then
    TEST_USER_ID=$(echo "$response" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    print_success "User created with ID: $TEST_USER_ID"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Create user failed"
    echo "Response: $response"
  fi
}

test_get_user() {
  print_test "GET /users/:id - Get user by ID"

  if [ -z "$TEST_USER_ID" ]; then
    echo "Skipping: No test user ID available"
    return
  fi

  response=$(curl -s -X GET "${BASE_URL}/users/${TEST_USER_ID}" \
    -H "Authorization: Bearer $TOKEN")

  if echo "$response" | grep -q '"success":true'; then
    print_success "Retrieved user"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Get user failed"
    echo "Response: $response"
  fi
}

test_update_user() {
  print_test "PUT /users/:id - Update user"

  if [ -z "$TEST_USER_ID" ]; then
    echo "Skipping: No test user ID available"
    return
  fi

  response=$(curl -s -X PUT "${BASE_URL}/users/${TEST_USER_ID}" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "full_name": "Updated Test User"
    }')

  if echo "$response" | grep -q '"success":true'; then
    print_success "User updated"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Update user failed"
    echo "Response: $response"
  fi
}

# ================================================
# PHASE 4: MRFC MANAGEMENT
# ================================================

test_list_mrfcs() {
  print_test "GET /mrfcs - List all MRFCs"

  response=$(curl -s -X GET "${BASE_URL}/mrfcs" \
    -H "Authorization: Bearer $TOKEN")

  if echo "$response" | grep -q '"success":true'; then
    mrfc_count=$(echo "$response" | grep -o '"id":' | wc -l)
    print_success "Listed $mrfc_count MRFCs"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "List MRFCs failed"
    echo "Response: $response"
  fi
}

test_create_mrfc() {
  print_test "POST /mrfcs - Create test MRFC"

  timestamp=$(date +%s)
  name="Test MRFC ${timestamp}"

  response=$(curl -s -X POST "${BASE_URL}/mrfcs" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"$name\",
      \"municipality\": \"Test Municipality\",
      \"province\": \"Test Province\",
      \"description\": \"Test MRFC created at $(date)\"
    }")

  if echo "$response" | grep -q '"success":true'; then
    TEST_MRFC_ID=$(echo "$response" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
    print_success "MRFC created with ID: $TEST_MRFC_ID"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Create MRFC failed"
    echo "Response: $response"
  fi
}

test_get_mrfc() {
  print_test "GET /mrfcs/:id - Get MRFC by ID"

  if [ -z "$TEST_MRFC_ID" ]; then
    echo "Skipping: No test MRFC ID available"
    return
  fi

  response=$(curl -s -X GET "${BASE_URL}/mrfcs/${TEST_MRFC_ID}" \
    -H "Authorization: Bearer $TOKEN")

  if echo "$response" | grep -q '"success":true'; then
    print_success "Retrieved MRFC"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Get MRFC failed"
    echo "Response: $response"
  fi
}

test_update_mrfc() {
  print_test "PUT /mrfcs/:id - Update MRFC"

  if [ -z "$TEST_MRFC_ID" ]; then
    echo "Skipping: No test MRFC ID available"
    return
  fi

  response=$(curl -s -X PUT "${BASE_URL}/mrfcs/${TEST_MRFC_ID}" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "description": "Updated test description"
    }')

  if echo "$response" | grep -q '"success":true'; then
    print_success "MRFC updated"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Update MRFC failed"
    echo "Response: $response"
  fi
}

test_grant_mrfc_access() {
  print_test "POST /users/:id/grant-mrfc-access - Grant MRFC access"

  if [ -z "$TEST_USER_ID" ] || [ -z "$TEST_MRFC_ID" ]; then
    echo "Skipping: Test data not available"
    return
  fi

  response=$(curl -s -X POST "${BASE_URL}/users/${TEST_USER_ID}/grant-mrfc-access" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"mrfc_ids\": [${TEST_MRFC_ID}]
    }")

  if echo "$response" | grep -q '"success":true'; then
    print_success "MRFC access granted"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Grant MRFC access failed"
    echo "Response: $response"
  fi
}

# ================================================
# CLEANUP TESTS
# ================================================

test_delete_user() {
  print_test "DELETE /users/:id - Delete test user"

  if [ -z "$TEST_USER_ID" ]; then
    echo "Skipping: No test user ID available"
    return
  fi

  response=$(curl -s -X DELETE "${BASE_URL}/users/${TEST_USER_ID}" \
    -H "Authorization: Bearer $TOKEN")

  if echo "$response" | grep -q '"success":true'; then
    print_success "User deleted (soft delete)"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Delete user failed"
    echo "Response: $response"
  fi
}

test_delete_mrfc() {
  print_test "DELETE /mrfcs/:id - Delete test MRFC"

  if [ -z "$TEST_MRFC_ID" ]; then
    echo "Skipping: No test MRFC ID available"
    return
  fi

  response=$(curl -s -X DELETE "${BASE_URL}/mrfcs/${TEST_MRFC_ID}" \
    -H "Authorization: Bearer $TOKEN")

  if echo "$response" | grep -q '"success":true'; then
    print_success "MRFC deleted (soft delete)"
    echo "Response (truncated): ${response:0:200}..."
  else
    print_error "Delete MRFC failed"
    echo "Response: $response"
  fi
}

# ================================================
# SUMMARY REPORT
# ================================================

print_summary() {
  print_header "TEST SUMMARY REPORT"

  echo -e "\nTotal Tests: $TOTAL"
  echo -e "${GREEN}Passed: $PASSED${NC}"
  echo -e "${RED}Failed: $FAILED${NC}"

  if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✓ ALL TESTS PASSED!${NC}"
    return 0
  else
    echo -e "\n${RED}✗ SOME TESTS FAILED${NC}"
    return 1
  fi
}

# ================================================
# MAIN EXECUTION
# ================================================

main() {
  clear
  print_header "MGB MRFC MANAGER - API TEST SUITE"

  # Check server
  check_server

  # Phase 1: Health Check
  print_header "PHASE 1: HEALTH CHECK"
  test_health_check

  # Phase 2: Authentication
  print_header "PHASE 2: AUTHENTICATION"
  test_login

  # Phase 3: User Management
  print_header "PHASE 3: USER MANAGEMENT"
  test_list_users
  test_create_user
  test_get_user
  test_update_user

  # Phase 4: MRFC Management
  print_header "PHASE 4: MRFC MANAGEMENT"
  test_list_mrfcs
  test_create_mrfc
  test_get_mrfc
  test_update_mrfc
  test_grant_mrfc_access

  # Cleanup
  print_header "CLEANUP"
  test_delete_user
  test_delete_mrfc

  # Summary
  print_summary
}

# Run main function
main
