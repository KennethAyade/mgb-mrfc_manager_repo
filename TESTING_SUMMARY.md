# Testing Summary - All Options Explained

## Overview

I've created **4 complete testing solutions** for you to test the API. Choose the one that works best for you!

---

## Solution 1: PowerShell Script (Windows - RECOMMENDED) ⭐

### File: `test-api.ps1`

**Run it:**
```powershell
powershell -ExecutionPolicy Bypass -File test-api.ps1
```

**Advantages:**
- ✅ Built-in on all Windows systems
- ✅ Modern scripting language
- ✅ Color-coded output (easy to read)
- ✅ Excellent error handling
- ✅ Automatic cleanup of test data

**What it does:**
1. Checks if server is running
2. Tests health endpoint
3. Logs in and gets JWT token
4. Creates test user
5. Tests all user management endpoints (list, get, update)
6. Creates test MRFC
7. Tests all MRFC endpoints (list, get, update)
8. Grants MRFC access to user
9. Cleans up test data (deletes user and MRFC)
10. Shows summary report

**Sample Output:**
```
================================
MGB MRFC MANAGER - API TEST SUITE
================================

[SUCCESS] Server is running
[PASS] Health check passed
[PASS] Login successful
[PASS] Listed 1 users
[PASS] User created with ID: 2
[PASS] Retrieved user
[PASS] User updated
[PASS] Listed 0 MRFCs
[PASS] MRFC created with ID: 1
[PASS] Retrieved MRFC
[PASS] MRFC updated
[PASS] MRFC access granted
[PASS] User deleted (soft delete)
[PASS] MRFC deleted (soft delete)

================================
TEST SUMMARY REPORT
================================

Total Tests: 12
Passed: 12
Failed: 0

✓ ALL TESTS PASSED!
```

---

## Solution 2: Batch Script (Windows Legacy)

### File: `test-api.bat`

**Run it:**
```cmd
test-api.bat
```

**Advantages:**
- ✅ Works on older Windows versions
- ✅ No additional software needed
- ✅ Simple to use

**Note:** Less sophisticated than PowerShell but still functional.

---

## Solution 3: Bash Script (Mac/Linux/Git Bash)

### File: `test-api.sh`

**Run it:**
```bash
bash test-api.sh
```

**Advantages:**
- ✅ Works on Mac, Linux, WSL
- ✅ Portable across platforms
- ✅ Standard shell scripting

---

## Solution 4: Manual Testing with cURL

### Using Command Line

**No script file needed - test individual endpoints:**

```bash
# 1. Check health
curl http://localhost:3000/api/v1/health

# 2. Login (get token)
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}'

# 3. Copy token from response, then use it in subsequent requests

# 4. List users
curl -X GET "http://localhost:3000/api/v1/users" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for all cURL examples.

---

## Test Credentials

```
Username: superadmin
Password: Change@Me#2025
Role: SUPER_ADMIN
```

---

## Pre-Test Checklist

- [ ] Server running: `npm run dev` (in `backend` folder)
- [ ] Database connected (check console output)
- [ ] Port 3000 accessible
- [ ] Test credentials are correct

### Verify Server is Running:
```bash
curl http://localhost:3000/api/v1/health
```

Expected response:
```json
{
  "success": true,
  "message": "MGB MRFC Manager API is running",
  "version": "1.0.0",
  "timestamp": "2025-10-25T10:30:00.000Z"
}
```

---

## What Gets Tested

### 12 Automated Tests (Scripts 1-3)

1. ✅ **Health Check** - Server status
2. ✅ **Login** - Get JWT token
3. ✅ **List Users** - Pagination & filtering
4. ✅ **Create User** - New user creation
5. ✅ **Get User** - User details retrieval
6. ✅ **Update User** - User information update
7. ✅ **List MRFCs** - MRFC listing
8. ✅ **Create MRFC** - New MRFC creation
9. ✅ **Get MRFC** - MRFC details
10. ✅ **Update MRFC** - MRFC information update
11. ✅ **Grant MRFC Access** - User MRFC permissions
12. ✅ **Delete User** - Soft delete cleanup
13. ✅ **Delete MRFC** - Soft delete cleanup

---

## Expected Results

### Success Scenario ✅
```
Total Tests: 12
Passed: 12
Failed: 0

