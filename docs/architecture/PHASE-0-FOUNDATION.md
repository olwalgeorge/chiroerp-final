# Phase 0: Development Infrastructure Foundation

**Status**: In Progress  
**Priority**: P0 (BLOCKING - Must complete before Phase 1)  
**Duration**: 2 weeks  
**Investment**: $150K  
**Team**: 2-3 platform engineers  
**Start Date**: February 3, 2026  
**Target Completion**: February 17, 2026

---

## Executive Summary

### The Gap We're Filling

The roadmap assumes a working development environment, but we currently have:
- ✅ Build system validated (Gradle 9.0 + Quarkus 3.31.1 + Java 21)
- ✅ Architecture documented (57 ADRs)
- ❌ **Zero domain modules implemented**
- ❌ **No local development infrastructure**
- ❌ **No CI/CD pipeline for Kotlin code**
- ❌ **No database schema or migrations**

**This phase bridges the gap between "Gradle builds work" → "We can write business logic"**

### Success Criteria

| Criterion | Acceptance Test | Success Metric |
|-----------|-----------------|----------------|
| **Module Structure Complete** | All 12 domain modules scaffolded | 100% modules build successfully |
| **Local Dev Stack Operational** | Docker Compose up → all services healthy | PostgreSQL + Kafka + Redis + Temporal running |
| **CI/CD Pipeline Working** | Push to main → build → test → deploy to staging | < 10 min pipeline duration |
| **Database Migrations** | Flyway migrations run successfully | Zero migration errors |
| **First Module Working** | Finance GL CRUD operations complete | Create/Read/Update/Delete GL accounts |
| **Team Onboarded** | 6-8 engineers productive on Day 1 of Phase 1 | IDE setup < 30 min, first PR < 2 hours |

---

## Week 1: Infrastructure & Module Scaffolding

### Day 1-2: Module Structure Creation

**Goal**: Create all 12 domain modules with consistent structure

**Deliverables**:
- [ ] Module scaffolding script (`scripts/scaffold-module.ps1`)
- [ ] 12 domain modules created with standard structure:
  - `finance-domain/` (General Ledger, Accounts Payable/Receivable)
  - `controlling-domain/` (Cost Centers, Profit Centers, Internal Orders)
  - `sales-distribution-domain/` (Sales Orders, Pricing, Delivery)
  - `inventory-management-domain/` (Material Master, Stock Management, Warehouse)
  - `procurement-domain/` (Purchase Orders, Vendor Management, Sourcing)
  - `production-planning-domain/` (Work Orders, BOM, Production Scheduling)
  - `quality-management-domain/` (Quality Plans, Inspections, Certificates)
  - `plant-maintenance-domain/` (Equipment, Work Orders, Preventive Maintenance)
  - `crm-domain/` (Accounts, Contacts, Opportunities)
  - `master-data-domain/` (Business Partners, Products, Locations)
  - `platform-shared/` (Configuration, Org Model, Workflow, Events)
  - `localization/` (Country Packs, Tax Rules, Regulatory Compliance)

**Standard Module Structure**:
```
<domain-name>/
├── build.gradle.kts              # Module build config
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/chiroerp/<domain>/
│   │   │       ├── api/          # REST API controllers
│   │   │       ├── application/  # Application services (CQRS commands/queries)
│   │   │       ├── domain/       # Domain entities, aggregates, value objects
│   │   │       ├── infrastructure/ # Repositories, external integrations
│   │   │       └── events/       # Domain events
│   │   └── resources/
│   │       ├── application.yml   # Quarkus config
│   │       └── db/migration/     # Flyway migrations
│   └── test/
│       └── kotlin/
│           └── com/chiroerp/<domain>/
│               ├── api/          # API tests
│               ├── application/  # Service tests
│               └── domain/       # Domain logic tests
└── README.md                     # Module documentation
```

**Success Metric**: `./gradlew buildAll` builds all 12 modules successfully

---

