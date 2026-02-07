# Scaffold Platform-Shared, Tenancy-Identity, and Finance Domains
# Generated: February 6, 2026
# Based on COMPLETE_STRUCTURE.txt and ADR-005 / ADR-006 governance
#
# ADR-006 GOVERNANCE COMPLIANCE:
# - platform-shared contains ONLY technical primitives and abstractions
# - Business value objects (Money, Address, PhoneNumber, Email) are DUPLICATED
#   in each bounded context's *-shared module with context-specific semantics
# - No domain models, business logic, or shared DTOs in platform-shared

param(
    [switch]$DryRun,
    [string]$BasePath = $PSScriptRoot
)

Write-Host "ChiroERP Platform + Tenancy + Finance Scaffolding Script" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "Base Path: $BasePath" -ForegroundColor Yellow
Write-Host "Dry Run: $($DryRun.ToString())" -ForegroundColor Yellow
Write-Host ""

function New-Directory {
    param([string]$Path)
    if ($DryRun) {
        Write-Host "[DRY RUN] Would create directory: $Path" -ForegroundColor Gray
    } else {
        if (!(Test-Path $Path)) {
            New-Item -ItemType Directory -Path $Path -Force | Out-Null
            Write-Host "Created directory: $Path" -ForegroundColor Green
        } else {
            Write-Host "Directory already exists: $Path" -ForegroundColor Blue
        }
    }
}

function New-File {
    param([string]$Path, [string]$Content = "")
    if ($DryRun) {
        Write-Host "[DRY RUN] Would create file: $Path" -ForegroundColor Gray
    } else {
        if (!(Test-Path $Path)) {
            $parentDir = Split-Path -Parent $Path
            if (!(Test-Path $parentDir)) {
                New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
            }
            New-Item -ItemType File -Path $Path -Value $Content -Force | Out-Null
            Write-Host "Created file: $Path" -ForegroundColor Green
        } else {
            Write-Host "File already exists: $Path" -ForegroundColor Blue
        }
    }
}

# =============================================================================
# PLATFORM-SHARED MODULES (ADR-006 Governance)
# =============================================================================
# These are TECHNICAL contracts only - NO business logic
# =============================================================================

$platformSharedModules = @(
    "common-types",
    "common-messaging",
    "common-security",
    "common-observability",
    "config-model",
    "org-model",
    "workflow-model"
)

Write-Host "`n=== PLATFORM-SHARED MODULES ===" -ForegroundColor Magenta
Write-Host "Governed by ADR-006: Technical contracts only, no business logic" -ForegroundColor DarkGray

foreach ($module in $platformSharedModules) {
    $modulePath = Join-Path $BasePath "platform-shared/$module"
    $moduleName = $module -replace "common-", "" -replace "-model", ""

    if ($module -eq "common-types") {
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
// ADR-006: Shared kernel primitives - NO business logic
// FORBIDDEN: Money, Address, PhoneNumber, Email, TaxId (must be duplicated per context)
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    // No dependencies - shared kernel primitives
}
"@
        $srcBase = Join-Path $modulePath "src/main/kotlin/com/chiroerp/shared/types"
        New-Directory -Path (Join-Path $srcBase "primitives")
        New-Directory -Path (Join-Path $srcBase "events")
        New-Directory -Path (Join-Path $srcBase "results")
        New-Directory -Path (Join-Path $srcBase "aggregate")
        New-Directory -Path (Join-Path $srcBase "cqrs")
        New-Directory -Path (Join-Path $modulePath "src/main/resources")
        New-Directory -Path (Join-Path $modulePath "src/test/kotlin/com/chiroerp/shared/types")
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
        continue
    }

    New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