✓ ALL TESTS PASSED!
```

### Failure Investigation
If any test fails:

1. **Check server logs** - See terminal where `npm run dev` is running
2. **Verify database** - Check if connected to Neon
3. **Check .env file** - All credentials in place
4. **Check API response** - Test script will print error details
5. **Try manual curl** - Isolate the issue

---

## Files Provided

| File | Description | Usage |
|------|-------------|-------|
| `test-api.ps1` | PowerShell automated tests | `powershell -ExecutionPolicy Bypass -File test-api.ps1` |
| `test-api.bat` | Windows batch tests | `test-api.bat` |
| `test-api.sh` | Bash automated tests | `bash test-api.sh` |
| `TESTING_GUIDE.md` | Detailed endpoint documentation | Read for cURL examples |
| `TEST_INSTRUCTIONS.md` | Step-by-step setup guide | Follow for detailed instructions |
| `QUICK_TEST_GUIDE.md` | 30-second quick reference | Get started fast |
| `TESTING_SUMMARY.md` | This file | Overview of all options |

---

## Quick Start

### Step 1: Start Server
```bash
cd backend
npm run dev
```

### Step 2: Wait for Output
```
Server running on port 3000
Database connected
```

### Step 3: Run Tests (New Terminal)

**Option A (PowerShell - Recommended):**
```powershell
powershell -ExecutionPolicy Bypass -File test-api.ps1
```

**Option B (Command Prompt):**
```cmd
test-api.bat
```

**Option C (Git Bash):**
```bash
bash test-api.sh
```

### Step 4: Review Results
- ✅ All green = Success!
- ❌ Any red = Debug using server logs

---

## Troubleshooting

### "Server is not running"
```bash
cd backend
npm run dev
```

### "Cannot find module" or compilation errors
```bash
cd backend
npm install
npm run dev
```

### "Database connection failed"
- Check `backend/.env` file exists
- Verify `DATABASE_URL` is correct
- Check internet connection (Neon is cloud-hosted)

### "Unauthorized" or "Invalid token"
- Ensure login succeeded
- Copy token correctly from login response
- Token has 24-hour expiration

### "Port 3000 already in use"
```powershell
# Find and kill the process
Get-Process | Where-Object {$_.Name -like "*node*"} | Stop-Process -Force

# Or restart the system
```

---

## Next Steps After Testing

### If All Tests Pass ✅
1. Review test output
2. Check database for test data
3. Proceed with implementation
4. Continue with next modules:
   - Proponent Management (5 endpoints)
   - Quarter Management (2 endpoints)
   - Agenda Management (5 endpoints)
   - And more...

### If Some Tests Fail ❌
1. Check error messages in test output
2. Review server logs (terminal running npm run dev)
3. Check API response details
4. Review relevant endpoint in [TESTING_GUIDE.md](TESTING_GUIDE.md)

---

## Advanced Testing

### Run Specific Test
Edit the script and comment out other tests, or manually run cURL commands.

### Performance Testing
Run multiple tests in parallel to test concurrency:
```bash
# Run 3 tests simultaneously
powershell -ExecutionPolicy Bypass -File test-api.ps1 &
powershell -ExecutionPolicy Bypass -File test-api.ps1 &
powershell -ExecutionPolicy Bypass -File test-api.ps1
```

### Load Testing
Use Apache Bench or similar tools:
```bash
ab -n 1000 -c 10 http://localhost:3000/api/v1/health
```

---

## Understanding Test Output

### [PASS]
- Test succeeded
- Endpoint working correctly
- Status code 2xx
- Response contains expected data

### [FAIL]
- Test failed
- Check:
  - Server running?
  - Database connected?
  - Correct token?
  - Valid input data?
  - Check server logs

### Token Output
```
Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
- First 30 characters shown (full token is much longer)
- Token valid for 24 hours
- Used in Authorization header for all requests

---

## Security Notes

- ✅ JWT tokens sent in Authorization header (secure)
- ✅ Passwords hashed with bcrypt
- ✅ Test data automatically cleaned up
- ✅ Super admin password in .env (secure)
- ⚠️ Never share tokens or credentials in production

---

## Postman Integration

For GUI-based testing:
1. Download [Postman](https://www.postman.com/downloads/)
2. See [TESTING_GUIDE.md](TESTING_GUIDE.md) for manual curl commands
3. Import each curl command into Postman
4. Set `Authorization: Bearer TOKEN` header
5. Test each endpoint

---

## Questions?

1. **Check TESTING_GUIDE.md** - Has detailed examples
2. **Check TEST_INSTRUCTIONS.md** - Step-by-step guide
3. **Check server logs** - Terminal running npm run dev
4. **Review script code** - Open test-api.ps1 to understand what it does

---

## Summary

| Test Method | Difficulty | Best For | Setup Time |
|------------|-----------|----------|-----------|
| PowerShell Script | Easy | Windows users | 30 seconds |
| Batch Script | Easy | Legacy Windows | 30 seconds |
| Bash Script | Easy | Mac/Linux users | 30 seconds |
| Manual cURL | Medium | Specific endpoints | 5 minutes |
| Postman GUI | Medium | Visual testing | 10 minutes |

**Recommendation:** Start with PowerShell script for fastest testing! ⭐

---

**Ready to test? Run this command:**
```powershell
powershell -ExecutionPolicy Bypass -File test-api.ps1
```

**Last Updated:** October 25, 2025