### Day 3-4: Docker Compose Development Stack

**Goal**: One-command local development environment

**Deliverables**:
- [ ] `docker-compose.yml` (PostgreSQL, Kafka, Redis, Temporal, Jaeger)
- [ ] `docker-compose.override.yml` (local dev overrides)
- [ ] `.env.local` template
- [ ] `scripts/dev-setup.ps1` (Windows)
- [ ] `scripts/dev-setup.sh` (Linux/Mac)
- [ ] Database initialization scripts

**Docker Compose Services**:

```yaml
services:
  # PostgreSQL - Database per bounded context
  postgres-finance:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: finance
      POSTGRES_USER: chiroerp
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-finance-data:/var/lib/postgresql/data
      - ./database/init/finance:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U chiroerp"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-sales:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: sales
      POSTGRES_USER: chiroerp
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5433:5432"
    volumes:
      - postgres-sales-data:/var/lib/postgresql/data

  # Add postgres instances for: inventory, procurement, production, etc.

  # Kafka + Zookeeper - Event streaming
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    ports:
      - "9092:9092"
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server=localhost:9092"]
      interval: 10s
      timeout: 10s
      retries: 5

  # Redis - Caching & session management
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Temporal - Workflow engine
  temporal:
    image: temporalio/auto-setup:1.22.4
    environment:
      - DB=postgresql
      - DB_PORT=5432
      - POSTGRES_USER=temporal
      - POSTGRES_PWD=temporal
      - POSTGRES_SEEDS=postgres-temporal
    ports:
      - "7233:7233"  # gRPC
      - "8233:8233"  # Web UI
    depends_on:
      postgres-temporal:
        condition: service_healthy

  postgres-temporal:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: temporal
      POSTGRES_USER: temporal
      POSTGRES_PASSWORD: temporal
    ports:
      - "5434:5432"
    volumes:
      - postgres-temporal-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U temporal"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Jaeger - Distributed tracing
  jaeger:
    image: jaegertracing/all-in-one:1.53
    environment:
      COLLECTOR_OTLP_ENABLED: "true"
    ports:
      - "16686:16686"  # Web UI
      - "4317:4317"    # OTLP gRPC
      - "4318:4318"    # OTLP HTTP

  # Prometheus - Metrics collection
  prometheus:
    image: prom/prometheus:v2.48.1
    volumes:
      - ./observability/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'

  # Grafana - Metrics visualization
  grafana:
    image: grafana/grafana:10.2.3
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - ./observability/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./observability/grafana/datasources:/etc/grafana/provisioning/datasources
      - grafana-data:/var/lib/grafana
    ports:
      - "3000:3000"

volumes:
  postgres-finance-data:
  postgres-sales-data:
  postgres-temporal-data:
  prometheus-data:
  grafana-data:
```

**Success Metric**: `docker-compose up -d` → all services healthy within 2 minutes

---

### Day 5: CI/CD Pipeline (GitHub Actions)

**Goal**: Automated build → test → deploy on every commit

**Deliverables**:
- [ ] `.github/workflows/ci-build.yml` (build + test all modules)
- [ ] `.github/workflows/ci-integration.yml` (integration tests with Docker services)
- [ ] `.github/workflows/cd-staging.yml` (deploy to staging environment)
- [ ] `.github/workflows/cd-production.yml` (deploy to production)

**CI Build Pipeline** (`.github/workflows/ci-build.yml`):

