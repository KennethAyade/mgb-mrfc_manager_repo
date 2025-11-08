# 🚀 Deploy Backend to Render - Quick Start

## ✅ What's Ready

Your backend is configured and ready to deploy to Render! The 401 Cloudinary issue is fixed.

---

## 📋 Files Created for Deployment

1. **`render.yaml`** (root directory)
   - Tells Render to deploy only the `backend` folder
   - Configures build and start commands

2. **`RENDER_DEPLOYMENT_GUIDE.md`**
   - Complete step-by-step deployment guide
   - Troubleshooting tips

3. **`backend/RENDER_ENV_TEMPLATE.txt`**
   - Template for environment variables
   - Copy these to Render dashboard

4. **`deploy-to-render.bat`**
   - Checklist before deployment
   - Quick reference guide

---

## 🎯 Quick Deployment (3 Steps)

### Step 1: Push to GitHub

```bash
git add .
git commit -m "Backend ready for Render deployment"
git push
```

### Step 2: Create Render Web Service

1. Go to https://render.com
2. Sign in with GitHub
3. Click **"New +"** → **"Web Service"**
4. Select your repository
5. Configure:
   - **Name:** `mgb-mrfc-backend`
   - **Root Directory:** `backend` ⚠️ **IMPORTANT!**
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
   - **Instance Type:** Free (or Starter)

### Step 3: Add Environment Variables

Click **"Environment"** tab and add (from `backend/RENDER_ENV_TEMPLATE.txt`):

**Required:**
- `NODE_ENV` = `production`
- `DATABASE_URL` = (get from Render PostgreSQL)
- `JWT_SECRET` = (generate random string)
- `CLOUDINARY_CLOUD_NAME` = `drxjbb7np`
- `CLOUDINARY_API_KEY` = (your key)
- `CLOUDINARY_API_SECRET` = (your secret)
- `SUPER_ADMIN_USERNAME` = `superadmin`
- `SUPER_ADMIN_PASSWORD` = (your password)
- `SUPER_ADMIN_EMAIL` = `admin@mgb.gov.ph`

Click **"Create Web Service"**

---

## 🗄️ PostgreSQL Database

### Option A: Use Render PostgreSQL (Recommended)

1. In Render dashboard: **"New +"** → **"PostgreSQL"**
2. Name: `mgb-mrfc-db`
3. Create Database
4. Copy **"Internal Database URL"**
5. Add to your web service as `DATABASE_URL`

### Option B: Use Existing Database

Add your existing PostgreSQL URL to `DATABASE_URL` environment variable.

---

## 🔧 Initialize Database (After First Deploy)

1. Wait for deployment to complete
2. Go to your web service in Render
3. Click **"Shell"** tab
4. Run:
```bash
npm run db:reset
```

This creates tables and superadmin user.

---

## ✅ Test Your Deployment

**Health Check:**
```
https://your-service-name.onrender.com/api/v1/health
```

**Login Test:**
```bash
curl -X POST https://your-service-name.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"your_password"}'
```

---

## 📱 Update Android App

Update your Android app's API base URL:

```kotlin
// Before
const val BASE_URL = "http://localhost:3000/api/v1/"

// After
const val BASE_URL = "https://your-service-name.onrender.com/api/v1/"
```

---

## 💡 Important Notes

### Free Tier Behavior
- Service **spins down after 15 minutes** of inactivity
- First request after spin-down takes **~30 seconds** to wake up
- Upgrade to Starter ($7/month) for always-on service

### Automatic Deployments
- Every push to `main` branch triggers automatic deployment
- Check deployment logs in Render dashboard

### Environment Variables
- Can be updated anytime in Render dashboard
- Changes require service restart (automatic)

---

## 🎉 You're Done!

Your backend will be live at:
```
https://your-service-name.onrender.com
```

API endpoints:
```
https://your-service-name.onrender.com/api/v1/health
https://your-service-name.onrender.com/api/v1/auth/login
https://your-service-name.onrender.com/api/v1/documents
... (all your endpoints)
```

---

## 📚 Need More Details?

See **`RENDER_DEPLOYMENT_GUIDE.md`** for complete documentation.

---

**Status:** ✅ Ready to deploy!

