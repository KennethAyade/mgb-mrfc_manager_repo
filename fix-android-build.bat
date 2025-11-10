@echo off
echo ========================================
echo Fixing Android Build - Kapt Cache Issue
echo ========================================
echo.

echo Step 1: Stopping Gradle daemon...
call gradlew.bat --stop
echo.

echo Step 2: Cleaning build directories...
rmdir /s /q .gradle 2>nul
rmdir /s /q app\.gradle 2>nul
rmdir /s /q app\build 2>nul
rmdir /s /q build 2>nul
echo.

echo Step 3: Cleaning Gradle cache...
rmdir /s /q "%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.jetbrains.kotlin" 2>nul
echo.

echo Step 4: Running clean build...
call gradlew.bat clean
echo.

echo Step 5: Building app...
call gradlew.bat :app:assembleDebug
echo.

echo ========================================
echo Build complete!
echo ========================================
echo.
echo If build still fails, please:
echo 1. Open Android Studio
echo 2. File -^> Invalidate Caches
echo 3. Check all boxes
echo 4. Click "Invalidate and Restart"
echo.
pause