```yaml
name: CI - Build & Test

on:
  push:
    branches: [main, develop, 'feature/**']
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        java: [21]
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ matrix.java }}
          cache: 'gradle'
      
      - name: Validate Gradle wrapper
        uses: gradle/wrapper-validation-action@v2
      
      - name: Build all modules
        run: ./gradlew buildAll --no-daemon --configuration-cache
      
      - name: Run unit tests
        run: ./gradlew test --no-daemon
      
      - name: Generate test report
        if: always()
        uses: dorny/test-reporter@v1
        with:
          name: Test Results
          path: '**/build/test-results/test/*.xml'
          reporter: java-junit
      
      - name: Upload build artifacts
        if: success()
        uses: actions/upload-artifact@v4
        with:
          name: build-artifacts
          path: |
            **/build/libs/*.jar
            **/build/quarkus-app/**
          retention-days: 7
      
      - name: Code coverage
        run: ./gradlew jacocoTestReport
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: '**/build/reports/jacoco/test/jacocoTestReport.xml'
          flags: unittests
          name: codecov-chiroerp

  integration-tests:
    runs-on: ubuntu-latest
    needs: build
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
      
      kafka:
        image: confluentinc/cp-kafka:7.6.0
        env:
          KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
          KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
        ports:
          - 9092:9092
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: 21
          cache: 'gradle'
      
      - name: Run integration tests
        run: ./gradlew integrationTest --no-daemon
        env:
          POSTGRES_HOST: localhost
          POSTGRES_PORT: 5432
          KAFKA_BOOTSTRAP_SERVERS: localhost:9092
      
      - name: Generate integration test report
        if: always()
        uses: dorny/test-reporter@v1
        with:
          name: Integration Test Results
          path: '**/build/test-results/integrationTest/*.xml'
          reporter: java-junit

  validate-architecture:
    runs-on: ubuntu-latest
    needs: build
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: 21
          cache: 'gradle'
      
      - name: Validate architecture rules
        run: ./gradlew validateArchitecture --no-daemon
      
      - name: Validate documentation
        run: pwsh -File scripts/validate-docs.ps1

  security-scan:
    runs-on: ubuntu-latest
    needs: build
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: 21
          cache: 'gradle'
      
      - name: Dependency check
        run: ./gradlew dependencyCheckAnalyze --no-daemon
      
      - name: Upload security report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: security-report
          path: build/reports/dependency-check-report.html
```

**Success Metric**: Pipeline runs in < 10 minutes, all checks pass

---

## Week 2: Database Schema & First Module Implementation

### Day 6-7: Database Schema Design & Migrations

**Goal**: Database-per-context with Flyway migrations

**Deliverables**:
- [ ] Flyway configuration in each module
- [ ] Initial schema migrations for all domains
- [ ] Seed data scripts for development
- [ ] Database migration testing framework

**Finance Domain Schema** (`finance-domain/src/main/resources/db/migration/V001__initial_schema.sql`):

