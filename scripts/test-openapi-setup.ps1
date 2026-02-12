# OpenAPI Setup Test Script
# Tests the OpenAPI automation setup for ChiroERP
# Run: .\scripts\test-openapi-setup.ps1

param(
    [string]$Module = "identity-core"
)

Write-Host "🔍 Testing OpenAPI Automation Setup" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "1️⃣  Checking prerequisites..." -ForegroundColor Yellow

# Check Node.js
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js installed: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js not found. Install from https://nodejs.org/" -ForegroundColor Red
    exit 1
}

# Check if Gradle wrapper exists
if (Test-Path ".\gradlew.bat") {
    Write-Host "✅ Gradle wrapper found" -ForegroundColor Green
} else {
    Write-Host "❌ gradlew.bat not found" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check configuration files
Write-Host "2️⃣  Checking configuration files..." -ForegroundColor Yellow

$configFiles = @(
    ".redocly.yaml",
    "config\openapi\application-openapi.properties",
    ".github\workflows\api-lint.yml",
    "docs\API_GOVERNANCE.md"
)

foreach ($file in $configFiles) {
    if (Test-Path $file) {
        Write-Host "✅ $file exists" -ForegroundColor Green
    } else {
        Write-Host "❌ $file missing" -ForegroundColor Red
    }
}

Write-Host ""

# Test Redocly CLI
Write-Host "3️⃣  Testing Redocly CLI..." -ForegroundColor Yellow
if (Get-Command redocly -ErrorAction SilentlyContinue) {
    $redoclyVersion = redocly --version
    Write-Host "✅ Redocly CLI installed: $redoclyVersion" -ForegroundColor Green
} else {
    Write-Host "❌ Redocly CLI not found. Install once with: npm install -g @redocly/cli" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Generate OpenAPI specs
Write-Host "4️⃣  Generating OpenAPI specs..." -ForegroundColor Yellow
Write-Host "   This will take a moment..." -ForegroundColor Gray

$buildOutput = .\gradlew.bat generateOpenApiSpecs 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ OpenAPI specs generated" -ForegroundColor Green
} else {
    Write-Host "❌ Spec generation failed" -ForegroundColor Red
    Write-Host $buildOutput -ForegroundColor Gray
    exit 1
}

Write-Host ""

# Check for generated OpenAPI spec
Write-Host "5️⃣  Checking for generated OpenAPI spec..." -ForegroundColor Yellow

$specPath = "bounded-contexts\tenancy-identity\$Module\build\openapi\openapi.yaml"
if (Test-Path $specPath) {
    Write-Host "✅ OpenAPI spec generated: $specPath" -ForegroundColor Green
    
    # Show spec info
    $specContent = Get-Content $specPath -Raw
    if ($specContent -match 'title:\s*(.+)') {
        Write-Host "   Title: $($matches[1])" -ForegroundColor Gray
    }
    if ($specContent -match 'version:\s*(.+)') {
        Write-Host "   Version: $($matches[1])" -ForegroundColor Gray
    }
    
    $pathCount = ([regex]::Matches($specContent, '^\s{2}/api/')).Count
    Write-Host "   Endpoints found: $pathCount" -ForegroundColor Gray
    
} else {
    Write-Host "❌ OpenAPI spec not found at $specPath" -ForegroundColor Red
    Write-Host "   Check that module has application.yml/properties with OpenAPI config" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test linting (if spec exists)
Write-Host "6️⃣  Testing OpenAPI linting..." -ForegroundColor Yellow
try {
    $lintOutput = redocly lint $specPath 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Spec passed linting" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Linting found issues:" -ForegroundColor Yellow
        Write-Host $lintOutput -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️  Could not run linting" -ForegroundColor Yellow
}

Write-Host ""

# Test doc generation
Write-Host "7️⃣  Testing documentation generation..." -ForegroundColor Yellow
$docPath = "docs\api\$Module.html"

try {
    redocly build-docs $specPath --output=$docPath 2>&1 | Out-Null
    if (Test-Path $docPath) {
        Write-Host "✅ Documentation generated: $docPath" -ForegroundColor Green
        $docSize = (Get-Item $docPath).Length
        Write-Host "   File size: $([math]::Round($docSize/1KB, 2)) KB" -ForegroundColor Gray
    } else {
        Write-Host "⚠️  Doc generation failed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Could not generate documentation" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "✅ OpenAPI Setup Test Complete!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Open $docPath in browser to view API docs" -ForegroundColor White
Write-Host "  2. Review $specPath to see generated OpenAPI spec" -ForegroundColor White
Write-Host "  3. Run '.\gradlew.bat apiGovernance' for full workflow" -ForegroundColor White
Write-Host "  4. See docs\API_GOVERNANCE.md for complete guide" -ForegroundColor White
Write-Host ""

# Offer to open docs
$response = Read-Host "Open generated docs in browser? (y/N)"
if ($response -eq 'y' -or $response -eq 'Y') {
    if (Test-Path $docPath) {
        Start-Process $docPath
    } else {
        Write-Host "Documentation file not found" -ForegroundColor Red
    }
}
