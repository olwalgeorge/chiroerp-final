# Scaffold Platform-Shared, Finance, and Inventory Domains
# Generated: February 6, 2026
# Based on COMPLETE_STRUCTURE.txt specifications and ADR-006 governance
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

Write-Host "ChiroERP Full Platform Scaffolding Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
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

    # Build file
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
        # ADR-006 compliant subdirectories for common-types
        $srcBase = Join-Path $modulePath "src/main/kotlin/com/chiroerp/shared/types"
        New-Directory -Path (Join-Path $srcBase "primitives")    # Identifier, AuditInfo, CorrelationId
        New-Directory -Path (Join-Path $srcBase "events")        # DomainEvent, EventMetadata, EventEnvelope
        New-Directory -Path (Join-Path $srcBase "results")       # Result, DomainError, ValidationError
        New-Directory -Path (Join-Path $srcBase "aggregate")     # AggregateRoot, Entity, ValueObject
        New-Directory -Path (Join-Path $srcBase "cqrs")          # Command, Query, CommandHandler, QueryHandler
        New-Directory -Path (Join-Path $modulePath "src/main/resources")
        
        # Test structure
        New-Directory -Path (Join-Path $modulePath "src/test/kotlin/com/chiroerp/shared/types")
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
    } else {
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
        # Source structure for other modules
        $srcPath = Join-Path $modulePath "src/main/kotlin/com/chiroerp/shared/$moduleName"
        New-Directory -Path $srcPath
        New-Directory -Path (Join-Path $modulePath "src/main/resources")

        # Test structure
        $testPath = Join-Path $modulePath "src/test/kotlin/com/chiroerp/shared/$moduleName"
        New-Directory -Path $testPath
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
    }
}

# =============================================================================
# FINANCE BOUNDED CONTEXT
# =============================================================================

$financeModules = @(
    "finance-shared",
    "finance-gl",
    "finance-ar",
    "finance-ap",
    "finance-assets",
    "finance-tax"
)

$layers = @("domain", "application", "infrastructure")

Write-Host "`n=== FINANCE BOUNDED CONTEXT ===" -ForegroundColor Magenta