```sql
-- General Ledger Accounts
CREATE TABLE gl_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    account_type VARCHAR(50) NOT NULL CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE')),
    balance_type VARCHAR(10) NOT NULL CHECK (balance_type IN ('DEBIT', 'CREDIT')),
    parent_account_id UUID REFERENCES gl_accounts(id),
    currency_code CHAR(3) NOT NULL DEFAULT 'USD',
    is_control_account BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE(tenant_id, account_number)
);

CREATE INDEX idx_gl_accounts_tenant ON gl_accounts(tenant_id);
CREATE INDEX idx_gl_accounts_parent ON gl_accounts(parent_account_id);
CREATE INDEX idx_gl_accounts_type ON gl_accounts(account_type);

-- GL Journal Entries
CREATE TABLE gl_journal_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    journal_entry_number VARCHAR(50) NOT NULL,
    posting_date DATE NOT NULL,
    document_date DATE NOT NULL,
    fiscal_year INTEGER NOT NULL,
    fiscal_period INTEGER NOT NULL CHECK (fiscal_period BETWEEN 1 AND 16),
    document_type VARCHAR(20) NOT NULL,
    reference_number VARCHAR(100),
    description TEXT,
    currency_code CHAR(3) NOT NULL,
    total_debit DECIMAL(19,4) NOT NULL,
    total_credit DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED')),
    posted_at TIMESTAMP,
    posted_by UUID,
    reversed_at TIMESTAMP,
    reversed_by UUID,
    reversal_journal_entry_id UUID REFERENCES gl_journal_entries(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_balanced CHECK (total_debit = total_credit),
    UNIQUE(tenant_id, journal_entry_number)
);

CREATE INDEX idx_je_tenant ON gl_journal_entries(tenant_id);
CREATE INDEX idx_je_posting_date ON gl_journal_entries(posting_date);
CREATE INDEX idx_je_fiscal ON gl_journal_entries(fiscal_year, fiscal_period);
CREATE INDEX idx_je_status ON gl_journal_entries(status);

-- GL Journal Entry Lines
CREATE TABLE gl_journal_entry_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id UUID NOT NULL REFERENCES gl_journal_entries(id) ON DELETE CASCADE,
    line_number INTEGER NOT NULL,
    account_id UUID NOT NULL REFERENCES gl_accounts(id),
    debit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    credit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    cost_center_id UUID,
    profit_center_id UUID,
    internal_order_id UUID,
    project_id UUID,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_debit_or_credit CHECK (
        (debit_amount > 0 AND credit_amount = 0) OR 
        (credit_amount > 0 AND debit_amount = 0)
    ),
    UNIQUE(journal_entry_id, line_number)
);

CREATE INDEX idx_jel_journal_entry ON gl_journal_entry_lines(journal_entry_id);
CREATE INDEX idx_jel_account ON gl_journal_entry_lines(account_id);
CREATE INDEX idx_jel_cost_center ON gl_journal_entry_lines(cost_center_id);

-- Account Balances (Materialized View for Performance)
CREATE TABLE gl_account_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    account_id UUID NOT NULL REFERENCES gl_accounts(id),
    fiscal_year INTEGER NOT NULL,
    fiscal_period INTEGER NOT NULL,
    beginning_balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    period_debits DECIMAL(19,4) NOT NULL DEFAULT 0,
    period_credits DECIMAL(19,4) NOT NULL DEFAULT 0,
    ending_balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, account_id, fiscal_year, fiscal_period)
);

CREATE INDEX idx_balances_tenant ON gl_account_balances(tenant_id);
CREATE INDEX idx_balances_account ON gl_account_balances(account_id);
CREATE INDEX idx_balances_fiscal ON gl_account_balances(fiscal_year, fiscal_period);

-- Audit Trail
CREATE TABLE gl_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'POST', 'REVERSE')),
    old_values JSONB,
    new_values JSONB,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address INET,
    user_agent TEXT
);

CREATE INDEX idx_audit_tenant ON gl_audit_log(tenant_id);
CREATE INDEX idx_audit_entity ON gl_audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_changed_at ON gl_audit_log(changed_at);
```

**Success Metric**: All Flyway migrations run successfully on fresh database

---

### Day 8-10: First Module Implementation (Finance GL)

**Goal**: Complete CRUD operations for GL Accounts with tests

**Deliverables**:
- [ ] Domain entities (GLAccount, JournalEntry, JournalEntryLine)
- [ ] Repository implementations (Panache/Hibernate)
- [ ] Application services (CreateGLAccount, UpdateGLAccount, etc.)
- [ ] REST API endpoints
- [ ] Unit tests (domain logic)
- [ ] Integration tests (database operations)
- [ ] API tests (REST endpoints)

**Domain Entity** (`finance-domain/src/main/kotlin/com/chiroerp/finance/domain/GLAccount.kt`):

