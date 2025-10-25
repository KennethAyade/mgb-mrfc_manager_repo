# API Testing Flowchart

## Quick Decision Tree

```
                    ┌─────────────────────┐
                    │  Want to test API?  │
                    └──────────┬──────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
              Using Windows?         Not Windows?
                    │                     │
              ┌─────┴─────┐         ┌─────┴──────┐
              │           │         │            │
         PowerShell    Batch      Bash        Postman
         (BEST) ⭐     (OLD)      (LINUX)      (GUI)
              │           │         │            │
            RUN:       RUN:       RUN:          RUN:
       powershell..  test-api.   bash ...   Import to
       test-api.ps1   bat                   Postman
              │           │         │            │
              └─────┬─────┴────┬────┴────────────┘
                    │          │
          Tests pass?    Tests fail?
                    │          │
                   ✅          ❌
                    │          │
            Continue with   Check error
            implementation  messages
```

---

## Detailed Testing Flow

```
┌─────────────────────────────────────┐
│ 1. START SERVER                     │
│ $ cd backend                        │
│ $ npm run dev                       │
│                                     │
│ Wait for: "Server running on 3000" │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│ 2. VERIFY SERVER (in new terminal)  │
│ $ curl http://localhost:3000/...    │
│   .../health                        │
│                                     │
│ Should return: { success: true }    │
└────────────┬────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
    YES            NO
      │             │
      ▼             ▼
   ✅            ❌ ERROR
 Continue      Server not
              running!
              See Step 1
              │
              └─► Go back to Step 1
                    │
                    ▼
                  Fixed?
                   │
              ┌────┴────┐
              │         │
             YES       NO
              │        │
              ▼        └─► Try again
            ✅
             │
             ▼
┌─────────────────────────────────────┐
│ 3. RUN TEST SCRIPT                  │
│                                     │
│ PowerShell:                         │
│ $ powershell -ExecutionPolicy ...   │
│   Bypass -File test-api.ps1         │
│                                     │
│ OR Batch:                           │
│ $ test-api.bat                      │
│                                     │
│ OR Bash:                            │
│ $ bash test-api.sh                  │
└────────────┬────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
    PASS          FAIL
      │             │
      ▼             ▼
   ✅✅✅         ❌ ERROR
 All 12       1+ tests
 tests        failed
 passed       │
      │       └──► Check error message
      │       │
      ▼       ▼
  ✅           Review output
  DONE        │
              ├─→ Server connection?
              ├─→ Login failed?
              ├─→ Database error?
              ├─→ Token invalid?
              │
              └─► Debug based on error
                  │
                  ├─→ Server logs?
                  ├─→ .env file?
                  ├─→ Database?
                  │
                  └─► Fix issue
                      │
                      ▼
                    Retry
                      │
                      └─► Run Step 3 again
```

---

## Testing Phases

### Phase 1: Health Check ✅
```
Request:  GET /health
Response: { success: true, message: "API is running" }
Time:     < 100ms
Status:   CRITICAL - If this fails, server not running
```

### Phase 2: Authentication ✅
```
Request:  POST /auth/login
Body:     { username: "superadmin", password: "Change@Me#2025" }
Response: { success: true, data: { token: "...", user: {...} } }
Time:     < 500ms
Status:   CRITICAL - If this fails, can't test other endpoints
Output:   JWT token (needed for all other tests)
```

### Phase 3: User Management ✅
```
Test 3a: GET /users (List)
Test 3b: POST /users (Create test user)
Test 3c: GET /users/:id (Get created user)
Test 3d: PUT /users/:id (Update user)
Test 3e: DELETE /users/:id (Delete user - cleanup)
Time:    < 5 seconds total
Status:  IMPORTANT - User management core feature
```

### Phase 4: MRFC Management ✅
```
Test 4a: GET /mrfcs (List)
Test 4b: POST /mrfcs (Create test MRFC)
Test 4c: GET /mrfcs/:id (Get created MRFC)
Test 4d: PUT /mrfcs/:id (Update MRFC)
Test 4e: POST /users/:id/grant-mrfc-access (Grant access)
Test 4f: DELETE /mrfcs/:id (Delete MRFC - cleanup)
Time:    < 5 seconds total
Status:  IMPORTANT - MRFC management core feature
```

### Phase 5: Summary Report ✅
```
Total Tests:  12
Passed:       ✅✅✅✅✅✅✅✅✅✅✅✅
Failed:       ❌ (if any)
Success Rate: 100% or X%
Time:         ~10-15 seconds
```

---

## Error Resolution Flowchart

```
┌──────────────────────────┐
│ Test Failed              │
│ Error: ???               │
└────────────┬─────────────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
 Server   Database   Token
 Error    Error      Error
    │        │        │
    │        │        │
    ▼        ▼        ▼
   Fix    Fix:     Fix:
  Step1   • Check  • Re-login
  Re-run  .env     • Copy token
  server  • Check  correctly
          Neon    • Use in
          cred    header
          • Test
          conn
             │
             ▼
        Resolved?
          │
     ┌────┴────┐
     │         │
    YES       NO
     │         │
     ▼         ▼
    ✅      Need more
   DONE     help?
             │
             ├─ Check logs
             ├─ Review guide
             ├─ Manual curl
             │
             ▼
           Fixed?
            │
       ┌────┴────┐
       │         │
      YES       NO
       │         │
       ▼         └─→ Contact support
      ✅
```

