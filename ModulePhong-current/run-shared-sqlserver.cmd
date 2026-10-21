@echo off
cd /d "%~dp0"
java -Djava.library.path=lib -cp "ModulePhong.jar;lib/*" app.HotelSharedApp --sql
if errorlevel 1 pause
