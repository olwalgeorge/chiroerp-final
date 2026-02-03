#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Tracks ChiroERP implementation progress by analyzing codebase and updating GitHub.
.DESCRIPTION
    This script provides interactive project tracking by:
    1. Scanning modules to determine implementation status
    2. Checking which ADRs have corresponding code
    3. Calculating completion percentages per domain
    4. Optionally updating GitHub Issues/Project boards
    5. Generating progress reports (JSON, Markdown, HTML)
.PARAMETER UpdateGitHub
    Push progress updates to GitHub Issues/Projects (requires GH_TOKEN)
.PARAMETER OutputFormat
    Report format: json, markdown, html, or console (default: console)
.PARAMETER OutputPath
    Output file path (default: progress-report.[format])
.EXAMPLE
    .\scripts\track-progress.ps1
    # Display progress in console
.EXAMPLE
    .\scripts\track-progress.ps1 -OutputFormat markdown -OutputPath docs/PROGRESS.md
    # Generate markdown progress report
.EXAMPLE
    .\scripts\track-progress.ps1 -UpdateGitHub
    # Scan code and update GitHub Project board
.NOTES
    Integrates with GitHub Actions (.github/workflows/progress-tracker.yml)
    Requires: git, GitHub CLI (optional for -UpdateGitHub)