foreach ($module in $financeModules) {
    $modulePath = Join-Path $BasePath "bounded-contexts/finance/$module"
    $moduleName = $module -replace "finance-", ""

    if ($module -eq "finance-shared") {
        # Finance-shared is a simple library module
        # ADR-006: Contains context-specific value objects (Money, Currency, TaxId, etc.)
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
// ADR-006: Context-specific value objects for Finance bounded context
// Contains: Money (finance precision), Currency, TaxId, AccountNumber
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@
        $srcPath = Join-Path $modulePath "src/main/kotlin/com/chiroerp/finance/shared"
        New-Directory -Path (Join-Path $srcPath "valueobjects")  # Money, Currency, TaxId, AccountNumber
        New-Directory -Path $srcPath
        New-Directory -Path (Join-Path $modulePath "src/main/resources")
        New-Directory -Path (Join-Path $modulePath "src/test/kotlin/com/chiroerp/finance/shared")
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
    } else {
        # Aggregator build file
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content "// $module module (aggregator)"

        # Layered modules (domain, application, infrastructure)
        foreach ($layer in $layers) {
            $layerModule = "${module}-${layer}"
            $layerPath = Join-Path $modulePath $layerModule
            $domainModule = "${module}-domain"
            $applicationModule = "${module}-application"

            # Build file for each layer
            if ($layer -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:${module}:${domainModule}"))
    implementation(project(":bounded-contexts:finance:${module}:${applicationModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}
"@
            } elseif ($layer -eq "application") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:${module}:${domainModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
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
    implementation(project(":bounded-contexts:finance:finance-shared"))
}
"@
            }

            # Source directories
            $srcBase = Join-Path $layerPath "src/main/kotlin/com/chiroerp/finance/$moduleName/$layer"

            if ($layer -eq "domain") {
                New-Directory -Path (Join-Path $srcBase "model")
                New-Directory -Path (Join-Path $srcBase "events")
                New-Directory -Path (Join-Path $srcBase "exceptions")
                New-Directory -Path (Join-Path $srcBase "services")
            } elseif ($layer -eq "application") {
                New-Directory -Path (Join-Path $srcBase "port/input/command")
                New-Directory -Path (Join-Path $srcBase "port/input/query")
                New-Directory -Path (Join-Path $srcBase "port/output")
                New-Directory -Path (Join-Path $srcBase "service/command")
                New-Directory -Path (Join-Path $srcBase "service/query")
            } else {
                New-Directory -Path (Join-Path $srcBase "adapter/input/rest")
                New-Directory -Path (Join-Path $srcBase "adapter/input/event")
                New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/entity")
                New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/repository")
                New-Directory -Path (Join-Path $srcBase "adapter/output/messaging/kafka")
                New-Directory -Path (Join-Path $srcBase "configuration")
            }

            New-Directory -Path (Join-Path $layerPath "src/main/resources")
            New-Directory -Path (Join-Path $layerPath "src/main/resources/db/migration")
            New-Directory -Path (Join-Path $layerPath "src/test/kotlin/com/chiroerp/finance/$moduleName/$layer")
            New-Directory -Path (Join-Path $layerPath "src/test/resources")

            # Infrastructure layer specific files
            if ($layer -eq "infrastructure") {
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

                # Main application class
                $appClassName = ($moduleName -split '-' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join ''
                New-File -Path (Join-Path $srcBase "${appClassName}Application.kt") -Content @"
package com.chiroerp.finance.$moduleName.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class ${appClassName}Application : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("${appClassName}Application started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(${appClassName}Application::class.java, *args)
}
"@
            }
        }
    }
}

# =============================================================================
# INVENTORY BOUNDED CONTEXT
# =============================================================================

$inventoryModules = @(
    @{ Name = "inventory-shared"; Type = "shared" },
    @{ Name = "inventory-core"; Type = "layered"; Layers = @("core-domain", "core-application", "core-infrastructure") },
    @{ Name = "inventory-warehouse"; Type = "layered"; Layers = @("warehouse-domain", "warehouse-application", "warehouse-infrastructure") }
)

Write-Host "`n=== INVENTORY BOUNDED CONTEXT ===" -ForegroundColor Magenta

foreach ($moduleConfig in $inventoryModules) {
    $module = $moduleConfig.Name
    $modulePath = Join-Path $BasePath "inventory/$module"
    $moduleName = $module -replace "inventory-", ""

    if ($moduleConfig.Type -eq "shared") {
        # Inventory-shared is a simple library module
        # ADR-006: Contains context-specific value objects (Quantity, Money for valuation, Weight)
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
// ADR-006: Context-specific value objects for Inventory bounded context
// Contains: Quantity, Money (valuation), Weight
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@
        $srcPath = Join-Path $modulePath "src/main/kotlin/com/chiroerp/inventory/shared"
        New-Directory -Path (Join-Path $srcPath "valueobjects")  # Quantity, Money, Weight
        New-Directory -Path $srcPath
        New-Directory -Path (Join-Path $modulePath "src/main/resources")
        New-Directory -Path (Join-Path $modulePath "src/test/kotlin/com/chiroerp/inventory/shared")
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
    } else {
        # Aggregator build file
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content "// $module module (aggregator)"

        # Layered modules
        foreach ($layerModuleName in $moduleConfig.Layers) {
            $layerPath = Join-Path $modulePath $layerModuleName
            $layerType = if ($layerModuleName -match "domain") { "domain" } elseif ($layerModuleName -match "application") { "application" } else { "infrastructure" }
            $subdomainName = $layerModuleName -replace "-domain|-application|-infrastructure", ""
            $domainModule = "${subdomainName}-domain"
            $applicationModule = "${subdomainName}-application"

            # Build file for each layer
            if ($layerType -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModuleName build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":inventory:${module}:${domainModule}"))
    implementation(project(":inventory:${module}:${applicationModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":inventory:inventory-shared"))
}
"@
            } elseif ($layerType -eq "application") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModuleName build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":inventory:${module}:${domainModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":inventory:inventory-shared"))
}
"@
            } else {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModuleName build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":inventory:inventory-shared"))
}
"@
            }

            # Source directories
            $srcBase = Join-Path $layerPath "src/main/kotlin/com/chiroerp/inventory/$subdomainName/$layerType"

            if ($layerType -eq "domain") {
                New-Directory -Path (Join-Path $srcBase "model")
                New-Directory -Path (Join-Path $srcBase "events")
                New-Directory -Path (Join-Path $srcBase "exceptions")
                New-Directory -Path (Join-Path $srcBase "services")
            } elseif ($layerType -eq "application") {
                New-Directory -Path (Join-Path $srcBase "port/input/command")
                New-Directory -Path (Join-Path $srcBase "port/input/query")
                New-Directory -Path (Join-Path $srcBase "port/output")
                New-Directory -Path (Join-Path $srcBase "service/command")
                New-Directory -Path (Join-Path $srcBase "service/query")
            } else {
                New-Directory -Path (Join-Path $srcBase "adapter/input/rest")
                New-Directory -Path (Join-Path $srcBase "adapter/input/event")
                New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/entity")
                New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/repository")
                New-Directory -Path (Join-Path $srcBase "adapter/output/messaging/kafka")
                New-Directory -Path (Join-Path $srcBase "configuration")
            }

            New-Directory -Path (Join-Path $layerPath "src/main/resources")
            New-Directory -Path (Join-Path $layerPath "src/main/resources/db/migration")
            New-Directory -Path (Join-Path $layerPath "src/test/kotlin/com/chiroerp/inventory/$subdomainName/$layerType")
            New-Directory -Path (Join-Path $layerPath "src/test/resources")

            # Infrastructure layer specific files
            if ($layerType -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "src/main/resources/application.yml") -Content @"
