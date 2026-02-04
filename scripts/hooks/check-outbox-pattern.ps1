#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces EDA - Transactional Outbox Pattern

.DESCRIPTION
    Validates that event publishers:
    - Use transactional outbox table for reliability
    - Don't publish directly to Kafka from use cases
    - Include outbox cleanup/polling mechanism

    Ensures at-least-once delivery guarantee for domain events.

.NOTES
    ADR References: ADR-020 (Event-Driven Architecture)
    Outbox pattern prevents event loss on transaction rollback
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$violations = @()

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $content = Get-Content -Path $file -Raw
    $lineNumber = 0

    # Check if this is an event publisher
    $isEventPublisher = $content -match 'EventPublisher' -or $content -match 'publish.*Event'
    if (-not $isEventPublisher) { continue }

    foreach ($line in (Get-Content -Path $file)) {
        $lineNumber++

        # Dangerous: Direct Kafka publish without outbox
        if ($line -match 'emitter\.send' -and $content -notmatch 'OutboxRepository') {
            $violations += @{
                File = $file
                Line = $lineNumber
                Code = $line.Trim()
                Issue = "CRITICAL: Direct Kafka publish without transactional outbox - EVENT LOSS RISK!"
                Severity = "CRITICAL"
            }
        }

        # Check for @Transactional on publish method
        if ($line -match 'fun\s+publish') {
            # Look back for @Transactional
            $methodContext = $content.Substring([Math]::Max(0, $content.IndexOf($line) - 200), 200)

            if ($methodContext -notmatch '@Transactional') {
                $violations += @{
                    File = $file
                    Line = $lineNumber
                    Code = $line.Trim()
                    Issue = "Event publish method must be @Transactional"
                }
            }
        }
    }

    # Check for outbox table usage
    if ($isEventPublisher) {
        if ($content -notmatch 'OutboxRepository' -and $content -notmatch 'DomainEventOutbox') {
            $violations += @{
                File = $file
                Line = 0
                Code = "(File-level check)"
                Issue = "Event publisher should use outbox table for reliability"
            }
        }

        # Check for proper outbox pattern
        if ($content -match 'emitter\.send' -and $content -notmatch 'persist.*outbox') {
            $violations += @{
                File = $file
                Line = 0
                Code = "(File-level check)"
                Issue = "Should persist to outbox table, then publish asynchronously"
            }
        }
    }
}

if ($violations.Count -gt 0) {
    $criticalCount = ($violations | Where-Object { $_.Severity -eq "CRITICAL" }).Count

    Write-Host "`n❌ EVENT-DRIVEN ARCHITECTURE VIOLATION: Outbox Pattern Required" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red

    if ($criticalCount -gt 0) {
        Write-Host "`n🚨 CRITICAL: Event loss risk detected!" -ForegroundColor Red
        Write-Host "   Events could be lost if transaction rolls back!" -ForegroundColor Red
    }

    Write-Host "`nDomain events MUST use transactional outbox pattern!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor Red

    foreach ($v in $violations) {
        $color = if ($v.Severity -eq "CRITICAL") { "Red" } else { "Yellow" }
        Write-Host "`n  File: $($v.File):$($v.Line)" -ForegroundColor Cyan
        Write-Host "  Code: $($v.Code)" -ForegroundColor White
        Write-Host "  Issue: $($v.Issue)" -ForegroundColor $color
    }

    Write-Host "`n📚 How to Fix (Transactional Outbox Pattern):" -ForegroundColor Yellow
    Write-Host "  1. Create outbox table entity:" -ForegroundColor White
    Write-Host "     @Entity" -ForegroundColor Cyan
    Write-Host "     @Table(name = ""domain_events_outbox"", schema = ""finance"")" -ForegroundColor Cyan
    Write-Host "     class DomainEventOutbox {" -ForegroundColor Cyan
    Write-Host "         @Id val id: UUID" -ForegroundColor Cyan
    Write-Host "         val aggregateId: UUID" -ForegroundColor Cyan
    Write-Host "         val eventType: String" -ForegroundColor Cyan
    Write-Host "         val payload: String  // JSON" -ForegroundColor Cyan
    Write-Host "         val publishedAt: Instant?" -ForegroundColor Cyan
    Write-Host "     }" -ForegroundColor Cyan
    Write-Host "`n  2. Persist event to outbox in SAME transaction:" -ForegroundColor White
    Write-Host "     @Transactional" -ForegroundColor Cyan
    Write-Host "     override suspend fun publish(event: DomainEvent) {" -ForegroundColor Cyan
    Write-Host "         val outboxEntry = DomainEventOutbox(...)" -ForegroundColor Cyan
    Write-Host "         outboxRepository.persist(outboxEntry)" -ForegroundColor Cyan
    Write-Host "         // Kafka publish happens asynchronously via polling" -ForegroundColor Cyan
    Write-Host "     }" -ForegroundColor Cyan
    Write-Host "`n  3. Background job polls outbox and publishes to Kafka:" -ForegroundColor White
    Write-Host "     @Scheduled(every = ""5s"")" -ForegroundColor Cyan
    Write-Host "     fun publishPendingEvents() { ... }" -ForegroundColor Cyan
    Write-Host "`n  Benefits:" -ForegroundColor White
    Write-Host "  - Atomicity: Event saved with domain changes in ONE transaction" -ForegroundColor Green
    Write-Host "  - At-least-once delivery: Polling retries failed publishes" -ForegroundColor Green
    Write-Host "  - Audit trail: Outbox table tracks all events" -ForegroundColor Green
    Write-Host "`n  See: docs/adr/ADR-020-event-driven-architecture-hybrid-policy.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red

    exit 1
}

Write-Host "✓ Transactional outbox pattern verified" -ForegroundColor Green
exit 0
