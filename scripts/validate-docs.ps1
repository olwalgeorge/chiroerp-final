#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Validates documentation consistency across ADRs, architecture docs, and workspace structure.

.DESCRIPTION
    This script prevents documentation drift by checking:
    1. ADR count consistency (docs/adr/ vs WORKSPACE-STRUCTURE.md vs README.md)
    2. Module/domain counts match across all architecture documents
    3. All ADR references point to existing files
    4. Microservices port assignments are unique and in valid ranges
    5. Documentation dates are current (warn if >30 days old)

.PARAMETER Fix
    Automatically fix issues where possible (e.g., update counts, dates)

.EXAMPLE
    .\scripts\validate-docs.ps1
    # Run validation checks (read-only)

.EXAMPLE
    .\scripts\validate-docs.ps1 -Fix
    # Run validation and auto-fix issues

.NOTES
    Part of CI/CD pipeline (GitHub Actions .github/workflows/ci-docs.yml)
    Exit codes: 0 = success, 1 = validation errors found
#>

[CmdletBinding()]
param(
    [switch]$Fix
)

$ErrorActionPreference = "Stop"
$script:ValidationIssues = @()
$script:ValidationWarnings = @()
$script:FixesApplied = @()

# Color output helpers
function Write-Success { param($Message) Write-Host "✓ $Message" -ForegroundColor Green }
function Write-ValidationError { param($Message) Write-Host "✗ $Message" -ForegroundColor Red; $script:ValidationIssues += $Message }
function Write-ValidationWarning { param($Message) Write-Host "⚠ $Message" -ForegroundColor Yellow; $script:ValidationWarnings += $Message }
function Write-Info { param($Message) Write-Verbose $Message }

# Constants
$ROOT_PATH = Split-Path -Parent $PSScriptRoot
$docsDir = Join-Path $ROOT_PATH "docs"
$ADR_DIR = Join-Path $docsDir "adr"
$ARCH_DIR = Join-Path $docsDir "architecture"
$WORKSPACE_STRUCTURE = Join-Path $ARCH_DIR "WORKSPACE-STRUCTURE.md"
$ARCH_README = Join-Path $ARCH_DIR "README.md"
$CURRENT_DATE = Get-Date -Format "yyyy-MM-dd"

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  ChiroERP Documentation Validation (CI/CD)                 ║" -ForegroundColor Cyan
Write-Host "║  Date: $($CURRENT_DATE)                                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

#region Helper Functions

function Get-ADRFiles {
    if (-not (Test-Path $ADR_DIR)) {
        Write-ValidationError "ADR directory not found: $ADR_DIR"
        return @()
    }
    Get-ChildItem -Path $ADR_DIR -Filter "ADR-*.md" | Sort-Object Name
}

function Get-ADRCount {
    return (Get-ADRFiles).Count
}

function Get-HighestADRNumber {
    $adrFiles = Get-ADRFiles
    if ($adrFiles.Count -eq 0) { return 0 }
    
    $numbers = $adrFiles | ForEach-Object {
        if ($_.Name -match '^ADR-(\d+)') {
            [int]$matches[1]
        }
    }
    return ($numbers | Measure-Object -Maximum).Maximum
}

function Get-FileContent {
    param([string]$Path)
    if (Test-Path $Path) {
        return Get-Content -Path $Path -Raw
    }
    return $null
}

function Extract-ADRReferences {
    param([string]$Content)
    if (-not $Content) { return @() }
    
    # Match patterns: ADR-001, ADR-057, (ADR-042), [ADR-001], etc.
    $matches = [regex]::Matches($Content, 'ADR-(\d{3})')
    return $matches | ForEach-Object { $_.Value } | Select-Object -Unique | Sort-Object
}

function Extract-DocumentDate {
    param([string]$Content)
    if (-not $Content) { return $null }
    
    # Match patterns: "Last Updated: 2026-02-03", "**Last Updated**: 2026-02-03"
    if ($Content -match '(?:\*{0,2})?(?:Last Updated|Date)(?:\*{0,2})?\s*:\s*(\d{4}-\d{2}-\d{2})') {
        return $matches[1]
    }
    return $null
}