#>
[CmdletBinding()]
param(
    [switch]$UpdateGitHub,
    [ValidateSet('json', 'markdown', 'html', 'console')]
    [string]$OutputFormat = 'console',
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

# =============================================================================
# CONFIGURATION
# =============================================================================

$script:ProjectRoot = Split-Path -Parent $PSScriptRoot
$script:ADRPath = Join-Path $ProjectRoot "docs/adr"
$script:ModulesPath = $ProjectRoot
$script:ArchDocsPath = Join-Path $ProjectRoot "docs/architecture"

# Progress tracking data
$script:ProgressData = @{
    timestamp = Get-Date -Format "o"
    summary = @{
        totalADRs = 0
        implementedADRs = 0
        totalModules = 0
        implementedModules = 0
        totalDomains = 12
        completionPercentage = 0
    }
    domains = @()
    modules = @()
    adrs = @()
}

# =============================================================================
# HELPER FUNCTIONS
# =============================================================================

function Write-Header {
    param($Message)
    Write-Host "`n╔$('═' * ($Message.Length + 2))╗" -ForegroundColor Cyan
    Write-Host "║ $Message ║" -ForegroundColor Cyan
    Write-Host "╚$('═' * ($Message.Length + 2))╝`n" -ForegroundColor Cyan
}

function Write-Progress-Bar {
    param(
        [int]$Current,
        [int]$Total,
        [string]$Label = "Progress"
    )
    
    $percentage = if ($Total -gt 0) { [math]::Round(($Current / $Total) * 100, 1) } else { 0 }
    $barLength = 40
    $filled = [math]::Floor(($percentage / 100) * $barLength)
    $empty = $barLength - $filled
    
    $bar = "[$('█' * $filled)$('░' * $empty)]"
    
    $color = if ($percentage -ge 75) { "Green" }
             elseif ($percentage -ge 50) { "Yellow" }
             elseif ($percentage -ge 25) { "DarkYellow" }
             else { "Red" }
    
    Write-Host "$Label`: " -NoNewline
    Write-Host $bar -ForegroundColor $color -NoNewline
    Write-Host " $percentage% ($Current/$Total)"
}

# =============================================================================
# SCANNING FUNCTIONS
# =============================================================================

function Get-ADRStatus {
    <#
    .SYNOPSIS
    Analyzes ADR files and determines implementation status
    #>
    
    Write-Host "📋 Scanning ADR files..." -ForegroundColor Cyan
    
    $adrFiles = Get-ChildItem -Path $ADRPath -Filter "ADR-*.md" | Sort-Object Name
    $script:ProgressData.summary.totalADRs = $adrFiles.Count
    
    foreach ($adr in $adrFiles) {
        $content = Get-Content $adr.FullName -Raw
        
        # Extract ADR number and title
        if ($adr.Name -match '^ADR-(\d+)-(.+)\.md$') {
            $adrNumber = $Matches[1]
            $adrSlug = $Matches[2]
        }
        
        # Determine status from content
        $status = "Not Started"
        if ($content -match '\*\*Status\*\*:\s*Implemented') {
            $status = "Implemented"
            $script:ProgressData.summary.implementedADRs++
        }
        elseif ($content -match '\*\*Status\*\*:\s*In Progress') {
            $status = "In Progress"
        }
        elseif ($content -match '\*\*Status\*\*:\s*Draft') {
            $status = "Draft"
        }
        
        # Extract tier
        $tier = "Unknown"
        if ($content -match '\*\*Tier\*\*:\s*(\w+)') {
            $tier = $Matches[1]
        }
        
        $script:ProgressData.adrs += @{
            number = $adrNumber
            slug = $adrSlug
            title = "$adrSlug"
            status = $status
            tier = $tier
            file = $adr.Name
        }
    }
    
    Write-Host "  ✓ Found $($adrFiles.Count) ADRs, $($script:ProgressData.summary.implementedADRs) implemented" -ForegroundColor Green
}

function Get-ModuleStatus {
    <#
    .SYNOPSIS
    Scans workspace for modules and checks implementation status
    #>
    
    Write-Host "`n📦 Scanning modules..." -ForegroundColor Cyan
    
    # Find all build.gradle.kts files (indicates a module)
    $buildFiles = Get-ChildItem -Path $ModulesPath -Filter "build.gradle.kts" -Recurse -Depth 5 |
        Where-Object { $_.FullName -notmatch 'build-logic|buildSrc|\.gradle' }
    
    $script:ProgressData.summary.totalModules = $buildFiles.Count
    
    foreach ($buildFile in $buildFiles) {
        $modulePath = $buildFile.DirectoryName
        $relativePath = $modulePath.Replace($ProjectRoot, "").TrimStart('\', '/')
        
        # Determine module name from path
        $moduleName = $relativePath -replace '[/\\]', ':'
        if (-not $moduleName.StartsWith(':')) {
            $moduleName = ":$moduleName"
        }
        
        # Check if module has source files
        $srcPath = Join-Path $modulePath "src/main"
        $hasSource = Test-Path $srcPath
        
        # Count source files
        $sourceFileCount = 0
        if ($hasSource) {
            $sourceFiles = Get-ChildItem -Path $srcPath -Include "*.kt","*.java" -Recurse -ErrorAction SilentlyContinue
            $sourceFileCount = $sourceFiles.Count
        }
        
        # Determine status
        $status = if ($sourceFileCount -eq 0) { "Not Started" }
                  elseif ($sourceFileCount -lt 5) { "Scaffolded" }
                  else { "Implemented" }
        
        if ($status -eq "Implemented") {
            $script:ProgressData.summary.implementedModules++
        }
        
        # Determine domain from path
        $domain = "platform-shared"
        if ($relativePath -match 'bounded-contexts[/\\]([^/\\]+)') {
            $domain = $Matches[1]
        }
        
        $script:ProgressData.modules += @{
            name = $moduleName
            path = $relativePath
            domain = $domain
            status = $status
            sourceFiles = $sourceFileCount
            hasTests = (Test-Path (Join-Path $modulePath "src/test"))
        }
    }
    
    Write-Host "  ✓ Found $($buildFiles.Count) modules, $($script:ProgressData.summary.implementedModules) implemented" -ForegroundColor Green
}

function Get-DomainProgress {
    <#
    .SYNOPSIS
    Calculates progress per domain based on modules
    #>
    
    Write-Host "`n🏢 Calculating domain progress..." -ForegroundColor Cyan
    
    $domains = @(
        @{ name = "Finance"; modules = @("finance-gl", "finance-ar", "finance-ap", "finance-fa", "finance-controlling") }
        @{ name = "Procurement"; modules = @("procurement-pr", "procurement-po") }
        @{ name = "Inventory"; modules = @("inventory-core", "inventory-wms") }
        @{ name = "Sales"; modules = @("sales-order", "sales-pricing", "sales-distribution") }
        @{ name = "Manufacturing"; modules = @("manufacturing-planning", "manufacturing-execution") }
        @{ name = "Warehouse"; modules = @("warehouse-execution") }
        @{ name = "Quality"; modules = @("quality-management") }
        @{ name = "Maintenance"; modules = @("plant-maintenance") }
        @{ name = "HR"; modules = @("hr-integration") }
        @{ name = "MDM"; modules = @("mdm-hub") }
        @{ name = "CRM"; modules = @("crm-customer") }
        @{ name = "Platform"; modules = @("platform-shared") }
    )
    
    foreach ($domain in $domains) {
        $domainModules = $script:ProgressData.modules | Where-Object { 
            $_.path -match $domain.name -or ($_.domain -eq $domain.name.ToLower())
        }
        
        $totalModules = $domain.modules.Count
        $implementedCount = ($domainModules | Where-Object { $_.status -eq "Implemented" }).Count
        $percentage = if ($totalModules -gt 0) { [math]::Round(($implementedCount / $totalModules) * 100, 1) } else { 0 }
        
        $script:ProgressData.domains += @{
            name = $domain.name
            totalModules = $totalModules
            implementedModules = $implementedCount
            completionPercentage = $percentage
        }
    }
    
    Write-Host "  ✓ Calculated progress for $($domains.Count) domains" -ForegroundColor Green
}

# =============================================================================
# OUTPUT FUNCTIONS
# =============================================================================

function Write-ConsoleReport {
    Write-Header "ChiroERP Implementation Progress"
    
    Write-Host "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" -ForegroundColor Gray
    
    # Overall summary
    Write-Host "━━━ OVERALL PROGRESS ━━━`n" -ForegroundColor Yellow
    
    $overallProgress = if ($script:ProgressData.summary.totalModules -gt 0) {
        [math]::Round(($script:ProgressData.summary.implementedModules / $script:ProgressData.summary.totalModules) * 100, 1)
    } else { 0 }
    
    $script:ProgressData.summary.completionPercentage = $overallProgress
    
    Write-Progress-Bar -Current $script:ProgressData.summary.implementedModules `
                       -Total $script:ProgressData.summary.totalModules `
                       -Label "Modules"
    
    Write-Progress-Bar -Current $script:ProgressData.summary.implementedADRs `
                       -Total $script:ProgressData.summary.totalADRs `
                       -Label "ADRs   "
    
    # Domain breakdown
    Write-Host "`n━━━ DOMAIN PROGRESS ━━━`n" -ForegroundColor Yellow
    
    foreach ($domain in ($script:ProgressData.domains | Sort-Object completionPercentage -Descending)) {
        Write-Progress-Bar -Current $domain.implementedModules `
                           -Total $domain.totalModules `
                           -Label ($domain.name.PadRight(15))
    }
    
    # Top priorities (domains <50% complete)
    $priorities = $script:ProgressData.domains | 
        Where-Object { $_.completionPercentage -lt 50 } | 
        Sort-Object completionPercentage
    
    if ($priorities.Count -gt 0) {
        Write-Host "`n━━━ TOP PRIORITIES ━━━`n" -ForegroundColor Red
        foreach ($domain in $priorities) {
            Write-Host "  • $($domain.name): $($domain.implementedModules)/$($domain.totalModules) modules" -ForegroundColor Yellow
        }
    }
    
    # Recently implemented
    $implemented = $script:ProgressData.modules | Where-Object { $_.status -eq "Implemented" }
    if ($implemented.Count -gt 0) {
        Write-Host "`n━━━ IMPLEMENTED MODULES ━━━`n" -ForegroundColor Green
        foreach ($module in ($implemented | Select-Object -First 10)) {
            Write-Host "  ✓ $($module.name) [$($module.sourceFiles) files]" -ForegroundColor Green
        }
    }
    
    Write-Host ""
}

function Export-MarkdownReport {
    param([string]$Path)
    
    $md = @"
# ChiroERP Implementation Progress

**Generated:** $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

## 📊 Overall Status

| Metric | Progress |
|--------|----------|
| **Modules** | $($script:ProgressData.summary.implementedModules) / $($script:ProgressData.summary.totalModules) ($($script:ProgressData.summary.completionPercentage)%) |
| **ADRs** | $($script:ProgressData.summary.implementedADRs) / $($script:ProgressData.summary.totalADRs) |
| **Domains** | $($script:ProgressData.summary.totalDomains) |

## 🏢 Domain Progress

| Domain | Modules | Progress | Status |
|--------|---------|----------|--------|
"@
    
    foreach ($domain in ($script:ProgressData.domains | Sort-Object completionPercentage -Descending)) {
        $bar = "█" * [math]::Floor($domain.completionPercentage / 5)
        $empty = "░" * (20 - $bar.Length)
        $status = if ($domain.completionPercentage -ge 75) { "✅" }
                  elseif ($domain.completionPercentage -ge 50) { "🟡" }
                  elseif ($domain.completionPercentage -ge 25) { "🟠" }
                  else { "🔴" }
        
        $md += "| $($domain.name) | $($domain.implementedModules)/$($domain.totalModules) | ``$bar$empty`` $($domain.completionPercentage)% | $status |`n"
    }
    
    $md += @"

## 📦 Module Status

| Module | Domain | Status | Files |
|--------|--------|--------|-------|
"@
    
    foreach ($module in ($script:ProgressData.modules | Sort-Object name)) {
        $statusIcon = switch ($module.status) {
            "Implemented" { "✅" }
            "Scaffolded" { "🟡" }
            default { "⭕" }
        }
        $md += "| ``$($module.name)`` | $($module.domain) | $statusIcon $($module.status) | $($module.sourceFiles) |`n"
    }
    
    $md += @"

---
*Report generated by `scripts/track-progress.ps1`*
"@
    
    $md | Out-File -FilePath $Path -Encoding UTF8
    Write-Host "✓ Markdown report saved: $Path" -ForegroundColor Green
}

function Export-JsonReport {
    param([string]$Path)
    
    $script:ProgressData | ConvertTo-Json -Depth 10 | Out-File -FilePath $Path -Encoding UTF8
    Write-Host "✓ JSON report saved: $Path" -ForegroundColor Green
}

# =============================================================================
# GITHUB INTEGRATION
# =============================================================================

function Update-GitHubProject {
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
        Write-Host "⚠️  GitHub CLI not installed. Skipping GitHub updates." -ForegroundColor Yellow
        Write-Host "   Install: https://cli.github.com/" -ForegroundColor Gray
        return
    }
    
    Write-Host "`n🐙 Updating GitHub Project..." -ForegroundColor Cyan
    
    # TODO: Implement GitHub API calls to update Project board
    # - Create/update issues for each module
    # - Set issue status based on implementation progress
    # - Update project fields (domain, completion %, etc.)
    
    Write-Host "  ⚠️  GitHub integration not yet implemented" -ForegroundColor Yellow
    Write-Host "     This feature will:" -ForegroundColor Gray
    Write-Host "     • Create issues for unimplemented modules" -ForegroundColor Gray
    Write-Host "     • Update issue status based on code analysis" -ForegroundColor Gray
    Write-Host "     • Track progress in GitHub Project board" -ForegroundColor Gray
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

try {
    Write-Header "ChiroERP Progress Tracker"
    
    # Scan codebase
    Get-ADRStatus
    Get-ModuleStatus
    Get-DomainProgress
    
    # Generate output
    switch ($OutputFormat) {
        'console' {
            Write-ConsoleReport
        }
        'markdown' {
            $path = if ($OutputPath) { $OutputPath } else { "progress-report.md" }
            Export-MarkdownReport -Path $path
        }
        'json' {
            $path = if ($OutputPath) { $OutputPath } else { "progress-report.json" }
            Export-JsonReport -Path $path
        }
        'html' {
            Write-Host "⚠️  HTML output not yet implemented" -ForegroundColor Yellow
        }
    }
    
    # Update GitHub if requested
    if ($UpdateGitHub) {
        Update-GitHubProject
    }
    
    exit 0
}
catch {
    Write-Host "`n❌ Error: $_" -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor Gray
    exit 1
}
