# API Testing Instructions

## Quick Start - Choose Your Testing Method

You have **4 options** to test the API:

### Option 1: PowerShell Script (Windows - Recommended) ⭐
```powershell
powershell -ExecutionPolicy Bypass -File test-api.ps1
```
**Advantages:**
- Easy to run on Windows
- No external dependencies (except curl)
- Color-coded output
- Good error handling

### Option 2: Batch Script (Windows)
```cmd
test-api.bat
```
**Advantages:**
- Traditional Windows batch
- Works on older Windows versions

### Option 3: Bash Script (Git Bash / WSL)
```bash
bash test-api.sh
```
**Advantages:**
- Works on macOS and Linux
- Portable across platforms

### Option 4: Postman GUI (Most Comprehensive)
Import `TESTING_GUIDE.md` manually or download a Postman collection

**Advantages:**
- Visual interface
- Better for manual testing
- Can save requests for reuse

---

## Pre-Test Checklist

- [ ] Server is running (`npm run dev`)
- [ ] Database is connected
- [ ] Port 3000 is accessible

### Check Server Status:
```bash
curl http://localhost:3000/api/v1/health
```

---

## Step-by-Step: Running Tests

### 1. Start the Server (if not already running)

**Terminal 1 (Server):**
```bash
cd backend
npm run dev
```

Wait for output like:
```
Server running on port 3000
Database connected
```

### 2. Run Tests (New Terminal)

**Terminal 2 (Tests):**

#### Option A: PowerShell (Recommended for Windows)
```powershell
# Allow running scripts if needed
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser

# Run tests
powershell -ExecutionPolicy Bypass -File test-api.ps1
```

#### Option B: Windows Command Prompt
```cmd
test-api.bat
```

#### Option C: Git Bash
```bash
bash test-api.sh
```

#### Option D: Manual cURL
```bash
# Test health
curl http://localhost:3000/api/v1/health

# Test login
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}'
```

---

## Test Credentials

```
Username: superadmin
Password: Change@Me#2025
Role: SUPER_ADMIN
```

---

## Expected Test Results

### If all tests pass:
```
================================
TEST SUMMARY REPORT
================================

Total Tests: 11
Passed: 11
Failed: 0

✓ ALL TESTS PASSED!
```

### If tests fail:
- Check server is running
- Check database connection
- Review error messages
- Check .env file is properly configured

---

## Manual Testing with cURL

### 1. Login and Get Token
```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Your token: $TOKEN"
```

### 2. Use Token in Requests
```bash
# List users (replace TOKEN with actual token)
curl -X GET "http://localhost:3000/api/v1/users" \
  -H "Authorization: Bearer $TOKEN"

# Create user
curl -X POST http://localhost:3000/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "test@mgb.gov.ph",
    "full_name": "Test Admin",
    "password": "SecurePass@123",
    "role": "ADMIN"
  }'

# List MRFCs
curl -X GET "http://localhost:3000/api/v1/mrfcs" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Test Coverage

### Endpoints Tested:
- ✅ Health Check
- ✅ Login
- ✅ List Users
- ✅ Create User
- ✅ Get User
- ✅ Update User
- ✅ Delete User
- ✅ List MRFCs
- ✅ Create MRFC
- ✅ Get MRFC
- ✅ Update MRFC
- ✅ Grant MRFC Access

### Total: **12 Test Cases**

---

## Troubleshooting

### Error: "Server is not running"
```bash
cd backend
npm run dev
```

### Error: "Cannot find module"
```bash
cd backend
npm install
npm run dev
```

### Error: "Database connection failed"
- Check `.env` file has valid `DATABASE_URL`
- Verify internet connection (Neon database)
- Check database password in `.env`

### Error: "Unauthorized / Invalid token"
- Make sure login succeeded
- Copy token correctly
- Token might have expired (24 hours)

### Error: "User already exists"
- The test creates users with timestamp, should be unique
- If still failing, manually check database
- Or wait a moment and retry

### Port already in use (EADDRINUSE)
```bash
# Find process using port 3000
lsof -i :3000

# Kill the process (Mac/Linux)
kill -9 <PID>

# On Windows:
netstat -ano | findstr :3000
taskkill /F /PID <PID>
```

---

## Next Steps After Testing

Once all tests pass:

1. **Verify User Management** - All 6 endpoints working ✅
2. **Verify MRFC Management** - All 5 endpoints working ✅
3. **Implement Proponent Management** - 5 new endpoints
4. **Implement Quarter Management** - 2 new endpoints
5. **Continue with remaining modules**

---

## Useful Resources

- Full Testing Guide: See `TESTING_GUIDE.md`
- API Documentation: See `IMPLEMENTATION_STATUS.md`
- Server Logs: Check terminal where `npm run dev` is running
- Database: Log in to [Neon Console](https://console.neon.tech)

---

## Questions?

If tests fail or you have questions:

1. **Check server logs** - Look at terminal running `npm run dev`
2. **Check response errors** - Scripts will print API error messages
3. **Review test code** - Open `test-api.ps1` (or .sh / .bat) to see what each test does
4. **Read TESTING_GUIDE.md** - Has detailed curl examples for each endpoint

---

**Last Updated:** October 25, 2025
**Status:** Ready to Test
