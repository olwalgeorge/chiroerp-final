@echo off
pwsh -File "%~dp0check-rest-dependencies.ps1" -Files %*
exit /b %ERRORLEVEL%