function Extract-ADRCount {
    param([string]$Content)
    if (-not $Content) { return $null }
    
    # Match patterns: "57+ ADRs", "57 ADRs", "ADR-001 through ADR-057"
    if ($Content -match '(\d+)\+?\s*ADRs?' -or $Content -match 'ADR-\d+\s+through\s+ADR-(\d+)') {
        return [int]$matches[1]
    }
    return $null
}

function Extract-ModuleCount {
    param([string]$Content)
    if (-not $Content) { return $null }
    
    # Match patterns: "92 modules", "92 Modules"
    if ($Content -match '(\d+)\s+[Mm]odules') {
        return [int]$matches[1]
    }
    return $null
}

function Extract-DomainCount {
    param([string]$Content)
    if (-not $Content) { return $null }
    
    # Match patterns: "12 domains", "12 Major Domains"
    if ($Content -match '(\d+)\s+(?:Major\s+)?[Dd]omains') {
        return [int]$matches[1]
    }
    return $null
}

function Extract-PortAssignments {
    param([string]$Content)
    if (-not $Content) { return @() }
    
    # Extract port numbers from tables (e.g., "| finance-gl | 8081 |")
    $matches = [regex]::Matches($Content, '\|\s*[^|]+\s*\|\s*(\d{4,5})\s*\|')
    return $matches | ForEach-Object { [int]$_.Groups[1].Value } | Sort-Object
}

#endregion

#region Validation Tests

Write-Host "[1/7] Checking ADR directory structure..." -ForegroundColor Cyan

$adrFiles = Get-ADRFiles
$adrCount = $adrFiles.Count
$highestADR = Get-HighestADRNumber

Write-Info "Found $adrCount ADR files in $ADR_DIR"
$highestADRFormatted = $highestADR.ToString("000")
Write-Info "Highest ADR number: ADR-$highestADRFormatted"

if ($adrCount -eq 0) {
    Write-ValidationError "No ADR files found in $ADR_DIR"
} else {
    Write-Success "Found $adrCount ADR files (ADR-001 to ADR-$highestADRFormatted)"
}

# Check for gaps in ADR numbering
$expectedNumbers = 1..$highestADR
$actualNumbers = $adrFiles | ForEach-Object {
    if ($_.Name -match '^ADR-(\d+)') { [int]$matches[1] }
}
$missingNumbers = $expectedNumbers | Where-Object { $_ -notin $actualNumbers }

if ($missingNumbers) {
    Write-ValidationWarning "Missing ADR numbers: $($missingNumbers -join ', ')"
} else {
    Write-Success "No gaps in ADR numbering (continuous 1-$highestADR)"
}

#region Test 2: WORKSPACE-STRUCTURE.md Validation

Write-Host "`n[2/7] Validating WORKSPACE-STRUCTURE.md..." -ForegroundColor Cyan