// ADR-006: Technical abstractions only - NO business logic
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@

    $srcPath = Join-Path $modulePath "src/main/kotlin/com/chiroerp/shared/$moduleName"
    New-Directory -Path $srcPath
    New-Directory -Path (Join-Path $modulePath "src/main/resources")

    if ($module -eq "common-messaging") {
        New-Directory -Path (Join-Path $srcPath "kafka")
    } elseif ($module -eq "common-security") {
        New-Directory -Path (Join-Path $srcPath "tenant")
        New-Directory -Path (Join-Path $srcPath "authentication")
        New-Directory -Path (Join-Path $srcPath "authorization")
        New-Directory -Path (Join-Path $srcPath "compliance/soc2")
        New-Directory -Path (Join-Path $srcPath "compliance/iso27001")
        New-Directory -Path (Join-Path $srcPath "compliance/gdpr")
        New-Directory -Path (Join-Path $srcPath "context")
        New-Directory -Path (Join-Path $srcPath "filter")
    } elseif ($module -eq "common-observability") {
        New-Directory -Path (Join-Path $srcPath "correlation")
        New-Directory -Path (Join-Path $srcPath "tracing")
        New-Directory -Path (Join-Path $srcPath "metrics")
        New-Directory -Path (Join-Path $srcPath "logging")
        New-Directory -Path (Join-Path $srcPath "health")
    } elseif ($module -eq "config-model") {
        New-Directory -Path (Join-Path $srcPath "model")
        New-Directory -Path (Join-Path $srcPath "tenant")
        New-Directory -Path (Join-Path $srcPath "module")
        New-Directory -Path (Join-Path $srcPath "customization")
        New-Directory -Path (Join-Path $srcPath "localization")
        New-Directory -Path (Join-Path $srcPath "source")

        $localizationBase = Join-Path $modulePath "src/main/resources/localization/country-packs"
        $regionCountries = @{
            "africa" = @("kenya", "uganda", "south-africa", "nigeria", "ghana")
            "north-america" = @("united-states", "canada", "mexico")
            "europe" = @("united-kingdom", "germany", "france", "netherlands", "spain", "italy")
            "asia-pacific" = @("india", "australia", "japan", "singapore")
            "middle-east" = @("united-arab-emirates", "saudi-arabia")
            "latin-america" = @("brazil")
        }
        foreach ($region in $regionCountries.Keys) {
            $regionPath = Join-Path $localizationBase $region
            New-Directory -Path $regionPath
            foreach ($country in $regionCountries[$region]) {
                New-Directory -Path (Join-Path $regionPath $country)
            }
        }
    } elseif ($module -eq "org-model") {
        New-Directory -Path (Join-Path $srcPath "structure")
        New-Directory -Path (Join-Path $srcPath "entity")
        New-Directory -Path (Join-Path $srcPath "department")
        New-Directory -Path (Join-Path $srcPath "costcenter")
        New-Directory -Path (Join-Path $srcPath "location")
        New-Directory -Path (Join-Path $srcPath "assignment")
    } elseif ($module -eq "workflow-model") {
        New-Directory -Path (Join-Path $srcPath "definition")
        New-Directory -Path (Join-Path $srcPath "state")
        New-Directory -Path (Join-Path $srcPath "transition")
        New-Directory -Path (Join-Path $srcPath "instance")
        New-Directory -Path (Join-Path $srcPath "task")
        New-Directory -Path (Join-Path $srcPath "approval")
        New-Directory -Path (Join-Path $srcPath "saga")
        New-Directory -Path (Join-Path $srcPath "temporal")
    }

    $testPath = Join-Path $modulePath "src/test/kotlin/com/chiroerp/shared/$moduleName"
    New-Directory -Path $testPath
    New-Directory -Path (Join-Path $modulePath "src/test/resources")
}

# =============================================================================
# TENANCY-IDENTITY BOUNDED CONTEXT (ADR-005)
# =============================================================================

Write-Host "`n=== TENANCY-IDENTITY BOUNDED CONTEXT ===" -ForegroundColor Magenta

# Tenancy-shared module
$tenancySharedPath = Join-Path $BasePath "tenancy-identity/tenancy-shared"
New-File -Path (Join-Path $tenancySharedPath "build.gradle.kts") -Content @"
// tenancy-shared build configuration
// ADR-005/ADR-006: Context-specific value objects for Tenancy & Identity
// Contains: TenantId, TenantContext, Email, PhoneNumber, Address
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@

