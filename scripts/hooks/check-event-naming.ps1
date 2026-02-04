#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces EDA - Domain event naming conventions

.DESCRIPTION
    Validates that domain events:
    - Use past tense naming (JournalEntryPosted, not JournalEntryPost)
    - Implement DomainEvent interface
    - Include required metadata (eventId, occurredAt, tenantId)
    - Are immutable (data class with val properties)

.NOTES
    ADR References: ADR-020 (Event-Driven Architecture)
    Event naming: Use past tense to indicate "something that happened"
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$violations = @()

# Past tense verbs commonly used in events
$validEventSuffixes = @(
    'Created', 'Updated', 'Deleted', 'Posted', 'Reversed', 'Closed',
    'Opened', 'Approved', 'Rejected', 'Submitted', 'Completed',
    'Cancelled', 'Processed', 'Failed', 'Succeeded', 'Changed',
    'Added', 'Removed', 'Assigned', 'Unassigned', 'Activated', 'Deactivated'
)

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $content = Get-Content -Path $file -Raw

    # Extract class name
    if ($content -match 'data class\s+(\w+)' -or $content -match 'class\s+(\w+)') {
        $className = $matches[1]

        # Check if event name uses past tense
        $hasPastTense = $false
        foreach ($suffix in $validEventSuffixes) {
            if ($className -match $suffix) {
                $hasPastTense = $true
                break
            }
        }

        if (-not $hasPastTense) {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Event name must use past tense (e.g., 'Posted' not 'Post')"
            }
        }

        # Check if implements DomainEvent interface
        if ($content -notmatch ': DomainEvent' -and $content -notmatch 'interface.*DomainEvent') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Event must implement DomainEvent interface"
            }
        }

        # Check for required event metadata
        if ($content -notmatch 'eventId\s*:\s*UUID') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Event must have 'eventId: UUID' property"
            }
        }

        if ($content -notmatch 'occurredAt\s*:\s*Instant') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Event must have 'occurredAt: Instant' property"
            }
        }

        if ($content -notmatch 'tenantId\s*:\s*') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Event must have 'tenantId' property for multi-tenancy"
            }
        }

        # Check if event is immutable (data class with val)
        if ($content -match 'var\s+\w+\s*:') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Event properties must be immutable (val, not var)"
            }
        }

        if ($content -notmatch 'data class') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Events should be data classes for structural equality"
            }
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Host "`n❌ EVENT-DRIVEN ARCHITECTURE VIOLATION: Domain Event Standards" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "`nDomain events must follow naming and structure conventions!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor Red

    foreach ($v in $violations) {
        Write-Host "`n  File: $($v.File)" -ForegroundColor Cyan
        Write-Host "  Class: $($v.ClassName)" -ForegroundColor White
        Write-Host "  Issue: $($v.Issue)" -ForegroundColor Red
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Use past tense naming:" -ForegroundColor White
    Write-Host "     ✓ JournalEntryPosted" -ForegroundColor Green
    Write-Host "     ✗ JournalEntryPost" -ForegroundColor Red
    Write-Host "`n  2. Implement DomainEvent interface:" -ForegroundColor White
    Write-Host "     data class JournalEntryPosted(...) : DomainEvent" -ForegroundColor Cyan
    Write-Host "`n  3. Include required metadata:" -ForegroundColor White
    Write-Host "     data class JournalEntryPosted(" -ForegroundColor Cyan
    Write-Host "         override val eventId: UUID = UUID.randomUUID()," -ForegroundColor Cyan
    Write-Host "         override val occurredAt: Instant = Instant.now()," -ForegroundColor Cyan
    Write-Host "         val tenantId: TenantId," -ForegroundColor Cyan
    Write-Host "         val entryId: JournalEntryId," -ForegroundColor Cyan
    Write-Host "         // ... domain data" -ForegroundColor Cyan
    Write-Host "     ) : DomainEvent" -ForegroundColor Cyan
    Write-Host "`n  4. Events are immutable - use 'val' not 'var'" -ForegroundColor White
    Write-Host "`n  See: docs/adr/ADR-020-event-driven-architecture-hybrid-policy.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red

    exit 1
}

Write-Host "✓ Domain event conventions verified" -ForegroundColor Green
exit 0
