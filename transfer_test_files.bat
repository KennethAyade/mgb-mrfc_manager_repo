@echo off
echo ================================================
echo FILE TRANSFER TO ANDROID EMULATOR
echo ================================================
echo.

REM Set ADB path - Update this if your Android SDK is in a different location
set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe

REM Check if ADB exists
if not exist "%ADB_PATH%" (
    echo ERROR: ADB not found at %ADB_PATH%
    echo.
    echo Please update the ADB_PATH in this script to match your Android SDK location.
    echo Common locations:
    echo   - %LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
    echo   - C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe
    echo.
    pause
    exit /b 1
)

echo ADB found at: %ADB_PATH%
echo.

REM Check connected devices
echo Checking connected devices...
"%ADB_PATH%" devices
echo.

REM Instructions
echo ================================================
echo INSTRUCTIONS:
echo ================================================
echo 1. Place your test files in this folder: D:\FREELANCE\MGB\test_files\
echo 2. Create that folder if it doesn't exist
echo 3. Then run this command to transfer:
echo.
echo    "%ADB_PATH%" push D:\FREELANCE\MGB\test_files\ /sdcard/Download/
echo.
echo Files will be transferred to the emulator's Download folder.
echo ================================================
echo.

REM Ask if user wants to proceed with transfer
set /p proceed="Do you have files in D:\FREELANCE\MGB\test_files\ to transfer? (Y/N): "
if /i "%proceed%"=="Y" (
    if exist "D:\FREELANCE\MGB\test_files\" (
        echo.
        echo Transferring files...
        "%ADB_PATH%" push "D:\FREELANCE\MGB\test_files\" /sdcard/Download/
        echo.
        echo ✅ Transfer complete! Files are now in the emulator's Download folder.
    ) else (
        echo.
        echo ERROR: Folder D:\FREELANCE\MGB\test_files\ does not exist!
        echo Please create it and add your test PDF files.
    )
) else (
    echo.
    echo No transfer performed. Follow the instructions above when ready.
)

echo.
echo ================================================
pause