```kotlin
package com.chiroerp.finance.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class GLAccount(
    val id: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val accountNumber: String,
    val accountName: String,
    val accountType: AccountType,
    val balanceType: BalanceType,
    val parentAccountId: UUID? = null,
    val currencyCode: String = "USD",
    val isControlAccount: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val createdBy: UUID,
    val updatedAt: Instant = Instant.now(),
    val updatedBy: UUID,
    val version: Long = 0
) {
    init {
        require(accountNumber.isNotBlank()) { "Account number cannot be blank" }
        require(accountName.isNotBlank()) { "Account name cannot be blank" }
        require(currencyCode.length == 3) { "Currency code must be 3 characters" }
    }

    fun update(
        accountName: String? = null,
        isActive: Boolean? = null,
        updatedBy: UUID
    ): GLAccount {
        return copy(
            accountName = accountName ?: this.accountName,
            isActive = isActive ?: this.isActive,
            updatedAt = Instant.now(),
            updatedBy = updatedBy,
            version = version + 1
        )
    }

    fun canBeDeleted(): Boolean {
        // Business rule: Control accounts and accounts with children cannot be deleted
        return !isControlAccount && parentAccountId != null
    }
}

enum class AccountType {
    ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
}

enum class BalanceType {
    DEBIT, CREDIT
}
```

**Repository** (`finance-domain/src/main/kotlin/com/chiroerp/finance/infrastructure/GLAccountRepository.kt`):

```kotlin
package com.chiroerp.finance.infrastructure

import com.chiroerp.finance.domain.GLAccount
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class GLAccountRepository : PanacheRepository<GLAccountEntity> {
    
    fun findByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): GLAccount? {
        return find("tenantId = ?1 and accountNumber = ?2", tenantId, accountNumber)
            .firstResult()
            ?.toDomain()
    }

    fun findAllByTenant(tenantId: UUID): List<GLAccount> {
        return list("tenantId = ?1 order by accountNumber", tenantId)
            .map { it.toDomain() }
    }

    fun findActiveByTenant(tenantId: UUID): List<GLAccount> {
        return list("tenantId = ?1 and isActive = true order by accountNumber", tenantId)
            .map { it.toDomain() }
    }

    fun existsByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): Boolean {
        return count("tenantId = ?1 and accountNumber = ?2", tenantId, accountNumber) > 0
    }

    fun save(account: GLAccount): GLAccount {
        val entity = GLAccountEntity.fromDomain(account)
        persist(entity)
        return entity.toDomain()
    }

    fun update(account: GLAccount): GLAccount {
        val entity = findById(account.id) ?: throw AccountNotFoundException(account.id)
        entity.updateFromDomain(account)
        return entity.toDomain()
    }
}
```

**Application Service** (`finance-domain/src/main/kotlin/com/chiroerp/finance/application/CreateGLAccountCommand.kt`):

```kotlin
package com.chiroerp.finance.application

import com.chiroerp.finance.domain.AccountType
import com.chiroerp.finance.domain.BalanceType
import com.chiroerp.finance.domain.GLAccount
import com.chiroerp.finance.infrastructure.GLAccountRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

data class CreateGLAccountCommand(
    val tenantId: UUID,
    val accountNumber: String,
    val accountName: String,
    val accountType: AccountType,
    val balanceType: BalanceType,
    val parentAccountId: UUID? = null,
    val currencyCode: String = "USD",
    val createdBy: UUID
)

@ApplicationScoped
class CreateGLAccountService(
    private val repository: GLAccountRepository
) {
    
    @Transactional
    fun execute(command: CreateGLAccountCommand): GLAccount {
        // Business rule: Account number must be unique per tenant
        if (repository.existsByTenantAndAccountNumber(command.tenantId, command.accountNumber)) {
            throw AccountNumberAlreadyExistsException(command.accountNumber)
        }

        // Business rule: Parent account must exist
        if (command.parentAccountId != null) {
            val parent = repository.findById(command.parentAccountId)
                ?: throw ParentAccountNotFoundException(command.parentAccountId)
            
            // Business rule: Parent must be same account type
            if (parent.accountType != command.accountType) {
                throw InvalidParentAccountTypeException(command.accountType, parent.accountType)
            }
        }

        val account = GLAccount(
            tenantId = command.tenantId,
            accountNumber = command.accountNumber,
            accountName = command.accountName,
            accountType = command.accountType,
            balanceType = command.balanceType,
            parentAccountId = command.parentAccountId,
            currencyCode = command.currencyCode,
            createdBy = command.createdBy,
            updatedBy = command.createdBy
        )

        return repository.save(account)
    }
}
```

