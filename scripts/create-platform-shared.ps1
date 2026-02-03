#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Creates platform-shared foundation modules with interface definitions.

.DESCRIPTION
    Creates 4 platform-shared modules that define abstractions for:
    - common-messaging: Event publishing/consuming interfaces
    - config-model: Configuration engine domain model (PricingRule, PostingRule, etc.)
    - org-model: Organizational hierarchy value objects
    - workflow-model: Workflow definitions (approval routes, escalations)

.NOTES
    Author: ChiroERP Platform Team
    Date: February 3, 2026
    Version: 1.0.0
#>

param(
    [switch]$Force = $false
)

$ErrorActionPreference = "Stop"

Write-Host "`n=== Creating Platform-Shared Foundation Modules ===" -ForegroundColor Cyan
Write-Host "This creates 4 interface modules that domains will depend on`n" -ForegroundColor Yellow

$rootDir = Split-Path -Parent $PSScriptRoot
$platformSharedDir = Join-Path $rootDir "platform-shared"

# Module definitions
$modules = @(
    @{
        Name = "common-messaging"
        Description = "Event publishing/consuming interfaces (Kafka abstractions)"
        Package = "com.chiroerp.shared.messaging"
    },
    @{
        Name = "config-model"
        Description = "Configuration engine domain model (PricingRule, PostingRule, TaxRule)"
        Package = "com.chiroerp.shared.config"
    },
    @{
        Name = "org-model"
        Description = "Organizational hierarchy value objects (OrgUnit, AuthorizationContext)"
        Package = "com.chiroerp.shared.org"
    },
    @{
        Name = "workflow-model"
        Description = "Workflow definitions (WorkflowDefinition, WorkflowStep, ApprovalRoute)"
        Package = "com.chiroerp.shared.workflow"
    }
)

function New-PlatformSharedModule {
    param(
        [string]$Name,
        [string]$Description,
        [string]$Package
    )

    Write-Host "`nCreating module: $Name" -ForegroundColor Green
    Write-Host "  Description: $Description" -ForegroundColor Gray

    $moduleDir = Join-Path $platformSharedDir $Name
    $srcDir = Join-Path $moduleDir "src/main/kotlin"
    $testDir = Join-Path $moduleDir "src/test/kotlin"
    $packagePath = $Package -replace '\.', '/'
    $packageDir = Join-Path $srcDir $packagePath

    # Check if module already exists
    if (Test-Path $moduleDir) {
        if ($Force) {
            Write-Host "  ⚠️  Module exists, recreating (--Force enabled)" -ForegroundColor Yellow
            Remove-Item -Path $moduleDir -Recurse -Force
        } else {
            Write-Host "  ⚠️  Module already exists, skipping (use --Force to recreate)" -ForegroundColor Yellow
            return
        }
    }

    # Create directory structure
    New-Item -ItemType Directory -Path $packageDir -Force | Out-Null
    New-Item -ItemType Directory -Path (Join-Path $testDir $packagePath) -Force | Out-Null

    # Create build.gradle.kts
    $buildGradle = @"
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    // Only depend on common-types (shared value objects)
    implementation(project(":platform-shared:common-types"))
    
    // No external dependencies - pure interfaces and value objects
    // Implementations will be in domain modules (hardcoded) or platform-infrastructure (config-driven)
}

tasks.test {
    useJUnitPlatform()
}
"@
    Set-Content -Path (Join-Path $moduleDir "build.gradle.kts") -Value $buildGradle

    # Create README.md
    $readme = @"
# $Name

$Description

## Purpose

This module defines **interfaces and value objects only** - no implementations.

Domain modules (finance, sales, etc.) depend on these interfaces. Implementations can be swapped:
- **Phase 0**: Hardcoded implementations in each domain module
- **Phase 1**: Drools-based configuration engine in platform-infrastructure
- **Phase 2**: AI-powered rule suggestions and validation

## Architecture Pattern

``````kotlin
// This module (platform-shared/$Name)
interface PostingRulesEngine {
    fun determineAccounts(context: PostingContext): AccountMapping
}

// Domain module (finance-domain) - depends on interface
class GLPostingService(
    private val postingRules: PostingRulesEngine  // Injected via CDI
) {
    fun post(transaction: Transaction) {
        val accounts = postingRules.determineAccounts(context)
        // ...
    }
}

// Phase 0: Hardcoded implementation in finance-domain
@ApplicationScoped
class HardcodedPostingRules : PostingRulesEngine {
    override fun determineAccounts(context: PostingContext): AccountMapping {
        // Hardcoded rules for Kenya MVP
    }
}

