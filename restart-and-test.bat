@echo off
echo ========================================
echo  Restart Backend and Test
echo ========================================
echo.
echo This will:
echo 1. Stop any running backend
echo 2. Start fresh backend
echo 3. Wait for startup
echo 4. Run the test
echo.
pause

echo.
echo Stopping old backend processes...
taskkill /F /IM node.exe /FI "WINDOWTITLE eq *backend*" 2>nul

echo.
echo Starting backend...
start "Backend Server" cmd /k "cd backend && npm run dev"

echo.
echo Waiting 10 seconds for backend to start...
timeout /t 10 /nobreak

echo.
echo Running test...
node test-quick.js

echo.
pause

