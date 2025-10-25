# ================================================
# MGB MRFC Manager - API Testing Script (PowerShell)
# ================================================
# This script tests all implemented endpoints
# Run: powershell -ExecutionPolicy Bypass -File test-api.ps1

# API Configuration
$BASE_URL = "http://localhost:3000/api/v1"
$TOKEN = ""
$TEST_USER_ID = ""
$TEST_MRFC_ID = ""

# Test counters
$PASSED = 0
$FAILED = 0
$TOTAL = 0

# ================================================
# Helper Functions
# ================================================

function Print-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
}

function Print-Test {
    param([string]$Message)
    $script:TOTAL++
    Write-Host ""
    Write-Host "[Test $TOTAL] $Message" -ForegroundColor Yellow
}

function Print-Success {
    param([string]$Message)
    $script:PASSED++
    Write-Host "[PASS] $Message" -ForegroundColor Green
}

function Print-Error {
    param([string]$Message)
    $script:FAILED++
    Write-Host "[FAIL] $Message" -ForegroundColor Red
}

function Test-ServerRunning {
    Write-Host "Checking if server is running..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/health" -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✓ Server is running" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "ERROR: Server is not running on $BASE_URL" -ForegroundColor Red
        Write-Host "Please start the server first with: npm run dev" -ForegroundColor Red
        return $false
    }
}

# ================================================
# PHASE 1: HEALTH CHECK
# ================================================

function Test-HealthCheck {
    Print-Test "Health Check"

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/health" -TimeoutSec 5
        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "Health check passed"
            Write-Host "Response: $($json | ConvertTo-Json)"
        } else {
            Print-Error "Health check failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Health check error: $_"
    }
}

# ================================================
# PHASE 2: AUTHENTICATION
# ================================================

function Test-Login {
    Print-Test "POST /auth/login - Super Admin Login"

    try {
        $body = @{
            username = "superadmin"
            password = "Change@Me#2025"
        } | ConvertTo-Json

        $response = Invoke-WebRequest -Uri "$BASE_URL/auth/login" `
            -Method POST `
            -Headers @{"Content-Type" = "application/json"} `
            -Body $body `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            $script:TOKEN = $json.data.token
            Print-Success "Login successful"
            Write-Host "Token: $($TOKEN.Substring(0, 30))..."
            return $true
        } else {
            Print-Error "Login failed"
            Write-Host "Response: $($response.Content)"
            return $false
        }
    } catch {
        Print-Error "Login error: $_"
        return $false
    }
}

# ================================================
# PHASE 3: USER MANAGEMENT
# ================================================

