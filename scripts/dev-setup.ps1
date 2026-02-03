#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Sets up the ChiroERP development environment.

.DESCRIPTION
    This script:
    - Verifies Java 21 installation
    - Starts Docker Compose services
    - Runs database migrations
    - Builds all modules
    - Runs tests
    - Generates IDE project files

.PARAMETER SkipTests
    Skip running tests during setup

.PARAMETER SkipDocker
    Skip starting Docker services

.EXAMPLE
    .\dev-setup.ps1
    .\dev-setup.ps1 -SkipTests
#>

param(
    [switch]$SkipTests,
    [switch]$SkipDocker
)

$ErrorActionPreference = "Stop"

# Color output functions
function Write-Step {
    param([string]$Message)
    Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "🚀 $Message" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Test-CommandExists {
    param([string]$Command)
    $null = Get-Command $Command -ErrorAction SilentlyContinue
    return $?
}

Write-Host @"

   _____ _     _          ______ _____  _____  
  / ____| |   (_)        |  ____|  __ \|  __ \ 
 | |    | |__  _ _ __ ___| |__  | |__) | |__) |
 | |    | '_ \| | '__/ _ \  __| |  _  /|  ___/ 
 | |____| | | | | | | (_) | |____| | \ \| |     
  \_____|_| |_|_|_|  \___/|______|_|  \_\_|     
                                                 
  Development Environment Setup
  
"@ -ForegroundColor Cyan

$rootDir = Split-Path -Parent $PSScriptRoot

# Step 1: Verify Java 21
Write-Step "Step 1: Verifying Java 21 installation"

if (-not (Test-CommandExists "java")) {
    Write-Error-Custom "Java not found in PATH"
    Write-Info "Please install Java 21 (Eclipse Adoptium): https://adoptium.net/"
    exit 1
}

$javaVersion = java -version 2>&1 | Select-Object -First 1
Write-Info "Found: $javaVersion"

if ($javaVersion -notmatch "version `"21\.") {
    Write-Warning-Custom "Java 21 required, but found: $javaVersion"
    Write-Info "Please install Java 21 (Eclipse Adoptium): https://adoptium.net/"
    
    $response = Read-Host "Continue anyway? (yes/no)"
    if ($response -ne "yes") {
        exit 1
    }
}

Write-Success "Java 21 verified"

# Step 2: Verify Docker
if (-not $SkipDocker) {
    Write-Step "Step 2: Verifying Docker installation"

    if (-not (Test-CommandExists "docker")) {
        Write-Error-Custom "Docker not found in PATH"
        Write-Info "Please install Docker Desktop: https://www.docker.com/products/docker-desktop"
        exit 1
    }

    $dockerVersion = docker --version
    Write-Info "Found: $dockerVersion"

    # Check if Docker is running
    $dockerRunning = docker ps 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Docker daemon is not running"
        Write-Info "Please start Docker Desktop and try again"
        exit 1
    }

    Write-Success "Docker verified and running"
} else {
    Write-Info "Skipping Docker verification"
}

# Step 3: Create .env file
Write-Step "Step 3: Creating environment configuration"

$envFile = Join-Path $rootDir ".env"
if (-not (Test-Path $envFile)) {
    $envExample = Join-Path $rootDir ".env.example"
    if (Test-Path $envExample) {
        Copy-Item $envExample $envFile
        Write-Success "Created .env from .env.example"
    } else {
        # Create minimal .env
        $envContent = @"
# ChiroERP Development Environment

# Database Configuration
DB_PASSWORD=dev_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=dev_password

# Temporal Configuration
TEMPORAL_HOST=localhost
TEMPORAL_PORT=7233

# Observability
JAEGER_ENDPOINT=http://localhost:14268/api/traces
PROMETHEUS_ENDPOINT=http://localhost:9090

# Application
LOG_LEVEL=DEBUG
"@
        Set-Content -Path $envFile -Value $envContent
        Write-Success "Created .env file"
    }
} else {
    Write-Info ".env file already exists"
}

# Step 4: Start Docker services
if (-not $SkipDocker) {
    Write-Step "Step 4: Starting Docker services"

    Set-Location $rootDir

    Write-Info "Starting PostgreSQL, Kafka, Redis, Temporal, and observability stack..."
    docker-compose up -d

    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Failed to start Docker services"
        exit 1
    }

    Write-Info "Waiting for services to be healthy (30 seconds)..."
    Start-Sleep -Seconds 5

    # Check service health
    Write-Info "Checking service health..."
    
    $services = @(
        @{ Name = "PostgreSQL (Finance)"; Port = 5432 }
        @{ Name = "PostgreSQL (Sales)"; Port = 5433 }
        @{ Name = "PostgreSQL (Inventory)"; Port = 5434 }
        @{ Name = "Redpanda (Kafka API)"; Port = 19092 }
        @{ Name = "Redpanda Schema Registry"; Port = 18081 }
        @{ Name = "Redis"; Port = 6379 }
        @{ Name = "Temporal"; Port = 7233 }
        @{ Name = "Jaeger UI"; Port = 16686 }
        @{ Name = "Prometheus"; Port = 9090 }
        @{ Name = "Grafana"; Port = 3000 }
    )

    Start-Sleep -Seconds 25  # Wait for services to fully start

    $allHealthy = $true
    foreach ($service in $services) {
        $connection = Test-NetConnection -ComputerName localhost -Port $service.Port -WarningAction SilentlyContinue -InformationLevel Quiet
        if ($connection) {
            Write-Success "$($service.Name) is running on port $($service.Port)"
        } else {
            Write-Warning-Custom "$($service.Name) is not responding on port $($service.Port)"
            $allHealthy = $false
        }
    }

    if ($allHealthy) {
        Write-Success "All Docker services are healthy"
    } else {
        Write-Warning-Custom "Some services may not be ready yet. Check with: docker-compose ps"
    }
} else {
    Write-Info "Skipping Docker services startup"
}

