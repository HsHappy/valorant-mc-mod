@echo off
cd /d "%~dp0"
echo ===============================================
echo   Starting Minecraft Valorant Test Client...
echo ===============================================
gradlew.bat runClient
pause
