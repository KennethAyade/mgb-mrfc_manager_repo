# 🔧 Railway Migration Crash Loop Fix

## 🔍 Root Cause Analysis

Your Railway deployment was stuck in a crash loop because:

1. **Non-Idempotent Migrations**: Migration files tried to create indexes/constraints that already existed
2. **Nested Transactions**: Some migrations had `BEGIN`/`COMMIT` blocks, conflicting with the migration script's transaction wrapper
3. **Logging Storm**: The crash loop generated 500+ logs/second, hitting Railway's rate limit

### The Errors:
```sql
ERROR: relation "agendas_mrfc_quarter_unique" already exists
ERROR: constraint "check_compliance_range" for relation "mrfcs" already exists
WARNING: there is already a transaction in progress
```

---

## ✅ What Was Fixed

### 1. Schema.sql: `backend/database/schema.sql` ⭐ **MAIN FIX**

**The Problem:**
Schema.sql creates indexes WITHOUT `IF NOT EXISTS`, so when Railway redeploys on an existing database, it tries to create indexes that already exist → crash loop.

**Fixed:**
- ✅ Added `IF NOT EXISTS` to **all 40+ indexes** (lines 348-416)
- ✅ Added `DROP TRIGGER IF EXISTS` before **all 7 triggers** (lines 433-458)
- ✅ Added `ON CONFLICT DO NOTHING` to **quarters INSERT** (line 472)

**Before:**
```sql
CREATE INDEX idx_users_email ON users(email);  -- ❌ Fails if exists
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users  -- ❌ Fails if exists
INSERT INTO quarters (...) VALUES (...);  -- ❌ Fails if duplicates
```

**After:**
```sql
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);  -- ✅ Idempotent
DROP TRIGGER IF EXISTS update_users_updated_at ON users;  -- ✅ Idempotent
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
INSERT INTO quarters (...) VALUES (...) ON CONFLICT (name) DO NOTHING;  -- ✅ Idempotent
```

---

### 2. Migration 002: `backend/database/migrations/002_allow_null_mrfc_id_in_agendas.sql`

**Before:**
```sql
BEGIN;  -- ❌ Nested transaction
CREATE UNIQUE INDEX agendas_mrfc_quarter_unique  -- ❌ No IF NOT EXISTS
ON agendas (mrfc_id, quarter_id)
WHERE mrfc_id IS NOT NULL;
COMMIT;
```

**After:**
```sql
-- ✅ Removed BEGIN/COMMIT (migration script handles transactions)
CREATE UNIQUE INDEX IF NOT EXISTS agendas_mrfc_quarter_unique  -- ✅ Idempotent
ON agendas (mrfc_id, quarter_id)
WHERE mrfc_id IS NOT NULL;
```

### 3. Migration 005: `backend/database/migrations/005_add_compliance_fields_to_mrfcs.sql`

**Before:**
```sql
ALTER TABLE mrfcs ADD CONSTRAINT check_compliance_range  -- ❌ No IF NOT EXISTS
  CHECK (compliance_percentage >= 0 AND compliance_percentage <= 100);

CREATE INDEX idx_mrfcs_compliance_status ON mrfcs(compliance_status);  -- ❌ No IF NOT EXISTS
```

**After:**
```sql
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'check_compliance_range'
  ) THEN
    ALTER TABLE mrfcs ADD CONSTRAINT check_compliance_range
      CHECK (compliance_percentage >= 0 AND compliance_percentage <= 100);
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_mrfcs_compliance_status ON mrfcs(compliance_status);  -- ✅ Idempotent
```

---

## 🚀 Option 1: Redeploy with Fixed Migrations (RECOMMENDED)

### Step 1: Commit the Fixes

```bash
cd backend
git add database/migrations/002_allow_null_mrfc_id_in_agendas.sql
git add database/migrations/005_add_compliance_fields_to_mrfcs.sql
git commit -m "fix: Make database migrations idempotent to prevent crash loops"
git push origin main
```

### Step 2: Railway Auto-Deploys

Railway will automatically:
- ✅ Detect the push
- ✅ Build the new code
- ✅ Run migrations (now idempotent!)
- ✅ Start the server successfully

### Step 3: Monitor Deployment

1. Go to Railway dashboard: https://railway.app/project/[your-project]
2. Watch the "Build & Deploy" logs
3. Look for:
   ```
   ✅ Step 2: Creating database schema - SUCCESS
   ✅ Step 3: Running database migrations - SUCCESS
   ✅ Step 4: Seeding quarters (Q1-Q4 2025) - SUCCESS
   🚀 Step 5: Starting server...
   Server running on port 3000
   ```

---

## 🔥 Option 2: Manual Database Reset (NUCLEAR OPTION)

⚠️ **WARNING: This deletes ALL data!** Only use if you don't have production data yet.

### Step 1: Reset PostgreSQL Database

In Railway dashboard:
1. Go to your PostgreSQL service
2. Click "Settings" tab
3. Scroll to "Danger Zone"
4. Click "Reset Database"
5. Confirm deletion

