# ✅ Railway Deployment Checklist

**Before you deploy, make sure you have everything ready!**

---

## 📋 Pre-Deployment Checklist

### **1. Repository Setup**
- [ ] Code pushed to GitHub
- [ ] `railway.toml` file exists in backend folder
- [ ] `nixpacks.toml` file exists in backend folder
- [ ] `.railwayignore` file exists in backend folder
- [ ] `scripts/railway-start.js` exists

### **2. AWS S3 Setup**
- [ ] S3 bucket created (e.g., `adhub-s3-demo`)
- [ ] IAM user created with S3 permissions
- [ ] Access Key ID obtained
- [ ] Secret Access Key obtained
- [ ] Bucket policy configured for public read access
- [ ] Test upload works locally

**Verify S3 Bucket Policy:**
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::adhub-s3-demo/mgb-mrfc/*"
  }]
}
```

### **3. Google Gemini API (Optional)**
- [ ] API key obtained from https://makersuite.google.com/app/apikey
- [ ] Free tier limits understood (15 req/min, 1,500/day)
- [ ] API key tested locally

**Note:** If you skip this, system will use keyword-based analysis instead.

### **4. Environment Variables Prepared**

Generate JWT Secret:
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

Copy the output and save it for Railway setup.

- [ ] `NODE_ENV=production`
- [ ] `JWT_SECRET=` (64-char random string)
- [ ] `S3_BUCKET_NAME=` (your bucket name)
- [ ] `AWS_ACCESS_KEY_ID=` (your AWS key)
- [ ] `AWS_SECRET_ACCESS_KEY=` (your AWS secret)
- [ ] `AWS_REGION=` (e.g., us-east-1)
- [ ] `GEMINI_API_KEY=` (optional)
- [ ] `PORT=3000`
- [ ] `CORS_ORIGIN=*`
- [ ] `SUPER_ADMIN_USERNAME=superadmin`
- [ ] `SUPER_ADMIN_PASSWORD=` (change from default!)
- [ ] `SUPER_ADMIN_EMAIL=admin@mgb.gov.ph`
- [ ] `SUPER_ADMIN_FULL_NAME=Super Administrator`

---

## 🚂 Railway Setup Checklist

### **1. Create Railway Account**
- [ ] Sign up at https://railway.app
- [ ] Verify email
- [ ] Connect GitHub account

### **2. Create New Project**
- [ ] Click "New Project"
- [ ] Select "Deploy from GitHub repo"
- [ ] Choose MGB MRFC Manager repository
- [ ] Wait for auto-detection (Node.js)

### **3. Add PostgreSQL Database**
- [ ] Click "+ New"
- [ ] Select "Database" → "PostgreSQL"
- [ ] Wait for database provisioning
- [ ] Verify `DATABASE_URL` variable exists

### **4. Configure Backend Service**
- [ ] Go to backend service
- [ ] Click "Variables" tab
- [ ] Click "Raw Editor"
- [ ] Paste all environment variables
- [ ] Add: `DATABASE_URL=${{Postgres.DATABASE_URL}}`
- [ ] Click "Deploy" button

### **5. Configure Build Settings (Optional)**
Railway uses `railway.toml` automatically, but you can verify:

- [ ] Build Command: `npm install && npm run build`
- [ ] Start Command: `node scripts/railway-start.js`
- [ ] Health Check Path: `/api/v1/health`
- [ ] Health Check Timeout: `300` seconds

### **6. Set Resource Limits**
- [ ] Go to "Settings" → "Resources"
- [ ] Set Memory: **1 GB** (minimum for OCR + canvas)
- [ ] Set CPU: **1 vCPU** or higher

### **7. Enable Auto-Deploy**
- [ ] Go to "Settings" → "Deployment"
- [ ] Enable "Auto-deploy on GitHub push"
- [ ] Set branch: `main` or `master`

---

## ✅ Post-Deployment Checklist

### **1. Verify Deployment**
- [ ] Build completed successfully (check logs)
- [ ] No errors in deployment logs
- [ ] Service status shows "Active"

### **2. Test Endpoints**

**Health Check:**
```bash
curl https://your-app.railway.app/api/v1/health
```
- [ ] Returns `{"status":"ok",...}`

**Swagger Docs:**
```bash
# Visit in browser:
https://your-app.railway.app/api-docs
```
- [ ] Swagger UI loads
- [ ] All endpoints visible

**Login API:**
```bash
curl -X POST https://your-app.railway.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Admin@123"}'
```
- [ ] Returns JWT token
- [ ] User object includes role

### **3. Verify Database**
- [ ] Go to Railway → PostgreSQL service → Data tab
- [ ] Tables exist: `users`, `mrfcs`, `proponents`, `agendas`, `documents`, `compliance_analyses`, `quarters`
- [ ] `users` table has Super Admin
- [ ] `quarters` table has 4 rows (Q1-Q4 2025)

**Check Quarters:**
```bash
curl https://your-app.railway.app/api/v1/quarters \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
- [ ] Returns 4 quarters (Q1-Q4 2025)

**If quarters empty, run:**
```bash
railway run npm run db:seed:quarters
```

### **4. Test File Upload**
```bash
curl -X POST https://your-app.railway.app/api/v1/documents/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test.pdf" \
  -F "quarter_id=4" \
  -F "document_type=CMVR" \
  -F "document_name=Test Upload"
```
- [ ] Upload succeeds
- [ ] S3 file visible in AWS console
- [ ] Document appears in database

### **5. Test OCR + AI Analysis**
- [ ] Upload a CMVR PDF
- [ ] Wait for OCR processing (2-3 min for scanned PDFs)
- [ ] Check compliance analysis exists
- [ ] Verify Gemini AI response (or keyword fallback)

---

## 📱 Update Android App

### **1. Update API URL**
**File:** `app/src/main/java/com/mgb/mrfcmanager/data/remote/ApiConfig.kt`

```kotlin
private const val BASE_URL = "https://your-app.railway.app/api/v1/"
```

- [ ] API URL updated
- [ ] Gradle sync completed
- [ ] App builds successfully

### **2. Test Android App**
- [ ] Login works
- [ ] Dashboard loads with real data
- [ ] MRFCs list loads
- [ ] Document upload works
- [ ] CMVR compliance analysis works
- [ ] No 401 errors
- [ ] No timeout errors

---

## 🔒 Security Checklist

- [ ] Change default Super Admin password from `Admin@123`
- [ ] JWT_SECRET is strong random string (not default)
- [ ] AWS credentials are correct (test uploads)
- [ ] S3 bucket policy allows only necessary access
- [ ] CORS_ORIGIN restricted (if needed)
- [ ] Railway MFA enabled
- [ ] .env files not committed to GitHub
- [ ] Sensitive data in Railway variables only

---

## 💰 Billing Checklist

- [ ] Railway billing plan selected
- [ ] Credit card added (for pay-as-you-go)
- [ ] Usage alerts configured
- [ ] Monthly budget set

**Recommended Plan:** Developer Plan ($20/month)

**Expected Usage:**
- Backend service: $10-15/month
- PostgreSQL: $5-10/month
- **Total:** ~$15-25/month

---

## 🆘 Troubleshooting Checklist

If something goes wrong, check:

- [ ] All environment variables set correctly
- [ ] DATABASE_URL uses `${{Postgres.DATABASE_URL}}` syntax
- [ ] PostgreSQL plugin added to project
- [ ] Quarters table seeded (critical!)
- [ ] S3 bucket policy correct
- [ ] AWS credentials valid
- [ ] Health check timeout set to 300 seconds
- [ ] Memory limit at least 1 GB
- [ ] Build logs show no errors
- [ ] Runtime logs show server started

---

## 📚 Documentation Links

- [ ] Read: [RAILWAY_QUICK_START.md](./RAILWAY_QUICK_START.md)
- [ ] Read: [RAILWAY_DEPLOYMENT_GUIDE.md](./RAILWAY_DEPLOYMENT_GUIDE.md)
- [ ] Read: [QUARTERS_SETUP.md](./QUARTERS_SETUP.md)
- [ ] Read: [S3_BUCKET_SETUP_GUIDE.md](../S3_BUCKET_SETUP_GUIDE.md)
- [ ] Read: [PROJECT_STATUS.md](../PROJECT_STATUS.md)

---

## ✅ Final Check

Before announcing deployment:

- [ ] All checklist items above completed
- [ ] Android app tested with Railway backend
- [ ] No errors in Railway logs
- [ ] File upload works
- [ ] OCR + AI analysis works
- [ ] Dashboard shows real data
- [ ] Login works for all user roles

---

**Deployment Date:** ___________________  
**Deployed By:** ___________________  
**Railway URL:** ___________________  
**Notes:** 
_____________________________________________________________
_____________________________________________________________
_____________________________________________________________

---

**🎉 Congratulations! Your MGB MRFC Manager is live on Railway!**

