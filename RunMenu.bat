@echo off
echo ========================================
echo    ARKANOID GAME - LAUNCHER
echo ========================================
echo.
echo Dang compile game...
cd src
javac *.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compile thanh cong!
    echo.
    echo Dang khoi dong Menu game...
    echo.
    java Menu
) else (
    echo.
    echo Loi compile! Vui long kiem tra code.
    pause
)
