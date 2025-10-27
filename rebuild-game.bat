@echo off
cd /d "%~dp0"
echo ========================================
echo    REBUILD ARKANOID GAME
echo ========================================
echo.
echo This will rebuild the entire project.
echo Use this when you have made code changes.
echo.
echo Press any key to continue or Ctrl+C to cancel...
pause >nul

echo.
echo [REBUILDING] Please wait...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] Rebuild completed!
echo ========================================
echo.
echo You can now run start-game.bat
echo.
pause
