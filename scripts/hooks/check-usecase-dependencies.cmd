@echo off
pwsh -File "%~dp0check-usecase-dependencies.ps1" -Files %*
exit /b %ERRORLEVEL%
