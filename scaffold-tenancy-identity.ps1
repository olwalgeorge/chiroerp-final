# Scaffold Tenancy-Identity Domain
# Generated: February 5, 2026
# Based on COMPLETE_STRUCTURE.txt specifications

param(
    [switch]$DryRun,
    [string]$BasePath = $PSScriptRoot
)

Write-Host "ChiroERP Tenancy-Identity Scaffolding Script" -ForegroundColor Cyan
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
            New-Item -ItemType File -Path $Path -Value $Content -Force | Out-Null
            Write-Host "Created file: $Path" -ForegroundColor Green
        } else {
            Write-Host "File already exists: $Path" -ForegroundColor Blue
        }
    }
}

# Tenancy-Identity modules
$tenancyModules = @(
    "tenancy-shared",
    "tenancy-core",
    "identity-core"
)

# Subdomain layers (only for core modules)
$layers = @("domain", "application", "infrastructure")

# Standard package structure
$packagePath = "src/main/kotlin/com/chiroerp"
$testPackagePath = "src/test/kotlin/com/chiroerp"

Write-Host "Creating Tenancy-Identity modules..." -ForegroundColor Magenta
foreach ($module in $tenancyModules) {
    $modulePath = Join-Path $BasePath "tenancy-identity/$module"

    # Build file
    if ($module -eq "tenancy-shared") {
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
// $module build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":tenancy-identity:tenancy-shared"))
}
"@
    }

    if ($module -eq "tenancy-shared") {
        # Shared module structure (like finance-shared)
        $srcPath = Join-Path $modulePath $packagePath
        $tenancyPath = Join-Path $srcPath "tenancy"
        $sharedPath = Join-Path $tenancyPath "shared"

        New-Directory -Path $sharedPath
        New-Directory -Path (Join-Path $modulePath "src/main/resources")

        # Test structure
        $testPath = Join-Path $modulePath $testPackagePath
        $testTenancyPath = Join-Path $testPath "tenancy"
        $testSharedPath = Join-Path $testTenancyPath "shared"
        New-Directory -Path $testSharedPath
        New-Directory -Path (Join-Path $modulePath "src/test/resources")
    } else {
        # Layered modules (tenancy-core, identity-core)
        foreach ($layer in $layers) {
            $layerModule = "$module-$layer"
            $layerPath = Join-Path $BasePath "tenancy-identity/$module/$layerModule"

            # Build file for layer
            if ($layer -eq "infrastructure") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project("`:tenancy-identity`:${module}-domain"))
    implementation(project("`:tenancy-identity`:${module}-application"))
    implementation(project("`:platform-shared`:common-types"))
    implementation(project("`:tenancy-identity`:tenancy-shared"))
}
"@
            } elseif ($layer -eq "application") {
                New-File -Path (Join-Path $layerPath "build.gradle.kts") -Content @"
// $layerModule build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project("`:tenancy-identity`:${module}`:${module}-domain"))
    implementation(project("`:platform-shared`:common-types"))
    implementation(project("`:tenancy-identity`:tenancy-shared"))
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
    implementation(project(":tenancy-identity:tenancy-shared"))
}
"@
            }

            # Source structure for layer
            $srcPath = Join-Path $layerPath $packagePath
            if ($module -eq "tenancy-core") {
                $tenancyPath = Join-Path $srcPath "tenancy"
                $moduleName = "core"
            } else {
                $identityPath = Join-Path $srcPath "identity"
                $moduleName = "core"
            }

            if ($layer -eq "domain") {
                if ($module -eq "tenancy-core") {
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/model")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/event")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/port")
                } else {
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/model")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/event")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/port")
                }
            } elseif ($layer -eq "application") {
                if ($module -eq "tenancy-core") {
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/command")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/query")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/handler")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/service")
                } else {
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/command")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/query")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/handler")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/service")
                }
            } else { # infrastructure
                if ($module -eq "tenancy-core") {
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/persistence")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/messaging")
                    New-Directory -Path (Join-Path $tenancyPath "$moduleName/$layer/web")
                } else {
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/persistence")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/messaging")
                    New-Directory -Path (Join-Path $identityPath "$moduleName/$layer/web")
                }
            }

            New-Directory -Path (Join-Path $layerPath "src/main/resources")
            New-Directory -Path (Join-Path $layerPath "src/main/resources/db/migration")

            # Test structure
            $testPath = Join-Path $layerPath $testPackagePath
            if ($module -eq "tenancy-core") {
                $testTenancyPath = Join-Path $testPath "tenancy"
                $testModulePath = Join-Path $testTenancyPath $moduleName
            } else {
                $testIdentityPath = Join-Path $testPath "identity"
                $testModulePath = Join-Path $testIdentityPath $moduleName
            }
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
      url: jdbc:postgresql://localhost:5432/tenancy_identity
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
      url: jdbc:postgresql://prod-db:5432/tenancy_identity
  hibernate-orm:
    database:
      generation: validate
"@

                # Main application class for infrastructure layer
                $appClassName = ($moduleName -split '-' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join '' + ($layer -split '-' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join ''
                if ($module -eq "tenancy-core") {
                    $packageName = "com.chiroerp.tenancy.core.infrastructure"
                    $classPath = "src/main/kotlin/com/chiroerp/tenancy/core/infrastructure/${appClassName}Application.kt"
                } else {
                    $packageName = "com.chiroerp.identity.core.infrastructure"
                    $classPath = "src/main/kotlin/com/chiroerp/identity/core/infrastructure/${appClassName}Application.kt"
                }
                New-File -Path (Join-Path $layerPath $classPath) -Content @"
package $packageName

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

Write-Host "" -ForegroundColor White
Write-Host "Tenancy-Identity scaffolding completed!" -ForegroundColor Green
Write-Host "Review the created structure and customize the generated files as needed." -ForegroundColor Yellow