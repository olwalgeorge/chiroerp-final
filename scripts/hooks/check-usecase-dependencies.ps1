#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces Hexagonal Architecture - Use cases depend on ports, not adapters

.DESCRIPTION
    Validates that use case implementations:
    - Depend on port interfaces (application/port/output/*)
    - Do NOT depend on concrete adapters (infrastructure/adapter/*)
    - Do NOT directly inject Panache repositories
    - Do NOT directly inject Kafka emitters

.NOTES
    ADR References: ADR-001 (CQRS with Use Cases)
    Dependency Inversion Principle: Depend on abstractions, not concretions
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$violations = @()

# Forbidden patterns in use case layer
$forbiddenPatterns = @(
    'import\s+.*\.infrastructure\.adapter\.',
    'import\s+.*\.infrastructure\.persistence\.',
    'import\s+io\.quarkus\.hibernate\.orm\.panache\.',
    'import\s+io\.smallrye\.reactive\.messaging\.',
    '@Inject\s+lateinit\s+var\s+.*Repository\s*:\s*.*Repository(?!Port)',  # Direct repository, not port
    '@Inject\s+lateinit\s+var\s+.*Emitter'
)

$requiredPatterns = @(
    'import\s+.*\.application\.port\.output\.'  # Should import from ports
)

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $content = Get-Content -Path $file -Raw
    $lineNumber = 0
    $hasPortImport = $false

    foreach ($line in (Get-Content -Path $file)) {
        $lineNumber++

        # Check for required port imports
        foreach ($pattern in $requiredPatterns) {
            if ($line -match $pattern) {
                $hasPortImport = $true
            }
        }

        # Check for forbidden adapter imports
        foreach ($pattern in $forbiddenPatterns) {
            if ($line -match $pattern) {
                $violations += @{
                    File = $file
                    Line = $lineNumber
                    Content = $line.Trim()
                    Violation = "Use case cannot depend on concrete adapters - use ports instead"
                }
            }
        }
    }

    # Warn if use case doesn't import any ports (might be missing dependencies)
    if (-not $hasPortImport -and $content -match 'class.*UseCase') {
        $violations += @{
            File = $file
            Line = 0
            Content = "(No port imports found)"
            Violation = "Use case should depend on output ports (repository/event publisher interfaces)"
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Host "`n❌ HEXAGONAL ARCHITECTURE VIOLATION: Use Case Dependencies" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "`nUse cases must depend on PORTS, not ADAPTERS!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor Red

    foreach ($v in $violations) {
        Write-Host "`n  File: $($v.File):$($v.Line)" -ForegroundColor Cyan
        Write-Host "  Code: $($v.Content)" -ForegroundColor White
        Write-Host "  Issue: $($v.Violation)" -ForegroundColor Red
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Define repository interface in application/port/output/" -ForegroundColor White
    Write-Host "     Example: JournalEntryRepositoryPort.kt" -ForegroundColor Cyan
    Write-Host "`n  2. Use case depends on port interface:" -ForegroundColor White
    Write-Host "     class CreateJournalEntryUseCaseImpl(" -ForegroundColor Cyan
    Write-Host "         private val repository: JournalEntryRepositoryPort," -ForegroundColor Cyan
    Write-Host "         private val eventPublisher: DomainEventPublisherPort" -ForegroundColor Cyan
    Write-Host "     )" -ForegroundColor Cyan
    Write-Host "`n  3. Adapter implements port in infrastructure/adapter/output/" -ForegroundColor White
    Write-Host "     class JournalEntryRepositoryAdapter : JournalEntryRepositoryPort" -ForegroundColor Cyan
    Write-Host "`n  4. Quarkus CDI wires port → adapter automatically" -ForegroundColor White
    Write-Host "`n  See: docs/adr/ADR-001-modular-cqrs.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red

    exit 1
}

Write-Host "✓ Use case port dependencies verified" -ForegroundColor Green
exit 0
