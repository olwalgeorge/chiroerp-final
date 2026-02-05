# Scaffold Platform-Shared and Finance Domains
# Generated: February 5, 2026
# Based on COMPLETE_STRUCTURE.txt specifications

param(
    [switch]$DryRun,
    [string]$BasePath = $PSScriptRoot
)

Write-Host "ChiroERP Platform-Shared and Finance Scaffolding Script" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
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
            New-Item -ItemType File -Path $Path -Value $Content -Force | Out-Null
            Write-Host "Created file: $Path" -ForegroundColor Green
        } else {
            Write-Host "File already exists: $Path" -ForegroundColor Blue
        }
    }
}

# Platform-Shared Domain Structure
$platformSharedModules = @(
    "common-types",
    "common-messaging",
    "common-security",
    "common-observability",
    "config-model",
    "org-model",
    "workflow-model"
)

# Finance Domain Structure
$financeModules = @(
    "finance-shared",
    "finance-gl",
    "finance-ar",
    "finance-ap",
    "finance-assets",
    "finance-tax"
)

# Subdomain layers
$layers = @("domain", "application", "infrastructure")

# Standard package structure
$packagePath = "src/main/kotlin/com/chiroerp"
$testPackagePath = "src/test/kotlin/com/chiroerp"

Write-Host "Creating Platform-Shared modules..." -ForegroundColor Magenta
foreach ($module in $platformSharedModules) {
    $modulePath = Join-Path $BasePath "platform-shared/$module"

    # Build file
    if ($module -eq "common-types") {
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    // No dependencies - shared kernel primitives
}
"@
    } else {
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@
    }

    # Source structure
    $srcPath = Join-Path $modulePath $packagePath
    $sharedPath = Join-Path $srcPath "shared"

    # Module-specific package
    $moduleName = $module -replace "common-", "" -replace "-model", ""
    $modulePackagePath = Join-Path $sharedPath $moduleName

    # Create directories (pure Kotlin libs, no runtime entrypoints)
    New-Directory -Path $modulePackagePath
    New-Directory -Path (Join-Path $modulePath "src/main/resources")

    # Test structure
    $testPath = Join-Path $modulePath $testPackagePath
    $testSharedPath = Join-Path $testPath "shared"
    $testModulePath = Join-Path $testSharedPath $moduleName
    New-Directory -Path $testModulePath
    New-Directory -Path (Join-Path $modulePath "src/test/resources")
}

Write-Host "Creating Finance modules..." -ForegroundColor Magenta
foreach ($module in $financeModules) {
    $modulePath = Join-Path $BasePath "finance/$module"

    # Build file
    if ($module -eq "finance-shared") {
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}
"@
    } else {
        New-File -Path (Join-Path $modulePath "build.gradle.kts") -Content @"
// $module module (aggregator)
"@
    }

    # For layered modules (GL, AR, AP, Assets, Tax)
    if ($module -ne "finance-shared") {
        foreach ($layer in $layers) {
            $layerModule = "$module-$layer"
            $layerPath = Join-Path $BasePath "finance/$module/$layerModule"
            $domainModule = "$module-domain"
            $applicationModule = "$module-application"

            # Build file for layer
            if ($layer -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:${module}:${domainModule}"))
    implementation(project(":finance:${module}:${applicationModule}"))
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
    implementation(project(":finance:${module}:${domainModule}"))
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

            # Source structure for layer
            $srcPath = Join-Path $layerPath $packagePath
            $financePath = Join-Path $srcPath "finance"
            $moduleName = $module -replace "finance-", ""
            $modulePackagePath = Join-Path $financePath $moduleName

            if ($layer -eq "domain") {
                New-Directory -Path (Join-Path $modulePackagePath "$layer/model")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/events")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/exceptions")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/services")
            } elseif ($layer -eq "application") {
                New-Directory -Path (Join-Path $modulePackagePath "$layer/port/input/command")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/port/input/query")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/port/output")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/service/command")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/service/query")
            } else { # infrastructure
                New-Directory -Path (Join-Path $modulePackagePath "$layer/adapter/input/rest")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/adapter/input/event")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/adapter/output/persistence/jpa/entity")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/adapter/output/persistence/jpa/repository")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/adapter/output/messaging/kafka")
                New-Directory -Path (Join-Path $modulePackagePath "$layer/configuration")
            }

            New-Directory -Path (Join-Path $layerPath "src/main/resources")
            New-Directory -Path (Join-Path $layerPath "src/main/resources/db/migration")

            # Test structure
            $testPath = Join-Path $layerPath $testPackagePath
            $testFinancePath = Join-Path $testPath "finance"
            $testModulePath = Join-Path $testFinancePath $moduleName
            New-Directory -Path (Join-Path $testModulePath $layer)
            New-Directory -Path (Join-Path $layerPath "src/test/resources")

            # Application properties for infrastructure layer
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

                New-File -Path (Join-Path $layerPath "src/main/resources/application-prod.yml") -Content @"
quarkus:
  datasource:
    db-kind: postgresql
    username: \${DB_USERNAME}
    password: \${DB_PASSWORD}
    jdbc:
      url: jdbc:postgresql://prod-db:5432/chiroerp
  hibernate-orm:
    database:
      generation: validate
"@

                # Main application class for infrastructure layer
                $appClassName = ($moduleName -split '-' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join ''
                New-File -Path (Join-Path $modulePackagePath "$layer/${appClassName}Application.kt") -Content @"
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
    } else {
        # finance-shared structure
        $srcPath = Join-Path $modulePath $packagePath
        $financePath = Join-Path $srcPath "finance"
        $sharedPath = Join-Path $financePath "shared"

        New-Directory -Path $sharedPath
        New-Directory -Path (Join-Path $modulePath "src/main/resources")
        New-Directory -Path (Join-Path $modulePath "src/test/resources")

        # Test structure
        $testPath = Join-Path $modulePath $testPackagePath
        $testFinancePath = Join-Path $testPath "finance"
        $testSharedPath = Join-Path $testFinancePath "shared"
        New-Directory -Path $testSharedPath
    }
}

Write-Host "" -ForegroundColor White
Write-Host "Scaffolding completed!" -ForegroundColor Green
Write-Host "Review the created structure and customize the generated files as needed." -ForegroundColor Yellow
