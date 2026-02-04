@echo off
pwsh -File "%~dp0check-command-validation.ps1" -Files %*
exit /b %ERRORLEVEL%
