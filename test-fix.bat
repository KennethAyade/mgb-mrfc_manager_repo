@echo off
echo ========================================
echo  Cloudinary 401 Fix - Backend Test
echo ========================================
echo.
echo This will simulate document download
echo through the backend API to verify the
echo 401 error is fixed.
echo.
echo Prerequisites:
echo   - Backend must be running
echo   - At least one document uploaded
echo.
pause

echo.
echo Running test...
echo.

node test-cloudinary-fix.js

echo.
pause
