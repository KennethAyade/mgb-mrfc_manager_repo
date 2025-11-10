@echo off
echo ========================================
echo Deleting Gradle Cache Completely
echo ========================================
echo.
echo This will delete the entire Gradle cache and force re-download
echo of all dependencies. This may take 5-10 minutes.
echo.
pause

echo Stopping all Gradle daemons...
call gradlew.bat --stop

echo.
echo Deleting Gradle cache directory...
rmdir /s /q "%USERPROFILE%\.gradle\caches"

echo.
echo Deleting project build directories...
rmdir /s /q .gradle
rmdir /s /q app\.gradle  
rmdir /s /q app\build
rmdir /s /q build

echo.
echo ========================================
echo Cache deleted! Now sync in Android Studio:
echo 1. File -^> Sync Project with Gradle Files
echo 2. Wait for sync to complete (5-10 minutes)
echo 3. Build -^> Rebuild Project
echo ========================================
echo.
pause

