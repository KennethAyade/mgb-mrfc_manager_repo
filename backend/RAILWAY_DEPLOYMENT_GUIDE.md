# 🚂 Railway Deployment Guide - MGB MRFC Manager Backend

**Last Updated:** November 11, 2025  
**Version:** 2.0.3

---

## 📋 Prerequisites

Before deploying to Railway, ensure you have:

1. ✅ **Railway Account** - Sign up at https://railway.app
2. ✅ **GitHub Repository** - Code pushed to GitHub (Railway deploys from GitHub)
3. ✅ **AWS S3 Bucket** - Configured with public read access (see S3_BUCKET_SETUP_GUIDE.md)
4. ✅ **AWS IAM Credentials** - Access key and secret key
5. ✅ **Google Gemini API Key** (OPTIONAL) - For AI-powered compliance analysis

---

## 🚀 Step-by-Step Deployment

### **Step 1: Create New Railway Project**

1. Go to https://railway.app/dashboard
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your **MGB MRFC Manager** repository
5. Railway will detect Node.js automatically

---

### **Step 2: Add PostgreSQL Database**

1. In your Railway project, click **"+ New"**
2. Select **"Database"** → **"Add PostgreSQL"**
3. Railway will automatically create `DATABASE_URL` environment variable
4. ✅ No manual configuration needed!

---

### **Step 3: Configure Environment Variables**

Go to your backend service → **Variables** tab and add:

#### **🔐 Required Variables (13)**

```env
# Node Environment
NODE_ENV=production

# Database (Auto-created by Railway PostgreSQL plugin)
DATABASE_URL=${{Postgres.DATABASE_URL}}

# JWT Secret (Generate with: node -e "console.log(require('crypto').randomBytes(64).toString('hex'))")
JWT_SECRET=your_64_character_random_string_here

# AWS S3 Configuration (REQUIRED for file storage)
S3_BUCKET_NAME=adhub-s3-demo
AWS_ACCESS_KEY_ID=AKIA...your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key_here
AWS_REGION=us-east-1

# Server Port (Railway sets automatically, but good to have fallback)
PORT=3000

# CORS Origin (Set to your Android app's IP or domain)
CORS_ORIGIN=*

# Super Admin Credentials (First-time setup)
SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_PASSWORD=Admin@123
SUPER_ADMIN_EMAIL=admin@mgb.gov.ph
SUPER_ADMIN_FULL_NAME=Super Administrator
```

#### **🤖 Optional Variables (1)**

```env
# Google Gemini AI (OPTIONAL - for intelligent compliance analysis)
GEMINI_API_KEY=AIzaSy...your_gemini_api_key

# If not provided, system falls back to keyword-based analysis
```

#### **📁 Legacy Variables (Keep for backwards compatibility)**

```env
# Cloudinary (Legacy - not used anymore, but kept for old data)
CLOUDINARY_CLOUD_NAME=drxjbb7np
CLOUDINARY_API_KEY=your_key_if_you_have_old_data
CLOUDINARY_API_SECRET=your_secret_if_you_have_old_data
```

---

### **Step 4: Configure Build Settings**

Railway auto-detects from `railway.toml`. Verify:

**Build Command:**
```bash
npm install && npm run build
```

**Start Command:**
```bash
npm run db:migrate && npm run db:seed:quarters && npm start
```

**Health Check:**
- Path: `/api/v1/health`
- Timeout: 300 seconds (5 minutes for OCR operations)

---

### **Step 5: Set Node.js Version (Optional)**

Railway uses Node.js 18+ by default. To specify exact version:

1. Go to **Settings** → **Environment**
2. Add variable: `NODE_VERSION=18.18.0`

Or use `.nvmrc` file:

```bash
echo "18.18.0" > backend/.nvmrc
```

---

### **Step 6: Deploy!**

1. Railway will automatically deploy when you push to GitHub
2. Or click **"Deploy"** button in Railway dashboard
3. Watch logs in **Deployments** tab

