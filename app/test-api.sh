#!/bin/bash
# API Integration Test Script
# Tests all critical endpoints to ensure frontend-backend alignment

echo "🧪 Job Applications Portal - API Integration Tests"
echo "=================================================="
echo ""

BASE_URL="http://localhost:8081/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4
    local token=$5
    
    echo -n "Testing: $description ... "
    
    if [ -n "$token" ]; then
        headers=(-H "Authorization: Bearer $token")
    else
        headers=()
    fi
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            "${headers[@]}" \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            "${headers[@]}")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [[ "$http_code" =~ ^2[0-9]{2}$ ]]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (HTTP $http_code)"
        echo "Response: $body"
        ((FAILED++))
        return 1
    fi
}

# Test 1: Health Check (if available)
echo ""
echo "📍 Testing Backend Availability"
echo "--------------------------------"
test_endpoint "GET" "/jobs" "Backend is running" "" ""

# Test 2: Registration
echo ""
echo "🔐 Testing Authentication Endpoints"
echo "-----------------------------------"

TIMESTAMP=$(date +%s)
TEST_EMAIL="test_${TIMESTAMP}@example.com"
TEST_PASSWORD="Test@123"

REGISTER_DATA='{
  "name": "Test User",
  "email": "'$TEST_EMAIL'",
  "password": "'$TEST_PASSWORD'",
  "role": "CANDIDATE"
}'

test_endpoint "POST" "/users/register" "User registration" "$REGISTER_DATA" ""

# Test 3: Login (should fail - email not verified)
echo ""
LOGIN_DATA='{
  "email": "'$TEST_EMAIL'",
  "password": "'$TEST_PASSWORD'"
}'

echo -n "Testing: Login (should fail - not verified) ... "
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/users/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA")
http_code=$(echo "$response" | tail -n1)

if [[ "$http_code" != "200" ]]; then
    echo -e "${GREEN}✓ PASSED${NC} (Correctly rejected unverified user)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC} (Should not allow unverified login)"
    ((FAILED++))
fi

# Test 4: Request email verification
echo ""
VERIFICATION_REQUEST='{
  "email": "'$TEST_EMAIL'"
}'

test_endpoint "POST" "/users/request-email-verification" "Request email verification" "$VERIFICATION_REQUEST" ""

# Note: For full verification test, user needs to manually get OTP from email
echo ""
echo -e "${YELLOW}ℹ️  Note: Email verification with OTP requires manual testing${NC}"
echo "   1. Check email at: $TEST_EMAIL"
echo "   2. Use OTP with: POST /verification/verify-email"
echo "   3. Body: { \"email\": \"$TEST_EMAIL\", \"otpCode\": \"123456\" }"

# Test 5: Resend verification
echo ""
test_endpoint "POST" "/verification/resend-verification" "Resend verification code" "$VERIFICATION_REQUEST" ""

# Test 6: Get jobs (public endpoint)
echo ""
echo "💼 Testing Job Endpoints"
echo "------------------------"
test_endpoint "GET" "/jobs" "Get public jobs list" "" ""
test_endpoint "GET" "/jobs?page=0&size=10" "Get jobs with pagination" "" ""

# Test 7: Verification status
echo ""
test_endpoint "GET" "/verification/status/$TEST_EMAIL" "Check verification status" "" ""

# If we have a valid token (from previous successful login), test authenticated endpoints
# For now, we'll skip authenticated tests since we can't auto-verify email

echo ""
echo "=================================================="
echo "📊 Test Results Summary"
echo "=================================================="
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"
echo "Total:  $((PASSED + FAILED))"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Start frontend: cd client && npm run dev"
    echo "2. Open: http://localhost:5173"
    echo "3. Register a real user with your email"
    echo "4. Check inbox for OTP"
    echo "5. Complete verification"
    echo "6. Login and test full application"
    exit 0
else
    echo -e "${RED}❌ Some tests failed${NC}"
    echo ""
    echo "Troubleshooting:"
    echo "1. Ensure backend is running: cd server/jta && ./mvnw spring-boot:run"
    echo "2. Check PostgreSQL is running and database exists"
    echo "3. Verify application.properties configuration"
    echo "4. Check backend logs for errors"
    exit 1
fi
