@echo off
echo ========================================
echo    ARKANOID GAME - Java Edition
echo ========================================
echo.

echo [1/2] Building project...
call mvn clean package -DskipTests -q

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo [2/2] Starting game...
echo.
java -jar target\arkanoid-game-1.0-SNAPSHOT.jar

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to run game!
    pause
    exit /b 1
)

pause
