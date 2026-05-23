# API Integration Test Script for Windows PowerShell
# Tests all critical endpoints to ensure frontend-backend alignment

Write-Host "🧪 Job Applications Portal - API Integration Tests" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

$BASE_URL = "http://localhost:8081/api"
$PASSED = 0
$FAILED = 0

# Function to test endpoint
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Description,
        [string]$Data = $null,
        [string]$Token = $null
    )
    
    Write-Host "Testing: $Description ... " -NoNewline
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    try {
        $uri = "$BASE_URL$Endpoint"
        
        if ($Data) {
            $response = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -Body $Data -UseBasicParsing -ErrorAction Stop
        } else {
            $response = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -UseBasicParsing -ErrorAction Stop
        }
        
        if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
            Write-Host "✓ PASSED" -ForegroundColor Green -NoNewline
            Write-Host " (HTTP $($response.StatusCode))"
            $script:PASSED++
            return $true
        } else {
            Write-Host "✗ FAILED" -ForegroundColor Red -NoNewline
            Write-Host " (HTTP $($response.StatusCode))"
            $script:FAILED++
            return $false
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "✗ FAILED" -ForegroundColor Red -NoNewline
        Write-Host " (HTTP $statusCode)"
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Yellow
        $script:FAILED++
        return $false
    }
}

# Test 1: Backend Availability
Write-Host ""
Write-Host "📍 Testing Backend Availability" -ForegroundColor Yellow
Write-Host "--------------------------------"
Test-Endpoint -Method "GET" -Endpoint "/jobs" -Description "Backend is running"

# Test 2: Registration
Write-Host ""
Write-Host "🔐 Testing Authentication Endpoints" -ForegroundColor Yellow
Write-Host "-----------------------------------"

$timestamp = [int][double]::Parse((Get-Date -UFormat %s))
$TEST_EMAIL = "test_${timestamp}@example.com"
$TEST_PASSWORD = "Test@123"

$registerData = @{
    name = "Test User"
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
    role = "CANDIDATE"
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Endpoint "/users/register" -Description "User registration" -Data $registerData

# Test 3: Login (should fail - email not verified)
Write-Host ""
$loginData = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
} | ConvertTo-Json

Write-Host "Testing: Login (should fail - not verified) ... " -NoNewline
try {
    $uri = "$BASE_URL/users/login"
    $response = Invoke-WebRequest -Uri $uri -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginData -UseBasicParsing -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✗ FAILED" -ForegroundColor Red
        Write-Host "  Should not allow unverified login"
        $script:FAILED++
    }
} catch {
    Write-Host "✓ PASSED" -ForegroundColor Green
    Write-Host "  Correctly rejected unverified user"
    $script:PASSED++
}

# Test 4: Request email verification
Write-Host ""
$verificationRequest = @{
    email = $TEST_EMAIL
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Endpoint "/users/request-email-verification" -Description "Request email verification" -Data $verificationRequest

# Note about manual verification
Write-Host ""
Write-Host "ℹ️  Note: Email verification with OTP requires manual testing" -ForegroundColor Yellow
Write-Host "   1. Check email at: $TEST_EMAIL"
Write-Host "   2. Use OTP with: POST /verification/verify-email"
Write-Host "   3. Body: { `"email`": `"$TEST_EMAIL`", `"otpCode`": `"123456`" }"

# Test 5: Resend verification
Write-Host ""
Test-Endpoint -Method "POST" -Endpoint "/verification/resend-verification" -Description "Resend verification code" -Data $verificationRequest

# Test 6: Get jobs (public endpoint)
Write-Host ""
Write-Host "💼 Testing Job Endpoints" -ForegroundColor Yellow
Write-Host "------------------------"
Test-Endpoint -Method "GET" -Endpoint "/jobs" -Description "Get public jobs list"
Test-Endpoint -Method "GET" -Endpoint "/jobs?page=0&size=10" -Description "Get jobs with pagination"

# Test 7: Verification status
Write-Host ""
Test-Endpoint -Method "GET" -Endpoint "/verification/status/$TEST_EMAIL" -Description "Check verification status"

# Summary
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "📊 Test Results Summary" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Passed: " -NoNewline
Write-Host "$PASSED" -ForegroundColor Green
Write-Host "Failed: " -NoNewline
Write-Host "$FAILED" -ForegroundColor Red
Write-Host "Total:  $($PASSED + $FAILED)"
Write-Host ""

if ($FAILED -eq 0) {
    Write-Host "✅ All tests passed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:"
    Write-Host "1. Start frontend: cd client && npm run dev"
    Write-Host "2. Open: http://localhost:5173"
    Write-Host "3. Register a real user with your email"
    Write-Host "4. Check inbox for OTP"
    Write-Host "5. Complete verification"
    Write-Host "6. Login and test full application"
    exit 0
} else {
    Write-Host "❌ Some tests failed" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:"
    Write-Host "1. Ensure backend is running: cd server\jta && .\mvnw.cmd spring-boot:run"
    Write-Host "2. Check PostgreSQL is running and database exists"
    Write-Host "3. Verify application.properties configuration"
    Write-Host "4. Check backend logs for errors"
    exit 1
}