**REST API** (`finance-domain/src/main/kotlin/com/chiroerp/finance/api/GLAccountResource.kt`):

```kotlin
package com.chiroerp.finance.api

import com.chiroerp.finance.application.CreateGLAccountCommand
import com.chiroerp.finance.application.CreateGLAccountService
import com.chiroerp.finance.domain.AccountType
import com.chiroerp.finance.domain.BalanceType
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/api/v1/finance/gl-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GLAccountResource(
    private val createGLAccountService: CreateGLAccountService
) {

    @POST
    fun createAccount(request: CreateGLAccountRequest): Response {
        val command = CreateGLAccountCommand(
            tenantId = UUID.fromString(request.tenantId),
            accountNumber = request.accountNumber,
            accountName = request.accountName,
            accountType = AccountType.valueOf(request.accountType),
            balanceType = BalanceType.valueOf(request.balanceType),
            parentAccountId = request.parentAccountId?.let { UUID.fromString(it) },
            currencyCode = request.currencyCode ?: "USD",
            createdBy = UUID.fromString(request.createdBy)
        )

        val account = createGLAccountService.execute(command)
        return Response.status(Response.Status.CREATED).entity(account).build()
    }

    @GET
    fun listAccounts(
        @QueryParam("tenantId") tenantId: String,
        @QueryParam("activeOnly") activeOnly: Boolean = true
    ): Response {
        // Implementation
        return Response.ok().build()
    }

    @GET
    @Path("/{id}")
    fun getAccount(@PathParam("id") id: String): Response {
        // Implementation
        return Response.ok().build()
    }

    @PUT
    @Path("/{id}")
    fun updateAccount(@PathParam("id") id: String, request: UpdateGLAccountRequest): Response {
        // Implementation
        return Response.ok().build()
    }
}

data class CreateGLAccountRequest(
    val tenantId: String,
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val balanceType: String,
    val parentAccountId: String? = null,
    val currencyCode: String? = "USD",
    val createdBy: String
)
```

**Success Metric**: Finance GL module has 80%+ test coverage, all CRUD operations work

---

### Day 11-12: Team Onboarding & Documentation

**Goal**: 6-8 engineers productive on Day 1 of Phase 1

**Deliverables**:
- [ ] Developer onboarding guide (`docs/DEVELOPER_SETUP.md`)
- [ ] Architecture walkthrough (`docs/ARCHITECTURE_GUIDE.md`)
- [ ] Module development guide (`docs/MODULE_DEVELOPMENT.md`)
- [ ] API standards guide (`docs/API_STANDARDS.md`)
- [ ] Testing guide (`docs/TESTING_GUIDE.md`)
- [ ] IDE setup scripts (IntelliJ IDEA, VS Code)
- [ ] Team training sessions (2x 2-hour sessions)

**Developer Setup Guide** (`docs/DEVELOPER_SETUP.md`):

