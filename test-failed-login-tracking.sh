#!/bin/bash

# Test script for failed login tracking
# This script tests that failed login attempts are properly tracked in the database

echo "========================================="
echo "Failed Login Tracking Test"
echo "========================================="
echo ""

# Configuration
USERNAME="testuser"
CORRECT_PASSWORD="testpass123"
WRONG_PASSWORD="wrongpass"
API_URL="http://localhost:8080"

echo "🧪 Test Setup"
echo "Creating test user: $USERNAME"
echo ""

# Register test user
echo "Step 1: Registering test user..."
curl -s -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$CORRECT_PASSWORD\",\"email\":\"test@example.com\",\"name\":\"Test User\"}" | jq '.'

echo ""
echo "========================================="
echo "🔴 Test 1: Failed Login Tracking"
echo "========================================="
echo ""

# Try wrong password 3 times
echo "Attempting 3 failed logins with wrong password..."
for i in {1..3}; do
  echo "Attempt $i:"
  curl -s -X POST $API_URL/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$WRONG_PASSWORD\"}" | jq '.success, .error'
  echo ""
done

echo "Checking database for failed attempts..."
mysql -u springuser -pspringpass -D userdb -e "
  SELECT u.username, c.failed_login_attempts, c.locked_until 
  FROM users u 
  JOIN user_credentials c ON u.id = c.user_id 
  WHERE u.username = '$USERNAME';
" 2>/dev/null

echo ""
echo "Expected: failed_login_attempts = 3, locked_until = NULL"
echo ""

echo "========================================="
echo "🔒 Test 2: Account Lockout (5 attempts)"
echo "========================================="
echo ""

echo "Attempting 2 more failed logins (total 5)..."
for i in {4..5}; do
  echo "Attempt $i:"
  curl -s -X POST $API_URL/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$WRONG_PASSWORD\"}" | jq '.success, .error'
  echo ""
done

echo "Checking database for lockout..."
mysql -u springuser -pspringpass -D userdb -e "
  SELECT u.username, c.failed_login_attempts, c.locked_until 
  FROM users u 
  JOIN user_credentials c ON u.id = c.user_id 
  WHERE u.username = '$USERNAME';
" 2>/dev/null

echo ""
echo "Expected: failed_login_attempts = 5, locked_until = (30 mins from now)"
echo ""

echo "========================================="
echo "🚫 Test 3: Locked Account Rejects Login"
echo "========================================="
echo ""

echo "Trying to login with CORRECT password while locked..."
curl -s -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$CORRECT_PASSWORD\"}" | jq '.'

echo ""
echo "Expected: Login should fail even with correct password!"
echo ""

echo "========================================="
echo "✅ Test 4: Successful Login Resets Counter"
echo "========================================="
echo ""

echo "Manually unlocking account..."
mysql -u springuser -pspringpass -D userdb -e "
  UPDATE user_credentials c
  JOIN users u ON u.id = c.user_id
  SET c.failed_login_attempts = 3, c.locked_until = NULL
  WHERE u.username = '$USERNAME';
" 2>/dev/null

echo "Current failed attempts: 3"
echo ""

echo "Logging in with correct password..."
LOGIN_RESPONSE=$(curl -s -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$CORRECT_PASSWORD\"}")

echo "$LOGIN_RESPONSE" | jq '.success, .message'
echo ""

echo "Checking database after successful login..."
mysql -u springuser -pspringpass -D userdb -e "
  SELECT u.username, c.failed_login_attempts, c.locked_until 
  FROM users u 
  JOIN user_credentials c ON u.id = c.user_id 
  WHERE u.username = '$USERNAME';
" 2>/dev/null

echo ""
echo "Expected: failed_login_attempts = 0 (RESET!), locked_until = NULL"
echo ""

echo "========================================="
echo "🧹 Cleanup"
echo "========================================="
echo ""

echo "Deleting test user..."
mysql -u springuser -pspringpass -D userdb -e "
  DELETE FROM user_credentials WHERE user_id IN (SELECT id FROM users WHERE username = '$USERNAME');
  DELETE FROM users WHERE username = '$USERNAME';
" 2>/dev/null

echo "Test completed!"
echo ""
echo "========================================="
echo "✅ Summary"
echo "========================================="
echo "If you saw:"
echo "  - Attempts increment from 0 to 3"
echo "  - Account locks at 5 attempts"
echo "  - Correct password rejected when locked"
echo "  - Counter resets to 0 on successful login"
echo ""
echo "Then the fix is working! 🎉"
echo "========================================="


