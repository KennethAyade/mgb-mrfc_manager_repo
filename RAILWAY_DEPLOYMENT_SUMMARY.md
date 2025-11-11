# 🚂 Railway Deployment Setup - Complete Summary

**Created:** November 11, 2025  
**Version:** 2.0.4  
**Purpose:** Deploy MGB MRFC Manager backend to Railway (replacing Render)

---

## 🎯 Why Railway?

**Problems with Render:**
- ❌ Free tier sleeps after 15 minutes of inactivity
- ❌ Cannot handle heavy OCR + AI workload (2-3 minute processing)
- ❌ Frequent 504 timeout errors
- ❌ Limited memory (512MB)

**Benefits of Railway:**
- ✅ Always-on (no sleeping)
- ✅ Better performance for heavy workloads
- ✅ Scalable resources (1 GB+ RAM)
- ✅ Simple PostgreSQL integration
- ✅ Auto-deploy from GitHub
- ✅ $20/month Developer Plan (vs. Render's $7-$25)
- ✅ Better support for long-running operations

---

## 📁 What Was Created for You

### **1. Railway Configuration Files**

```
backend/
├── railway.toml                     # Railway project configuration
├── nixpacks.toml                    # Build optimization (Node.js + canvas/cairo)
├── .railwayignore                   # Files to exclude from deployment
├── scripts/
│   └── railway-start.js             # Auto-migration + seeding on startup
├── RAILWAY_QUICK_START.md           # 5-minute deployment guide ⭐
├── RAILWAY_DEPLOYMENT_GUIDE.md      # Complete guide with troubleshooting
├── RAILWAY_DEPLOYMENT_CHECKLIST.md  # Pre-flight checklist
└── RAILWAY_ENV_TEMPLATE.txt         # Environment variables template
```

### **2. Key Features**

#### **railway.toml**
- Auto-detects Node.js
- Runs migrations + quarters seeding on every deploy
- Health check at `/api/v1/health`
- 5-minute timeout (for OCR operations)

#### **nixpacks.toml**
- Installs Node.js 18 + Python + Cairo + Pango (for canvas/PDF processing)
- Optimized build process
- Custom start script

#### **scripts/railway-start.js**
- Tests database connection
- Runs migrations automatically
- Seeds quarters (Q1-Q4 2025) if empty
- Starts server
- Handles errors gracefully

#### **.railwayignore**
- Excludes dev files, tests, temp files
- Reduces deployment size
- Keeps tesseract language data (needed for OCR)

---

## 🚀 Quick Deployment Steps

### **Option 1: Super Fast (5 Minutes)**

Follow: **`backend/RAILWAY_QUICK_START.md`**

1. Create Railway project (1 min)
2. Add PostgreSQL (30 sec)
3. Set environment variables (2 min)
4. Deploy automatically (5-7 min build)
5. Test endpoints (30 sec)

### **Option 2: Comprehensive (with checklist)**

Follow: **`backend/RAILWAY_DEPLOYMENT_CHECKLIST.md`**

- Pre-deployment checklist (AWS S3, Gemini API, etc.)
- Step-by-step Railway setup
- Post-deployment verification
- Android app update instructions
- Security checklist

### **Option 3: Full Guide (with troubleshooting)**

Follow: **`backend/RAILWAY_DEPLOYMENT_GUIDE.md`**

- Complete deployment instructions
- Troubleshooting section (8 common issues)
- Performance optimization tips
- Railway pricing breakdown
- Security best practices

---

## 🔑 Required Information

Before you start, prepare these:

### **1. AWS S3 Credentials**
```env
S3_BUCKET_NAME=adhub-s3-demo
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
```

### **2. JWT Secret**
Generate with:
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

### **3. Google Gemini API Key (Optional)**
Get from: https://makersuite.google.com/app/apikey

### **4. Super Admin Password**
Change from default `Admin@123` to something secure.

---

## ⚙️ Environment Variables (14 total)

Copy from: **`backend/RAILWAY_ENV_TEMPLATE.txt`**

**Required (13):**
- `NODE_ENV=production`
- `DATABASE_URL=${{Postgres.DATABASE_URL}}` ← Railway auto-creates this
- `JWT_SECRET=your_64_char_string`
- `S3_BUCKET_NAME=adhub-s3-demo`
- `AWS_ACCESS_KEY_ID=AKIA...`
- `AWS_SECRET_ACCESS_KEY=...`
- `AWS_REGION=us-east-1`
- `PORT=3000`
- `CORS_ORIGIN=*`
- `SUPER_ADMIN_USERNAME=superadmin`
- `SUPER_ADMIN_PASSWORD=Admin@123`
- `SUPER_ADMIN_EMAIL=admin@mgb.gov.ph`
- `SUPER_ADMIN_FULL_NAME=Super Administrator`

**Optional (1):**
- `GEMINI_API_KEY=AIzaSy...` (for AI analysis, falls back to keyword if not set)

---

## ✅ Verification Steps

After deployment, test these:

### **1. Health Check**
```bash
curl https://your-app.railway.app/api/v1/health
# Expected: {"status":"ok",...}
```

### **2. Login API**
```bash
curl -X POST https://your-app.railway.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Admin@123"}'
# Expected: {"success":true,"data":{"token":"...","user":{...}}}
```

### **3. Quarters API**
```bash
curl https://your-app.railway.app/api/v1/quarters \
  -H "Authorization: Bearer YOUR_TOKEN"
# Expected: 4 quarters (Q1-Q4 2025)
```

### **4. Swagger Docs**
Visit: `https://your-app.railway.app/api-docs`

---

## 🔧 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| ❌ "Loading quarters..." in app | Run: `railway run npm run db:seed:quarters` |
| ❌ Build fails | Check package.json has `build` and `start` scripts |
| ❌ 504 Timeout | Set `healthcheckTimeout = 300` in railway.toml |
| ❌ S3 403 Forbidden | Verify AWS credentials, check bucket policy |
| ❌ Cannot connect to DB | Set `DATABASE_URL=${{Postgres.DATABASE_URL}}` |

---

## 📱 Update Android App

After deployment, update API URL in:

**File:** `app/src/main/java/com/mgb/mrfcmanager/data/remote/ApiConfig.kt`

```kotlin
// Change this line:
private const val BASE_URL = "https://your-app.railway.app/api/v1/"
```

Then rebuild the app!

---

## 💰 Estimated Costs

**Railway Developer Plan:** $20/month (pay-as-you-go)

**Estimated Monthly Usage:**
- Backend service: $10-15/month
- PostgreSQL: $5-10/month
- **Total:** ~$15-25/month

**Compare to Render:**
- Render free tier: $0 but sleeps + unreliable ❌
- Render paid: $7-$25/month (still has issues)
- Railway: $15-25/month but always-on + reliable ✅

---

## 📚 Documentation Map

**Start Here:**
1. 🚀 **backend/RAILWAY_QUICK_START.md** - If you just want to deploy NOW
2. ✅ **backend/RAILWAY_DEPLOYMENT_CHECKLIST.md** - If you want a systematic approach
3. 📖 **backend/RAILWAY_DEPLOYMENT_GUIDE.md** - If you need detailed troubleshooting

**Supporting Docs:**
- **backend/RAILWAY_ENV_TEMPLATE.txt** - Copy environment variables from here
- **backend/QUARTERS_SETUP.md** - CRITICAL: Understand quarters requirement
- **S3_BUCKET_SETUP_GUIDE.md** - AWS S3 configuration (if not done yet)
- **PROJECT_STATUS.md** - Full project documentation (updated with Railway info)

---

## 🎯 Recommended Deployment Path

```
Step 1: Read RAILWAY_QUICK_START.md (5 min read)
   ↓
Step 2: Prepare checklist items (10 min):
   - AWS S3 credentials
   - Generate JWT secret
   - Get Gemini API key (optional)
   ↓
Step 3: Create Railway project (1 min)
   ↓
Step 4: Add PostgreSQL (30 sec)
   ↓
Step 5: Set environment variables (2 min)
   ↓
Step 6: Push to GitHub → Auto-deploy (7 min)
   ↓
Step 7: Verify with health check (30 sec)
   ↓
Step 8: Update Android app URL (2 min)
   ↓
Step 9: Test full app (5 min)
   ↓
✅ DONE! You're live on Railway!
```

**Total Time:** ~35 minutes (including build time)

---

## 🆘 Need Help?

**Railway Support:**
- Discord: https://discord.gg/railway
- Docs: https://docs.railway.app
- Status: https://status.railway.app

**Project-Specific Issues:**
- Check: `backend/RAILWAY_DEPLOYMENT_GUIDE.md` → Troubleshooting section
- Review: `backend/QUARTERS_SETUP.md` → Common quarters issues

---

## ✨ What Happens on First Deploy?

Railway will:
1. ✅ Clone your GitHub repository
2. ✅ Detect Node.js from package.json
3. ✅ Run `npm install && npm run build`
4. ✅ Create PostgreSQL database
5. ✅ Set DATABASE_URL automatically
6. ✅ Run `node scripts/railway-start.js`:
   - Test database connection
   - Run migrations
   - Seed quarters (Q1-Q4 2025)
   - Start server
7. ✅ Health check `/api/v1/health`
8. ✅ Mark deployment as successful

**Build Logs Show:**
```
🚂 Railway Start Script - MGB MRFC Manager
============================================

📍 Step 1: Testing database connection...
✅ DATABASE_URL found

📍 Step 2: Running database migrations...
✅ Step 2: Running database migrations - SUCCESS

📍 Step 3: Seeding quarters (Q1-Q4 2025)...
✅ Step 3: Seeding quarters (Q1-Q4 2025) - SUCCESS

🚀 Step 4: Starting server...
============================================

Server running on port 3000
Database connected successfully
Health Check: https://your-app.railway.app/api/v1/health
```

---

## 🎉 Success Checklist

After deployment, you should see:

- ✅ Railway dashboard shows "Active"
- ✅ Health endpoint returns `{"status":"ok"}`
- ✅ Swagger docs load at `/api-docs`
- ✅ Login API returns JWT token
- ✅ Quarters API returns 4 quarters
- ✅ Android app connects successfully
- ✅ File upload works
- ✅ OCR + AI analysis works
- ✅ No 504 timeout errors

---

## 📌 Important Notes

1. **Quarters MUST be seeded!** File upload won't work without them. (See QUARTERS_SETUP.md)
2. **Health check timeout is 300 seconds** (5 minutes) to support OCR operations
3. **Railway auto-deploys** on every GitHub push to main/master branch
4. **DATABASE_URL syntax:** Must use `${{Postgres.DATABASE_URL}}` (Railway-specific)
5. **Memory requirement:** Set to 1 GB minimum for canvas/OCR operations
6. **Gemini AI is optional:** System falls back to keyword analysis if not provided

---

**Ready to deploy? Start with: `backend/RAILWAY_QUICK_START.md`** 🚀

**Questions? Check: `backend/RAILWAY_DEPLOYMENT_GUIDE.md`** 📖

**Detailed checklist? Use: `backend/RAILWAY_DEPLOYMENT_CHECKLIST.md`** ✅

---

**Good luck with your deployment! 🚂💨**

