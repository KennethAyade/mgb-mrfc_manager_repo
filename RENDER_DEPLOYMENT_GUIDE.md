# MGB MRFC Manager - Backend Deployment to Render

This guide explains how to deploy the backend to Render.

## 📋 Prerequisites

1. GitHub account (to connect your repository)
2. Render account (free tier available at https://render.com)
3. PostgreSQL database credentials (Render provides free PostgreSQL)
4. Cloudinary account credentials

---

## 🚀 Deployment Steps

### Step 1: Prepare Your Backend

The backend folder is already configured with:
- ✅ `package.json` with all dependencies
- ✅ `tsconfig.json` for TypeScript compilation
- ✅ Environment variable support

### Step 2: Create a Render Account

1. Go to https://render.com
2. Sign up with GitHub
3. Click "New +" → "Web Service"

### Step 3: Configure the Web Service

**Basic Settings:**
- **Name:** `mgb-mrfc-backend`
- **Region:** Choose closest to your location (e.g., Singapore)
- **Branch:** `main` (or your default branch)
- **Root Directory:** `backend` ⚠️ **IMPORTANT**
- **Runtime:** `Node`
- **Build Command:** `npm install && npm run build`
- **Start Command:** `npm start`

**Instance Type:**
- Free tier (or Starter if you need more resources)

### Step 4: Add Environment Variables

Click "Environment" and add these variables:

```
NODE_ENV=production

# Database (Render PostgreSQL or your own)
DATABASE_URL=<your_postgresql_connection_string>
# OR if using individual credentials:
DB_HOST=<your_db_host>
DB_PORT=5432
DB_NAME=mrfc_manager
DB_USER=<your_db_user>
DB_PASSWORD=<your_db_password>

# JWT Secret (generate a strong random string)
JWT_SECRET=<your_strong_random_secret>

# Cloudinary
CLOUDINARY_CLOUD_NAME=<your_cloudinary_cloud_name>
CLOUDINARY_API_KEY=<your_cloudinary_api_key>
CLOUDINARY_API_SECRET=<your_cloudinary_api_secret>
CLOUDINARY_FOLDER_DOCUMENTS=mgb-mrfc/documents
CLOUDINARY_FOLDER_ATTENDANCE=mgb-mrfc/attendance
CLOUDINARY_FOLDER_RECORDINGS=mgb-mrfc/recordings

# Super Admin (initial admin user)
SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_PASSWORD=<your_secure_password>
SUPER_ADMIN_EMAIL=admin@mgb.gov.ph
SUPER_ADMIN_FULL_NAME=Super Administrator

# Server Port (Render assigns this automatically)
PORT=3000
```

### Step 5: Add PostgreSQL Database (Optional - if you don't have one)

1. In Render dashboard, click "New +" → "PostgreSQL"
2. Name: `mgb-mrfc-db`
3. Region: Same as your web service
4. PostgreSQL Version: 16
5. Click "Create Database"
6. Copy the "Internal Database URL"
7. Go back to your web service → Environment → Add `DATABASE_URL`

### Step 6: Deploy

1. Click "Create Web Service"
2. Render will automatically:
   - Clone your repo
   - Navigate to `backend` folder
   - Run `npm install && npm run build`
   - Start the server with `npm start`

3. Wait for deployment (usually 3-5 minutes)

### Step 7: Initialize Database

After first deployment, you need to set up the database schema:

**Option A: Using Render Shell**
1. In Render dashboard, go to your web service
2. Click "Shell" tab
3. Run:
```bash
npm run db:reset
```

**Option B: Using Database Migrations**
If you have migration files:
```bash
npm run migrate
```

---

## 🔧 Build Configuration

Make sure your `backend/package.json` has these scripts:

```json
{
  "scripts": {
    "build": "tsc",
    "start": "node dist/server.js",
    "dev": "ts-node src/server.ts",
    "db:reset": "ts-node src/scripts/reset-database.ts"
  }
}
```

---

## 📁 Project Structure for Render

```
mgb-mrfc_manager_repo/
├── backend/              ← Render deploys from here
│   ├── src/
│   ├── package.json
│   ├── tsconfig.json
│   └── ...
└── app/                  ← Android app (not deployed)
```

---

## ✅ Verify Deployment

After deployment completes:

1. **Check Health Endpoint:**
   ```
   https://mgb-mrfc-backend.onrender.com/api/v1/health
   ```
   Should return:
   ```json
   {
     "success": true,
     "message": "MGB MRFC Manager API is running",
     "version": "1.0.0"
   }
   ```

2. **Test Login:**
   ```bash
   curl -X POST https://mgb-mrfc-backend.onrender.com/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"superadmin","password":"your_password"}'
   ```

---

## 🔄 Automatic Deployments

Render automatically redeploys when you push to your GitHub repository:
- Push to `main` branch → Automatic deployment
- Check deployment logs in Render dashboard

---

## 🐛 Troubleshooting

### Build Fails
- Check build logs in Render dashboard
- Verify `backend` folder has all dependencies in `package.json`
- Make sure TypeScript compiles: `npm run build` locally

### Database Connection Issues
- Check `DATABASE_URL` environment variable
- Verify database is running
- Check database credentials

### App Can't Connect
- Update Android app API endpoint to: `https://mgb-mrfc-backend.onrender.com`
- Make sure to include `/api/v1` prefix
- Check CORS settings if needed

---

## 💡 Pro Tips

1. **Free Tier Limitations:**
   - Service spins down after 15 min of inactivity
   - First request after spin-down takes ~30 seconds
   - Upgrade to Starter for always-on service

2. **Database Backups:**
   - Render free PostgreSQL doesn't include backups
   - Consider upgrading for production use

3. **Environment Variables:**
   - Never commit `.env` to git
   - Use Render's environment variable UI
   - Can be updated without redeployment

4. **Logs:**
   - View real-time logs in Render dashboard
   - Useful for debugging

---

## 📱 Update Android App

After deployment, update your Android app to use the Render URL:

```kotlin
// In your API configuration
const val BASE_URL = "https://mgb-mrfc-backend.onrender.com/api/v1/"
```

---

## 🎉 Done!

Your backend is now deployed to Render and accessible globally!

**Your API URL:** `https://mgb-mrfc-backend.onrender.com/api/v1`

Test it and let me know if you need any adjustments!

