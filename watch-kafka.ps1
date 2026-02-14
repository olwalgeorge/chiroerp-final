#!/usr/bin/env pwsh
# Real-time Kafka message consumer - watches for new tenant events

param(
    [string]$Topic = "chiroerp.tenancy.events",
    [switch]$FromBeginning
)

Write-Host "=== Real-time Kafka Consumer ===" -ForegroundColor Cyan
Write-Host "Topic: $Topic" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""

$offsetFlag = if ($FromBeginning) { "--offset start" } else { "--offset end" }

# Consume messages in real-time with formatting
docker exec -it chiroerp-redpanda rpk topic consume $Topic $offsetFlag --format json | ForEach-Object {
    $timestamp = Get-Date -Format "HH:mm:ss"
    Write-Host "[$timestamp] " -NoNewline -ForegroundColor Gray
    
    # Parse and format the JSON message
    try {
        $msg = $_ | ConvertFrom-Json
        $payload = $msg.value | ConvertFrom-Json
        
        Write-Host "Event: " -NoNewline -ForegroundColor Cyan
        Write-Host $payload.eventType -NoNewline -ForegroundColor Yellow
        Write-Host " | Tenant: " -NoNewline -ForegroundColor Cyan
        Write-Host $payload.tenantId -ForegroundColor Green
        
        if ($payload.data) {
            Write-Host "  Data: " -NoNewline -ForegroundColor Gray
            Write-Host ($payload.data | ConvertTo-Json -Compress) -ForegroundColor White
        }
    }
    catch {
        Write-Host $_ -ForegroundColor White
    }
    
    Write-Host ""
}
