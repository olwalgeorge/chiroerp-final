#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Install and configure pre-commit hooks for ChiroERP

.DESCRIPTION
    Sets up pre-commit framework and installs architectural compliance hooks.
    Validates that all hook scripts are executable and properly configured.

.EXAMPLE
    .\scripts\install-hooks.ps1
#>

$ErrorActionPreference = "Stop"

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  ChiroERP Pre-Commit Hook Installation                     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

$ROOT_PATH = Split-Path -Parent $PSScriptRoot

# Check if Python is installed
Write-Host "[1/5] Checking Python installation..." -ForegroundColor Cyan
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✓ Python found: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Python not found!" -ForegroundColor Red
    Write-Host "  Please install Python 3.8+ from https://www.python.org/" -ForegroundColor Yellow
    exit 1
}

# Check if pip is available
Write-Host "`n[2/5] Checking pip..." -ForegroundColor Cyan
try {
    $pipVersion = pip --version 2>&1
    Write-Host "✓ pip found: $pipVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ pip not found!" -ForegroundColor Red
    Write-Host "  Please reinstall Python with pip included" -ForegroundColor Yellow
    exit 1
}

# Install pre-commit
Write-Host "`n[3/5] Installing pre-commit framework..." -ForegroundColor Cyan
try {
    pip install pre-commit --quiet
    Write-Host "✓ pre-commit installed successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to install pre-commit" -ForegroundColor Red
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

# Validate hook scripts
Write-Host "`n[4/5] Validating hook scripts..." -ForegroundColor Cyan
$hookScripts = @(
    "check-hexagonal-architecture.ps1",
    "check-usecase-dependencies.ps1",
    "check-domain-behavior.ps1",
    "check-event-naming.ps1",
    "check-command-validation.ps1",
    "check-tenant-isolation.ps1",
    "check-rest-dependencies.ps1",
    "check-outbox-pattern.ps1"
)

$missingScripts = @()
foreach ($script in $hookScripts) {
    $scriptPath = Join-Path $ROOT_PATH "scripts\hooks\$script"
    if (-not (Test-Path $scriptPath)) {
        $missingScripts += $script
    }
}

if ($missingScripts.Count -gt 0) {
    Write-Host "✗ Missing hook scripts:" -ForegroundColor Red
    foreach ($script in $missingScripts) {
        Write-Host "  - $script" -ForegroundColor Red
    }
    exit 1
}

Write-Host "✓ All $($hookScripts.Count) hook scripts found" -ForegroundColor Green

# Install git hooks
Write-Host "`n[5/5] Installing git pre-commit hooks..." -ForegroundColor Cyan
Set-Location $ROOT_PATH

try {
    pre-commit install 2>&1 | Out-Null
    Write-Host "✓ Git pre-commit hooks installed" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to install git hooks" -ForegroundColor Red
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

# Test configuration
Write-Host "`n[Bonus] Validating pre-commit configuration..." -ForegroundColor Cyan
try {
    pre-commit validate-config 2>&1 | Out-Null
    Write-Host "✓ Configuration is valid" -ForegroundColor Green
} catch {
    Write-Host "⚠ Configuration validation failed (non-fatal)" -ForegroundColor Yellow
}

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✓ Pre-Commit Hooks Installed Successfully!                ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Green

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Test hooks:    pre-commit run --all-files" -ForegroundColor Cyan
Write-Host "  2. Make changes:  git add <files>" -ForegroundColor Cyan
Write-Host "  3. Commit:        git commit -m 'Your message'" -ForegroundColor Cyan
Write-Host "                    (hooks run automatically)" -ForegroundColor Gray
Write-Host "`n  See: scripts/hooks/README.md for detailed usage" -ForegroundColor Cyan
Write-Host ""
