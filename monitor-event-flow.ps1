#!/usr/bin/env pwsh
# Comprehensive Event Flow Monitoring Dashboard

param(
    [string]$TenantId
)

function Show-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Yellow
    Write-Host ("=" * 80) -ForegroundColor Cyan
}

function Check-Container {
    param([string]$Name)
    $status = docker ps --filter "name=$Name" --format "{{.Status}}"
    if ($status) {
        Write-Host "  ✅ $Name : $status" -ForegroundColor Green
        return $true
    } else {
        Write-Host "  ❌ $name : Not running" -ForegroundColor Red
        return $false
    }
}

Clear-Host
Write-Host @"
╔════════════════════════════════════════════════════════════════════════════╗
║                   ChiroERP Event Flow Monitor                              ║
║                Tenant Creation → Kafka → Identity Bootstrap                ║
╚════════════════════════════════════════════════════════════════════════════╝
"@ -ForegroundColor Cyan

# Check all services
Show-Section "Service Health"
$allRunning = $true
$allRunning = (Check-Container "chiroerp-postgres-app") -and $allRunning
$allRunning = (Check-Container "chiroerp-redpanda") -and $allRunning
$allRunning = (Check-Container "chiroerp-tenancy-core") -and $allRunning
$allRunning = (Check-Container "chiroerp-identity-core") -and $allRunning
$allRunning = (Check-Container "chiroerp-redpanda-console") -and $allRunning

if (-not $allRunning) {
    Write-Host ""
    Write-Host "⚠️  Some services are not running. Start with: docker compose up -d" -ForegroundColor Yellow
    exit 1
}

# Kafka/Redpanda Status
Show-Section "Kafka/Redpanda Status"
Write-Host "  Topics:" -ForegroundColor Cyan
docker exec chiroerp-redpanda rpk topic list | ForEach-Object {
    if ($_ -match "chiroerp") {
        Write-Host "    • $_" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "  Consumer Groups:" -ForegroundColor Cyan
docker exec chiroerp-redpanda rpk group list | ForEach-Object {
    Write-Host "    • $_" -ForegroundColor Green
}

# Topic Details
Show-Section "Topic: chiroerp.tenancy.events"
$topicInfo = docker exec chiroerp-redpanda rpk topic describe chiroerp.tenancy.events 2>$null
if ($topicInfo) {
    Write-Host $topicInfo
} else {
    Write-Host "  Topic not found or no messages yet" -ForegroundColor Yellow
}

# Consumer Group Lag
Show-Section "Consumer Group: identity-core-tenancy-events"
$groupInfo = docker exec chiroerp-redpanda rpk group describe identity-core-tenancy-events 2>$null
if ($groupInfo) {
    Write-Host $groupInfo
} else {
    Write-Host "  Consumer group not found or no consumption yet" -ForegroundColor Yellow
}

# Database: Outbox
Show-Section "Tenancy-Core Outbox (Published Events)"
$outboxQuery = if ($TenantId) {
    "SELECT event_id, event_type, created_at, published_at FROM tenancy_core.tenant_outbox WHERE aggregate_id = '$TenantId' ORDER BY created_at DESC LIMIT 5;"
} else {
    "SELECT event_id, aggregate_id, event_type, created_at, published_at FROM tenancy_core.tenant_outbox ORDER BY created_at DESC LIMIT 5;"
}

docker exec -i chiroerp-postgres-app psql -U chiroerp_admin -d chiroerp -c $outboxQuery 2>$null

# Database: Consumption
Show-Section "Identity-Core Consumption (Consumed Events)"
$consumptionQuery = if ($TenantId) {
    "SELECT event_id, event_type, occurred_at, processed_at FROM identity_core.identity_tenant_event_consumption WHERE tenant_id = '$TenantId' ORDER BY processed_at DESC LIMIT 5;"
} else {
    "SELECT event_id, tenant_id, event_type, occurred_at, processed_at FROM identity_core.identity_tenant_event_consumption ORDER BY processed_at DESC LIMIT 5;"
}

docker exec -i chiroerp-postgres-app psql -U chiroerp_admin -d chiroerp -c $consumptionQuery 2>$null

# Database: Bootstrap Users
if ($TenantId) {
    Show-Section "Bootstrap Users for Tenant: $TenantId"
    docker exec -i chiroerp-postgres-app psql -U chiroerp_admin -d chiroerp -c "SELECT id, email, status, created_at FROM identity_core.identity_users WHERE tenant_id = '$TenantId' ORDER BY created_at;" 2>$null
} else {
    Show-Section "Recent Bootstrap Users"
    docker exec -i chiroerp-postgres-app psql -U chiroerp_admin -d chiroerp -c "SELECT id, tenant_id, email, status, created_at FROM identity_core.identity_users ORDER BY created_at DESC LIMIT 5;" 2>$null
}

# Recent Logs
Show-Section "Recent Identity-Core Logs (Event Processing)"
docker logs chiroerp-identity-core --tail 20 2>&1 | Select-String -Pattern "Bootstrap|TenantCreated|TenantActivated|event" | Select-Object -Last 10

# Footer with helpful commands
Write-Host ""
Write-Host ("=" * 80) -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Monitoring Tools:" -ForegroundColor Yellow
Write-Host "  • Redpanda Console (Web UI): " -NoNewline -ForegroundColor Cyan
Write-Host "http://localhost:8090" -ForegroundColor Green
Write-Host "  • Watch Kafka messages:      " -NoNewline -ForegroundColor Cyan
Write-Host ".\watch-kafka.ps1" -ForegroundColor Green
Write-Host "  • Kafka status:              " -NoNewline -ForegroundColor Cyan
Write-Host ".\monitor-kafka.ps1" -ForegroundColor Green
Write-Host "  • Monitor specific tenant:   " -NoNewline -ForegroundColor Cyan
Write-Host ".\monitor-event-flow.ps1 -TenantId <uuid>" -ForegroundColor Green
Write-Host ""