function Test-ListUsers {
    Print-Test "GET /users - List all users"

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/users" `
            -Headers @{"Authorization" = "Bearer $TOKEN"} `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            $userCount = $json.data.users.Count
            Print-Success "Listed $userCount users"
            Write-Host "Response: $($json.data | ConvertTo-Json | Select-Object -First 200)"
        } else {
            Print-Error "List users failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "List users error: $_"
    }
}

function Test-CreateUser {
    Print-Test "POST /users - Create test user"

    try {
        $timestamp = Get-Date -Format "yyyyMMddHHmmss"
        $username = "testuser_$timestamp"
        $email = "testuser$timestamp@mgb.gov.ph"

        $body = @{
            username  = $username
            email     = $email
            full_name = "Test User"
            password  = "TestPass@123"
            role      = "USER"
        } | ConvertTo-Json

        $response = Invoke-WebRequest -Uri "$BASE_URL/users" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $TOKEN"
                "Content-Type"  = "application/json"
            } `
            -Body $body `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            $script:TEST_USER_ID = $json.data.id
            Print-Success "User created with ID: $TEST_USER_ID"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Create user failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Create user error: $_"
    }
}

function Test-GetUser {
    Print-Test "GET /users/:id - Get user by ID"

    if ([string]::IsNullOrEmpty($TEST_USER_ID)) {
        Write-Host "Skipping: No test user ID available"
        return
    }

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/users/$TEST_USER_ID" `
            -Headers @{"Authorization" = "Bearer $TOKEN"} `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "Retrieved user"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Get user failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Get user error: $_"
    }
}

function Test-UpdateUser {
    Print-Test "PUT /users/:id - Update user"

    if ([string]::IsNullOrEmpty($TEST_USER_ID)) {
        Write-Host "Skipping: No test user ID available"
        return
    }

    try {
        $body = @{
            full_name = "Updated Test User"
        } | ConvertTo-Json

        $response = Invoke-WebRequest -Uri "$BASE_URL/users/$TEST_USER_ID" `
            -Method PUT `
            -Headers @{
                "Authorization" = "Bearer $TOKEN"
                "Content-Type"  = "application/json"
            } `
            -Body $body `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "User updated"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Update user failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Update user error: $_"
    }
}

# ================================================
# PHASE 4: MRFC MANAGEMENT
# ================================================

function Test-ListMrfcs {
    Print-Test "GET /mrfcs - List all MRFCs"

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/mrfcs" `
            -Headers @{"Authorization" = "Bearer $TOKEN"} `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            $mrfcCount = $json.data.mrfcs.Count
            Print-Success "Listed $mrfcCount MRFCs"
            Write-Host "Response: $($json.data | ConvertTo-Json | Select-Object -First 200)"
        } else {
            Print-Error "List MRFCs failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "List MRFCs error: $_"
    }
}

function Test-CreateMrfc {
    Print-Test "POST /mrfcs - Create test MRFC"

    try {
        $timestamp = Get-Date -Format "yyyyMMddHHmmss"
        $name = "Test MRFC $timestamp"

        $body = @{
            name         = $name
            municipality = "Test Municipality"
            province     = "Test Province"
            description  = "Test MRFC created at $(Get-Date)"
        } | ConvertTo-Json

        $response = Invoke-WebRequest -Uri "$BASE_URL/mrfcs" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $TOKEN"
                "Content-Type"  = "application/json"
            } `
            -Body $body `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            $script:TEST_MRFC_ID = $json.data.id
            Print-Success "MRFC created with ID: $TEST_MRFC_ID"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Create MRFC failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Create MRFC error: $_"
    }
}

function Test-GetMrfc {
    Print-Test "GET /mrfcs/:id - Get MRFC by ID"

    if ([string]::IsNullOrEmpty($TEST_MRFC_ID)) {
        Write-Host "Skipping: No test MRFC ID available"
        return
    }

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/mrfcs/$TEST_MRFC_ID" `
            -Headers @{"Authorization" = "Bearer $TOKEN"} `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "Retrieved MRFC"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Get MRFC failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Get MRFC error: $_"
    }
}

function Test-UpdateMrfc {
    Print-Test "PUT /mrfcs/:id - Update MRFC"

    if ([string]::IsNullOrEmpty($TEST_MRFC_ID)) {
        Write-Host "Skipping: No test MRFC ID available"
        return
    }

    try {
        $body = @{
            description = "Updated test description"
        } | ConvertTo-Json

        $response = Invoke-WebRequest -Uri "$BASE_URL/mrfcs/$TEST_MRFC_ID" `
            -Method PUT `
            -Headers @{
                "Authorization" = "Bearer $TOKEN"
                "Content-Type"  = "application/json"
            } `
            -Body $body `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "MRFC updated"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Update MRFC failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Update MRFC error: $_"
    }
}

function Test-GrantMrfcAccess {
    Print-Test "POST /users/:id/grant-mrfc-access - Grant MRFC access"

    if ([string]::IsNullOrEmpty($TEST_USER_ID) -or [string]::IsNullOrEmpty($TEST_MRFC_ID)) {
        Write-Host "Skipping: Test data not available"
        return
    }

    try {
        $body = @{
            mrfc_ids = @($TEST_MRFC_ID)
        } | ConvertTo-Json

        $response = Invoke-WebRequest -Uri "$BASE_URL/users/$TEST_USER_ID/grant-mrfc-access" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $TOKEN"
                "Content-Type"  = "application/json"
            } `
            -Body $body `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "MRFC access granted"
            Write-Host "Response: $($json.data | ConvertTo-Json)"
        } else {
            Print-Error "Grant MRFC access failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Grant MRFC access error: $_"
    }
}

# ================================================
# CLEANUP TESTS
# ================================================

function Test-DeleteUser {
    Print-Test "DELETE /users/:id - Delete test user"

    if ([string]::IsNullOrEmpty($TEST_USER_ID)) {
        Write-Host "Skipping: No test user ID available"
        return
    }

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/users/$TEST_USER_ID" `
            -Method DELETE `
            -Headers @{"Authorization" = "Bearer $TOKEN"} `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "User deleted (soft delete)"
            Write-Host "Response: $($json | ConvertTo-Json)"
        } else {
            Print-Error "Delete user failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Delete user error: $_"
    }
}

