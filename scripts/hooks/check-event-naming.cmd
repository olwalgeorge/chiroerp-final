@echo off
pwsh -File "%~dp0check-event-naming.ps1" -Files %*
exit /b %ERRORLEVEL%
