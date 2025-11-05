@echo off
echo ================================================
echo DATABASE RESET - MGB MRFC MANAGER
echo ================================================
echo.
echo WARNING: This will DELETE ALL DATA in the database!
echo Only the superadmin user will remain.
echo.
set /p confirm="Are you sure you want to continue? (yes/no): "

if /i "%confirm%"=="yes" (
    echo.
    echo Starting database reset...
    cd backend
    npm run db:reset
) else (
    echo.
    echo Database reset cancelled.
)

pause

