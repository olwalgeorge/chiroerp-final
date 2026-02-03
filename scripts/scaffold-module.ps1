#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Scaffolds a new ChiroERP domain module with standard structure.

.DESCRIPTION
    Creates a new domain module with:
    - Standard directory structure (api, application, domain, infrastructure, events)
    - build.gradle.kts with Quarkus dependencies
    - application.yml configuration
    - Database migration folder
    - Test structure
    - README.md

.PARAMETER ModuleName
    The name of the module (e.g., "finance-domain", "sales-distribution-domain")

.PARAMETER PackageName
    The Java package name (e.g., "finance", "sales.distribution")

.PARAMETER DatabaseName
    The database name for this bounded context (e.g., "finance", "sales")

.PARAMETER Port
    The HTTP port for this module (e.g., 8081, 8082)

.EXAMPLE
    .\scaffold-module.ps1 -ModuleName "finance-domain" -PackageName "finance" -DatabaseName "finance" -Port 8081
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$ModuleName,
    
    [Parameter(Mandatory=$true)]
    [string]$PackageName,
    
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,
    
    [Parameter(Mandatory=$true)]
    [int]$Port
)

$ErrorActionPreference = "Stop"

# Color output functions
function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

# Validate inputs
if ($ModuleName -notmatch '^[a-z0-9-]+$') {
    Write-Error-Custom "Module name must contain only lowercase letters, numbers, and hyphens"
    exit 1
}

if ($PackageName -notmatch '^[a-z][a-z0-9.]*$') {
    Write-Error-Custom "Package name must contain only lowercase letters, numbers, and dots"
    exit 1
}

if ($Port -lt 8080 -or $Port -gt 9000) {
    Write-Error-Custom "Port must be between 8080 and 9000"
    exit 1
}

# Paths
$rootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$moduleDir = Join-Path $rootDir $ModuleName
$srcMainKotlin = Join-Path $moduleDir "src/main/kotlin/com/chiroerp/$($PackageName.Replace('.', '/'))"
$srcMainResources = Join-Path $moduleDir "src/main/resources"
$srcTestKotlin = Join-Path $moduleDir "src/test/kotlin/com/chiroerp/$($PackageName.Replace('.', '/'))"
$dbMigration = Join-Path $srcMainResources "db/migration"

Write-Info "Scaffolding module: $ModuleName"
Write-Info "Package: com.chiroerp.$PackageName"
Write-Info "Database: $DatabaseName"
Write-Info "Port: $Port"
Write-Host ""

# Check if module already exists
if (Test-Path $moduleDir) {
    Write-Warning "Module directory already exists: $moduleDir"
    $response = Read-Host "Do you want to overwrite? (yes/no)"
    if ($response -ne "yes") {
        Write-Info "Cancelled by user"
        exit 0
    }
    Remove-Item -Path $moduleDir -Recurse -Force
}

# Create directory structure
Write-Info "Creating directory structure..."

$directories = @(
    $moduleDir,
    "$srcMainKotlin/api",
    "$srcMainKotlin/application",
    "$srcMainKotlin/domain",
    "$srcMainKotlin/infrastructure",
    "$srcMainKotlin/events",
    $srcMainResources,
    $dbMigration,
    "$srcTestKotlin/api",
    "$srcTestKotlin/application",
    "$srcTestKotlin/domain",
    "$srcTestKotlin/infrastructure"
)

foreach ($dir in $directories) {
    New-Item -Path $dir -ItemType Directory -Force | Out-Null
}

Write-Success "Directory structure created"

# Create build.gradle.kts
Write-Info "Creating build.gradle.kts..."

$buildGradle = @"
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
}

dependencies {
    // Quarkus
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-kafka-client")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-opentelemetry")

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    // Platform Shared - Interfaces
    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:config-model"))
    implementation(project(":platform-shared:org-model"))
    implementation(project(":platform-shared:workflow-model"))

    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:kotlin-extensions")
    testImplementation(libs.mockk)
    testImplementation(libs.assertj.core)
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
"@

