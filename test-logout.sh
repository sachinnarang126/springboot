#!/bin/bash

# Test script for logout functionality
# This script tests the complete logout flow

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
USERNAME="testuser"
PASSWORD="password123"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Testing Logout Functionality${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# Step 1: Login
echo -e "${YELLOW}Step 1: Logging in...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")

echo "Login Response: $LOGIN_RESPONSE"
echo ""

# Extract token from response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Failed to login or extract token${NC}"
    echo "Please ensure:"
    echo "  1. The application is running on port 8080"
    echo "  2. A user with username '${USERNAME}' and password '${PASSWORD}' exists"
    echo "  3. You can register this user using the /api/auth/register endpoint"
    exit 1
fi

echo -e "${GREEN}✓ Successfully logged in${NC}"
echo "Token: ${TOKEN:0:50}..."
echo ""

# Step 2: Test authenticated endpoint (should work)
echo -e "${YELLOW}Step 2: Testing authenticated endpoint (should work)...${NC}"
CONNECTED_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BASE_URL}/api/auth/connected" \
  -H "Authorization: Bearer ${TOKEN}")

HTTP_STATUS=$(echo "$CONNECTED_RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
RESPONSE_BODY=$(echo "$CONNECTED_RESPONSE" | sed '/HTTP_STATUS:/d')

echo "Response: $RESPONSE_BODY"
echo "HTTP Status: $HTTP_STATUS"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "${GREEN}✓ Authenticated request successful${NC}"
else
    echo -e "${RED}✗ Authenticated request failed${NC}"
    exit 1
fi
echo ""

# Step 3: Logout
echo -e "${YELLOW}Step 3: Logging out...${NC}"
LOGOUT_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/api/auth/logout" \
  -H "Authorization: Bearer ${TOKEN}")

HTTP_STATUS=$(echo "$LOGOUT_RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
RESPONSE_BODY=$(echo "$LOGOUT_RESPONSE" | sed '/HTTP_STATUS:/d')

echo "Response: $RESPONSE_BODY"
echo "HTTP Status: $HTTP_STATUS"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "${GREEN}✓ Successfully logged out${NC}"
else
    echo -e "${RED}✗ Logout failed${NC}"
    exit 1
fi
echo ""

# Step 4: Try to use the same token again (should fail)
echo -e "${YELLOW}Step 4: Testing with logged-out token (should fail)...${NC}"
CONNECTED_RESPONSE_2=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BASE_URL}/api/auth/connected" \
  -H "Authorization: Bearer ${TOKEN}")

HTTP_STATUS=$(echo "$CONNECTED_RESPONSE_2" | grep "HTTP_STATUS:" | cut -d: -f2)
RESPONSE_BODY=$(echo "$CONNECTED_RESPONSE_2" | sed '/HTTP_STATUS:/d')

echo "Response: $RESPONSE_BODY"
echo "HTTP Status: $HTTP_STATUS"
echo ""

if [ "$HTTP_STATUS" = "403" ] || [ "$HTTP_STATUS" = "401" ]; then
    echo -e "${GREEN}✓ Token successfully invalidated (received ${HTTP_STATUS})${NC}"
else
    echo -e "${RED}✗ Token still valid after logout (unexpected status: ${HTTP_STATUS})${NC}"
    exit 1
fi
echo ""

# Summary
echo -e "${YELLOW}========================================${NC}"
echo -e "${GREEN}✓ All logout tests passed!${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Summary:"
echo "  1. Login successful"
echo "  2. Authenticated request successful"
echo "  3. Logout successful"
echo "  4. Token invalidated after logout"
echo ""
echo -e "${GREEN}Logout functionality is working correctly!${NC}"