### Step 2: Redeploy Backend

1. Go to backend service
2. Click "Deployments" tab
3. Click "Redeploy" on latest deployment
4. Or push a dummy commit:
   ```bash
   git commit --allow-empty -m "trigger: Railway redeploy after DB reset"
   git push origin main
   ```

### Step 3: Verify Fresh Start

All migrations will run from scratch on clean database.

---

## 🛠️ Option 3: Manual Migration Cleanup (ADVANCED)

If you want to keep existing data but fix the migration state:

### Step 1: Connect to Railway PostgreSQL

```bash
# Get connection string from Railway dashboard
# Settings > PostgreSQL > Connection String

psql "postgresql://postgres:password@[railway-host]:5432/railway"
```

### Step 2: Check Existing Objects

```sql
-- Check for duplicate index
SELECT indexname FROM pg_indexes 
WHERE tablename = 'agendas' 
AND indexname = 'agendas_mrfc_quarter_unique';

-- Check for duplicate constraint
SELECT conname FROM pg_constraint 
WHERE conname = 'check_compliance_range';
```

### Step 3: Drop Duplicates (if needed)

```sql
-- Drop the duplicate index
DROP INDEX IF EXISTS agendas_mrfc_quarter_unique;

-- Drop the duplicate constraint
ALTER TABLE mrfcs DROP CONSTRAINT IF EXISTS check_compliance_range;
```

### Step 4: Redeploy Backend

```bash
git commit --allow-empty -m "trigger: Redeploy after manual cleanup"
git push origin main
```

Now migrations will run successfully since duplicates are removed.

---

## 🎯 Recommended Approach

### For Staging/Development:
**Use Option 2** (Nuclear reset) - fastest, clean slate

### For Production with Data:
**Use Option 1** (Fixed migrations) - safe, preserves data

### For Troubleshooting:
**Use Option 3** (Manual cleanup) - surgical fix

---

## 📊 How to Monitor Success

### 1. Check Railway Logs

Look for these success indicators:
```
✅ Step 2: Creating database schema - SUCCESS
✅ Step 3: Running database migrations - SUCCESS
✅ Step 4: Seeding quarters (Q1-Q4 2025) - SUCCESS
🚀 Step 5: Starting server...
Server running on port 3000
Database connected successfully
```

### 2. Test Health Endpoint

```bash
curl https://your-app.railway.app/api/v1/health
```

Expected:
```json
{
  "success": true,
  "message": "MGB MRFC Manager API is running",
  "version": "1.0.0"
}
```

### 3. Check Logs Rate

Railway logs should now be < 10 logs/second (normal rate), not 500+/second (crash loop).

---

## 🔍 Prevention Tips

### 1. Always Make Migrations Idempotent

✅ **Good:**
```sql
CREATE TABLE IF NOT EXISTS my_table (...);
CREATE INDEX IF NOT EXISTS idx_name ON table(column);
ALTER TABLE my_table ADD COLUMN IF NOT EXISTS my_column TEXT;
```

❌ **Bad:**
```sql
CREATE TABLE my_table (...);  -- Fails if exists
CREATE INDEX idx_name ON table(column);  -- Fails if exists
ALTER TABLE my_table ADD COLUMN my_column TEXT;  -- Fails if exists
```

### 2. Never Use BEGIN/COMMIT in Migration Files

The migration script (`backend/scripts/migrate.js`) already wraps each file in a transaction.

❌ **Bad:**
```sql
BEGIN;
-- migration code
COMMIT;
```

✅ **Good:**
```sql
-- migration code (script handles transactions)
```

### 3. Test Migrations Locally First

```bash
# Run migrations locally
cd backend
npm run db:migrate

# Test idempotency (run again)
npm run db:migrate  # Should skip already-applied migrations
```

### 4. Use Railway Preview Deployments

Test migrations on a preview branch before merging to main:
```bash
git checkout -b fix/migration-test
git push origin fix/migration-test
# Railway creates preview deployment with fresh DB
```

---

## 🆘 Still Having Issues?

### Check Railway Logs

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# View logs
railway logs
```

### Common Issues

1. **"transaction in progress" warning**
   - ✅ Fixed by removing BEGIN/COMMIT from migrations

2. **"already exists" errors**
   - ✅ Fixed by adding IF NOT EXISTS

3. **500 logs/sec rate limit**
   - ✅ Fixed by stopping crash loop (above fixes)

4. **Timeout on health check**
   - Check `railway.toml` has `healthcheckTimeout = 300`
   - Backend needs 5 minutes for OCR operations

---

## 📝 Summary

**Problem:** Non-idempotent migrations caused crash loop → 500 logs/sec → Railway rate limit

**Solution:** Made migrations idempotent with IF NOT EXISTS and removed nested transactions

**Next Steps:**
1. ✅ Migrations fixed (already done!)
2. 🚀 Commit and push to trigger redeploy
3. 📊 Monitor Railway logs for success
4. ✅ Test API endpoints

**Status:** Ready to deploy! 🎉

