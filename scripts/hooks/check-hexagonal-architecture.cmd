@echo off
pwsh -File "%~dp0check-hexagonal-architecture.ps1" -Files %*
exit /b %ERRORLEVEL%
