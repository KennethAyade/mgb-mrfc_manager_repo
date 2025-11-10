@echo off
echo =========================================
echo  Fix Render Build Error ^& Deploy
echo =========================================
echo.
echo Fixed: Moved TypeScript and @types to dependencies
echo This allows Render to build the project successfully
echo.
pause

echo.
echo [1/3] Committing the fix...
git add backend/package.json
git commit -m "Fix: Move TypeScript and @types to dependencies for Render build"

echo.
echo [2/3] Pushing to GitHub...
git push

echo.
echo [3/3] Done!
echo.
echo Render will automatically detect the push and redeploy.
echo Check your Render dashboard for deployment progress.
echo.
echo The build should succeed now!
echo.
pause