// Phase 1: Config-driven implementation in platform-infrastructure
@ApplicationScoped
class DroolsPostingRules : PostingRulesEngine {
    override fun determineAccounts(context: PostingContext): AccountMapping {
        // Load rules from config database
    }
}
``````

**Zero domain code changes** when swapping implementations!

## Dependencies

- `platform-shared/common-types` - Shared value objects (Money, UUID extensions, etc.)
- No other dependencies

## Usage

Add to your domain module's \`build.gradle.kts\`:

``````kotlin
dependencies {
    implementation(project(":platform-shared:$Name"))
}
``````

Then inject interfaces via CDI:

``````kotlin
@ApplicationScoped
class YourService(
    private val someEngine: SomeEngine  // From this module
) {
    // Use the interface
}
``````

## See Also

- [ADR-044: Configuration Rules Framework](../../docs/adr/ADR-044-configuration-rules-framework.md)
- [ADR-045: Enterprise Organizational Model](../../docs/adr/ADR-045-enterprise-organizational-model.md)
- [ADR-046: Workflow & Approval Engine](../../docs/adr/ADR-046-workflow-approval-engine.md)
"@
    Set-Content -Path (Join-Path $moduleDir "README.md") -Value $readme

    Write-Host "  ✅ Module structure created" -ForegroundColor Green
    Write-Host "     📁 $moduleDir" -ForegroundColor Gray
}

# Create all modules
foreach ($module in $modules) {
    New-PlatformSharedModule -Name $module.Name -Description $module.Description -Package $module.Package
}

# Update settings.gradle.kts
Write-Host "`n📝 Updating settings.gradle.kts..." -ForegroundColor Cyan
$settingsFile = Join-Path $rootDir "settings.gradle.kts"
$settingsContent = Get-Content $settingsFile -Raw

# Check if platform-shared modules are already included
$modulesToAdd = @()
foreach ($module in $modules) {
    $includeLine = "include(`"platform-shared:$($module.Name)`")"
    if ($settingsContent -notmatch [regex]::Escape($includeLine)) {
        $modulesToAdd += $includeLine
    }
}

if ($modulesToAdd.Count -gt 0) {
    # Find the platform-shared section and add modules
    $platformSharedSection = "// Platform-shared modules (cross-cutting concerns)"
    if ($settingsContent -match [regex]::Escape($platformSharedSection)) {
        # Add after existing platform-shared modules
        $insertPoint = $settingsContent.IndexOf("include(`"platform-shared:common-types`")") + "include(`"platform-shared:common-types`")".Length
        $newModules = "`n" + ($modulesToAdd -join "`n")
        $settingsContent = $settingsContent.Insert($insertPoint, $newModules)
    } else {
        # Add new section
        $newSection = @"


// Platform-shared modules (cross-cutting concerns)
include("platform-shared:common-types")
$($modulesToAdd -join "`n")
"@
        $settingsContent += $newSection
    }
    
    Set-Content -Path $settingsFile -Value $settingsContent -NoNewline
    Write-Host "  ✅ settings.gradle.kts updated" -ForegroundColor Green
} else {
    Write-Host "  ℹ️  All modules already in settings.gradle.kts" -ForegroundColor Gray
}

# Test build
Write-Host "`n🔨 Testing Gradle build..." -ForegroundColor Cyan
Push-Location $rootDir
try {
    $buildOutput = & .\gradlew :platform-shared:common-messaging:build :platform-shared:config-model:build :platform-shared:org-model:build :platform-shared:workflow-model:build --console=plain 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ All platform-shared modules build successfully" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Build had warnings/errors (this is expected if interfaces not yet created)" -ForegroundColor Yellow
        Write-Host $buildOutput -ForegroundColor Gray
    }
} catch {
    Write-Host "  ⚠️  Build error: $_" -ForegroundColor Yellow
} finally {
    Pop-Location
}

Write-Host "`n✅ Platform-Shared Foundation Created!" -ForegroundColor Green
Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "  1. Create interface definitions in each module (see PHASE-0-FOUNDATION.md Day 7.5)" -ForegroundColor White
Write-Host "  2. Update scaffold-module.ps1 to add platform-shared dependencies" -ForegroundColor White
Write-Host "  3. Create finance-domain module using platform interfaces" -ForegroundColor White
Write-Host "  4. Implement hardcoded platform services in finance-domain" -ForegroundColor White
Write-Host "`nModules created:" -ForegroundColor Cyan
foreach ($module in $modules) {
    Write-Host "  📦 platform-shared/$($module.Name)" -ForegroundColor White
}
Write-Host ""