quarkus:
  application:
    name: $layerModuleName
  profile: dev

quarkus.http:
  port: 8080
"@

                # Main application class
                $appClassName = ($subdomainName -split '-' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join ''
                $appClassName = "Inventory${appClassName}"
                New-File -Path (Join-Path $srcBase "${appClassName}Application.kt") -Content @"
package com.chiroerp.inventory.$subdomainName.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class ${appClassName}Application : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("${appClassName}Application started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(${appClassName}Application::class.java, *args)
}
"@
            }
        }
    }
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "Scaffolding completed!" -ForegroundColor Green

# =============================================================================
# COMMERCE BOUNDED CONTEXT (ADR-025)
# =============================================================================

$commerceModules = @(
    @{ Name = "commerce-shared"; Type = "shared" },
    @{ Name = "commerce-ecommerce"; Type = "layered"; Layers = @("ecommerce-domain", "ecommerce-application", "ecommerce-infrastructure"); Port = 9301 },
    @{ Name = "commerce-pos"; Type = "layered"; Layers = @("pos-domain", "pos-application", "pos-infrastructure"); Port = 9302 },
    @{ Name = "commerce-b2b"; Type = "layered"; Layers = @("b2b-domain", "b2b-application", "b2b-infrastructure"); Port = 9303 },
    @{ Name = "commerce-marketplace"; Type = "layered"; Layers = @("marketplace-domain", "marketplace-application", "marketplace-infrastructure"); Port = 9304 },
    @{ Name = "commerce-pricing"; Type = "layered"; Layers = @("pricing-domain", "pricing-application", "pricing-infrastructure"); Port = 9305 }
)

Write-Host "`n=== COMMERCE BOUNDED CONTEXT (ADR-025) ===" -ForegroundColor Magenta

