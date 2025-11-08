@echo off
echo =========================================
echo  Render Deployment Checklist
echo =========================================
echo.
echo Before deploying to Render, make sure:
echo.
echo [1] Git repository is up to date
echo     Run: git add . ^&^& git commit -m "Ready for deployment" ^&^& git push
echo.
echo [2] Environment variables are ready:
echo     - DATABASE_URL (PostgreSQL connection string)
echo     - JWT_SECRET (strong random string)
echo     - CLOUDINARY_CLOUD_NAME
echo     - CLOUDINARY_API_KEY
echo     - CLOUDINARY_API_SECRET
echo     - SUPER_ADMIN_USERNAME
echo     - SUPER_ADMIN_PASSWORD
echo     - SUPER_ADMIN_EMAIL
echo.
echo [3] render.yaml is in the root directory
echo     This tells Render to deploy only the backend folder
echo.
echo =========================================
echo  Deployment Steps
echo =========================================
echo.
echo 1. Go to https://render.com
echo 2. Sign in with GitHub
echo 3. Click "New +" -^> "Blueprint"
echo 4. Select this repository
echo 5. Render will read render.yaml automatically
echo 6. Add environment variables in the dashboard
echo 7. Click "Apply"
echo.
echo OR manually create Web Service:
echo 1. Click "New +" -^> "Web Service"
echo 2. Connect your GitHub repository
echo 3. Set "Root Directory" to: backend
echo 4. Build Command: npm install ^&^& npm run build
echo 5. Start Command: npm start
echo 6. Add environment variables
echo 7. Click "Create Web Service"
echo.
echo =========================================
echo  After Deployment
echo =========================================
echo.
echo 1. Wait for build to complete (3-5 minutes)
echo 2. Open the Render Shell and run:
echo    npm run db:reset
echo.
echo 3. Test your API:
echo    https://your-service-name.onrender.com/api/v1/health
echo.
echo 4. Update Android app with new API URL
echo.
echo =========================================
echo.
pause