Set-Content -Path (Join-Path $moduleDir "build.gradle.kts") -Value $buildGradle
Write-Success "build.gradle.kts created"

# Create application.yml
Write-Info "Creating application.yml..."

$applicationYml = @"
# $ModuleName Configuration

quarkus:
  application:
    name: $ModuleName
  http:
    port: $Port
    cors:
      ~: true
      origins: "*"
      methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
      headers: accept,content-type,authorization,x-tenant-id
  
  datasource:
    db-kind: postgresql
    username: chiroerp
    password: dev_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/$DatabaseName
      max-size: 20
      min-size: 5
  
  hibernate-orm:
    database:
      generation: none
    log:
      sql: true
      format-sql: true
  
  flyway:
    migrate-at-start: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    table: flyway_schema_history
  
  kafka:
    bootstrap-servers: localhost:9092
  
  redis:
    hosts: redis://localhost:6379
  
  smallrye-openapi:
    path: /openapi
    info-title: $ModuleName API
    info-version: 1.0.0
    info-description: ChiroERP $ModuleName REST API
  
  swagger-ui:
    always-include: true
    path: /swagger-ui
  
  log:
    level: INFO
    category:
      "com.chiroerp.$PackageName":
        level: DEBUG

# Production profile
"%prod":
  quarkus:
    datasource:
      password: \${DB_PASSWORD}
      jdbc:
        url: jdbc:postgresql://\${DB_HOST}:\${DB_PORT:5432}/$DatabaseName
    kafka:
      bootstrap-servers: \${KAFKA_BOOTSTRAP_SERVERS}
    redis:
      hosts: redis://\${REDIS_HOST}:\${REDIS_PORT:6379}
"@

Set-Content -Path (Join-Path $srcMainResources "application.yml") -Value $applicationYml
Write-Success "application.yml created"

# Create sample domain entity
Write-Info "Creating sample domain entity..."

$packagePath = $PackageName.Replace('.', '/')
$domainEntityName = (Get-Culture).TextInfo.ToTitleCase($PackageName.Split('.')[-1]) + "Entity"

$domainEntity = @"
package com.chiroerp.$PackageName.domain

import java.time.Instant
import java.util.UUID

/**
 * Sample domain entity for $ModuleName
 * Replace this with your actual domain model
 */
data class $domainEntityName(
    val id: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val createdBy: UUID,
    val updatedAt: Instant = Instant.now(),
    val updatedBy: UUID,
    val version: Long = 0
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
    }

    fun update(
        name: String? = null,
        description: String? = null,
        isActive: Boolean? = null,
        updatedBy: UUID
    ): $domainEntityName {
        return copy(
            name = name ?: this.name,
            description = description ?: this.description,
            isActive = isActive ?: this.isActive,
            updatedAt = Instant.now(),
            updatedBy = updatedBy,
            version = version + 1
        )
    }
}
"@

Set-Content -Path (Join-Path $srcMainKotlin "domain/$domainEntityName.kt") -Value $domainEntity
Write-Success "Sample domain entity created"

# Create sample API resource
Write-Info "Creating sample API resource..."

$resourceName = (Get-Culture).TextInfo.ToTitleCase($PackageName.Split('.')[-1]) + "Resource"

$apiResource = @"
package com.chiroerp.$PackageName.api

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/v1/$($PackageName.Replace('.', '/'))")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class $resourceName {

    @GET
    fun list(): Response {
        // TODO: Implement list operation
        return Response.ok(emptyList<Any>()).build()
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: String): Response {
        // TODO: Implement get operation
        return Response.ok().build()
    }

    @POST
    fun create(request: Any): Response {
        // TODO: Implement create operation
        return Response.status(Response.Status.CREATED).build()
    }

    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: String, request: Any): Response {
        // TODO: Implement update operation
        return Response.ok().build()
    }

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: String): Response {
        // TODO: Implement delete operation
        return Response.noContent().build()
    }
}
"@

Set-Content -Path (Join-Path $srcMainKotlin "api/$resourceName.kt") -Value $apiResource
Write-Success "Sample API resource created"

