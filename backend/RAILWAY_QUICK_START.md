# 🚀 Railway Quick Start - 5 Minutes

**Deploy MGB MRFC Manager to Railway in 5 simple steps!**

---

## ⚡ Super Fast Deployment

### **Step 1: Create Railway Project (1 min)**

```bash
# Visit Railway dashboard
https://railway.app/new

# Click: "Deploy from GitHub repo"
# Select: Your MGB MRFC Manager repository
# Railway auto-detects Node.js ✅
```

---

### **Step 2: Add PostgreSQL (30 seconds)**

```bash
# In your Railway project:
# Click: "+ New"
# Select: "Database" → "PostgreSQL"
# Done! DATABASE_URL created automatically ✅
```

---

### **Step 3: Set Environment Variables (2 min)**

Go to your backend service → **Variables** → **Raw Editor**, paste:

```env
NODE_ENV=production
DATABASE_URL=${{Postgres.DATABASE_URL}}
JWT_SECRET=your_64_char_random_string_from_crypto_randomBytes
S3_BUCKET_NAME=adhub-s3-demo
AWS_ACCESS_KEY_ID=AKIA...your_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
GEMINI_API_KEY=AIzaSy...your_key
PORT=3000
CORS_ORIGIN=*
SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_PASSWORD=Admin@123
SUPER_ADMIN_EMAIL=admin@mgb.gov.ph
SUPER_ADMIN_FULL_NAME=Super Administrator
```

**Generate JWT_SECRET:**
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

---

### **Step 4: Deploy! (5-7 min build time)**

```bash
# Railway deploys automatically!
# Watch logs in "Deployments" tab
# Wait for: "✅ BUILD SUCCESSFUL"
```

---

### **Step 5: Test It! (30 seconds)**

```bash
# Get your Railway URL (looks like: https://your-app.railway.app)
# Test health endpoint:

curl https://your-app.railway.app/api/v1/health

# Expected: {"status":"ok","timestamp":"..."}

# Test login:
curl -X POST https://your-app.railway.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Admin@123"}'

# Expected: {"success":true,"data":{"token":"...","user":{...}}}
```

---

## ✅ Done!

Your backend is live at: `https://your-app.railway.app`

**Next Steps:**

1. **Update Android App:**
   - Edit: `app/src/main/java/.../data/remote/ApiConfig.kt`
   - Change: `BASE_URL = "https://your-app.railway.app/api/v1/"`
   - Rebuild app

2. **Access Swagger Docs:**
   - Visit: `https://your-app.railway.app/api-docs`

3. **View Logs:**
   - Railway dashboard → Your service → "Logs" tab

---

## 🆘 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| ❌ Build fails | Check `package.json` has `build` and `start` scripts |
| ❌ "Cannot connect to DB" | Add PostgreSQL plugin, set `DATABASE_URL=${{Postgres.DATABASE_URL}}` |
| ❌ "Loading quarters..." | SSH into Railway: `railway run npm run db:seed:quarters` |
| ❌ 504 Timeout | In `railway.toml`: set `healthcheckTimeout = 300` |
| ❌ S3 403 Forbidden | Verify AWS credentials, check bucket policy |

---

## 📚 Full Guide

For detailed troubleshooting, see: **RAILWAY_DEPLOYMENT_GUIDE.md**

---

**That's it! 🎉 Your backend is production-ready on Railway!**

