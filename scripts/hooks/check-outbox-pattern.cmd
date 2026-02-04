@echo off
pwsh -File "%~dp0check-outbox-pattern.ps1" -Files %*
exit /b %ERRORLEVEL%
