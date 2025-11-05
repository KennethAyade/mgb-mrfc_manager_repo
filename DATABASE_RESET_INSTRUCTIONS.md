# 🗑️ DATABASE RESET - INSTRUCTIONS

## ⚠️ **WARNING**
This will **DELETE ALL DATA** from your database!
Only the superadmin credentials from `.env` will remain.

---

## 🚀 **How to Reset Database**

### **Option 1: Using NPM Script (Recommended)**

```bash
# Navigate to backend folder
cd backend

# Run reset script
npm run db:reset
```

### **Option 2: Using Batch File (Windows)**

```bash
# From project root
reset-db.bat
```

### **Option 3: Using Shell Script (Linux/Mac)**

```bash
# From project root
chmod +x reset-db.sh
./reset-db.sh
```

---

## 📋 **What Gets Deleted**

❌ **All MRFCs**
❌ **All Proponents**
❌ **All Users** (except superadmin)
❌ **All Meetings/Agendas**
❌ **All Quarters**
❌ **All Attendance Records**
❌ **All Minutes**
❌ **All Notes**
❌ **All Agenda Items**
❌ **All Matters Arising**
❌ **All User-MRFC Access Records**
❌ **All Audit Logs**

---

## ✅ **What Remains**

✅ **Superadmin User** (from `.env`)
- Username: `SUPERADMIN_USERNAME` (default: `superadmin`)
- Password: `SUPERADMIN_PASSWORD` (default: `admin123`)
- Email: `SUPERADMIN_EMAIL` (default: `superadmin@mgb.gov.ph`)
- Role: `SUPER_ADMIN`

---

## 📝 **Environment Variables**

Make sure these are set in your `backend/.env` file:

```env
# Superadmin Credentials
SUPERADMIN_USERNAME=superadmin
SUPERADMIN_PASSWORD=admin123
SUPERADMIN_EMAIL=superadmin@mgb.gov.ph
SUPERADMIN_FULLNAME=Super Administrator
```

---

## 🔄 **After Reset**

1. ✅ **Database is empty** (except superadmin)
2. ✅ **All tables are fresh**
3. ✅ **You can login as superadmin**
4. ✅ **Ready for clean testing**

---

## 🎯 **Usage Example**

```bash
# 1. Stop your backend server (Ctrl+C)

# 2. Navigate to backend
cd backend

# 3. Run reset
npm run db:reset

# 4. You should see:
# ================================================
# DATABASE RESET - STARTING
# ================================================
# 
# 📡 Connecting to database...
# ✅ Database connected
# 
# 🗑️  Dropping all tables...
# ✅ All tables dropped and recreated
# 
# 👤 Creating superadmin user...
# ✅ Superadmin user created
#    Username: superadmin
#    Password: admin123
#    Email: superadmin@mgb.gov.ph
# 
# ================================================
# DATABASE RESET - COMPLETED
# ================================================
# ✅ All data cleared
# ✅ Fresh tables created
# ✅ Superadmin user ready
# 
# 📝 You can now login with superadmin credentials
# ================================================

# 5. Restart your backend server
npm run dev

# 6. Login to your app with superadmin credentials
```

---

## ⚙️ **How It Works**

The reset script:

1. **Connects** to the PostgreSQL database
2. **Drops** all tables using `sequelize.sync({ force: true })`
3. **Recreates** all tables with fresh schema
4. **Creates** superadmin user with credentials from `.env`
5. **Exits** successfully

---

## 🔒 **Safety Features**

- ✅ **Confirmation prompt** (in batch/shell scripts)
- ✅ **Clear warnings** before execution
- ✅ **Preserves superadmin** so you can still login
- ✅ **Uses .env credentials** for consistency

---

## 📊 **When to Use This**

### ✅ **Good Times to Reset:**
- Starting fresh testing
- Demo/presentation preparation
- After importing bad data
- Development cleanup
- Before deploying to staging/production

### ❌ **DON'T Reset When:**
- You have production data
- You haven't backed up important data
- Users are actively using the system
- You're not sure what you're doing

---

## 💾 **Backup First (Optional)**

If you want to backup your data before resetting:

```bash
# PostgreSQL backup
pg_dump -U postgres -d mrfc_manager > backup.sql

# Restore later if needed
psql -U postgres -d mrfc_manager < backup.sql
```

---

## 🐛 **Troubleshooting**

### **Error: "Cannot connect to database"**
- Make sure PostgreSQL is running
- Check your `.env` database credentials

### **Error: "Superadmin creation failed"**
- Check your `.env` has `SUPERADMIN_USERNAME` and `SUPERADMIN_PASSWORD`
- Make sure the values are valid

### **Error: "Permission denied"**
- For shell scripts: Run `chmod +x reset-db.sh` first
- For Windows: Run as Administrator if needed

---

## ✅ **Verification**

After reset, verify by:

1. **Login** to your app with superadmin credentials
2. **Check** that all lists are empty (MRFCs, Users, etc.)
3. **Create** a test MRFC or user to verify it works

---

**Your database will be fresh and ready for clean testing! 🎉**

