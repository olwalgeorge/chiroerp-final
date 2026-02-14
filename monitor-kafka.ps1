#!/usr/bin/env pwsh
# Kafka/Redpanda Monitoring Script

Write-Host "=== ChiroERP Kafka Monitoring ===" -ForegroundColor Cyan
Write-Host ""

# Check if containers are running
$redpanda = docker ps --filter "name=redpanda" --format "{{.Status}}"
if (-not $redpanda) {
    Write-Host "❌ Redpanda container is not running" -ForegroundColor Red
    Write-Host "Start with: docker compose up -d redpanda" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Redpanda is running: $redpanda" -ForegroundColor Green
Write-Host ""

# List all topics
Write-Host "=== Topics ===" -ForegroundColor Cyan
docker exec chiroerp-redpanda rpk topic list
Write-Host ""

# Show topic details
Write-Host "=== Topic: chiroerp.tenancy.events ===" -ForegroundColor Cyan
docker exec chiroerp-redpanda rpk topic describe chiroerp.tenancy.events
Write-Host ""

# Show consumer groups
Write-Host "=== Consumer Groups ===" -ForegroundColor Cyan
docker exec chiroerp-redpanda rpk group list
Write-Host ""

# Show consumer group details
Write-Host "=== Consumer Group: identity-core-tenancy-events ===" -ForegroundColor Cyan
docker exec chiroerp-redpanda rpk group describe identity-core-tenancy-events
Write-Host ""

# Consume last 10 messages from the topic
Write-Host "=== Last 10 Messages from chiroerp.tenancy.events ===" -ForegroundColor Cyan
Write-Host "(Press Ctrl+C to stop)" -ForegroundColor Yellow
docker exec chiroerp-redpanda rpk topic consume chiroerp.tenancy.events --num 10 --format json
