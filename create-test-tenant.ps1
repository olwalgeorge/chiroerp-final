# Create tenant and monitor Kafka event flow

Write-Host "=== Creating Tenant ===" -ForegroundColor Green

# Generate unique domain with timestamp
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()

$tenantData = @{
    name = "Acme Chiropractic Clinic"
    domain = "acme-$timestamp.chiroerp.local"
    tier = "STANDARD"
    dataResidencyCountry = "US"
    locale = "en_US"
    timezone = "America/New_York"
    currency = "USD"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8071/api/tenants" `
        -Method POST `
        -Headers @{
            "Content-Type" = "application/json"
            "X-Tenant-ID" = "platform"
        } `
        -Body $tenantData
    
    Write-Host "✅ Tenant created successfully!" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 5)
    
    Write-Host "`n=== Checking Tenancy-Core Logs ===" -ForegroundColor Cyan
    docker logs chiroerp-tenancy-core --tail 20 --since 10s
    
    Write-Host "`n=== Waiting 3 seconds for Kafka event processing ===" -ForegroundColor Yellow
    Start-Sleep -Seconds 3
    
    Write-Host "`n=== Checking Identity-Core Logs ===" -ForegroundColor Cyan
    docker logs chiroerp-identity-core --tail 20 --since 10s
    
} catch {
    Write-Host "❌ Error creating tenant:" -ForegroundColor Red
    Write-Host $_.Exception.Message
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    }
}