function Test-DeleteMrfc {
    Print-Test "DELETE /mrfcs/:id - Delete test MRFC"

    if ([string]::IsNullOrEmpty($TEST_MRFC_ID)) {
        Write-Host "Skipping: No test MRFC ID available"
        return
    }

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/mrfcs/$TEST_MRFC_ID" `
            -Method DELETE `
            -Headers @{"Authorization" = "Bearer $TOKEN"} `
            -TimeoutSec 5

        $json = $response.Content | ConvertFrom-Json

        if ($json.success) {
            Print-Success "MRFC deleted (soft delete)"
            Write-Host "Response: $($json | ConvertTo-Json)"
        } else {
            Print-Error "Delete MRFC failed"
            Write-Host "Response: $($response.Content)"
        }
    } catch {
        Print-Error "Delete MRFC error: $_"
    }
}

# ================================================
# SUMMARY REPORT
# ================================================

function Print-Summary {
    Print-Header "TEST SUMMARY REPORT"

    Write-Host ""
    Write-Host "Total Tests: $TOTAL"
    Write-Host "Passed: $PASSED" -ForegroundColor Green
    Write-Host "Failed: $FAILED" -ForegroundColor Red
    Write-Host ""

    if ($FAILED -eq 0) {
        Write-Host "✓ ALL TESTS PASSED!" -ForegroundColor Green
    } else {
        Write-Host "✗ SOME TESTS FAILED" -ForegroundColor Red
    }
}

# ================================================
# MAIN EXECUTION
# ================================================

function Main {
    Clear-Host
    Print-Header "MGB MRFC MANAGER - API TEST SUITE"

    # Check server
    if (-not (Test-ServerRunning)) {
        pause
        exit 1
    }

    # Phase 1: Health Check
    Print-Header "PHASE 1: HEALTH CHECK"
    Test-HealthCheck

    # Phase 2: Authentication
    Print-Header "PHASE 2: AUTHENTICATION"
    if (-not (Test-Login)) {
        pause
        exit 1
    }

    # Phase 3: User Management
    Print-Header "PHASE 3: USER MANAGEMENT"
    Test-ListUsers
    Test-CreateUser
    Test-GetUser
    Test-UpdateUser

    # Phase 4: MRFC Management
    Print-Header "PHASE 4: MRFC MANAGEMENT"
    Test-ListMrfcs
    Test-CreateMrfc
    Test-GetMrfc
    Test-UpdateMrfc
    Test-GrantMrfcAccess

    # Cleanup
    Print-Header "CLEANUP"
    Test-DeleteUser
    Test-DeleteMrfc

    # Summary
    Print-Summary

    Write-Host ""
    Read-Host "Press Enter to exit"
}

# Run main function
Main