# Create initial database migration
Write-Info "Creating initial database migration..."

$migrationSql = @"
-- Initial schema for $DatabaseName database
-- Created: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Sample table (replace with actual schema)
CREATE TABLE sample_entities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sample_tenant_name UNIQUE(tenant_id, name)
);

CREATE INDEX idx_sample_tenant ON sample_entities(tenant_id);
CREATE INDEX idx_sample_active ON sample_entities(is_active);
CREATE INDEX idx_sample_created ON sample_entities(created_at);

-- Audit log table
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')),
    old_values JSONB,
    new_values JSONB,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address INET,
    user_agent TEXT
);

CREATE INDEX idx_audit_tenant ON audit_log(tenant_id);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_changed_at ON audit_log(changed_at);

-- Add comments
COMMENT ON TABLE sample_entities IS 'Sample entities for $ModuleName - replace with actual domain model';
COMMENT ON TABLE audit_log IS 'Audit trail for all changes in $DatabaseName database';
"@

Set-Content -Path (Join-Path $dbMigration "V001__initial_schema.sql") -Value $migrationSql
Write-Success "Initial database migration created"

# Create README.md
Write-Info "Creating README.md..."

$readme = @"
# $ModuleName

## Overview

This module is part of the ChiroERP system, implementing the **$(($ModuleName -replace '-domain', '') | ForEach-Object { (Get-Culture).TextInfo.ToTitleCase($_) })** bounded context.

## Architecture

- **Package**: ``com.chiroerp.$PackageName``
- **Database**: ``$DatabaseName``
- **Port**: ``$Port``
- **Pattern**: CQRS + Event Sourcing (where applicable)

## Structure

``````
$ModuleName/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/chiroerp/$($PackageName.Replace('.', '/'))/
│   │   │       ├── api/          # REST API controllers
│   │   │       ├── application/  # Application services (commands/queries)
│   │   │       ├── domain/       # Domain entities, aggregates, value objects
│   │   │       ├── infrastructure/ # Repositories, external integrations
│   │   │       └── events/       # Domain events
│   │   └── resources/
│   │       ├── application.yml   # Quarkus configuration
│   │       └── db/migration/     # Flyway database migrations
│   └── test/
│       └── kotlin/
│           └── com/chiroerp/$($PackageName.Replace('.', '/'))/
│               ├── api/          # API tests (REST endpoints)
│               ├── application/  # Service tests
│               └── domain/       # Domain logic tests
└── build.gradle.kts              # Module build configuration
``````

## Development

### Running Locally

``````bash
# Start dependencies (PostgreSQL, Kafka, Redis)
docker-compose up -d

# Run in development mode (hot reload)
./gradlew :$ModuleName:quarkusDev

# Access Swagger UI
open http://localhost:$Port/swagger-ui
``````

### Building

``````bash
# Build module
./gradlew :$ModuleName:build

# Run tests
./gradlew :$ModuleName:test

# Run with coverage
./gradlew :$ModuleName:test jacocoTestReport
``````

### Database Migrations

``````bash
# Run migrations
./gradlew :$ModuleName:flywayMigrate

# View migration status
./gradlew :$ModuleName:flywayInfo

# Rollback (if needed)
./gradlew :$ModuleName:flywayUndo
``````

## API Endpoints

Base URL: ``http://localhost:$Port/api/v1/$($PackageName.Replace('.', '/'))``

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | ``/``    | List all entities |
| GET    | ``/{id}`` | Get entity by ID |
| POST   | ``/``    | Create new entity |
| PUT    | ``/{id}`` | Update entity |
| DELETE | ``/{id}`` | Delete entity |

## Domain Model

TODO: Document your domain model here

### Entities

- **TODO**: List your aggregates and entities

### Value Objects

- **TODO**: List your value objects

### Domain Events

- **TODO**: List domain events published by this module

## Integration

### Published Events

This module publishes the following events to Kafka:

- **TODO**: Document events

### Consumed Events

This module subscribes to the following events:

- **TODO**: Document event subscriptions

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| ``DB_HOST`` | PostgreSQL host | ``localhost`` | No |
| ``DB_PORT`` | PostgreSQL port | ``5432`` | No |
| ``DB_PASSWORD`` | Database password | ``dev_password`` | Yes (prod) |
| ``KAFKA_BOOTSTRAP_SERVERS`` | Kafka brokers | ``localhost:9092`` | No |
| ``REDIS_HOST`` | Redis host | ``localhost`` | No |

### Application Properties

See ``src/main/resources/application.yml`` for all configuration options.

## Testing

### Unit Tests

``````bash
./gradlew :$ModuleName:test
``````

### Integration Tests

``````bash
./gradlew :$ModuleName:integrationTest
``````

### API Tests

``````bash
./gradlew :$ModuleName:apiTest
``````

## Deployment

### Docker

``````bash
# Build Docker image
./gradlew :${ModuleName}:quarkusBuild -Dquarkus.container-image.build=true

# Run container
docker run -p ${Port}:${Port} ${ModuleName}:latest
``````

### Kubernetes

``````bash
# Deploy to Kubernetes
kubectl apply -f k8s/${ModuleName}/
``````

## Monitoring

### Health Check

- URL: ``http://localhost:$Port/q/health``
- Liveness: ``http://localhost:$Port/q/health/live``
- Readiness: ``http://localhost:$Port/q/health/ready``

### Metrics

- Prometheus: ``http://localhost:$Port/q/metrics``
- Application metrics: ``http://localhost:$Port/q/metrics/application``

### Tracing

- Jaeger UI: ``http://localhost:16686``

## Contributing

1. Follow the [Architecture Guide](../../docs/ARCHITECTURE_GUIDE.md)
2. Write tests (minimum 80% coverage)
3. Update this README with any changes
4. Submit pull request for review

## References

- [ADRs](../../docs/adr/)
- [API Standards](../../docs/API_STANDARDS.md)
- [Testing Guide](../../docs/TESTING_GUIDE.md)
"@

Set-Content -Path (Join-Path $moduleDir "README.md") -Value $readme
Write-Success "README.md created"

# Create .gitkeep files for empty directories
$gitkeepDirs = @(
    "$srcMainKotlin/application",
    "$srcMainKotlin/infrastructure",
    "$srcMainKotlin/events",
    "$srcTestKotlin/api",
    "$srcTestKotlin/application",
    "$srcTestKotlin/infrastructure"
)

foreach ($dir in $gitkeepDirs) {
    Set-Content -Path (Join-Path $dir ".gitkeep") -Value ""
}

# Update settings.gradle.kts to include new module
Write-Info "Updating settings.gradle.kts..."

$settingsFile = Join-Path $rootDir "settings.gradle.kts"
if (Test-Path $settingsFile) {
    $settingsContent = Get-Content $settingsFile -Raw
    if ($settingsContent -notmatch "include\(`"$ModuleName`"\)") {
        Add-Content -Path $settingsFile -Value "include(`"$ModuleName`")"
        Write-Success "Added module to settings.gradle.kts"
    } else {
        Write-Warning "Module already exists in settings.gradle.kts"
    }
} else {
    Write-Warning "settings.gradle.kts not found"
}

Write-Host ""
Write-Success "Module '$ModuleName' scaffolded successfully!"
Write-Host ""
Write-Info "Next steps:"
Write-Host "  1. Review and customize the generated files"
Write-Host "  2. Implement your domain model in src/main/kotlin/.../domain/"
Write-Host "  3. Create database migration in src/main/resources/db/migration/"
Write-Host "  4. Build module: ./gradlew :$ModuleName:build"
Write-Host "  5. Run in dev mode: ./gradlew :$ModuleName:quarkusDev"
Write-Host ""
Write-Info "Module endpoint: http://localhost:$Port/api/v1/$($PackageName.Replace('.', '/'))"
Write-Info "Swagger UI: http://localhost:$Port/swagger-ui"
Write-Host ""
