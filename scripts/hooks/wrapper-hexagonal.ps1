#!/usr/bin/env pwsh
# Wrapper for check-hexagonal-architecture.ps1
# Pre-commit passes files as separate arguments, we need to collect them into an array

$scriptPath = Join-Path $PSScriptRoot "check-hexagonal-architecture.ps1"
& pwsh -File $scriptPath -Files $args
exit $LASTEXITCODE