$tenancySharedSrc = Join-Path $tenancySharedPath "src/main/kotlin/com/chiroerp/tenancy/shared"
New-Directory -Path (Join-Path $tenancySharedSrc "valueobjects")
New-Directory -Path $tenancySharedSrc
New-Directory -Path (Join-Path $tenancySharedPath "src/main/resources")
New-Directory -Path (Join-Path $tenancySharedPath "src/test/kotlin/com/chiroerp/tenancy/shared")
New-Directory -Path (Join-Path $tenancySharedPath "src/test/resources")

$tenancyModules = @(
    @{ Name = "tenancy-core"; PackageRoot = "tenancy"; Subdomain = "core"; DbName = "tenancy_identity" },
    @{ Name = "identity-core"; PackageRoot = "identity"; Subdomain = "core"; DbName = "tenancy_identity" }
)

foreach ($moduleConfig in $tenancyModules) {
    $moduleName = $moduleConfig.Name
    $packageRoot = $moduleConfig.PackageRoot
    $subdomainName = $moduleConfig.Subdomain

    $modulePath = Join-Path $BasePath "tenancy-identity/$moduleName"

    New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $moduleName build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":tenancy-identity:tenancy-shared"))
}
"@

    $srcBase = Join-Path $modulePath "src/main/kotlin/com/chiroerp/$packageRoot/$subdomainName"
    New-Directory -Path (Join-Path $srcBase "domain/model")
    New-Directory -Path (Join-Path $srcBase "domain/event")
    New-Directory -Path (Join-Path $srcBase "domain/port")
    New-Directory -Path (Join-Path $srcBase "application/command")
    New-Directory -Path (Join-Path $srcBase "application/query")
    New-Directory -Path (Join-Path $srcBase "application/handler")
    New-Directory -Path (Join-Path $srcBase "application/service")
    New-Directory -Path (Join-Path $srcBase "infrastructure/persistence")
    New-Directory -Path (Join-Path $srcBase "infrastructure/messaging")
    New-Directory -Path (Join-Path $srcBase "infrastructure/web")

    if ($moduleName -eq "identity-core") {
        New-Directory -Path (Join-Path $srcBase "infrastructure/security")
        New-Directory -Path (Join-Path $srcBase "infrastructure/sso")
    }

    New-Directory -Path (Join-Path $modulePath "src/main/resources")
    New-Directory -Path (Join-Path $modulePath "src/main/resources/db/migration")

    $testBase = Join-Path $modulePath "src/test/kotlin/com/chiroerp/$packageRoot/$subdomainName"
    New-Directory -Path (Join-Path $testBase "domain")
    New-Directory -Path (Join-Path $testBase "application")
    New-Directory -Path (Join-Path $testBase "infrastructure")
    New-Directory -Path (Join-Path $modulePath "src/test/resources")
}

# =============================================================================
# FINANCE BOUNDED CONTEXT
# =============================================================================

$financeModules = @(
    @{ Name = "finance-gl"; Short = "gl"; AppSuffix = "GL" },
    @{ Name = "finance-ar"; Short = "ar"; AppSuffix = "AR" },
    @{ Name = "finance-ap"; Short = "ap"; AppSuffix = "AP" },
    @{ Name = "finance-assets"; Short = "assets"; AppSuffix = "Assets" },
    @{ Name = "finance-tax"; Short = "tax"; AppSuffix = "Tax" }
)

$financeLayers = @("domain", "application", "infrastructure")

Write-Host "`n=== FINANCE BOUNDED CONTEXT ===" -ForegroundColor Magenta

# Finance shared module
$financeSharedPath = Join-Path $BasePath "finance/finance-shared"
New-File -Path (Join-Path $financeSharedPath "build.gradle.kts") -Content @"
// finance-shared build configuration
// ADR-006: Context-specific value objects for Finance bounded context
// Contains: Money (finance precision), Currency, TaxId, AccountNumber
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@

$financeSharedSrc = Join-Path $financeSharedPath "src/main/kotlin/com/chiroerp/finance/shared"
New-Directory -Path (Join-Path $financeSharedSrc "identifiers")
New-Directory -Path (Join-Path $financeSharedSrc "valueobjects")
New-Directory -Path (Join-Path $financeSharedSrc "enums")
New-Directory -Path $financeSharedSrc
New-Directory -Path (Join-Path $financeSharedPath "src/main/resources")
New-Directory -Path (Join-Path $financeSharedPath "src/test/kotlin/com/chiroerp/finance/shared")
New-Directory -Path (Join-Path $financeSharedPath "src/test/resources")