if (-not (Test-Path $WORKSPACE_STRUCTURE)) {
    Write-ValidationError "WORKSPACE-STRUCTURE.md not found at $WORKSPACE_STRUCTURE"
} else {
    $wsContent = Get-FileContent $WORKSPACE_STRUCTURE
    
    # Check ADR count
    $wsADRCount = Extract-ADRCount $wsContent
    if (-not $wsADRCount) {
        Write-ValidationError "WORKSPACE-STRUCTURE.md: Cannot extract ADR count"
    } elseif ($wsADRCount -ne $highestADR) {
        Write-ValidationError "WORKSPACE-STRUCTURE.md: ADR count mismatch (claims $wsADRCount, actual $highestADR)"
        
        if ($Fix) {
            $highestADRFormatted = $highestADR.ToString("000")
            $wsContent = $wsContent -replace '(\d+)\+?\s*ADRs?', "$highestADR+ ADRs"
            $wsContent = $wsContent -replace '(ADR-\d+\s+through\s+)ADR-\d+', "`${1}ADR-$highestADRFormatted"
            Set-Content -Path $WORKSPACE_STRUCTURE -Value $wsContent -NoNewline
            $script:FixesApplied += "Updated WORKSPACE-STRUCTURE.md ADR count: $wsADRCount → $highestADR"
            Write-Success "Fixed: Updated ADR count to $highestADR"
        }
    } else {
        Write-Success "ADR count correct ($wsADRCount)"
    }
    
    # Check date
    $wsDate = Extract-DocumentDate $wsContent
    if (-not $wsDate) {
        Write-ValidationWarning "WORKSPACE-STRUCTURE.md: Cannot extract last updated date"
    } else {
        $daysDiff = ((Get-Date) - (Get-Date $wsDate)).Days
        if ($daysDiff -gt 30) {
            Write-ValidationWarning "WORKSPACE-STRUCTURE.md: Last updated $daysDiff days ago ($wsDate)"
        } else {
            Write-Success "Last updated date: $wsDate ($daysDiff days ago)"
        }
        
        if ($Fix -and $daysDiff -gt 7) {
            $wsContent = $wsContent -replace 'Last Updated:\s*\d{4}-\d{2}-\d{2}', "Last Updated: $CURRENT_DATE"
            Set-Content -Path $WORKSPACE_STRUCTURE -Value $wsContent -NoNewline
            $script:FixesApplied += "Updated WORKSPACE-STRUCTURE.md date: $wsDate → $CURRENT_DATE"
            Write-Success "Fixed: Updated date to $CURRENT_DATE"
        }
    }
    
    # Check module/domain counts
    $wsModuleCount = Extract-ModuleCount $wsContent
    $wsDomainCount = Extract-DomainCount $wsContent
    
    if ($wsModuleCount) {
        Write-Success "Module count: $wsModuleCount"
    } else {
        Write-ValidationWarning "WORKSPACE-STRUCTURE.md: Cannot extract module count"
    }
    
    if ($wsDomainCount) {
        Write-Success "Domain count: $wsDomainCount"
    } else {
        Write-ValidationWarning "WORKSPACE-STRUCTURE.md: Cannot extract domain count"
    }
}

#endregion

#region Test 3: Architecture README.md Validation

Write-Host "`n[3/7] Validating Architecture README.md..." -ForegroundColor Cyan

if (-not (Test-Path $ARCH_README)) {
    Write-ValidationError "Architecture README.md not found at $ARCH_README"
} else {
    $readmeContent = Get-FileContent $ARCH_README
    
    # Check ADR count
    $readmeADRCount = Extract-ADRCount $readmeContent
    if (-not $readmeADRCount) {
        Write-ValidationWarning "Architecture README.md: Cannot extract ADR count"
    } elseif ($readmeADRCount -ne $highestADR) {
        Write-ValidationError "Architecture README.md: ADR count mismatch (claims $readmeADRCount, actual $highestADR)"
        
        if ($Fix) {
            $readmeContent = $readmeContent -replace '(\d+)\+?\s*ADRs?', "$highestADR ADRs"
            Set-Content -Path $ARCH_README -Value $readmeContent -NoNewline
            $script:FixesApplied += "Updated Architecture README.md ADR count: $readmeADRCount → $highestADR"
            Write-Success "Fixed: Updated ADR count to $highestADR"
        }
    } else {
        Write-Success "ADR count correct ($readmeADRCount)"
    }
    
    # Check module/domain counts match WORKSPACE-STRUCTURE.md
    $readmeModuleCount = Extract-ModuleCount $readmeContent
    $readmeDomainCount = Extract-DomainCount $readmeContent
    
    if ($readmeModuleCount -and $wsModuleCount -and $readmeModuleCount -ne $wsModuleCount) {
        Write-ValidationError "Module count mismatch: README=$readmeModuleCount, WORKSPACE-STRUCTURE=$wsModuleCount"
    } elseif ($readmeModuleCount) {
        Write-Success "Module count matches: $readmeModuleCount"
    }
    
    if ($readmeDomainCount -and $wsDomainCount -and $readmeDomainCount -ne $wsDomainCount) {
        Write-ValidationError "Domain count mismatch: README=$readmeDomainCount, WORKSPACE-STRUCTURE=$wsDomainCount"
    } elseif ($readmeDomainCount) {
        Write-Success "Domain count matches: $readmeDomainCount"
    }
}

