#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces Hexagonal Architecture - Domain layer isolation

.DESCRIPTION
    Validates that domain layer code does not import from:
    - infrastructure (JPA, Quarkus, Panache, REST)
    - application layer (use cases, commands)
    - external frameworks (javax.persistence, jakarta.*, io.quarkus.*)

    Domain should only depend on:
    - Standard library (kotlin.*, java.*)
    - Other domain packages
    - Shared kernel (com.chiroerp.shared.*)

.NOTES
    ADR References: ADR-001 (CQRS), ADR-009 (Financial Domain)
    Hexagonal Architecture: Domain is the innermost layer
#>

param(
    [Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)]
    [string[]]$Files
)

$ErrorActionPreference = "Stop"
$violations = @()

# Forbidden imports for domain layer
$forbiddenPatterns = @(
    'import\s+jakarta\.',
    'import\s+javax\.persistence\.',
    'import\s+io\.quarkus\.',
    'import\s+io\.smallrye\.',
    'import\s+org\.hibernate\.',
    'import\s+jakarta\.ws\.rs\.',
    'import\s+jakarta\.persistence\.',
    'import\s+jakarta\.inject\.',
    'import\s+jakarta\.transaction\.',
    'import\s+.*\.infrastructure\.',
    'import\s+.*\.application\.usecase\.',
    'import\s+.*\.application\.port\.output\.',
    'import\s+.*\.adapter\.'
)

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $lineNumber = 0

    foreach ($line in (Get-Content -Path $file)) {
        $lineNumber++

        foreach ($pattern in $forbiddenPatterns) {
            if ($line -match $pattern) {
                $violations += @{
                    File = $file
                    Line = $lineNumber
                    Content = $line.Trim()
                    Violation = "Domain layer cannot depend on infrastructure/framework code"
                }
            }
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Host "`n❌ HEXAGONAL ARCHITECTURE VIOLATION: Domain Layer Isolation" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "`nDomain layer must NOT depend on infrastructure or frameworks!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor Red

    foreach ($v in $violations) {
        Write-Host "`n  File: $($v.File):$($v.Line)" -ForegroundColor Cyan
        Write-Host "  Code: $($v.Content)" -ForegroundColor White
        Write-Host "  Issue: $($v.Violation)" -ForegroundColor Red
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Domain models should be plain Kotlin data classes" -ForegroundColor White
    Write-Host "  2. No @Entity, @Table, @Id annotations in domain layer" -ForegroundColor White
    Write-Host "  3. No Jakarta, Quarkus, or Hibernate imports" -ForegroundColor White
    Write-Host "  4. Move JPA entities to infrastructure/adapter/output/persistence/" -ForegroundColor White
    Write-Host "  5. Define repository ports in application/port/output/" -ForegroundColor White
    Write-Host "`n  See: docs/adr/ADR-001-modular-cqrs.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red

    exit 1
}

Write-Host "✓ Domain layer isolation verified" -ForegroundColor Green
exit 0
