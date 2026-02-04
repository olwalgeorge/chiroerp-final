#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces Hexagonal Architecture - REST depends on use cases

.DESCRIPTION
    Validates that REST resources:
    - Inject use case interfaces, not repositories
    - Don't have business logic (delegate to use cases)
    - Convert requests to commands/queries
    - Handle results and map to HTTP responses

.NOTES
    ADR References: ADR-001 (CQRS), ADR-010 (REST Validation Standard)
    REST layer is an INPUT ADAPTER, not business logic
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$violations = @()

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $content = Get-Content -Path $file -Raw
    $lineNumber = 0

    foreach ($line in (Get-Content -Path $file)) {
        $lineNumber++

        # Check for repository injection (should use use cases instead)
        if ($line -match '@Inject.*Repository(?!Port)') {
            $violations += @{
                File = $file
                Line = $lineNumber
                Code = $line.Trim()
                Issue = "REST resource should inject use cases, not repositories"
            }
        }

        # Check for direct Panache usage
        if ($line -match 'repository\.(persist|find|delete|update|listAll|findAll)') {
            $violations += @{
                File = $file
                Line = $lineNumber
                Code = $line.Trim()
                Issue = "REST resource doing persistence - delegate to use case"
            }
        }

        # Check for domain service injection (should use use cases)
        if ($line -match '@Inject.*Service(?!Client)') {
            $violations += @{
                File = $file
                Line = $lineNumber
                Code = $line.Trim()
                Issue = "REST resource should inject use cases, not domain services"
            }
        }

        # Check for business logic in REST layer
        if ($line -match '(validate|calculate|compute|process)[A-Z]') {
            $violations += @{
                File = $file
                Line = $lineNumber
                Code = $line.Trim()
                Issue = "Business logic in REST layer - move to domain/use case"
                Severity = "WARNING"
            }
        }

        # Check for @Valid annotation on request objects
        if ($line -match '@(POST|PUT|PATCH)' -or $line -match 'fun\s+(create|update)') {
            # Look ahead in content for @Valid
            $methodContext = $content.Substring([Math]::Max(0, $content.IndexOf($line) - 100),
                                                [Math]::Min(500, $content.Length - $content.IndexOf($line)))

            if ($methodContext -match 'fun.*\(.*request' -and $methodContext -notmatch '@Valid') {
                $violations += @{
                    File = $file
                    Line = $lineNumber
                    Code = $line.Trim()
                    Issue = "Request parameter should be validated with @Valid annotation"
                    Severity = "WARNING"
                }
            }
        }
    }
}

if ($violations.Count -gt 0) {
    $errorCount = ($violations | Where-Object { $_.Severity -ne "WARNING" }).Count

    if ($errorCount -gt 0) {
        Write-Host "`n❌ HEXAGONAL ARCHITECTURE VIOLATION: REST Layer Dependencies" -ForegroundColor Red
    } else {
        Write-Host "`n⚠️  REST LAYER WARNING" -ForegroundColor Yellow
    }

    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Yellow" })
    Write-Host "`nREST resources should delegate to USE CASES, not repositories!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Yellow" })

    foreach ($v in $violations) {
        $color = if ($v.Severity -eq "WARNING") { "Yellow" } else { "Red" }
        Write-Host "`n  File: $($v.File):$($v.Line)" -ForegroundColor Cyan
        Write-Host "  Code: $($v.Code)" -ForegroundColor White
        Write-Host "  Issue: $($v.Issue)" -ForegroundColor $color
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Inject use case instead of repository:" -ForegroundColor White
    Write-Host "     @Inject lateinit var createJournalEntryUseCase: CreateJournalEntryUseCase" -ForegroundColor Cyan
    Write-Host "`n  2. REST resource responsibility:" -ForegroundColor White
    Write-Host "     a. Validate request (@Valid annotation)" -ForegroundColor Cyan
    Write-Host "     b. Convert request → command" -ForegroundColor Cyan
    Write-Host "     c. Call use case" -ForegroundColor Cyan
    Write-Host "     d. Convert result → HTTP response" -ForegroundColor Cyan
    Write-Host "`n  3. Example pattern:" -ForegroundColor White
    Write-Host "     @POST" -ForegroundColor Cyan
    Write-Host "     fun create(@Valid request: CreateJournalEntryRequest): Response {" -ForegroundColor Cyan
    Write-Host "         val command = request.toCommand()" -ForegroundColor Cyan
    Write-Host "         val result = useCase.execute(command)" -ForegroundColor Cyan
    Write-Host "         return result.fold(" -ForegroundColor Cyan
    Write-Host "             onSuccess = { Response.created(...).build() }," -ForegroundColor Cyan
    Write-Host "             onFailure = { Response.status(400).entity(...).build() }" -ForegroundColor Cyan
    Write-Host "         )" -ForegroundColor Cyan
    Write-Host "     }" -ForegroundColor Cyan
    Write-Host "`n  See: docs/adr/ADR-001-modular-cqrs.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Yellow" })

    if ($errorCount -gt 0) {
        exit 1
    } else {
        exit 0  # Warnings only
    }
}

Write-Host "✓ REST use case dependencies verified" -ForegroundColor Green
exit 0
