@echo off
cd /d "%~dp0"
java -Djava.library.path=lib -cp "ModulePhong.jar;lib/*" app.ModulePhongApp --sql
if errorlevel 1 pause
