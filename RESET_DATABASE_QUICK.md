# 🗑️ DATABASE RESET - QUICK GUIDE

## ⚠️ **WARNING**
**This will DELETE ALL DATA from your database!**

Only the superadmin credentials from `.env` will remain.

---

## 🚀 **How to Reset** (EASY!)

### **Step 1: Stop Backend Server**
Press `Ctrl+C` in your backend terminal to stop the server.

### **Step 2: Run Reset Command**

```bash
# Navigate to backend folder
cd backend

# Run reset
npm run db:reset
```

### **Step 3: Wait for Completion**

You should see:
```
================================================
DATABASE RESET - STARTING
================================================

📡 Connecting to database...
✅ Database connected

🗑️  Dropping all tables...
✅ All tables dropped and recreated

👤 Creating superadmin user...
✅ Superadmin user created
   Username: superadmin
   Password: admin123
   Email: superadmin@mgb.gov.ph

================================================
DATABASE RESET - COMPLETED
================================================
✅ All data cleared
✅ Fresh tables created
✅ Superadmin user ready

📝 You can now login with superadmin credentials
================================================
```

### **Step 4: Restart Backend Server**

```bash
npm run dev
```

### **Step 5: Test Login**

Open your app and login with:
- **Username:** `superadmin` (or whatever is in your `.env`)
- **Password:** `admin123` (or whatever is in your `.env`)

---

## ✅ **What Gets Deleted**

❌ All MRFCs
❌ All Proponents
❌ All Users (except superadmin)
❌ All Meetings
❌ All Quarters
❌ All Attendance
❌ All Minutes
❌ All Notes
❌ All Agenda Items
❌ All Matters Arising
❌ All Access Records
❌ All Audit Logs

---

## ✅ **What Stays**

✅ **Superadmin user from .env:**
- Username: `SUPERADMIN_USERNAME`
- Password: `SUPERADMIN_PASSWORD`
- Email: `SUPERADMIN_EMAIL`
- Role: `SUPER_ADMIN`

---

## 📝 **Make Sure Your .env Has These**

```env
SUPERADMIN_USERNAME=superadmin
SUPERADMIN_PASSWORD=admin123
SUPERADMIN_EMAIL=superadmin@mgb.gov.ph
SUPERADMIN_FULLNAME=Super Administrator
```

---

## 🎯 **Done!**

Your database is now clean and ready for fresh testing! 🎉

**Start creating your test data from scratch!**