**Expected Build Time:**
- First build: ~5-7 minutes (npm install + TypeScript build)
- Subsequent builds: ~2-3 minutes (cached dependencies)

---

## ✅ Post-Deployment Verification

### **1. Check Health Endpoint**

```bash
curl https://your-railway-app.railway.app/api/v1/health
```

**Expected Response:**
```json
{
  "status": "ok",
  "timestamp": "2025-11-11T12:00:00.000Z",
  "uptime": 123.456,
  "environment": "production"
}
```

### **2. Check Swagger Docs**

Visit: `https://your-railway-app.railway.app/api-docs`

You should see the full API documentation.

### **3. Test Login API**

```bash
curl -X POST https://your-railway-app.railway.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "superadmin",
    "password": "Admin@123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "superadmin",
      "role": "SUPER_ADMIN",
      ...
    }
  }
}
```

### **4. Verify Database Tables**

Check Railway PostgreSQL dashboard:

**Expected Tables:**
- users
- mrfcs
- proponents
- agendas
- agenda_items
- documents
- compliance_analyses
- attendance
- notifications
- quarters ⚠️ **CRITICAL** (must have Q1-Q4 2025 data)
- audit_logs

### **5. Verify Quarters Are Seeded**

```bash
curl https://your-railway-app.railway.app/api/v1/quarters \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Q1 2025", "year": 2025, "quarter": 1, "is_current": false },
    { "id": 2, "name": "Q2 2025", "year": 2025, "quarter": 2, "is_current": false },
    { "id": 3, "name": "Q3 2025", "year": 2025, "quarter": 3, "is_current": false },
    { "id": 4, "name": "Q4 2025", "year": 2025, "quarter": 4, "is_current": true }
  ]
}
```

⚠️ **If quarters are empty, file upload will fail!** See troubleshooting below.

---

## 🔧 Troubleshooting

### **Issue 1: "Loading quarters..." forever in Android app**

**Cause:** Quarters table is empty  
**Solution:**

```bash
# SSH into Railway container
railway run bash

# Inside container:
npm run db:seed:quarters

# Or manually seed:
node scripts/seed-quarters.js
```

**Verify:**
```sql
-- In Railway PostgreSQL dashboard
SELECT * FROM quarters;
```

Should show 4 rows (Q1-Q4 2025).

---

### **Issue 2: Build fails with "npm ERR! code ELIFECYCLE"**

**Cause:** TypeScript compilation errors  
**Solution:**

```bash
# Locally test build
cd backend
npm run build

# Fix any TypeScript errors
# Push fixes to GitHub
```

---

### **Issue 3: "Cannot connect to database"**

**Cause:** DATABASE_URL not set  
**Solution:**

1. Go to Railway dashboard → PostgreSQL service
2. Copy `DATABASE_URL` from **Variables** tab
3. Go to backend service → **Variables** tab
4. Add: `DATABASE_URL=${{Postgres.DATABASE_URL}}`
5. Redeploy

---

### **Issue 4: OCR times out (504 Gateway Timeout)**

**Cause:** Railway's default timeout is too short for OCR  
**Solution:**

Update `railway.toml`:

```toml
[deploy]
healthcheckTimeout = 300  # 5 minutes
```

Or use Railway Pro (10-minute timeout).

---

### **Issue 5: S3 403 Forbidden errors**

**Cause:** AWS credentials invalid or bucket policy missing  
**Solution:**

1. Verify AWS credentials in Railway dashboard
2. Check S3 bucket policy (see S3_BUCKET_SETUP_GUIDE.md)
3. Test S3 upload manually:

```bash
curl -X POST https://your-railway-app.railway.app/api/v1/documents/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test.pdf" \
  -F "quarter_id=4"
```

---

### **Issue 6: Gemini AI not working**

**Symptom:** Compliance analysis shows "PARTIALLY_COMPLIANT" with generic scores  
**Cause:** GEMINI_API_KEY not set or invalid  
**Solution:**

