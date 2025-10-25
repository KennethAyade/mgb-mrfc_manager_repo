# Quick Test Guide - 30 Seconds Setup

## One-Command Testing

### For Windows (PowerShell) - EASIEST ⭐
```powershell
powershell -ExecutionPolicy Bypass -File test-api.ps1
```

### For Windows (Command Prompt)
```cmd
test-api.bat
```

### For Mac/Linux (Bash)
```bash
bash test-api.sh
```

---

## What It Tests

The automated test scripts will:

1. ✅ Check if server is running
2. ✅ Test health endpoint
3. ✅ Login (get JWT token)
4. ✅ List all users
5. ✅ Create a new test user
6. ✅ Get user details
7. ✅ Update user information
8. ✅ List all MRFCs
9. ✅ Create a new test MRFC
10. ✅ Get MRFC details
11. ✅ Update MRFC information
12. ✅ Grant MRFC access to user
13. ✅ Cleanup (delete test data)

---

## Expected Output

### Success (All Green ✅)
```
================================
TEST SUMMARY REPORT
================================

Total Tests: 12
Passed: 12
Failed: 0

✓ ALL TESTS PASSED!
```

### Failure (Some Red ✗)
```
================================
TEST SUMMARY REPORT
================================

Total Tests: 12
Passed: 11
Failed: 1

✗ SOME TESTS FAILED
```

---

## Test Credentials

```
Username: superadmin
Password: Change@Me#2025
```

---

## Files Included

| File | Purpose | Usage |
|------|---------|-------|
| `test-api.ps1` | PowerShell test script | `powershell -ExecutionPolicy Bypass -File test-api.ps1` |
| `test-api.bat` | Batch file test script | `test-api.bat` |
| `test-api.sh` | Bash script test | `bash test-api.sh` |
| `TESTING_GUIDE.md` | Complete testing documentation | Read for detailed info |
| `TEST_INSTRUCTIONS.md` | Step-by-step instructions | Follow for setup |
| `QUICK_TEST_GUIDE.md` | This file | Quick reference |

---

## Troubleshooting

### Server not running?
```
cd backend
npm run dev
```

### Port already in use?
```powershell
# Find and kill process using port 3000
Get-Process | Where-Object {$_.Description -like "*node*"} | Stop-Process -Force
```

### Database connection error?
- Check `.env` file exists with `DATABASE_URL`
- Verify Neon database credentials
- Check internet connection

### Invalid credentials?
- Use: `superadmin` / `Change@Me#2025`
- Make sure super admin was created during server startup

---

## Manual Testing (No Script)

If you prefer manual testing, use cURL:

```bash
# 1. Login
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}'

# Copy the "token" from response

# 2. Test any endpoint (replace TOKEN)
curl -X GET "http://localhost:3000/api/v1/users" \
  -H "Authorization: Bearer TOKEN"
```

See `TESTING_GUIDE.md` for more cURL examples.

---

## Next Steps

After testing succeeds:

1. Review test output
2. Check database for created test data
3. Review `IMPLEMENTATION_STATUS.md` for progress
4. Continue implementing remaining endpoints

---

**Ready to test?**

→ Run: `powershell -ExecutionPolicy Bypass -File test-api.ps1`
