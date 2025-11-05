# 🔐 LOGIN CREDENTIALS
**Updated:** November 4, 2025

---

## ✅ **SUPERADMIN CREDENTIALS**

### **For Mobile App Login:**
```
Username: superadmin
Password: Change@Me
```

**⚠️ NOTE:** The password is `Change@Me` (NOT `Change@Me#2025`)  
This is set in your `.env` file via `SUPERADMIN_PASSWORD` variable.

### **Default From .env File:**
If you have environment variables set in `.env`:
- `SUPER_ADMIN_USERNAME` → Your custom username
- `SUPER_ADMIN_PASSWORD` → Your custom password
- `SUPER_ADMIN_EMAIL` → Your custom email
- `SUPER_ADMIN_FULL_NAME` → Your custom full name

Otherwise, the defaults above are used.

---

## 🔧 **If You Want to Change Password:**

### **Option 1: Environment Variable (.env file)**
Add to `backend/.env`:
```
SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_PASSWORD=YourNewPassword123!
SUPER_ADMIN_EMAIL=admin@mgb.gov.ph
SUPER_ADMIN_FULL_NAME=Super Administrator
```

Then reset the database:
```bash
cd backend
npm run db:reset
```

### **Option 2: Direct Database Update**
Use a database client to update the password hash in the `users` table.

---

## 📱 **Testing:**

1. Open your mobile app
2. Enter:
   - Username: `superadmin`
   - Password: `Change@Me#2025`
3. Click Login
4. ✅ You should see the admin dashboard!

---

## 🛠️ **Troubleshooting:**

### **Still Getting "Invalid Credentials"?**
1. Check the backend terminal - is it running?
2. Check if backend shows the login attempt (look for `POST /api/v1/auth/login`)
3. Verify the password is **exactly**: `Change@Me#2025` (case-sensitive)
4. Try resetting the database again: `npm run db:reset`

### **Database Connection Issues?**
1. Check backend `.env` file has correct database credentials
2. Verify database server is running
3. Check backend terminal for connection errors

### **Backend Not Running?**
```bash
cd backend
npm run dev
```

---

## 📝 **Notes:**

- ✅ Password is case-sensitive
- ✅ No spaces before/after username or password
- ✅ Special characters (#, @) are required in password
- ✅ Database was just reset, so only superadmin exists
- ✅ All other data has been cleared

---

## ✅ **Summary:**

**LOGIN WITH:**
```
Username: superadmin
Password: Change@Me
```

**⚠️ IMPORTANT:** It's `Change@Me` not `Change@Me#2025`!

**That's it! Simple!** 🎉