#endregion

#region Test 4: ADR Reference Validation

Write-Host "`n[4/7] Validating ADR references..." -ForegroundColor Cyan

$allArchDocs = Get-ChildItem -Path $ARCH_DIR -Filter "*.md" -Recurse
$brokenReferences = @()

foreach ($doc in $allArchDocs) {
    $content = Get-FileContent $doc.FullName
    $references = Extract-ADRReferences $content
    
    foreach ($ref in $references) {
        if ($ref -match 'ADR-(\d+)') {
            $adrNum = [int]$matches[1]
            $adrNumFormatted = $adrNum.ToString("000")
            $expectedFile = "ADR-$adrNumFormatted-*.md"
            $adrFile = Get-ChildItem -Path $ADR_DIR -Filter $expectedFile -ErrorAction SilentlyContinue
            
            if (-not $adrFile) {
                $brokenReferences += @{
                    Document = $doc.Name
                    Reference = $ref
                    Path = $doc.FullName
                }
            }
        }
    }
}

if ($brokenReferences.Count -gt 0) {
    Write-ValidationError "Found $($brokenReferences.Count) broken ADR references:"
    foreach ($broken in $brokenReferences) {
        Write-Host "  - $($broken.Document): $($broken.Reference)" -ForegroundColor Red
    }
} else {
    Write-Success "All ADR references valid (checked $($allArchDocs.Count) documents)"
}

#endregion

#region Test 5: Port Assignment Validation

Write-Host "`n[5/7] Validating microservices port assignments..." -ForegroundColor Cyan

if (Test-Path $WORKSPACE_STRUCTURE) {
    $wsContent = Get-FileContent $WORKSPACE_STRUCTURE
    $ports = Extract-PortAssignments $wsContent
    
    if ($ports.Count -eq 0) {
        Write-ValidationWarning "No port assignments found in WORKSPACE-STRUCTURE.md"
    } else {
        Write-Info "Found $($ports.Count) port assignments"
        
        # Check for duplicates
        $duplicates = $ports | Group-Object | Where-Object { $_.Count -gt 1 }
        if ($duplicates) {
            Write-ValidationError "Duplicate port assignments found:"
            foreach ($dup in $duplicates) {
                Write-Host "  - Port $($dup.Name) assigned $($dup.Count) times" -ForegroundColor Red
            }
        } else {
            Write-Success "No duplicate port assignments"
        }
        
        # Check port ranges (8000-10999 for microservices)
        $invalidPorts = $ports | Where-Object { $_ -lt 8000 -or $_ -gt 10999 }
        if ($invalidPorts) {
            Write-ValidationWarning "Ports outside recommended range (8000-10999):"
            foreach ($port in $invalidPorts) {
                Write-Host "  - Port $port" -ForegroundColor Yellow
            }
        } else {
            Write-Success "All ports in valid range (8000-10999)"
        }
        
        # Check for gaps (potential missing services)
        $portGaps = @()
        for ($i = 0; $i -lt $ports.Count - 1; $i++) {
            if ($ports[$i+1] - $ports[$i] -gt 100) {
                $portGaps += "Gap between $($ports[$i]) and $($ports[$i+1])"
            }
        }
        if ($portGaps) {
            Write-Info "Large port gaps detected (may indicate missing services):"
            foreach ($gap in $portGaps) {
                Write-Host "  - $gap" -ForegroundColor Cyan
            }
        }
    }
}

#endregion

#region Test 6: Cross-Document Consistency

Write-Host "`n[6/7] Checking cross-document consistency..." -ForegroundColor Cyan

$consistencyChecks = @(
    @{
        Name = "ADR count"
        Values = @($highestADR, $wsADRCount, $readmeADRCount) | Where-Object { $_ }
    },
    @{
        Name = "Module count"
        Values = @($wsModuleCount, $readmeModuleCount) | Where-Object { $_ }
    },
    @{
        Name = "Domain count"
        Values = @($wsDomainCount, $readmeDomainCount) | Where-Object { $_ }
    }
)

