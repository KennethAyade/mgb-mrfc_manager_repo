@echo off
REM ================================================
REM MGB MRFC Manager - API Testing Script (Windows)
REM ================================================
REM This script tests all implemented endpoints
REM Run: test-api.bat

setlocal enabledelayedexpansion

REM API Configuration
set "BASE_URL=http://localhost:3000/api/v1"
set "TOKEN="
set "TEST_USER_ID="
set "TEST_MRFC_ID="

REM Test counters
set "PASSED=0"
set "FAILED=0"
set "TOTAL=0"

REM ================================================
REM Color codes (Windows 10+)
REM ================================================
for /F %%A in ('echo prompt $H ^| cmd') do set "BS=%%A"

REM ================================================
REM Helper Functions
REM ================================================

:print_header
echo.
echo ================================
echo %~1
echo ================================
exit /b

:check_server
echo Checking if server is running...
curl -s "%BASE_URL%/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Server is not running on %BASE_URL%
    echo Please start the server first with: npm run dev
    pause
    exit /b 1
)
echo [SUCCESS] Server is running
exit /b

:test_health_check
set /a TOTAL+=1
echo.
echo [Test !TOTAL!] Health Check
curl -s "%BASE_URL%/health" > temp_response.json
findstr /R "success" temp_response.json >nul
if errorlevel 1 (
    set /a FAILED+=1
    echo [FAIL] Health check failed
    type temp_response.json
) else (
    set /a PASSED+=1
    echo [PASS] Health check passed
    type temp_response.json
)
del temp_response.json
exit /b

:test_login
set /a TOTAL+=1
echo.
echo [Test !TOTAL!] POST /auth/login - Super Admin Login
curl -s -X POST "%BASE_URL%/auth/login" ^
    -H "Content-Type: application/json" ^
    -d "{\"username\": \"superadmin\", \"password\": \"Change@Me#2025\"}" > temp_response.json

findstr /R "success" temp_response.json >nul
if errorlevel 1 (
    set /a FAILED+=1
    echo [FAIL] Login failed
    type temp_response.json
    del temp_response.json
    exit /b 1
) else (
    set /a PASSED+=1
    echo [PASS] Login successful

    REM Extract token from response
    for /F "tokens=2 delims=:" %%A in ('findstr /R "\"token\":" temp_response.json') do (
        set "TOKEN=%%A"
        set "TOKEN=!TOKEN:,=!"
        set "TOKEN=!TOKEN:"=!"
        set "TOKEN=!TOKEN: =!"
    )

    echo Token: !TOKEN:~0,30!...
    type temp_response.json
)
del temp_response.json
exit /b

:test_list_users
set /a TOTAL+=1
echo.
echo [Test !TOTAL!] GET /users - List all users
curl -s -X GET "%BASE_URL%/users" ^
    -H "Authorization: Bearer !TOKEN!" > temp_response.json

findstr /R "success" temp_response.json >nul
if errorlevel 1 (
    set /a FAILED+=1
    echo [FAIL] List users failed
) else (
    set /a PASSED+=1
    echo [PASS] Listed users
)
type temp_response.json
del temp_response.json
exit /b

:test_create_user
set /a TOTAL+=1
echo.
echo [Test !TOTAL!] POST /users - Create test user

REM Generate random number for unique username
for /F "tokens=1-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /F "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set timestamp=!mydate!!mytime!

set "username=testuser_!timestamp!"
set "email=testuser!timestamp!@mgb.gov.ph"

curl -s -X POST "%BASE_URL%/users" ^
    -H "Authorization: Bearer !TOKEN!" ^
    -H "Content-Type: application/json" ^
    -d "{\"username\": \"!username!\", \"email\": \"!email!\", \"full_name\": \"Test User\", \"password\": \"TestPass@123\", \"role\": \"USER\"}" > temp_response.json

