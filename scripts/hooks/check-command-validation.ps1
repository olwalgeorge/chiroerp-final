#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforces CQRS - Commands must have validation

.DESCRIPTION
    Validates that command classes:
    - Use Jakarta Bean Validation annotations (@NotNull, @NotBlank, @Valid, etc.)
    - Are immutable data classes
    - Follow naming convention (ends with 'Command')
    - Include tenant context

.NOTES
    ADR References: ADR-001 (Pragmatic CQRS Implementation)
    Validation at boundary: Commands validated before reaching use cases
#>

param([Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)][string[]]$Files)

$ErrorActionPreference = "Stop"
$violations = @()

$validationAnnotations = @(
    '@field:NotNull',
    '@field:NotBlank',
    '@field:NotEmpty',
    '@field:Valid',
    '@field:Size',
    '@field:Min',
    '@field:Max',
    '@field:Pattern',
    '@field:Email',
    '@field:Positive',
    '@field:PositiveOrZero'
)

foreach ($file in $Files) {
    if (-not (Test-Path $file)) { continue }

    $content = Get-Content -Path $file -Raw

    # Extract class name
    if ($content -match 'data class\s+(\w+)') {
        $className = $matches[1]

        # Check naming convention
        if ($className -notmatch 'Command$') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Command class must end with 'Command' suffix"
            }
        }

        # Check if it's a data class (immutability)
        if ($content -notmatch 'data class') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Commands must be immutable data classes"
            }
        }

        # Check for var properties (should be val)
        if ($content -match 'var\s+\w+\s*:') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Command properties must be immutable (val, not var)"
            }
        }

        # Count properties and validation annotations
        $propertyCount = ([regex]::Matches($content, 'val\s+\w+\s*:')).Count
        $annotationCount = 0

        foreach ($annotation in $validationAnnotations) {
            $annotationCount += ([regex]::Matches($content, [regex]::Escape($annotation))).Count
        }

        # Warn if command has properties but no validation
        if ($propertyCount -gt 0 -and $annotationCount -eq 0) {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Command has $propertyCount properties but no validation annotations"
            }
        }

        # Check for Jakarta import
        if ($annotationCount -gt 0 -and $content -notmatch 'import jakarta\.validation\.constraints\.') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Missing Jakarta validation imports"
            }
        }

        # Check for tenant context (should have tenantId or use AuthorizationContext)
        if ($content -notmatch 'tenantId' -and $content -notmatch 'AuthorizationContext') {
            $violations += @{
                File = $file
                ClassName = $className
                Issue = "Command should include tenant context (tenantId or via AuthorizationContext)"
            }
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Host "`n❌ CQRS VIOLATION: Command Validation Requirements" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "`nCommands must have validation annotations!" -ForegroundColor Yellow
    Write-Host "`nViolations found:" -ForegroundColor Red

    foreach ($v in $violations) {
        Write-Host "`n  File: $($v.File)" -ForegroundColor Cyan
        Write-Host "  Class: $($v.ClassName)" -ForegroundColor White
        Write-Host "  Issue: $($v.Issue)" -ForegroundColor Red
    }

    Write-Host "`n📚 How to Fix:" -ForegroundColor Yellow
    Write-Host "  1. Add Jakarta Bean Validation annotations:" -ForegroundColor White
    Write-Host "     import jakarta.validation.constraints.*" -ForegroundColor Cyan
    Write-Host "`n     data class CreateJournalEntryCommand(" -ForegroundColor Cyan
    Write-Host "         @field:NotBlank val companyCode: String," -ForegroundColor Cyan
    Write-Host "         @field:NotNull val entryType: JournalEntryType," -ForegroundColor Cyan
    Write-Host "         @field:Valid val lines: List<JournalEntryLineCommand>" -ForegroundColor Cyan
    Write-Host "     )" -ForegroundColor Cyan
    Write-Host "`n  2. Commands are immutable - use 'data class' with 'val'" -ForegroundColor White
    Write-Host "`n  3. Include tenant context:" -ForegroundColor White
    Write-Host "     @field:NotNull val tenantId: TenantId" -ForegroundColor Cyan
    Write-Host "`n  4. Validate at REST boundary:" -ForegroundColor White
    Write-Host "     @POST fun create(@Valid request: CreateJournalEntryRequest)" -ForegroundColor Cyan
    Write-Host "`n  See: docs/adr/ADR-001-modular-cqrs.md" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red

    exit 1
}

Write-Host "✓ Command validation annotations verified" -ForegroundColor Green
exit 0