```markdown
# Developer Setup Guide

## Prerequisites

- **Java 21** (Eclipse Adoptium 21.0.9+10-LTS)
- **Docker Desktop** 4.26+ (for local development stack)
- **Git** 2.40+
- **IDE**: IntelliJ IDEA Ultimate 2024.3+ or VS Code with Kotlin extension

## Quick Start (< 30 minutes)

### 1. Clone Repository

git clone https://github.com/your-org/chiroerp.git
cd chiroerp

### 2. Run Setup Script

**Windows (PowerShell)**:
.\scripts\dev-setup.ps1

**Linux/Mac**:
./scripts/dev-setup.sh

This script will:
- Verify Java 21 installation
- Start Docker Compose services
- Run database migrations
- Build all modules
- Run tests
- Generate IDE project files

### 3. Verify Setup

# Check all services are running
docker-compose ps

# Run build
.\gradlew buildAll

# Run tests
.\gradlew test

### 4. Open in IDE

**IntelliJ IDEA**:
1. Open `chiroerp` folder
2. Trust Gradle project
3. Wait for indexing to complete
4. Run `finance-domain` module to verify setup

**VS Code**:
1. Open `chiroerp` folder
2. Install recommended extensions (prompt will appear)
3. Trust workspace
4. Run task: `Build All Modules`

## Your First Contribution (< 2 hours)

1. **Create feature branch**: `git checkout -b feature/your-name-first-task`
2. **Pick starter task**: See `docs/STARTER_TASKS.md`
3. **Write code**: Follow patterns in existing modules
4. **Write tests**: Minimum 80% coverage
5. **Run validation**: `.\gradlew test validateArchitecture`
6. **Create PR**: Push and create pull request

## Development Workflow

### Running Services Locally

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Reset databases
docker-compose down -v
docker-compose up -d

### Building Modules

# Build all modules
.\gradlew buildAll

# Build specific module
.\gradlew :finance-domain:build

# Clean build
.\gradlew clean buildAll

### Running Tests

# All tests
.\gradlew test

# Specific module
.\gradlew :finance-domain:test

# Integration tests
.\gradlew integrationTest

# Test with coverage
.\gradlew test jacocoTestReport

### Database Migrations

# Run migrations
.\gradlew flywayMigrate

# Rollback migration
.\gradlew flywayUndo

# View migration status
.\gradlew flywayInfo

### API Testing

# Start Quarkus in dev mode (hot reload)
.\gradlew :finance-domain:quarkusDev

# Access Swagger UI
http://localhost:8080/q/swagger-ui

# Test API endpoint
curl -X POST http://localhost:8080/api/v1/finance/gl-accounts \
  -H "Content-Type: application/json" \
  -d '{...}'

## Troubleshooting

### "JAVA_HOME not set"
- Verify Java 21 installation: `java -version`
- Set JAVA_HOME environment variable

### "Docker services not starting"
- Ensure Docker Desktop is running
- Check port conflicts: `netstat -an | findstr "5432"`
- Reset Docker: `docker-compose down -v && docker-compose up -d`

### "Build fails with compilation errors"
- Update Gradle: `.\gradlew wrapper --gradle-version=9.0`
- Clean build: `.\gradlew clean buildAll`
- Invalidate IDE caches (IntelliJ: File → Invalidate Caches)

### "Tests failing"
- Ensure Docker services are running
- Check database connectivity
- Review test logs: `build/reports/tests/test/index.html`

## Next Steps

- Read [Architecture Guide](ARCHITECTURE_GUIDE.md)
- Review [Module Development Guide](MODULE_DEVELOPMENT.md)
- Join daily standup (9 AM EAT)
- Join #chiroerp-dev Slack channel
```

**Success Metric**: New engineer can make first commit within 2 hours of onboarding

---

## Phase 0 Exit Criteria

| Criterion | Target | Current | Status |
|-----------|--------|---------|--------|
| **Module Structure** | 12 modules building | 0/12 | ❌ |
| **Docker Services** | 8 services healthy | 0/8 | ❌ |
| **CI/CD Pipeline** | < 10 min build time | N/A | ❌ |
| **Database Migrations** | All migrations successful | 0/12 | ❌ |
| **First Module (Finance GL)** | CRUD operations + 80% coverage | 0% | ❌ |
| **Team Onboarding** | 6-8 engineers ready | 0/8 | ❌ |
| **Developer Setup Time** | < 30 minutes | N/A | ❌ |

**GO Decision**: All criteria must be ✅ before starting Phase 1

---

## Resource Requirements

### Team

| Role | Count | Daily Rate | Duration | Total |
|------|-------|------------|----------|-------|
| Platform Engineer (Lead) | 1 | $1,000 | 10 days | $10,000 |
| Backend Engineer | 2 | $800 | 10 days | $16,000 |
| DevOps Engineer | 1 | $900 | 10 days | $9,000 |
| QA Engineer | 1 | $700 | 5 days | $3,500 |

