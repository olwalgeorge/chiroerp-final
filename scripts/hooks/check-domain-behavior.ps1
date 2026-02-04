#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces DDD - Rich domain models (not anemic)

.DESCRIPTION
    Validates that domain entities contain business logic:
    - Have behavior methods (not just getters/setters)
    - Validate their own invariants
    - Encapsulate domain rules

    Warns about anemic models (data classes with no behavior).

.NOTES
    ADR References: ADR-009 (Financial Accounting Domain)
    DDD Principle: Domain models should encapsulate behavior, not just data
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$warnings = @()

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    # Skip test files
    if ($file -match '/test/') { continue }

    # Skip DTOs, requests, responses (allowed to be anemic)
    if ($file -match '(DTO|Request|Response|Event)\.kt$') { continue }

    $content = Get-Content -Path $file -Raw

    # Check if this is a domain entity/aggregate
    $isEntity = $content -match 'data class.*\(' -or $content -match 'class.*\('
    if (-not $isEntity) { continue }

    # Count properties vs methods
    $propertyCount = ([regex]::Matches($content, 'val\s+\w+\s*:')).Count
    $methodCount = ([regex]::Matches($content, 'fun\s+\w+\s*\(')).Count

    # Check for validation methods
    $hasValidation = $content -match 'fun\s+validate' -or $content -match 'Result<'

    # Check for business rule methods
    $hasBusinessLogic = $content -match 'fun\s+(can|is|has|apply|calculate|compute|process|execute)'

    # Anemic model detection
    if ($propertyCount -gt 3 -and $methodCount -eq 0) {
        $warnings += @{
            File = $file
            Issue = "ANEMIC MODEL - Only properties, no behavior methods"
            Severity = "ERROR"
            PropertyCount = $propertyCount
            MethodCount = $methodCount
        }
    }
    elseif ($propertyCount -gt 5 -and -not $hasValidation -and -not $hasBusinessLogic) {
        $warnings += @{
            File = $file
            Issue = "Potential anemic model - Missing validation and business logic"
            Severity = "WARNING"
            PropertyCount = $propertyCount
            MethodCount = $methodCount
        }
    }
}

if ($warnings.Count -gt 0) {
    $errorCount = ($warnings | Where-Object { $_.Severity -eq "ERROR" }).Count

    if ($errorCount -gt 0) {
        Write-Host "`n❌ DDD VIOLATION: Anemic Domain Models Detected" -ForegroundColor Red
    } else {
        Write-Host "`n⚠️  DDD WARNING: Potential Anemic Models" -ForegroundColor Yellow
    }

    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Yellow" })
    Write-Host "`nDomain models should have BEHAVIOR, not just data!" -ForegroundColor Yellow
    Write-Host "`nIssues found:" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Yellow" })

    foreach ($w in $warnings) {
        $color = if ($w.Severity -eq "ERROR") { "Red" } else { "Yellow" }
        Write-Host "`n  File: $($w.File)" -ForegroundColor Cyan
        Write-Host "  Properties: $($w.PropertyCount) | Methods: $($w.MethodCount)" -ForegroundColor White
        Write-Host "  Issue: $($w.Issue)" -ForegroundColor $color
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Add validation methods:" -ForegroundColor White
    Write-Host "     fun validateBalance(): Result<Unit> { ... }" -ForegroundColor Cyan
    Write-Host "`n  2. Add business rule methods:" -ForegroundColor White
    Write-Host "     fun canPost(): Boolean { ... }" -ForegroundColor Cyan
    Write-Host "     fun post(userId: String): Result<JournalEntry> { ... }" -ForegroundColor Cyan
    Write-Host "`n  3. Encapsulate invariants:" -ForegroundColor White
    Write-Host "     fun addLine(line: JournalEntryLine): Result<JournalEntry> {" -ForegroundColor Cyan
    Write-Host "         // Validate balance after adding line" -ForegroundColor Cyan
    Write-Host "     }" -ForegroundColor Cyan
    Write-Host "`n  4. Tell, don't ask (avoid getters for business logic)" -ForegroundColor White
    Write-Host "`n  See: docs/adr/ADR-009-financial-accounting-domain.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Yellow" })

    if ($errorCount -gt 0) {
        exit 1  # Fail on errors
    } else {
        exit 0  # Warnings only
    }
}

Write-Host "✓ Domain models have adequate behavior" -ForegroundColor Green
exit 0
