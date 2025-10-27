@echo off
cd /d "%~dp0"
echo ========================================
echo    ARKANOID GAME - Java Edition
echo ========================================
echo.

REM Check if JAR file exists
if not exist "target\arkanoid-game-1.0-SNAPSHOT.jar" (
    echo [INFO] JAR file not found. Building project...
    call mvn clean package -DskipTests -q
    
    if %errorlevel% neq 0 (
        echo.
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    echo [SUCCESS] Build completed!
) else (
    echo [INFO] Using existing JAR file (skip build for faster startup)
)

timeout /t 1 /nobreak >nul

echo [STARTING GAME] Please wait...
echo.
REM Run with optimized JVM flags for better performance
java -Xms256m -Xmx512m -XX:+UseG1GC -jar target\arkanoid-game-1.0-SNAPSHOT.jar

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to run game!
    pause
    exit /b 1
)

pause
