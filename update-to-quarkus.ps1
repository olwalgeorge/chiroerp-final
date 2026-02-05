# Update all scaffolded modules to use Quarkus instead of Spring Boot
# This script updates build.gradle.kts, application.yml files, and main application classes

param(
    [string]$BasePath = $PSScriptRoot
)

Write-Host "Updating scaffolded modules to use Quarkus..." -ForegroundColor Cyan

# Platform-shared modules
$platformModules = @(
    "common-types",
    "common-messaging",
    "common-security",
    "common-observability",
    "config-model",
    "org-model",
    "workflow-model"
)

# Finance modules
$financeModules = @(
    "finance-shared",
    "finance-gl",
    "finance-ar",
    "finance-ap",
    "finance-assets",
    "finance-tax"
)

# Layer modules for finance
$layerModules = @(
    "finance-gl-domain",
    "finance-gl-application",
    "finance-gl-infrastructure",
    "finance-ar-domain",
    "finance-ar-application",
    "finance-ar-infrastructure",
    "finance-ap-domain",
    "finance-ap-application",
    "finance-ap-infrastructure",
    "finance-assets-domain",
    "finance-assets-application",
    "finance-assets-infrastructure",
    "finance-tax-domain",
    "finance-tax-application",
    "finance-tax-infrastructure"
)

function Update-BuildFile {
    param([string]$FilePath)

    if (Test-Path $FilePath) {
        $content = Get-Content $FilePath -Raw

        # Replace Spring Boot plugins with Quarkus conventions
        $content = $content -replace 'plugins\s*\{[^}]*kotlin\("jvm"\)\s+version\s+"[^"]*"[^}]*kotlin\("plugin\.spring"\)\s+version\s+"[^"]*"[^}]*\}', 'plugins {
    id("chiroerp.quarkus-conventions")
}'

        # Remove Spring Boot dependencies
        $content = $content -replace '\s*implementation\("org\.springframework\.boot:spring-boot-starter[^"]*"\)[^\n]*\n', ''
        $content = $content -replace '\s*implementation\("org\.springframework\.boot:spring-boot-starter-data-jpa[^"]*"\)[^\n]*\n', ''
        $content = $content -replace '\s*implementation\("org\.springframework\.kafka:spring-kafka[^"]*"\)[^\n]*\n', ''
        $content = $content -replace '\s*testImplementation\("org\.springframework\.boot:spring-boot-starter-test[^"]*"\)[^\n]*\n', ''

        Set-Content -Path $FilePath -Value $content
        Write-Host "Updated: $FilePath" -ForegroundColor Green
    }
}

function Update-ApplicationYml {
    param([string]$FilePath)

    if (Test-Path $FilePath) {
        $content = Get-Content $FilePath -Raw

        # Replace Spring configuration with Quarkus
        $content = $content -replace 'spring:', 'quarkus:'
        $content = $content -replace 'profiles:\s*active:\s*dev', 'profile: dev'
        $content = $content -replace 'server:\s*port:\s*(\d+)', 'quarkus.http:
  port: $1'
        $content = $content -replace 'jpa:\s*hibernate:\s*ddl-auto:\s*validate', 'hibernate-orm:
    database:
      generation: validate'
        $content = $content -replace 'datasource:\s*url:\s*jdbc:postgresql://([^/]+)/([^/]+)', 'datasource:
    db-kind: postgresql
    jdbc:
      url: jdbc:postgresql://$1/$2'
        $content = $content -replace 'datasource:\s*username:\s*([^\n]+)', 'datasource:
    username: $1'
        $content = $content -replace 'datasource:\s*password:\s*([^\n]+)', '    password: $1'

        Set-Content -Path $FilePath -Value $content
        Write-Host "Updated: $FilePath" -ForegroundColor Green
    }
}

function Update-ApplicationClass {
    param([string]$FilePath)

    if (Test-Path $FilePath) {
        $content = Get-Content $FilePath -Raw

        # Replace Spring Boot application with Quarkus
        $content = $content -replace 'import org\.springframework\.boot\.autoconfigure\.SpringBootApplication[^\n]*\n', 'import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

'
        $content = $content -replace '@SpringBootApplication[^\n]*\nclass ([^\s]+)', '@QuarkusMain
class $1 : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("$1 started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}'
        $content = $content -replace 'fun main\(args: Array<String>\)\s*\{\s*runApplication<([^>]+)>\(\*args\)\s*\}', 'fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run($1::class.java, *args)
}'

        Set-Content -Path $FilePath -Value $content
        Write-Host "Updated: $FilePath" -ForegroundColor Green
    }
}

# Update platform-shared modules
foreach ($module in $platformModules) {
    $modulePath = Join-Path $BasePath "platform-shared/$module"

    # Update build.gradle.kts
    Update-BuildFile -FilePath (Join-Path $modulePath "build.gradle.kts")

    # Update application.yml files
    Update-ApplicationYml -FilePath (Join-Path $modulePath "src/main/resources/application.yml")
    Update-ApplicationYml -FilePath (Join-Path $modulePath "src/main/resources/application-dev.yml")
    Update-ApplicationYml -FilePath (Join-Path $modulePath "src/main/resources/application-prod.yml")

    # Update main application class
    $appClassPath = Join-Path $modulePath "src/main/kotlin/com/chiroerp/shared"
    $moduleName = $module -replace "common-", "" -replace "-model", ""
    $appFile = Join-Path $appClassPath "$moduleName/infrastructure/${module}Application.kt"
    Update-ApplicationClass -FilePath $appFile
}

# Update finance modules
foreach ($module in $financeModules) {
    $modulePath = Join-Path $BasePath "finance/$module"

    # Update build.gradle.kts
    Update-BuildFile -FilePath (Join-Path $modulePath "build.gradle.kts")
}

# Update finance layer modules
foreach ($layer in $layerModules) {
    $parts = $layer -split "-"
    $domain = $parts[0] + "-" + $parts[1]
    $layerType = $parts[2]
    $layerPath = Join-Path $BasePath "finance/$domain/$layer"

    # Update build.gradle.kts
    Update-BuildFile -FilePath (Join-Path $layerPath "build.gradle.kts")

    # Update application files for infrastructure layers
    if ($layerType -eq "infrastructure") {
        Update-ApplicationYml -FilePath (Join-Path $layerPath "src/main/resources/application.yml")
        Update-ApplicationYml -FilePath (Join-Path $layerPath "src/main/resources/application-dev.yml")
        Update-ApplicationYml -FilePath (Join-Path $layerPath "src/main/resources/application-prod.yml")

        # Update main application class
        $appClassPath = Join-Path $layerPath "src/main/kotlin/com/chiroerp/finance"
        $shortDomain = $parts[1]
        $appFile = Join-Path $appClassPath "$shortDomain/infrastructure/${shortDomain}Application.kt"
        Update-ApplicationClass -FilePath $appFile
    }
}

Write-Host "All modules updated to use Quarkus!" -ForegroundColor Green