foreach ($moduleConfig in $financeModules) {
    $moduleName = $moduleConfig.Name
    $shortName = $moduleConfig.Short
    $appSuffix = $moduleConfig.AppSuffix

    $modulePath = Join-Path $BasePath "finance/$moduleName"

    foreach ($layer in $financeLayers) {
        $layerModule = "${shortName}-${layer}"
        $layerPath = Join-Path $modulePath $layerModule
        $domainModule = "${shortName}-domain"
        $applicationModule = "${shortName}-application"

        if ($layer -eq "infrastructure") {
            New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:${moduleName}:${domainModule}"))
    implementation(project(":finance:${moduleName}:${applicationModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
"@
        } elseif ($layer -eq "application") {
            New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":finance:${moduleName}:${domainModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
"@
        } else {
            New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
"@
        }

        $srcBase = Join-Path $layerPath "src/main/kotlin/com/chiroerp/finance/$shortName/$layer"

        if ($layer -eq "domain") {
            New-Directory -Path (Join-Path $srcBase "model")
            New-Directory -Path (Join-Path $srcBase "events")
            New-Directory -Path (Join-Path $srcBase "exceptions")
            New-Directory -Path (Join-Path $srcBase "services")
        } elseif ($layer -eq "application") {
            New-Directory -Path (Join-Path $srcBase "command")
            New-Directory -Path (Join-Path $srcBase "query")
            New-Directory -Path (Join-Path $srcBase "handler")
            New-Directory -Path (Join-Path $srcBase "service")
        } else {
            New-Directory -Path (Join-Path $srcBase "adapter/input/rest/dto/request")
            New-Directory -Path (Join-Path $srcBase "adapter/input/rest/dto/response")
            New-Directory -Path (Join-Path $srcBase "adapter/input/event")
            New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/entity")
            New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/repository")
            New-Directory -Path (Join-Path $srcBase "adapter/output/reporting/document")
            New-Directory -Path (Join-Path $srcBase "adapter/output/audit")
            New-Directory -Path (Join-Path $srcBase "adapter/output/integration")
            New-Directory -Path (Join-Path $srcBase "adapter/output/messaging/kafka/schema")
            New-Directory -Path (Join-Path $srcBase "adapter/output/messaging/outbox")
            New-Directory -Path (Join-Path $srcBase "client")
            New-Directory -Path (Join-Path $srcBase "configuration")
        }

        New-Directory -Path (Join-Path $layerPath "src/main/resources")
        New-Directory -Path (Join-Path $layerPath "src/test/kotlin/com/chiroerp/finance/$shortName/$layer")
        New-Directory -Path (Join-Path $layerPath "src/test/resources")

        if ($layer -eq "infrastructure") {
            New-Directory -Path (Join-Path $layerPath "src/main/resources/db/migration")

            New-File -Path (Join-Path $layerPath "src/main/resources/application.yml") -Content @"
quarkus:
  application:
    name: $layerModule
  profile: dev

quarkus.http:
  port: 8080
"@

            New-File -Path (Join-Path $layerPath "src/main/resources/application-dev.yml") -Content @"
quarkus:
  datasource:
    db-kind: postgresql
    username: chiroerp
    password: chiroerp
    jdbc:
      url: jdbc:postgresql://localhost:5432/chiroerp
  hibernate-orm:
    database:
      generation: validate
"@

            New-File -Path (Join-Path $layerPath "src/main/resources/application-prod.yml") -Content @"
quarkus:
  datasource:
    db-kind: postgresql
    jdbc:
      url: jdbc:postgresql://prod-host:5432/chiroerp
"@

            $appClassName = "Finance${appSuffix}Application"
            New-File -Path (Join-Path $srcBase "${appClassName}.kt") -Content @"
package com.chiroerp.finance.$shortName.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class ${appClassName} : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("${appClassName} started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(${appClassName}::class.java, *args)
}
"@
        }
    }
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "Scaffolding completed!" -ForegroundColor Green