foreach ($moduleConfig in $commerceModules) {
    $module = $moduleConfig.Name
    $modulePath = Join-Path $BasePath "commerce/$module"
    $moduleName = $module -replace "commerce-", ""

    if ($moduleConfig.Type -eq "shared") {
        # Commerce-shared is a simple library module
        # ADR-006: Contains ONLY identifiers, value objects, and enums
        # NO domain models, NO business logic
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
// ADR-006 COMPLIANT: Identifiers, VOs, Enums ONLY
// NO domain models (Product, Category, Order), NO business logic (PricingEngine)
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@
        $srcPath = Join-Path $modulePath "src/main/kotlin/com/chiroerp/commerce/shared"
        New-Directory -Path (Join-Path $srcPath "identifiers")   # ProductId, CategoryId, OrderId, CustomerId
        New-Directory -Path (Join-Path $srcPath "valueobjects")  # Money, SKU, Price, Discount, Tax, Address
        New-Directory -Path (Join-Path $srcPath "enums")         # OrderStatus, PaymentStatus, FulfillmentStatus
        New-Directory -Path $srcPath
        New-Directory -Path (Join-Path $modulePath "src/main/resources")
        New-Directory -Path (Join-Path $modulePath "src/test/kotlin/com/chiroerp/commerce/shared")
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
    } else {
        # Aggregator build file
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content "// $module module (aggregator) - Port $($moduleConfig.Port)"

        # Layered modules
        foreach ($layerModuleName in $moduleConfig.Layers) {
            $layerPath = Join-Path $modulePath $layerModuleName
            $layerType = if ($layerModuleName -match "domain") { "domain" } elseif ($layerModuleName -match "application") { "application" } else { "infrastructure" }
            $subdomainName = $layerModuleName -replace "-domain|-application|-infrastructure", ""
            $domainModule = "${subdomainName}-domain"
            $applicationModule = "${subdomainName}-application"

            # Build file for each layer
            if ($layerType -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModuleName build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":commerce:${module}:${domainModule}"))
    implementation(project(":commerce:${module}:${applicationModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":commerce:commerce-shared"))
}
"@
            } elseif ($layerType -eq "application") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModuleName build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":commerce:${module}:${domainModule}"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":commerce:commerce-shared"))
}
"@
            } else {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModuleName build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":commerce:commerce-shared"))
}
"@
            }

            # Source directories
            $srcBase = Join-Path $layerPath "src/main/kotlin/com/chiroerp/commerce/$subdomainName/$layerType"

            if ($layerType -eq "domain") {
                New-Directory -Path (Join-Path $srcBase "model")
                New-Directory -Path (Join-Path $srcBase "event")
                New-Directory -Path (Join-Path $srcBase "exception")
                New-Directory -Path (Join-Path $srcBase "service")
            } elseif ($layerType -eq "application") {
                New-Directory -Path (Join-Path $srcBase "port/input")
                New-Directory -Path (Join-Path $srcBase "port/output")
                New-Directory -Path (Join-Path $srcBase "command")
                New-Directory -Path (Join-Path $srcBase "query")
                New-Directory -Path (Join-Path $srcBase "service")
            } else {
                New-Directory -Path (Join-Path $srcBase "adapter/input/rest")
                New-Directory -Path (Join-Path $srcBase "adapter/input/event")
                New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/entity")
                New-Directory -Path (Join-Path $srcBase "adapter/output/persistence/jpa/repository")
                New-Directory -Path (Join-Path $srcBase "adapter/output/messaging/kafka")
                New-Directory -Path (Join-Path $srcBase "configuration")
            }

            New-Directory -Path (Join-Path $layerPath "src/main/resources")
            New-Directory -Path (Join-Path $layerPath "src/main/resources/db/migration")
            New-Directory -Path (Join-Path $layerPath "src/test/kotlin/com/chiroerp/commerce/$subdomainName/$layerType")
            New-Directory -Path (Join-Path $layerPath "src/test/resources")

            # Infrastructure layer specific files
            if ($layerType -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "src/main/resources/application.yml") -Content @"
quarkus:
  application:
    name: commerce-$subdomainName
  profile: dev

quarkus.http:
  port: $($moduleConfig.Port)
"@

                # Main application class
                $appClassName = ($subdomainName -split '-' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join ''
                $appClassName = "Commerce${appClassName}"
                New-File -Path (Join-Path $srcBase "${appClassName}Application.kt") -Content @"
package com.chiroerp.commerce.$subdomainName.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class ${appClassName}Application : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("${appClassName}Application started on port $($moduleConfig.Port)")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(${appClassName}Application::class.java, *args)
}
"@
            }
        }
    }
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "Full scaffolding completed!" -ForegroundColor Green
Write-Host "Review the created structure and customize the generated files as needed." -ForegroundColor Yellow
Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "  1. Run './gradlew build' to verify the structure" -ForegroundColor White
Write-Host "  2. Implement domain models in the domain layers" -ForegroundColor White
Write-Host "  3. Add use cases in the application layers" -ForegroundColor White
Write-Host "  4. Implement adapters in the infrastructure layers" -ForegroundColor White
