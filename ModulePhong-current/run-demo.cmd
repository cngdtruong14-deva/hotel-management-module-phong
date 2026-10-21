@echo off
cd /d "%~dp0"
java -cp "ModulePhong.jar;lib/*" demo.ModulePhongApp --demo
if errorlevel 1 pause
