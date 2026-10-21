@echo off
cd /d "%~dp0"
java -Djava.library.path=lib -cp "ModulePhong.jar;lib/*" demo.ModulePhongApp --sql
if errorlevel 1 pause