findstr /R "success" temp_response.json >nul
if errorlevel 1 (
    set /a FAILED+=1
    echo [FAIL] Create user failed
) else (
    set /a PASSED+=1
    echo [PASS] User created

    REM Extract user ID
    for /F "tokens=2 delims=:" %%A in ('findstr /R "\"id\":" temp_response.json') do (
        set "TEST_USER_ID=%%A"
        set "TEST_USER_ID=!TEST_USER_ID:,=!"
        set "TEST_USER_ID=!TEST_USER_ID:"=!"
        set "TEST_USER_ID=!TEST_USER_ID: =!"
    )
    echo User ID: !TEST_USER_ID!
)
type temp_response.json
del temp_response.json
exit /b

:test_list_mrfcs
set /a TOTAL+=1
echo.
echo [Test !TOTAL!] GET /mrfcs - List all MRFCs
curl -s -X GET "%BASE_URL%/mrfcs" ^
    -H "Authorization: Bearer !TOKEN!" > temp_response.json

findstr /R "success" temp_response.json >nul
if errorlevel 1 (
    set /a FAILED+=1
    echo [FAIL] List MRFCs failed
) else (
    set /a PASSED+=1
    echo [PASS] Listed MRFCs
)
type temp_response.json
del temp_response.json
exit /b

:test_create_mrfc
set /a TOTAL+=1
echo.
echo [Test !TOTAL!] POST /mrfcs - Create test MRFC

REM Generate random number for unique name
for /F "tokens=1-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /F "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set timestamp=!mydate!!mytime!

set "name=Test MRFC !timestamp!"

curl -s -X POST "%BASE_URL%/mrfcs" ^
    -H "Authorization: Bearer !TOKEN!" ^
    -H "Content-Type: application/json" ^
    -d "{\"name\": \"!name!\", \"municipality\": \"Test Municipality\", \"province\": \"Test Province\", \"description\": \"Test MRFC created\"}" > temp_response.json

findstr /R "success" temp_response.json >nul
if errorlevel 1 (
    set /a FAILED+=1
    echo [FAIL] Create MRFC failed
) else (
    set /a PASSED+=1
    echo [PASS] MRFC created

    REM Extract MRFC ID
    for /F "tokens=2 delims=:" %%A in ('findstr /R "\"id\":" temp_response.json') do (
        set "TEST_MRFC_ID=%%A"
        set "TEST_MRFC_ID=!TEST_MRFC_ID:,=!"
        set "TEST_MRFC_ID=!TEST_MRFC_ID:"=!"
        set "TEST_MRFC_ID=!TEST_MRFC_ID: =!"
        goto break_loop
    )
    :break_loop
    echo MRFC ID: !TEST_MRFC_ID!
)
type temp_response.json
del temp_response.json
exit /b

:print_summary
echo.
echo ================================
echo TEST SUMMARY REPORT
echo ================================
echo.
echo Total Tests: !TOTAL!
echo Passed: !PASSED!
echo Failed: !FAILED!
echo.

if !FAILED! equ 0 (
    echo [SUCCESS] ALL TESTS PASSED!
) else (
    echo [FAIL] SOME TESTS FAILED
)
exit /b

REM ================================================
REM MAIN EXECUTION
REM ================================================

:main
cls
echo ================================
echo MGB MRFC MANAGER - API TEST SUITE
echo ================================

call :check_server
if errorlevel 1 exit /b 1

echo.
echo ================================
echo PHASE 1: HEALTH CHECK
echo ================================
call :test_health_check

echo.
echo ================================
echo PHASE 2: AUTHENTICATION
echo ================================
call :test_login
if errorlevel 1 exit /b 1

echo.
echo ================================
echo PHASE 3: USER MANAGEMENT
echo ================================
call :test_list_users
call :test_create_user

echo.
echo ================================
echo PHASE 4: MRFC MANAGEMENT
echo ================================
call :test_list_mrfcs
call :test_create_mrfc

call :print_summary

pause