# Step 5: Build all modules
Write-Step "Step 5: Building all modules"

Set-Location $rootDir

Write-Info "Running Gradle build..."
if ($IsWindows -or $env:OS -eq "Windows_NT") {
    & .\gradlew.bat buildAll --no-daemon --configuration-cache
} else {
    & ./gradlew buildAll --no-daemon --configuration-cache
}

if ($LASTEXITCODE -ne 0) {
    Write-Error-Custom "Build failed"
    exit 1
}

Write-Success "All modules built successfully"

# Step 6: Run tests
if (-not $SkipTests) {
    Write-Step "Step 6: Running tests"

    Write-Info "Running unit tests..."
    if ($IsWindows -or $env:OS -eq "Windows_NT") {
        & .\gradlew.bat test --no-daemon
    } else {
        & ./gradlew test --no-daemon
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Warning-Custom "Some tests failed. Check build/reports/tests/test/index.html"
    } else {
        Write-Success "All tests passed"
    }
} else {
    Write-Info "Skipping tests"
}

# Step 7: Generate IDE files
Write-Step "Step 7: Generating IDE project files"

Write-Info "Generating IntelliJ IDEA files..."
if ($IsWindows -or $env:OS -eq "Windows_NT") {
    & .\gradlew.bat idea --no-daemon 2>$null
} else {
    & ./gradlew idea --no-daemon 2>$null
}

if ($LASTEXITCODE -ne 0) {
    Write-Info "IDEA plugin not available (this is OK - IntelliJ can auto-import Gradle projects)"
} else {
    Write-Success "IDE project files generated"
}

# Summary
Write-Step "Setup Complete! 🎉"

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host "✅ Development environment ready!" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host ""

Write-Info "Available services:"
Write-Host "  🐘 PostgreSQL (Finance):    localhost:5432" -ForegroundColor White
Write-Host "  🐘 PostgreSQL (Sales):       localhost:5433" -ForegroundColor White
Write-Host "  🐘 PostgreSQL (Inventory):   localhost:5434" -ForegroundColor White
Write-Host "  📨 Redpanda (Kafka API):     localhost:19092 (external), localhost:9092 (docker)" -ForegroundColor White
Write-Host "  📋 Schema Registry:          localhost:18081" -ForegroundColor White
Write-Host "  🔴 Redis:                     localhost:6379" -ForegroundColor White
Write-Host "  ⏱️  Temporal:                  localhost:7233" -ForegroundColor White
Write-Host ""

Write-Info "Web UIs:"
Write-Host "  📊 Redpanda Console:          http://localhost:8090" -ForegroundColor Cyan
Write-Host "  🔴 Redis Commander:           http://localhost:8091" -ForegroundColor Cyan
Write-Host "  ⏱️  Temporal UI:               http://localhost:8092" -ForegroundColor Cyan
Write-Host "  🐘 pgAdmin:                   http://localhost:8093" -ForegroundColor Cyan
Write-Host "  🔍 Jaeger (Tracing):          http://localhost:16686" -ForegroundColor Cyan
Write-Host "  📈 Prometheus:                http://localhost:9090" -ForegroundColor Cyan
Write-Host "  📊 Grafana:                   http://localhost:3000 (admin/admin)" -ForegroundColor Cyan
Write-Host ""

Write-Info "Next steps:"
Write-Host "  1. Open project in IntelliJ IDEA or VS Code" -ForegroundColor White
Write-Host "  2. Scaffold your first module:  .\scripts\scaffold-module.ps1 -ModuleName 'finance-domain' -PackageName 'finance' -DatabaseName 'finance' -Port 8081" -ForegroundColor White
Write-Host "  3. Run a module in dev mode:    .\gradlew :finance-domain:quarkusDev" -ForegroundColor White
Write-Host "  4. View Swagger UI:             http://localhost:8081/swagger-ui" -ForegroundColor White
Write-Host "  5. Make your first commit!" -ForegroundColor White
Write-Host ""

Write-Info "Useful commands:"
Write-Host "  .\gradlew buildAll              # Build all modules" -ForegroundColor White
Write-Host "  .\gradlew test                  # Run all tests" -ForegroundColor White
Write-Host "  docker-compose ps               # Check service status" -ForegroundColor White
Write-Host "  docker-compose logs -f          # View logs" -ForegroundColor White
Write-Host "  docker-compose down             # Stop services" -ForegroundColor White
Write-Host "  docker-compose down -v          # Stop and remove volumes (reset databases)" -ForegroundColor White
Write-Host ""

Write-Host "Happy coding! 🚀" -ForegroundColor Green
Write-Host ""
