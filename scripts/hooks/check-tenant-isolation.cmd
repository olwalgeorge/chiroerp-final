@echo off
pwsh -File "%~dp0check-tenant-isolation.ps1" -Files %*
exit /b %ERRORLEVEL%
