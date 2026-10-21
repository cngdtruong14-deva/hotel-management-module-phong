@echo off
cd /d "%~dp0"
java -cp "ModulePhong.jar;lib/*" app.HotelSharedApp
if errorlevel 1 pause
