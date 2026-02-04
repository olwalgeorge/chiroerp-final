#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces Multi-Tenancy - Repository tenant isolation

.DESCRIPTION
    Validates that repository query methods:
    - Accept tenantId as first parameter
    - Filter by tenantId in WHERE clause
    - Don't expose cross-tenant data leakage

    Critical for SaaS security and data isolation.

.NOTES
    ADR References: ADR-005 (Multi-Tenancy Isolation)
    Security: Every query MUST filter by tenant to prevent data leaks
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$violations = @()

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $content = Get-Content -Path $file -Raw
    $lines = Get-Content -Path $file

    # Check for multi-line find() calls
    $findMatches = [regex]::Matches($content, '(?s)return\s+find\s*\([^)]+\)')
    foreach ($match in $findMatches) {
        if ($match.Value -notmatch 'tenantId') {
            $position = $match.Index
            $lineNumber = ($content.Substring(0, $position) -split "`n").Count

            $violations += @{
                File = $file
                Line = $lineNumber
                Method = "return find("
                Issue = "Panache find() call must filter by tenantId"
            }
        }
    }

    $lineNumber = 0
    foreach ($line in $lines) {
        $lineNumber++

        # Check for dangerous patterns (findAll, listAll without tenant filter)
        if ($line -match '(findAll|listAll)\(\)' -and $line -notmatch '//.*allow') {
            $violations += @{
                File = $file
                Line = $lineNumber
                Method = $line.Trim()
                Issue = "CRITICAL: findAll()/listAll() without tenant filter - DATA LEAK RISK!"
                Severity = "CRITICAL"
            }
        }
    }
}

if ($violations.Count -gt 0) {
    $criticalCount = ($violations | Where-Object { $_.Severity -eq "CRITICAL" }).Count

    Write-Host "`n❌ MULTI-TENANCY VIOLATION: Tenant Isolation Required" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red

    if ($criticalCount -gt 0) {
        Write-Host "`n🚨 CRITICAL SECURITY ISSUE: Data leak risk detected!" -ForegroundColor Red
        Write-Host "   This could expose data across tenants in production!" -ForegroundColor Red
    }

    Write-Host "`nAll repository queries MUST filter by tenantId!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor Red

    foreach ($v in $violations) {
        $color = if ($v.Severity -eq "CRITICAL") { "Red" } else { "Yellow" }
        Write-Host "`n  File: $($v.File):$($v.Line)" -ForegroundColor Cyan
        Write-Host "  Code: $($v.Method)" -ForegroundColor White
        Write-Host "  Issue: $($v.Issue)" -ForegroundColor $color
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Add tenantId as first parameter:" -ForegroundColor White
    Write-Host "     fun findByTenantAndEntryNumber(" -ForegroundColor Cyan
    Write-Host "         tenantId: UUID," -ForegroundColor Cyan
    Write-Host "         entryNumber: String" -ForegroundColor Cyan
    Write-Host "     ): JournalEntryEntity?" -ForegroundColor Cyan
    Write-Host "`n  2. Filter in Panache query:" -ForegroundColor White
    Write-Host "     return find(""tenantId = ?1 and entryNumber = ?2"", tenantId, entryNumber)" -ForegroundColor Cyan
    Write-Host "         .firstResult()" -ForegroundColor Cyan
    Write-Host "`n  3. NEVER use findAll() or listAll() without tenant filter!" -ForegroundColor White
    Write-Host "`n  4. Extract tenantId from security context in use case layer" -ForegroundColor White
    Write-Host "`n  See: docs/adr/ADR-005-multi-tenancy-isolation.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red

    exit 1
}

Write-Host "✓ Repository tenant isolation verified" -ForegroundColor Green
exit 0
