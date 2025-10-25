$BASE_URL = "http://localhost:3000/api/v1"
$PASSED = 0
$FAILED = 0

Write-Host "================================" -ForegroundColor Cyan
Write-Host "MGB MRFC API Test" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health
Write-Host "1. Health Check..." -ForegroundColor Yellow
try {
    $r = Invoke-WebRequest -Uri "$BASE_URL/health" -TimeoutSec 5
    Write-Host "[PASS] Server OK" -ForegroundColor Green
    $PASSED++
} catch {
    Write-Host "[FAIL] Server not running" -ForegroundColor Red
    $FAILED++
    exit 1
}

# Test 2: Login
Write-Host "2. Login..." -ForegroundColor Yellow
try {
    $body = @{"username" = "superadmin"; "password" = "Change@Me#2025"} | ConvertTo-Json
    $r = Invoke-WebRequest -Uri "$BASE_URL/auth/login" -Method POST -Headers @{"Content-Type" = "application/json"} -Body $body -TimeoutSec 5
    $j = $r.Content | ConvertFrom-Json
    if ($j.success) {
        $TOKEN = $j.data.token
        Write-Host "[PASS] Login OK" -ForegroundColor Green
        $PASSED++
    } else {
        Write-Host "[FAIL] Login" -ForegroundColor Red
        $FAILED++
        exit 1
    }
} catch {
    Write-Host "[FAIL] Login error: $_" -ForegroundColor Red
    $FAILED++
    exit 1
}

# Test 3: List Users
Write-Host "3. List Users..." -ForegroundColor Yellow
try {
    $r = Invoke-WebRequest -Uri "$BASE_URL/users" -Headers @{"Authorization" = "Bearer $TOKEN"} -TimeoutSec 5
    $j = $r.Content | ConvertFrom-Json
    if ($j.success) {
        Write-Host "[PASS] List Users OK" -ForegroundColor Green
        $PASSED++
    } else {
        Write-Host "[FAIL] List Users" -ForegroundColor Red
        $FAILED++
    }
} catch {
    Write-Host "[FAIL] List Users error: $_" -ForegroundColor Red
    $FAILED++
}

# Test 4: Create User
Write-Host "4. Create User..." -ForegroundColor Yellow
try {
    $ts = Get-Date -Format "yyyyMMddHHmmss"
    $body = @{
        "username" = "testuser$ts"
        "email" = "test$ts@mgb.gov.ph"
        "full_name" = "Test User"
        "password" = "TestPass@123"
        "role" = "USER"
    } | ConvertTo-Json

    $r = Invoke-WebRequest -Uri "$BASE_URL/users" -Method POST -Headers @{"Authorization" = "Bearer $TOKEN"; "Content-Type" = "application/json"} -Body $body -TimeoutSec 5
    $j = $r.Content | ConvertFrom-Json
    if ($j.success) {
        $UID = $j.data.id
        Write-Host "[PASS] Create User OK (ID: $UID)" -ForegroundColor Green
        $PASSED++
    } else {
        Write-Host "[FAIL] Create User" -ForegroundColor Red
        $FAILED++
    }
} catch {
    Write-Host "[FAIL] Create User error: $_" -ForegroundColor Red
    $FAILED++
}

# Test 5: List MRFCs
Write-Host "5. List MRFCs..." -ForegroundColor Yellow
try {
    $r = Invoke-WebRequest -Uri "$BASE_URL/mrfcs" -Headers @{"Authorization" = "Bearer $TOKEN"} -TimeoutSec 5
    $j = $r.Content | ConvertFrom-Json
    if ($j.success) {
        Write-Host "[PASS] List MRFCs OK" -ForegroundColor Green
        $PASSED++
    } else {
        Write-Host "[FAIL] List MRFCs" -ForegroundColor Red
        $FAILED++
    }
} catch {
    Write-Host "[FAIL] List MRFCs error: $_" -ForegroundColor Red
    $FAILED++
}

# Test 6: Create MRFC
Write-Host "6. Create MRFC..." -ForegroundColor Yellow
try {
    $ts = Get-Date -Format "yyyyMMddHHmmss"
    $body = @{
        "name" = "Test MRFC $ts"
        "municipality" = "Test City"
        "province" = "Test Province"
        "description" = "Test MRFC"
    } | ConvertTo-Json

    $r = Invoke-WebRequest -Uri "$BASE_URL/mrfcs" -Method POST -Headers @{"Authorization" = "Bearer $TOKEN"; "Content-Type" = "application/json"} -Body $body -TimeoutSec 5
    $j = $r.Content | ConvertFrom-Json
    if ($j.success) {
        $MID = $j.data.id
        Write-Host "[PASS] Create MRFC OK (ID: $MID)" -ForegroundColor Green
        $PASSED++
    } else {
        Write-Host "[FAIL] Create MRFC" -ForegroundColor Red
        $FAILED++
    }
} catch {
    Write-Host "[FAIL] Create MRFC error: $_" -ForegroundColor Red
    $FAILED++
}

# Summary
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Passed: $PASSED" -ForegroundColor Green
Write-Host "Failed: $FAILED" -ForegroundColor Red
Write-Host ""

if ($FAILED -eq 0) {
    Write-Host "SUCCESS! All tests passed!" -ForegroundColor Green
} else {
    Write-Host "Some tests failed" -ForegroundColor Red
}