---

## Testing Decision Matrix

```
SCENARIO                          RECOMMENDED ACTION
─────────────────────────────────────────────────────
"I just want to test quickly"     → Run PowerShell script
                                    (30 seconds, all tests)

"I want to test one endpoint"     → Use curl or Postman
                                    (manual testing)

"I want visual interface"          → Use Postman
                                    (GUI-based)

"I'm on Mac/Linux"                → Use Bash script
                                    (portable)

"Something failed, debug"         → Check server logs
                                    + Use manual curl

"I need detailed endpoint info"   → Read TESTING_GUIDE.md
                                    (has all endpoints)

"I want step-by-step guide"       → Read TEST_INSTRUCTIONS.md
                                    (detailed walkthrough)
```

---

## Success Indicators

### Green Lights ✅
- [PASS] appears before each test
- No red error text
- "ALL TESTS PASSED!" message at end
- Tests complete in < 30 seconds
- No database errors
- No timeout errors

### Red Lights ❌
- [FAIL] appears before any test
- "SOME TESTS FAILED" message
- Server not responding
- Database connection error
- Authentication failure
- Timeout errors

---

## Performance Expectations

```
Test                    Expected Time
────────────────────────────────────
Health Check            < 100ms  ✅
Login                   < 500ms  ✅
List Users              < 300ms  ✅
Create User             < 500ms  ✅
Get User                < 300ms  ✅
Update User             < 500ms  ✅
List MRFCs              < 300ms  ✅
Create MRFC             < 500ms  ✅
Get MRFC                < 300ms  ✅
Update MRFC             < 500ms  ✅
Grant Access            < 500ms  ✅
Delete User             < 500ms  ✅
Delete MRFC             < 500ms  ✅
────────────────────────────────────
TOTAL                   < 30s    ✅
```

---

## Response Codes Summary

```
CODE    MEANING              WHAT TO DO
────    ───────────────────  ──────────
200     OK                   ✅ Test passed
201     Created              ✅ Resource created
204     No Content           ✅ Success, no data
400     Bad Request          ❌ Check request body
401     Unauthorized         ❌ Re-login, get token
403     Forbidden            ❌ Check permissions
404     Not Found            ❌ Check ID/URL
500     Server Error         ❌ Check server logs
503     Service Unavailable  ❌ Server down
```

---

## Complete Testing Timeline

```
Time    Activity                      Status
────    ──────────────────────────   ───────
0s      Start server (npm run dev)    🟡
2s      Server ready                   ✅
3s      Start test script              🟡
3.5s    Health check                  ✅
4s      Login                          ✅
5s      User tests (4 tests)           ✅
10s     MRFC tests (4 tests)           ✅
12s     Final cleanup                  ✅
13s     Report generated               ✅
────    ──────────────────────────   ───────
TOTAL   ~15-20 seconds
```

---

## Quick Reference Card

```
╔════════════════════════════════════════════╗
║  QUICK REFERENCE - TESTING COMMANDS        ║
╠════════════════════════════════════════════╣
║                                            ║
║  START SERVER:                             ║
║  $ cd backend && npm run dev               ║
║                                            ║
║  RUN TESTS (Windows PowerShell):           ║
║  $ powershell -ExecutionPolicy Bypass \    ║
║    -File test-api.ps1                      ║
║                                            ║
║  RUN TESTS (Windows Cmd):                  ║
║  $ test-api.bat                            ║
║                                            ║
║  RUN TESTS (Mac/Linux):                    ║
║  $ bash test-api.sh                        ║
║                                            ║
║  HEALTH CHECK:                             ║
║  $ curl http://localhost:3000/api/v1/...   ║
║    ...health                               ║
║                                            ║
║  TEST CREDENTIALS:                         ║
║  Username: superadmin                      ║
║  Password: Change@Me#2025                  ║
║                                            ║
╚════════════════════════════════════════════╝
```

---

## Next Actions Based on Results

### ✅ All Tests Passed
```
1. Review the test output
2. Check database entries were created
3. Proceed with implementation
4. Move to next module (Proponent Management)
```

### ❌ Some Tests Failed
```
1. Identify which test failed (read error message)
2. Check server logs (terminal with npm run dev)
3. Review error details in test output
4. Fix the issue:
   - Server problem? → Restart npm run dev
   - Database? → Check .env file
   - Token? → Re-login
   - Other? → Check TESTING_GUIDE.md
5. Run tests again
```

### 🚀 Ready for Next Phase
```
Once all tests pass, continue with:
1. Proponent Management (5 endpoints)
2. Quarter Management (2 endpoints)
3. Agenda Management (5 endpoints)
4. ... and more modules
```

---

**Visual Guide Last Updated:** October 25, 2025
