# 🧪 SUPER_ADMIN User Management - Testing Checklist

## Pre-Testing Setup

### Step 1: Clean and Rebuild
```
In Android Studio:
1. Build → Clean Project
2. Build → Rebuild Project
3. Wait for Gradle sync to complete
```

### Step 2: Start Backend
```bash
cd D:\FREELANCE\MGB\backend
npm run dev
```
**Expected:** `Server running on port 3000`

### Step 3: Run Mobile App
```
In Android Studio:
1. Click Run (green play button)
2. Wait for app to launch
```

---

## 🔐 Test 1: Login as SUPER_ADMIN

**Steps:**
1. App opens to splash screen
2. Navigates to login screen
3. Enter credentials:
   - Username: `superadmin`
   - Password: `Change@Me#2025`
4. Click "Login"

**Expected Result:**
- ✅ Loading spinner appears
- ✅ Successfully logs in
- ✅ Navigates to Admin Dashboard
- ✅ Navigation header shows:
  - Name: "System Administrator"
  - Role: "Super Administrator"

**If Failed:**
- Check backend is running
- Check Logcat for errors
- Verify network_security_config.xml exists

---

## 📋 Test 2: Access User Management Menu

**Steps:**
1. From Admin Dashboard
2. Tap hamburger menu (☰)
3. Scroll down to "Administration" section

**Expected Result:**
- ✅ "Administration" section is visible
- ✅ "User Management" menu item exists
- ✅ Menu item has people icon

**If Failed:**
- You're not logged in as SUPER_ADMIN
- Check role in navigation header

---

## 👥 Test 3: View User List

**Steps:**
1. Tap "User Management"
2. Wait for screen to load

**Expected Result:**
- ✅ User Management screen opens
- ✅ Shows toolbar with "User Management" title
- ✅ Shows search bar at top
- ✅ Shows at least 1 user (superadmin)
- ✅ Each user card shows:
  - Username (@username)
  - Full name (bold)
  - Email
  - Role chip (Super Admin/Admin/User)
  - Status chip (Active/Inactive)
- ✅ Floating (+) button visible at bottom-right

**If Failed:**
- Check backend terminal for errors
- Check Logcat for network errors
- Verify backend has users in database

---

## 🔍 Test 4: Search Users

**Steps:**
1. In User Management screen
2. Type "super" in search box
3. Tap "Search" button

**Expected Result:**
- ✅ Loading spinner appears briefly
- ✅ Filters to show only matching users
- ✅ Should show "superadmin" user

**To Reset:**
- Clear search box
- Tap "Search" again

---

## ➕ Test 5: Create ADMIN User (SUPER_ADMIN Only)

**Steps:**
1. Tap floating (+) button
2. Fill in form:
   - Username: `testadmin`
   - Password: `Admin123!`
   - Full Name: `Test Administrator`
   - Email: `testadmin@mgb.gov.ph`
   - Role: Select "ADMIN" from dropdown
3. Tap "Create User"

**Expected Result:**
- ✅ Loading spinner appears
- ✅ Toast shows "User created successfully!"
- ✅ Returns to User Management screen
- ✅ New user appears in list with:
  - Username: @testadmin
  - Role chip: "Admin"
  - Status chip: "Active"

**Backend Expected:**
```
POST /api/v1/auth/login 200
POST /api/v1/users 201
GET /api/v1/users 200
```

**If Failed:**
- Check backend logs for validation errors
- Verify all fields are filled
- Check password meets requirements (6+ chars)

---

## 👤 Test 6: Create USER Account

**Steps:**
1. Tap floating (+) button
2. Fill in form:
   - Username: `testuser`
   - Password: `User123!`
   - Full Name: `Test User`
   - Email: `testuser@mgb.gov.ph`
   - Role: Select "USER" from dropdown
3. Tap "Create User"

**Expected Result:**
- ✅ User created successfully
- ✅ Appears in list with Role: "User"

---

## 🔒 Test 7: ADMIN Cannot Create ADMIN (Test Restriction)

**Steps:**
1. Logout from superadmin
2. Login as testadmin:
   - Username: `testadmin`
   - Password: `Admin123!`
3. Navigate to Admin Dashboard
4. Check navigation drawer

**Expected Result:**
- ✅ "Administration" section NOT visible
- ✅ "User Management" menu hidden
- ✅ Navigation header shows:
  - Role: "Administrator" (NOT "Super Administrator")

**Alternative Test (Direct Access):**
If you implement direct navigation:
1. Try to access User Management
2. Open Create User screen
3. Role dropdown should ONLY show "USER"
4. Should NOT show "ADMIN" option

---

## ✅ Test 8: Validation

**Test Empty Fields:**
1. Tap (+) button
2. Leave fields empty
3. Tap "Create User"

**Expected:** Toast shows "Username is required"

**Test Short Password:**
1. Fill username
2. Password: `123` (too short)
3. Tap "Create User"

**Expected:** Toast shows "Password must be at least 6 characters"

**Test Duplicate Username:**
1. Try to create user with username `superadmin`
2. Tap "Create User"

**Expected:** Toast shows "Username already taken" or similar error

---

## 🐛 Common Issues & Solutions

### Issue: "Network error"
**Solution:**
- Verify backend is running: `npm run dev`
- Check terminal shows "Server running on port 3000"
- Restart backend if needed

### Issue: "401 Unauthorized"
**Solution:**
- Logout and login again
- Token may have expired
- Check TokenManager is working

### Issue: Build errors
**Solution:**
```
1. Build → Clean Project
2. File → Invalidate Caches → Invalidate and Restart
3. Build → Rebuild Project
```

### Issue: User Management menu not visible
**Solution:**
- You're logged in as ADMIN, not SUPER_ADMIN
- Logout and login as superadmin
- Check role in navigation header

### Issue: App crashes on create user
**Solution:**
- Check Logcat for error message
- Verify backend is receiving request
- Check backend terminal for errors

---

## 📊 Success Criteria

All tests should pass:
- [  ] 1. Login as SUPER_ADMIN ✓
- [  ] 2. Access User Management Menu ✓
- [  ] 3. View User List ✓
- [  ] 4. Search Users ✓
- [  ] 5. Create ADMIN User ✓
- [  ] 6. Create USER Account ✓
- [  ] 7. ADMIN Restrictions Working ✓
- [  ] 8. Validation Working ✓

---

## 📝 Testing Notes Template

Use this to document your testing:

```
Date: ___________
Tester: ___________

Test 1 - Login: ☐ Pass ☐ Fail
Notes: _________________________________

Test 2 - Menu Access: ☐ Pass ☐ Fail  
Notes: _________________________________

Test 3 - View List: ☐ Pass ☐ Fail
Notes: _________________________________

Test 4 - Search: ☐ Pass ☐ Fail
Notes: _________________________________

Test 5 - Create ADMIN: ☐ Pass ☐ Fail
Notes: _________________________________

Test 6 - Create USER: ☐ Pass ☐ Fail
Notes: _________________________________

Test 7 - ADMIN Restrictions: ☐ Pass ☐ Fail
Notes: _________________________________

Test 8 - Validation: ☐ Pass ☐ Fail
Notes: _________________________________

Overall Result: ☐ All Pass ☐ Some Failures
```

---

## 🎯 What to Report

If you find issues, please provide:
1. Which test failed
2. What you expected
3. What actually happened
4. Screenshots (if UI issue)
5. Logcat output (if crash)
6. Backend terminal output

---

**Ready to test!** Start with Test 1 and work through the checklist. 🚀