1. Get API key from https://makersuite.google.com/app/apikey
2. Add to Railway: `GEMINI_API_KEY=AIzaSy...`
3. Redeploy

**Note:** System falls back to keyword-based analysis if Gemini unavailable.

---

## 📊 Railway Performance Optimization

### **1. Enable Persistent Storage (Optional)**

For caching OCR results:

```toml
[deploy]
volumes = [
  { name = "tesseract-cache", mountPath = "/app/tessdata" }
]
```

### **2. Increase Memory Limit**

OCR + canvas require more memory:

1. Go to **Settings** → **Resources**
2. Set memory to **1 GB** or higher (default: 512MB)

### **3. Enable Auto-Scaling**

1. Go to **Settings** → **Deployment**
2. Enable **Auto-deploy** on GitHub push
3. Set **Health Check** retry policy

---

## 📱 Update Android App

After deployment, update Android app's API URL:

**File:** `app/src/main/java/com/mgb/mrfcmanager/data/remote/ApiConfig.kt`

```kotlin
object ApiConfig {
    // Old (Render)
    // private const val BASE_URL = "https://mgb-mrfc.onrender.com/api/v1/"
    
    // New (Railway)
    private const val BASE_URL = "https://your-app.railway.app/api/v1/"
    
    // Increased timeouts for OCR operations
    private const val READ_TIMEOUT = 300L  // 5 minutes
    private const val WRITE_TIMEOUT = 120L // 2 minutes
    private const val CONNECT_TIMEOUT = 30L
}
```

Rebuild Android app and test!

---

## 🔒 Security Best Practices

1. **Never commit .env files** - Use Railway environment variables
2. **Rotate JWT_SECRET** - Use strong random strings
3. **Use strong passwords** - Change default Super Admin password
4. **Enable Railway MFA** - Two-factor authentication
5. **Restrict S3 bucket access** - Only allow Railway IP ranges (if possible)
6. **Monitor API usage** - Watch Gemini AI free tier limits (15 req/min)

---

## 💰 Railway Pricing

**Starter Plan (FREE):**
- $5 free credit/month
- 512 MB RAM
- 1 GB disk
- Good for testing

**Developer Plan ($20/month):**
- $20 credit (pay-as-you-go)
- 8 GB RAM
- 100 GB disk
- **Recommended for production** (heavy OCR workload)

**Estimated Monthly Cost:**
- Backend service: ~$10-15/month
- PostgreSQL: ~$5-10/month
- **Total: $15-25/month**

**Compare to Render:**
- Render free tier: Sleeps after 15 min inactivity ❌
- Railway: Always on ✅
- Railway: Better for heavy workloads (OCR + AI) ✅

---

## 📚 Additional Resources

- **Railway Docs:** https://docs.railway.app
- **Node.js on Railway:** https://docs.railway.app/guides/nodejs
- **PostgreSQL Plugin:** https://docs.railway.app/databases/postgresql
- **MGB MRFC Docs:**
  - [PROJECT_STATUS.md](../PROJECT_STATUS.md) - Full feature list
  - [QUARTERS_SETUP.md](./QUARTERS_SETUP.md) - ⚠️ REQUIRED reading
  - [S3_BUCKET_SETUP_GUIDE.md](../S3_BUCKET_SETUP_GUIDE.md) - AWS S3 setup
  - [GEMINI_AI_INTEGRATION.md](../GEMINI_AI_INTEGRATION.md) - AI setup

---

## 🆘 Need Help?

**Common Issues:**
1. ❌ "Loading quarters..." → Run `npm run db:seed:quarters`
2. ❌ "504 Timeout" → Increase healthcheckTimeout to 300
3. ❌ "S3 403 Forbidden" → Check AWS credentials and bucket policy
4. ❌ "Cannot connect to DB" → Verify `DATABASE_URL=${{Postgres.DATABASE_URL}}`

**Railway Support:**
- Discord: https://discord.gg/railway
- Docs: https://docs.railway.app
- Status: https://status.railway.app

---

**Happy Deploying! 🚂🚀**

