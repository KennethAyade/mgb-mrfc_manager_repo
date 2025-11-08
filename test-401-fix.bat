@echo off
echo =========================================
echo  TESTING THE 401 FIX
echo =========================================
echo.
echo This will:
echo 1. Kill old backend processes
echo 2. Start fresh backend with new code
echo 3. Wait for startup
echo 4. Test document streaming
echo.
pause

echo.
echo [1/4] Stopping old backend...
taskkill /F /IM node.exe 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [2/4] Starting backend with new code...
cd backend
start /B cmd /c "npm run dev > backend.log 2>&1"
cd ..

echo.
echo [3/4] Waiting 8 seconds for backend to initialize...
timeout /t 8 /nobreak

echo.
echo [4/4] Running test...
echo.
node test-quick.js

echo.
echo =========================================
echo  Check results above
echo =========================================
pause

