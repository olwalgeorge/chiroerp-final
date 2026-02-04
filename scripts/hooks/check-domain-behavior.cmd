@echo off
pwsh -File "%~dp0check-domain-behavior.ps1" -Files %*
exit /b %ERRORLEVEL%