foreach ($check in $consistencyChecks) {
    $unique = $check.Values | Select-Object -Unique
    if ($unique.Count -gt 1) {
        Write-ValidationError "$($check.Name) inconsistent across documents: $($check.Values -join ', ')"
    } else {
        Write-Success "$($check.Name) consistent: $($unique[0])"
    }
}

#endregion

#region Test 7: File Naming Conventions

Write-Host "`n[7/7] Validating ADR file naming conventions..." -ForegroundColor Cyan

$namingIssues = @()
foreach ($adrFile in $adrFiles) {
    # ADR-001-descriptive-title.md format
    if ($adrFile.Name -notmatch '^ADR-\d{3}-[a-z0-9-]+\.md$') {
        $namingIssues += $adrFile.Name
    }
}

if ($namingIssues.Count -gt 0) {
    Write-ValidationError "ADR files with invalid naming conventions:"
    foreach ($fileName in $namingIssues) {
        Write-Host "  - $fileName (expected: ADR-XXX-kebab-case.md)" -ForegroundColor Red
    }
} else {
    Write-Success "All ADR files follow naming convention (ADR-XXX-kebab-case.md)"
}

#endregion

#region Summary Report

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  VALIDATION SUMMARY                                         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Total ADRs: " -NoNewline
Write-Host "$adrCount" -ForegroundColor Green
Write-Host "Highest ADR: " -NoNewline
$highestADRDisplay = $highestADR.ToString("000")
Write-Host "ADR-$highestADRDisplay" -ForegroundColor Green
Write-Host "Documents Checked: " -NoNewline
Write-Host "$($allArchDocs.Count)" -ForegroundColor Green
Write-Host "Port Assignments: " -NoNewline
Write-Host "$($ports.Count)" -ForegroundColor Green

Write-Host "`n"

if ($script:ValidationIssues.Count -gt 0) {
    Write-Host "✗ ERRORS: $($script:ValidationIssues.Count)" -ForegroundColor Red
    foreach ($item in $script:ValidationIssues) {
        Write-Host "  - $item" -ForegroundColor Red
    }
    Write-Host ""
}

if ($script:ValidationWarnings.Count -gt 0) {
    Write-Host "⚠ WARNINGS: $($script:ValidationWarnings.Count)" -ForegroundColor Yellow
    foreach ($warning in $script:ValidationWarnings) {
        Write-Host "  - $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}

if ($script:FixesApplied.Count -gt 0) {
    Write-Host "✓ FIXES APPLIED: $($script:FixesApplied.Count)" -ForegroundColor Green
    foreach ($fix in $script:FixesApplied) {
        Write-Host "  - $fix" -ForegroundColor Green
    }
    Write-Host ""
}

if ($script:ValidationIssues.Count -eq 0 -and $script:ValidationWarnings.Count -eq 0) {
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║  ✓ ALL VALIDATION CHECKS PASSED                            ║" -ForegroundColor Green
    Write-Host "║  Documentation is consistent and up-to-date                ║" -ForegroundColor Green
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
    exit 0
} elseif ($script:ValidationIssues.Count -eq 0) {
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
    Write-Host "║  ⚠ VALIDATION PASSED WITH WARNINGS                         ║" -ForegroundColor Yellow
    Write-Host "║  Review warnings above                                     ║" -ForegroundColor Yellow
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "║  ✗ VALIDATION FAILED                                       ║" -ForegroundColor Red
    Write-Host "║  Fix errors above or run with -Fix flag                    ║" -ForegroundColor Red
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Red
    
    if (-not $Fix) {
        Write-Host "`nTip: Run with -Fix flag to automatically fix some issues:" -ForegroundColor Cyan
        Write-Host "  .\scripts\validate-docs.ps1 -Fix" -ForegroundColor Cyan
    }
    
    exit 1
}

#endregion