**Total Team Cost**: $38,500

### Infrastructure

| Service | Monthly Cost | Duration | Total |
|---------|--------------|----------|-------|
| AWS EC2 (staging) | $500 | 0.5 months | $250 |
| AWS RDS (PostgreSQL) | $300 | 0.5 months | $150 |
| AWS MSK (Kafka) | $400 | 0.5 months | $200 |
| GitHub Actions (CI/CD) | $200 | 0.5 months | $100 |
| Docker Hub | $0 | N/A | $0 |

**Total Infrastructure Cost**: $700

### Tools & Services

| Service | Cost |
|---------|------|
| IntelliJ IDEA licenses (8) | $2,400 |
| JetBrains TeamCity | $1,000 |
| Confluence (documentation) | $500 |
| Slack (communication) | $0 (free tier) |

**Total Tools Cost**: $3,900

### Contingency

| Category | Amount |
|----------|--------|
| Risk buffer (20%) | $8,620 |
| Scope changes | $5,000 |

**Total Contingency**: $13,620

---

## Total Phase 0 Investment

| Category | Cost |
|----------|------|
| Team | $38,500 |
| Infrastructure | $700 |
| Tools | $3,900 |
| Contingency | $13,620 |
| **TOTAL** | **$56,720** |

**Rounded**: **$60K** (vs original estimate of $150K - we're efficient!)

---

## Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Docker compatibility issues (Windows/Mac)** | 40% | HIGH | Test on all platforms, provide VM alternative |
| **Java 21 adoption challenges** | 30% | MEDIUM | Training sessions, pair programming |
| **Database migration conflicts** | 50% | HIGH | Schema versioning, rollback testing |
| **CI/CD pipeline failures** | 35% | HIGH | Manual deployment fallback, pipeline monitoring |
| **Team onboarding delays** | 25% | MEDIUM | Starter tasks, buddy system, daily check-ins |

---

## Success Metrics (Phase 0 → Phase 1 Handoff)

✅ **Technical Readiness**:
- 12 domain modules building with zero errors
- Docker Compose stack running all 8 services
- CI/CD pipeline deploying to staging automatically
- Finance GL module demonstrating full CRUD operations

✅ **Team Readiness**:
- 6-8 engineers onboarded and productive
- < 30 minutes developer setup time
- First PR submitted within 2 hours of onboarding

✅ **Documentation Complete**:
- Developer setup guide
- Architecture walkthrough
- Module development guide
- API standards guide
- Testing guide

✅ **Infrastructure Validated**:
- All database migrations successful
- Kafka topics created
- Redis caching working
- Temporal workflows executable
- Distributed tracing operational (Jaeger)

**Phase 1 can start when**: All exit criteria are ✅ and team is confident in development workflow

---

## Timeline

```
Week 1: Infrastructure & Module Scaffolding
├─ Day 1-2: Module structure creation (scaffold-module.ps1)
├─ Day 3-4: Docker Compose stack (PostgreSQL/Kafka/Redis/Temporal)
└─ Day 5: CI/CD pipeline (GitHub Actions)

Week 2: Database Schema & First Module Implementation
├─ Day 6-7: Database migrations (Flyway schemas for all domains)
├─ Day 8-10: Finance GL module (CRUD + tests)
└─ Day 11-12: Team onboarding (docs + training)

Phase 0 Complete → Phase 1 Starts (Week 3)
```

---

## Next Steps

1. **Review & Approve**: Stakeholder sign-off on Phase 0 scope
2. **Team Assignment**: Allocate 2-3 platform engineers
3. **Kickoff**: February 3, 2026 (TODAY!)
4. **Daily Standups**: 9 AM EAT, 15-minute check-ins
5. **Phase Gate Review**: February 17, 2026 (Go/No-Go decision)

**Phase 1 cannot start until Phase 0 is complete. This is non-negotiable.**
