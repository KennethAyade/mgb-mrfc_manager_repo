# Simple Test Script for MGB MRFC API
# Run: powershell -ExecutionPolicy Bypass -File test-simple.ps1

$BASE_URL = "http://localhost:3000/api/v1"
$PASSED = 0
$FAILED = 0

Write-Host "================================" -ForegroundColor Cyan
Write-Host "MGB MRFC API Test Suite" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check server
Write-Host "1. Checking if server is running..." -ForegroundColor Yellow
try {
    $health = Invoke-WebRequest -Uri "$BASE_URL/health" -TimeoutSec 5
    if ($health.StatusCode -eq 200) {
        Write-Host "[PASS] Server is running" -ForegroundColor Green
        $PASSED++
    }
}
catch {
    Write-Host "[FAIL] Server is not running. Start with: npm run dev" -ForegroundColor Red
    $FAILED++
    exit 1
}

# Login
Write-Host "2. Testing login..." -ForegroundColor Yellow
try {
    $login = Invoke-WebRequest -Uri "$BASE_URL/auth/login" `
        -Method POST `
        -Headers @{"Content-Type" = "application/json"} `
        -Body '{"username":"superadmin","password":"Change@Me#2025"}' `
        -TimeoutSec 5

    $json = $login.Content | ConvertFrom-Json
    if ($json.success) {
        $TOKEN = $json.data.token
        Write-Host "[PASS] Login successful" -ForegroundColor Green
        Write-Host "  Token: $($TOKEN.Substring(0, 30))..."
        $PASSED++
    }
    else {
        Write-Host "[FAIL] Login failed" -ForegroundColor Red
        $FAILED++
        exit 1
    }
}
catch {
    Write-Host "[FAIL] Login error: $_" -ForegroundColor Red
    $FAILED++
    exit 1
}

# List Users
Write-Host "3. Testing list users..." -ForegroundColor Yellow
try {
    $users = Invoke-WebRequest -Uri "$BASE_URL/users" `
        -Headers @{"Authorization" = "Bearer $TOKEN"} `
        -TimeoutSec 5

    $json = $users.Content | ConvertFrom-Json
    if ($json.success) {
        Write-Host "[PASS] Listed users" -ForegroundColor Green
        $PASSED++
    }
    else {
        Write-Host "[FAIL] List users failed" -ForegroundColor Red
        $FAILED++
    }
}
catch {
    Write-Host "[FAIL] List users error: $_" -ForegroundColor Red
    $FAILED++
}

# Create User
Write-Host "4. Testing create user..." -ForegroundColor Yellow
try {
    $timestamp = Get-Date -Format "yyyyMMddHHmmss"
    $createUser = Invoke-WebRequest -Uri "$BASE_URL/users" `
        -Method POST `
        -Headers @{
            "Authorization" = "Bearer $TOKEN"
            "Content-Type" = "application/json"
        } `
        -Body "{`"username`":`"testuser$timestamp`",`"email`":`"test$timestamp@mgb.gov.ph`",`"full_name`":`"Test User`",`"password`":`"TestPass@123`",`"role`":`"USER`"}" `
        -TimeoutSec 5

    $json = $createUser.Content | ConvertFrom-Json
    if ($json.success) {
        $USER_ID = $json.data.id
        Write-Host "[PASS] User created (ID: $USER_ID)" -ForegroundColor Green
        $PASSED++
    }
    else {
        Write-Host "[FAIL] Create user failed" -ForegroundColor Red
        $FAILED++
    }
}
catch {
    Write-Host "[FAIL] Create user error: $_" -ForegroundColor Red
    $FAILED++
}

# List MRFCs
Write-Host "5. Testing list MRFCs..." -ForegroundColor Yellow
try {
    $mrfcs = Invoke-WebRequest -Uri "$BASE_URL/mrfcs" `
        -Headers @{"Authorization" = "Bearer $TOKEN"} `
        -TimeoutSec 5

    $json = $mrfcs.Content | ConvertFrom-Json
    if ($json.success) {
        Write-Host "[PASS] Listed MRFCs" -ForegroundColor Green
        $PASSED++
    }
    else {
        Write-Host "[FAIL] List MRFCs failed" -ForegroundColor Red
        $FAILED++
    }
}
catch {
    Write-Host "[FAIL] List MRFCs error: $_" -ForegroundColor Red
    $FAILED++
}

# Create MRFC
Write-Host "6. Testing create MRFC..." -ForegroundColor Yellow
try {
    $timestamp = Get-Date -Format "yyyyMMddHHmmss"
    $createMrfc = Invoke-WebRequest -Uri "$BASE_URL/mrfcs" `
        -Method POST `
        -Headers @{
            "Authorization" = "Bearer $TOKEN"
            "Content-Type" = "application/json"
        } `
        -Body "{`"name`":`"Test MRFC $timestamp`",`"municipality`":`"Test Municipality`",`"province`":`"Test Province`",`"description`":`"Test MRFC`"}" `
        -TimeoutSec 5

    $json = $createMrfc.Content | ConvertFrom-Json
    if ($json.success) {
        $MRFC_ID = $json.data.id
        Write-Host "[PASS] MRFC created (ID: $MRFC_ID)" -ForegroundColor Green
        $PASSED++
    }
    else {
        Write-Host "[FAIL] Create MRFC failed" -ForegroundColor Red
        $FAILED++
    }
}
catch {
    Write-Host "[FAIL] Create MRFC error: $_" -ForegroundColor Red
    $FAILED++
}

# Summary
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Passed: $PASSED" -ForegroundColor Green
Write-Host "Failed: $FAILED" -ForegroundColor Red

if ($FAILED -eq 0) {
    Write-Host ""
    Write-Host "✓ ALL TESTS PASSED!" -ForegroundColor Green
}
else {
    Write-Host ""
    Write-Host "✗ SOME TESTS FAILED" -ForegroundColor Red
}

Write-Host ""
Read-Host "Press Enter to exit"
