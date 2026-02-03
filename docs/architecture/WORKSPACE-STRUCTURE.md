# ChiroERP - Complete Workspace File Tree Structure

> **Based on**: 57+ ADRs (ADR-001 through ADR-057) | 12 Major Domains | 92 Modules  
> **Last Updated**: 2026-02-03  
> **Architecture Principles**: Modular CQRS (ADR-001), Database-per-Context (ADR-002), Event-Driven (ADR-003), API Gateway (ADR-004)  
> **Status**: Target workspace structure (see [Architecture README](./README.md) for current implementation status)

> ⚠️ **Important**: This document shows the **target/desired workspace structure** for complete ChiroERP implementation. For the **current architecture index** with actual domain coverage, module counts, ports, and implementation status, see [docs/architecture/README.md](./README.md).

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture Principles](#architecture-principles)
3. [Complete Directory Structure](#complete-directory-structure)
4. [Bounded Contexts & Microservices](#bounded-contexts--microservices)
5. [Shared Libraries](#shared-libraries)
6. [Infrastructure & Platform](#infrastructure--platform)
7. [Frontend Applications](#frontend-applications)
8. [Industry Extensions](#industry-extensions)
9. [Deployment Configurations](#deployment-configurations)
10. [Technology Stack](#technology-stack)

---

## Overview

ChiroERP is a **cloud-native, microservices-based ERP system** designed for multi-tenant SaaS deployment with optional on-premise support. The architecture supports:

- **Domain Coverage**: 92 modules across 12 domains (Finance, Inventory, Sales, Manufacturing, Quality, Maintenance, CRM, MDG, Analytics, HCM, Fleet, Procurement)
- **Industry Extensions**: 10+ verticals (Banking, Process Manufacturing, Utilities, Public Sector, Insurance, Real Estate, Fleet Management, Retail AI, etc.)
- **Recent Enhancements**: AI Demand Forecasting (ADR-056), Dynamic Pricing (ADR-057), HCM modules (ADR-052, 054, 055), Fleet Management (ADR-053)
- **Two Deployment Modes**: SMB (Docker Compose bundled) | Enterprise (Kubernetes distributed)
- **Event-Driven Integration**: Kafka for async communication (ADR-003, ADR-020)
- **CQRS Pattern**: Command/Query separation with event sourcing support (ADR-001)

> For **current implementation status** and actual module counts per domain, see [Architecture README](./README.md).

---

## Architecture Principles

### Core ADRs Driving Structure

| ADR | Principle | Impact on Structure |
|-----|-----------|---------------------|
| **ADR-001** | Modular CQRS | Separate command/query handlers per bounded context |
| **ADR-002** | Database-per-Context | Each microservice owns its database schema |
| **ADR-003** | Event-Driven Integration | Kafka event streams, Avro schemas in `platform-events` |
| **ADR-004** | API Gateway Pattern | Single entry point via `api-gateway` service |
| **ADR-005** | Multi-Tenancy Isolation | Tenant discriminator in all aggregates |
| **ADR-006** | Platform-Shared Governance | Strict rules for `platform-shared` modules (technical only) |
| **ADR-044** | Configuration Framework | Dedicated `configuration-engine` service |
| **ADR-045** | Organizational Model | Dedicated `org-model-service` |
| **ADR-046** | Workflow Engine | Dedicated `workflow-engine` service |
| **ADR-047** | Localization Framework | Country packs as plugins in `localization/` |

---

## Complete Directory Structure

> **⚠️ Target Structure**: This directory tree represents the **complete target workspace** for full ChiroERP implementation. Many directories/files do not yet exist in the actual repository. For current architecture status (what's actually implemented), see [docs/architecture/README.md](./README.md).

```
chiroerp/
│
├── .github/                                    # GitHub Actions CI/CD workflows
│   ├── workflows/
│   │   ├── ci-microservices.yml               # Build & test all services
│   │   ├── cd-dev.yml                         # Deploy to dev environment
│   │   ├── cd-staging.yml                     # Deploy to staging
│   │   ├── cd-production.yml                  # Production deployment
│   │   ├── security-scan.yml                  # SAST/DAST scans (ADR-008)
│   │   └── performance-tests.yml              # Load testing (ADR-017)
│   └── CODEOWNERS                             # Code ownership by bounded context
│
├── .vscode/                                    # VS Code workspace settings
│   ├── settings.json
│   ├── launch.json                            # Debug configurations
│   └── extensions.json                        # Recommended extensions
│
├── docs/                                       # **EXISTING** Documentation
│   ├── adr/                                    # All 57+ ADRs
│   │   ├── ADR-001-modular-cqrs.md
│   │   ├── ADR-002-database-per-context.md
│   │   ├── ...
│   │   ├── ADR-056-ai-demand-forecasting-replenishment.md
│   │   └── ADR-057-dynamic-pricing-markdown-optimization.md
│   │
│   ├── architecture/                           # Architecture docs
│   │   ├── gap-to-sap-grade-roadmap.md        # Main 18-month roadmap
│   │   ├── WORKSPACE-STRUCTURE.md              # **THIS FILE**
│   │   │
│   │   ├── retail/                            # Retail AI Enhancement
│   │   │   └── retail-ai-architecture.md      # 40K word retail AI spec
│   │   │
│   │   ├── finance/                           # Finance domain (ADR-009)
│   │   │   ├── finance-gl.md
│   │   │   ├── finance-ap.md
│   │   │   ├── finance-ar.md
│   │   │   ├── finance-assets.md              # ADR-021 (actual filename)
│   │   │   ├── ...
│   │   │   ├── gl/                            # GL subdomain modules
│   │   │   │   ├── gl-domain.md
│   │   │   │   ├── gl-application.md
│   │   │   │   ├── gl-infrastructure.md
│   │   │   │   ├── gl-api.md
│   │   │   │   └── gl-events.md
│   │   │   ├── ap/
│   │   │   │   └── [same structure]
│   │   │   └── ar/
│   │   │       └── [same structure]
│   │   │
│   │   ├── controlling/                       # Controlling domain (ADR-028)
│   │   │   ├── controlling-cost-center.md
│   │   │   ├── controlling-profitability.md
│   │   │   └── ...
│   │   │
│   │   ├── inventory/                         # Inventory domain (ADR-024)
│   │   │   ├── inventory-core.md
│   │   │   ├── inventory-atp.md
│   │   │   ├── inventory-valuation.md
│   │   │   ├── inventory-warehouse.md         # ADR-038 (WMS)
│   │   │   ├── inventory-advanced-ops.md      # Advanced Ops add-on (separate module)
│   │   │   └── ...
│   │   │
│   │   ├── sales/                             # Sales domain (ADR-025)
│   │   │   ├── sales-core.md
│   │   │   ├── sales-pricing.md
│   │   │   ├── sales-credit.md                # (actual filename)
│   │   │   └── ...
│   │   │
│   │   ├── procurement/                       # Procurement domain (ADR-023)
│   │   │   ├── procurement-core.md
│   │   │   ├── procurement-sourcing.md
│   │   │   └── ...
│   │   │
│   │   ├── manufacturing/                     # Manufacturing domain (ADR-037)
│   │   │   ├── manufacturing-bom.md
│   │   │   ├── manufacturing-mrp.md
│   │   │   ├── manufacturing-shop-floor.md
│   │   │   ├── manufacturing-costing.md
│   │   │   └── ...
│   │   │
│   │   ├── quality/                           # Quality domain (ADR-039)
│   │   │   ├── quality-inspection-planning.md
│   │   │   ├── quality-execution.md
│   │   │   ├── quality-capa.md
│   │   │   └── ...
│   │   │
│   │   ├── maintenance/                       # Maintenance domain (ADR-040)
│   │   │   ├── maintenance-equipment.md
│   │   │   ├── maintenance-work-orders.md
│   │   │   ├── maintenance-preventive.md
│   │   │   └── ...
│   │   │
│   │   ├── crm/                               # CRM domain (ADR-042, ADR-043)
│   │   │   ├── crm-customer360.md
│   │   │   ├── crm-contracts.md
│   │   │   ├── crm-dispatch.md                # ADR-042 (Field Service)
│   │   │   └── ...
│   │   │
│   │   ├── mdm/                               # Master Data domain (ADR-027)
│   │   │   ├── mdm-hub.md
│   │   │   ├── mdm-data-quality.md
│   │   │   └── ...
│   │   │
│   │   ├── analytics/                         # Analytics domain (ADR-016)
│   │   │   ├── analytics-warehouse.md
│   │   │   ├── analytics-olap.md
│   │   │   ├── analytics-kpi.md
│   │   │   └── ...
│   │   │
│   │   ├── hr/                                # Human Capital Management (ADR-034, 052, 054, 055)
│   │   │   ├── hr-travel-expense.md           # ADR-054
│   │   │   ├── hr-contingent-workforce.md     # ADR-052 (VMS)
│   │   │   ├── hr-workforce-scheduling.md     # ADR-055 (WFM)
│   │   │   └── ...
│   │   │
│   │   └── fleet/                             # Fleet Management (ADR-053)
│   │       ├── fleet-vehicle-lifecycle.md
│   │       ├── fleet-telematics.md
│   │       └── ...
│   │
│   └── runbooks/                              # Operational runbooks (ADR-018)
│       ├── deployment.md
│       ├── incident-response.md
│       ├── disaster-recovery.md
│       └── monitoring.md
│
├── platform-shared/                            # 🔧 PLATFORM SHARED (ADR-006)
│   │                                          # STRICT GOVERNANCE: Technical primitives ONLY
│   │
│   ├── common-types/                          # Type-safe primitives
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.types/
│   │           ├── TenantId.kt                # Multi-tenancy (ADR-005)
│   │           ├── UserId.kt
│   │           ├── Money.kt
│   │           ├── Quantity.kt
│   │           ├── UnitOfMeasure.kt
│   │           ├── Currency.kt
│   │           ├── LocalizationContext.kt     # ADR-047
│   │           └── Result.kt                  # Railway-oriented programming
│   │
│   ├── common-api/                            # REST API standards (ADR-010)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.api/
│   │           ├── ErrorResponse.kt           # Standardized error format
│   │           ├── PageRequest.kt
│   │           ├── PageResponse.kt
│   │           ├── ApiVersion.kt
│   │           ├── RateLimiting.kt
│   │           └── CorrelationIdInterceptor.kt
│   │
│   ├── common-security/                       # AuthN/AuthZ (ADR-007)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.security/
│   │           ├── JwtTokenValidator.kt
│   │           ├── OAuth2Config.kt
│   │           ├── TenantContextHolder.kt     # Tenant isolation
│   │           ├── PermissionChecker.kt       # ADR-014 (AuthZ Objects)
│   │           └── SeparationOfDuties.kt      # ADR-014 (SoD)
│   │
│   ├── common-observability/                  # Monitoring (ADR-017)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.observability/
│   │           ├── CorrelationId.kt
│   │           ├── TraceContext.kt
│   │           ├── MetricsCollector.kt
│   │           ├── StructuredLogging.kt
│   │           └── PerformanceMonitor.kt      # ADR-017 (SLA tracking)
│   │
│   ├── common-events/                         # Event contracts (ADR-003)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.events/
│   │           ├── DomainEvent.kt             # Base interface
│   │           ├── EventMetadata.kt
│   │           ├── EventEnvelope.kt
│   │           ├── EventPublisher.kt          # Kafka abstraction
│   │           └── EventConsumer.kt
│   │
│   ├── common-cqrs/                           # CQRS primitives (ADR-001)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.cqrs/
│   │           ├── Command.kt
│   │           ├── Query.kt
│   │           ├── CommandHandler.kt
│   │           ├── QueryHandler.kt
│   │           ├── CommandBus.kt
│   │           └── QueryBus.kt
│   │
│   ├── common-saga/                           # Saga orchestration (ADR-011)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.saga/
│   │           ├── SagaDefinition.kt
│   │           ├── SagaStep.kt
│   │           ├── CompensatingAction.kt
│   │           ├── SagaOrchestrator.kt
│   │           └── SagaState.kt
│   │
│   ├── common-testing/                        # Testing standards (ADR-019)
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/
│   │       └── com.erp.shared.testing/
│   │           ├── IntegrationTest.kt         # Base class
│   │           ├── E2ETest.kt
│   │           ├── ContractTest.kt            # Pact support
│   │           ├── TestContainers.kt          # Docker test containers
│   │           └── TestDataBuilder.kt
│   │
│   └── common-resilience/                     # Network resilience (ADR-008)
│       ├── build.gradle.kts
│       └── src/main/kotlin/
│           └── com.erp.shared.resilience/
│               ├── CircuitBreaker.kt
│               ├── RetryPolicy.kt
│               ├── Bulkhead.kt
│               ├── RateLimiter.kt
│               └── Timeout.kt
│
├── platform-events/                            # 📡 EVENT DEFINITIONS (ADR-003)
│   │                                          # Avro schemas for all domain events
│   │
│   ├── build.gradle.kts                       # Avro code generation
│   │
│   ├── finance-events/
│   │   └── src/main/avro/
│   │       ├── JournalEntryPostedEvent.avsc
│   │       ├── InvoiceCreatedEvent.avsc
│   │       ├── PaymentReceivedEvent.avsc
│   │       └── ...
│   │
│   ├── inventory-events/
│   │   └── src/main/avro/
│   │       ├── StockMovementRecordedEvent.avsc
│   │       ├── ReorderPointTriggeredEvent.avsc
│   │       ├── GoodsReceivedEvent.avsc
│   │       └── ...
│   │
│   ├── sales-events/
│   │   └── src/main/avro/
│   │       ├── SalesOrderCreatedEvent.avsc
│   │       ├── OrderFulfilledEvent.avsc
│   │       ├── InvoiceGeneratedEvent.avsc
│   │       └── ...
│   │
│   ├── manufacturing-events/
│   │   └── src/main/avro/
│   │       ├── ProductionOrderCreatedEvent.avsc
│   │       ├── OperationCompletedEvent.avsc
│   │       ├── MaterialConsumedEvent.avsc
│   │       └── ...
│   │
│   ├── quality-events/
│   │   └── src/main/avro/
│   │       ├── InspectionLotCreatedEvent.avsc
│   │       ├── QualityDefectDetectedEvent.avsc
│   │       ├── StockBlockedEvent.avsc
│   │       └── ...
│   │
│   ├── maintenance-events/
│   │   └── src/main/avro/
│   │       ├── WorkOrderCreatedEvent.avsc
│   │       ├── PreventiveMaintenanceScheduledEvent.avsc
│   │       ├── EquipmentDowntimeEvent.avsc
│   │       └── ...
│   │
│   ├── crm-events/
│   │   └── src/main/avro/
│   │       ├── CustomerCreatedEvent.avsc
│   │       ├── ContractRenewedEvent.avsc
│   │       ├── ServiceTicketClosedEvent.avsc
│   │       └── ...
│   │
│   ├── mdm-events/
│   │   └── src/main/avro/
│   │       ├── MasterDataChangedEvent.avsc
│   │       ├── DataQualityIssueDetectedEvent.avsc
│   │       └── ...
│   │
│   └── retail-ai-events/                      # ADR-056, ADR-057
│       └── src/main/avro/
│           ├── DemandForecastGeneratedEvent.avsc
│           ├── ReorderPointAdjustedEvent.avsc
│           ├── PriceRecommendationEvent.avsc
│           └── MarkdownOptimizationEvent.avsc
│
├── bounded-contexts/                           # 🎯 MICROSERVICES (ADR-001)
│   │                                          # One service per bounded context
│   │
│   ├── finance/                               # 💰 FINANCE DOMAIN (ADR-009, 021, 022, 026, 029)
│   │   │
│   │   ├── finance-gl/                        # General Ledger
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── gl-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.finance.gl.domain/
│   │   │   │           ├── model/             # Aggregates & Entities
│   │   │   │           │   ├── JournalEntry.kt
│   │   │   │           │   ├── Account.kt
│   │   │   │           │   ├── ChartOfAccounts.kt
│   │   │   │           │   ├── FiscalYear.kt
│   │   │   │           │   └── PostingPeriod.kt
│   │   │   │           ├── events/            # Domain events
│   │   │   │           │   ├── JournalEntryPostedEvent.kt
│   │   │   │           │   ├── PeriodClosedEvent.kt
│   │   │   │           │   └── ReversalPostedEvent.kt
│   │   │   │           ├── exceptions/
│   │   │   │           │   ├── PeriodClosedException.kt
│   │   │   │           │   └── BalanceNotZeroException.kt
│   │   │   │           └── services/          # Domain services
│   │   │   │               ├── PostingRulesService.kt
│   │   │   │               └── BalanceCalculator.kt
│   │   │   │
│   │   │   ├── gl-application/                # CQRS handlers
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.finance.gl.application/
│   │   │   │           ├── commands/
│   │   │   │           │   ├── PostJournalEntryCommand.kt
│   │   │   │           │   ├── PostJournalEntryHandler.kt
│   │   │   │           │   ├── ClosePeriodCommand.kt
│   │   │   │           │   └── ClosePeriodHandler.kt
│   │   │   │           ├── queries/
│   │   │   │           │   ├── GetTrialBalanceQuery.kt
│   │   │   │           │   ├── GetTrialBalanceHandler.kt
│   │   │   │           │   ├── GetAccountHistoryQuery.kt
│   │   │   │           │   └── GetAccountHistoryHandler.kt
│   │   │   │           └── ports/             # Hexagonal architecture
│   │   │   │               ├── JournalEntryRepository.kt
│   │   │   │               ├── AccountRepository.kt
│   │   │   │               └── EventPublisher.kt
│   │   │   │
│   │   │   └── gl-infrastructure/             # Adapters
│   │   │       └── src/main/kotlin/
│   │   │           └── com.erp.finance.gl.infrastructure/
│   │   │               ├── rest/              # REST API (ADR-010)
│   │   │               │   ├── GLController.kt
│   │   │               │   ├── TrialBalanceController.kt
│   │   │               │   └── dto/
│   │   │               │       ├── PostJournalEntryRequest.kt
│   │   │               │       └── TrialBalanceResponse.kt
│   │   │               ├── persistence/       # Database-per-context (ADR-002)
│   │   │               │   ├── JpaJournalEntryRepository.kt
│   │   │               │   ├── JpaAccountRepository.kt
│   │   │               │   └── entities/
│   │   │               │       ├── JournalEntryEntity.kt
│   │   │               │       └── AccountEntity.kt
│   │   │               ├── messaging/         # Kafka integration (ADR-003)
│   │   │               │   ├── KafkaEventPublisher.kt
│   │   │               │   ├── APInvoiceEventConsumer.kt  # Consumes from AP
│   │   │               │   └── ARInvoiceEventConsumer.kt  # Consumes from AR
│   │   │               └── config/
│   │   │                   ├── SecurityConfig.kt
│   │   │                   └── DatabaseConfig.kt
│   │   │
│   │   ├── finance-ap/                        # Accounts Payable
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── ap-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.finance.ap.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Vendor.kt
│   │   │   │           │   ├── Invoice.kt
│   │   │   │           │   ├── Payment.kt
│   │   │   │           │   └── PaymentTerm.kt
│   │   │   │           ├── events/
│   │   │   │           │   ├── InvoiceReceivedEvent.kt
│   │   │   │           │   └── PaymentMadeEvent.kt
│   │   │   │           └── services/
│   │   │   │               └── ThreeWayMatchService.kt
│   │   │   ├── ap-application/
│   │   │   │   └── [CQRS handlers]
│   │   │   └── ap-infrastructure/
│   │   │       └── [REST, persistence, messaging]
│   │   │
│   │   ├── finance-ar/                        # Accounts Receivable
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── ar-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.finance.ar.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Customer.kt
│   │   │   │           │   ├── Invoice.kt
│   │   │   │           │   ├── Payment.kt
│   │   │   │           │   ├── CreditMemo.kt
│   │   │   │           │   └── AgingBucket.kt
│   │   │   │           ├── events/
│   │   │   │           │   ├── InvoiceGeneratedEvent.kt
│   │   │   │           │   ├── PaymentReceivedEvent.kt
│   │   │   │           │   └── DunningLetterSentEvent.kt
│   │   │   │           └── services/
│   │   │   │               ├── CreditCheckService.kt
│   │   │   │               └── DunningService.kt
│   │   │   ├── ar-application/
│   │   │   │   └── [CQRS handlers]
│   │   │   └── ar-infrastructure/
│   │   │       └── [REST, persistence, messaging]
│   │   │
│   │   ├── finance-fixed-assets/              # ADR-021
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── fixed-assets-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.finance.fixedassets.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Asset.kt
│   │   │   │           │   ├── Depreciation.kt
│   │   │   │           │   └── AssetAcquisition.kt
│   │   │   │           └── services/
│   │   │   │               └── DepreciationCalculator.kt
│   │   │   ├── fixed-assets-application/
│   │   │   └── fixed-assets-infrastructure/
│   │   │
│   │   ├── finance-treasury/                  # ADR-026
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── treasury-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.finance.treasury.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── BankAccount.kt
│   │   │   │           │   ├── CashPosition.kt
│   │   │   │           │   └── FXContract.kt
│   │   │   │           └── services/
│   │   │   │               └── LiquidityForecast.kt
│   │   │   ├── treasury-application/
│   │   │   └── treasury-infrastructure/
│   │   │
│   │   ├── finance-intercompany/              # ADR-029
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── intercompany-domain/
│   │   │   │   └── [Intercompany transactions & netting]
│   │   │   ├── intercompany-application/
│   │   │   └── intercompany-infrastructure/
│   │   │
│   │   └── finance-lease-accounting/          # ADR-033 (IFRS 16)
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── lease-domain/
│   │       │   └── [Lease contracts, ROU assets, amortization]
│   │       ├── lease-application/
│   │       └── lease-infrastructure/
│   │
│   ├── controlling/                           # 📊 CONTROLLING DOMAIN (ADR-028)
│   │   │
│   │   ├── controlling-cost-center/           # Cost Center Accounting
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── cost-center-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.controlling.costcenter.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── CostCenter.kt
│   │   │   │           │   ├── CostCenterHierarchy.kt
│   │   │   │           │   ├── ActualCosts.kt
│   │   │   │           │   └── PlanCosts.kt
│   │   │   │           └── services/
│   │   │   │               └── VarianceAnalysis.kt
│   │   │   ├── cost-center-application/
│   │   │   └── cost-center-infrastructure/
│   │   │
│   │   ├── controlling-profitability/         # Profitability Analysis
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── profitability-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.controlling.profitability.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── ProfitabilitySegment.kt
│   │   │   │           │   ├── ContributionMargin.kt
│   │   │   │           │   └── CostAllocation.kt
│   │   │   │           └── services/
│   │   │   │               └── ProfitabilityCalculator.kt
│   │   │   ├── profitability-application/
│   │   │   └── profitability-infrastructure/
│   │   │
│   │   ├── controlling-product-costing/       # Product Costing
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── product-costing-domain/
│   │   │   │   └── [Standard costing, variance analysis]
│   │   │   ├── product-costing-application/
│   │   │   └── product-costing-infrastructure/
│   │   │
│   │   └── controlling-budgeting/             # ADR-032 (FP&A)
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── budgeting-domain/
│   │       │   └── [Budget planning, rolling forecasts]
│   │       ├── budgeting-application/
│   │       └── budgeting-infrastructure/
│   │
│   ├── inventory/                             # 📦 INVENTORY DOMAIN (ADR-024, 038)
│   │   │
│   │   ├── inventory-core/                    # Core Inventory
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── inventory-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.inventory.core.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Material.kt
│   │   │   │           │   ├── StorageLocation.kt
│   │   │   │           │   ├── Stock.kt
│   │   │   │           │   └── StockMovement.kt
│   │   │   │           ├── events/
│   │   │   │           │   ├── StockMovementRecordedEvent.kt
│   │   │   │           │   └── StockAdjustmentEvent.kt
│   │   │   │           └── services/
│   │   │   │               └── StockBalanceService.kt
│   │   │   ├── inventory-application/
│   │   │   └── inventory-infrastructure/
│   │   │
│   │   ├── inventory-atp/                     # ATP & Allocation
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── atp-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.inventory.atp.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── ATPQuantity.kt
│   │   │   │           │   ├── Reservation.kt
│   │   │   │           │   └── AllocationRule.kt
│   │   │   │           └── services/
│   │   │   │               └── ATPCalculator.kt
│   │   │   ├── atp-application/
│   │   │   └── atp-infrastructure/
│   │   │
│   │   ├── inventory-valuation/              # Valuation & Costing
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── valuation-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.inventory.valuation.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── MaterialValuation.kt
│   │   │   │           │   ├── ValuationPrice.kt
│   │   │   │           │   └── InventoryValue.kt
│   │   │   │           └── services/
│   │   │   │               └── ValuationCalculator.kt  # FIFO/LIFO/WAC
│   │   │   ├── valuation-application/
│   │   │   └── valuation-infrastructure/
│   │   │
│   │   └── inventory-warehouse/               # ADR-038 (WMS)
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── warehouse-domain/
│   │       │   └── src/main/kotlin/
│   │       │       └── com.erp.inventory.warehouse.domain/
│   │       │           ├── model/
│   │       │           │   ├── WarehouseTask.kt
│   │       │           │   ├── WaveManagement.kt
│   │       │           │   ├── BinLocation.kt
│   │       │           │   └── PickingStrategy.kt
│   │       │           └── services/
│   │       │               ├── TaskOptimizer.kt
│   │       │               └── WaveReleaseService.kt
│   │       ├── warehouse-application/
│   │       └── warehouse-infrastructure/
│   │
│   ├── sales/                                 # 💵 SALES DOMAIN (ADR-025)
│   │   │
│   │   ├── sales-core/                        # Core Sales Orders
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── sales-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.sales.core.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── SalesOrder.kt
│   │   │   │           │   ├── OrderLine.kt
│   │   │   │           │   ├── ShippingAddress.kt
│   │   │   │           │   └── OrderStatus.kt
│   │   │   │           ├── events/
│   │   │   │           │   ├── SalesOrderCreatedEvent.kt
│   │   │   │           │   ├── OrderFulfilledEvent.kt
│   │   │   │           │   └── OrderCancelledEvent.kt
│   │   │   │           └── services/
│   │   │   │               ├── OrderValidationService.kt
│   │   │   │               └── ATPCheckService.kt
│   │   │   ├── sales-application/
│   │   │   └── sales-infrastructure/
│   │   │
│   │   ├── sales-pricing/                     # Pricing & Promotions
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── pricing-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.sales.pricing.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── PriceList.kt
│   │   │   │           │   ├── PricingCondition.kt
│   │   │   │           │   ├── Discount.kt
│   │   │   │           │   └── Promotion.kt
│   │   │   │           └── services/
│   │   │   │               └── PriceDeterminationService.kt
│   │   │   ├── pricing-application/
│   │   │   └── pricing-infrastructure/
│   │   │
│   │   ├── sales-credits/                     # Credits & Returns
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── credits-domain/
│   │   │   │   └── [Credit memos, returns authorization]
│   │   │   ├── credits-application/
│   │   │   └── credits-infrastructure/
│   │   │
│   │   └── sales-shipping/                    # Shipping & Logistics
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── shipping-domain/
│   │       │   └── [Delivery documents, carrier integration]
│   │       ├── shipping-application/
│   │       └── shipping-infrastructure/
│   │
│   ├── procurement/                           # 🛒 PROCUREMENT DOMAIN (ADR-023)
│   │   │
│   │   ├── procurement-core/                  # Core Procurement
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── procurement-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.procurement.core.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── PurchaseRequisition.kt
│   │   │   │           │   ├── PurchaseOrder.kt
│   │   │   │           │   ├── GoodsReceipt.kt
│   │   │   │           │   └── Vendor.kt
│   │   │   │           ├── events/
│   │   │   │           │   ├── PurchaseOrderCreatedEvent.kt
│   │   │   │           │   └── GoodsReceivedEvent.kt
│   │   │   │           └── services/
│   │   │   │               └── ThreeWayMatchService.kt
│   │   │   ├── procurement-application/
│   │   │   └── procurement-infrastructure/
│   │   │
│   │   └── procurement-sourcing/              # Sourcing & RFQ
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── sourcing-domain/
│   │       │   └── src/main/kotlin/
│   │       │       └── com.erp.procurement.sourcing.domain/
│   │       │           ├── model/
│   │       │           │   ├── RFQ.kt
│   │       │           │   ├── Quotation.kt
│   │       │           │   └── ContractAgreement.kt
│   │       │           └── services/
│   │       │               └── QuotationEvaluator.kt
│   │       ├── sourcing-application/
│   │       └── sourcing-infrastructure/
│   │
│   ├── manufacturing/                         # 🏭 MANUFACTURING DOMAIN (ADR-037)
│   │   │
│   │   ├── manufacturing-bom/                 # BOM Management
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── bom-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.manufacturing.bom.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── BillOfMaterial.kt
│   │   │   │           │   ├── BOMLine.kt
│   │   │   │           │   ├── Routing.kt
│   │   │   │           │   └── Operation.kt
│   │   │   │           ├── events/
│   │   │   │           │   ├── BOMPublishedEvent.kt
│   │   │   │           │   └── RoutingUpdatedEvent.kt
│   │   │   │           └── services/
│   │   │   │               └── BOMExplosionService.kt
│   │   │   ├── bom-application/
│   │   │   └── bom-infrastructure/
│   │   │
│   │   ├── manufacturing-mrp/                 # MRP (Material Requirements Planning)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── mrp-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.manufacturing.mrp.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── PlannedOrder.kt
│   │   │   │           │   ├── MRPElement.kt
│   │   │   │           │   └── PlanningHorizon.kt
│   │   │   │           └── services/
│   │   │   │               └── NetRequirementsCalculator.kt
│   │   │   ├── mrp-application/
│   │   │   └── mrp-infrastructure/
│   │   │
│   │   ├── manufacturing-shop-floor/          # Shop Floor Execution
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── shopfloor-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.manufacturing.shopfloor.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── ProductionOrder.kt
│   │   │   │           │   ├── OperationConfirmation.kt
│   │   │   │           │   ├── WorkCenter.kt
│   │   │   │           │   └── MaterialConsumption.kt
│   │   │   │           └── services/
│   │   │   │               └── CapacityScheduler.kt
│   │   │   ├── shopfloor-application/
│   │   │   └── shopfloor-infrastructure/
│   │   │
│   │   └── manufacturing-costing/             # Production Costing
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── costing-domain/
│   │       │   └── [Actual costing, variance analysis, scrap]
│   │       ├── costing-application/
│   │       └── costing-infrastructure/
│   │
│   ├── quality/                               # ✅ QUALITY DOMAIN (ADR-039)
│   │   │
│   │   ├── quality-inspection-planning/       # Inspection Planning
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── inspection-planning-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.quality.planning.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── InspectionPlan.kt
│   │   │   │           │   ├── Characteristic.kt
│   │   │   │           │   └── SamplingProcedure.kt
│   │   │   │           └── services/
│   │   │   │               └── SamplingCalculator.kt
│   │   │   ├── inspection-planning-application/
│   │   │   └── inspection-planning-infrastructure/
│   │   │
│   │   ├── quality-execution/                 # Quality Execution
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── execution-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.quality.execution.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── InspectionLot.kt
│   │   │   │           │   ├── InspectionResult.kt
│   │   │   │           │   ├── UsageDecision.kt
│   │   │   │           │   └── Defect.kt
│   │   │   │           └── services/
│   │   │   │               └── UsageDecisionService.kt
│   │   │   ├── execution-application/
│   │   │   └── execution-infrastructure/
│   │   │
│   │   └── quality-capa/                      # CAPA (Corrective & Preventive Actions)
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── capa-domain/
│   │       │   └── [Nonconformance, root cause analysis, action tracking]
│   │       ├── capa-application/
│   │       └── capa-infrastructure/
│   │
│   ├── maintenance/                           # 🔧 MAINTENANCE DOMAIN (ADR-040)
│   │   │
│   │   ├── maintenance-equipment/             # Equipment Master
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── equipment-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.maintenance.equipment.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Equipment.kt
│   │   │   │           │   ├── TechnicalObject.kt
│   │   │   │           │   └── BillOfMaterial.kt
│   │   │   │           └── services/
│   │   │   │               └── EquipmentHierarchyService.kt
│   │   │   ├── equipment-application/
│   │   │   └── equipment-infrastructure/
│   │   │
│   │   ├── maintenance-work-orders/           # Work Orders
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── work-orders-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.maintenance.workorders.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── WorkOrder.kt
│   │   │   │           │   ├── Operation.kt
│   │   │   │           │   ├── SparePartRequirement.kt
│   │   │   │           │   └── Notification.kt
│   │   │   │           └── services/
│   │   │   │               └── WorkOrderScheduler.kt
│   │   │   ├── work-orders-application/
│   │   │   └── work-orders-infrastructure/
│   │   │
│   │   └── maintenance-preventive/            # Preventive Maintenance
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── preventive-domain/
│   │       │   └── [Maintenance plans, scheduling, task lists]
│   │       ├── preventive-application/
│   │       └── preventive-infrastructure/
│   │
│   ├── crm/                                   # 👥 CRM DOMAIN (ADR-042, 043)
│   │   │
│   │   ├── crm-customer360/                   # Customer 360° View
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── customer360-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.crm.customer360.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Customer.kt
│   │   │   │           │   ├── Account.kt
│   │   │   │           │   ├── Contact.kt
│   │   │   │           │   └── Interaction.kt
│   │   │   │           └── services/
│   │   │   │               └── CustomerSegmentationService.kt
│   │   │   ├── customer360-application/
│   │   │   └── customer360-infrastructure/
│   │   │
│   │   ├── crm-contracts/                     # Contracts & SLAs
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── contracts-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.crm.contracts.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── ServiceContract.kt
│   │   │   │           │   ├── Entitlement.kt
│   │   │   │           │   ├── SLA.kt
│   │   │   │           │   └── Renewal.kt
│   │   │   │           └── services/
│   │   │   │               └── RenewalForecastService.kt
│   │   │   ├── contracts-application/
│   │   │   └── contracts-infrastructure/
│   │   │
│   │   └── crm-dispatch/                      # ADR-042 (Field Service)
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── dispatch-domain/
│   │       │   └── src/main/kotlin/
│   │       │       └── com.erp.crm.dispatch.domain/
│   │       │           ├── model/
│   │       │           │   ├── ServiceAppointment.kt
│   │       │           │   ├── Technician.kt
│   │       │           │   ├── WorkOrder.kt
│   │       │           │   └── TimeSlot.kt
│   │       │           └── services/
│   │       │               ├── RouteOptimizer.kt
│   │       │               └── TechnicianMatcher.kt
│   │       ├── dispatch-application/
│   │       └── dispatch-infrastructure/
│   │
│   ├── mdm/                                   # 📚 MASTER DATA DOMAIN (ADR-027)
│   │   │
│   │   ├── mdm-hub/                           # Master Data Hub
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── hub-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.mdm.hub.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── MasterDataObject.kt
│   │   │   │           │   ├── Attribute.kt
│   │   │   │           │   ├── Version.kt
│   │   │   │           │   └── Subscription.kt
│   │   │   │           └── services/
│   │   │   │               └── PublishSubscribeService.kt
│   │   │   ├── hub-application/
│   │   │   └── hub-infrastructure/
│   │   │
│   │   └── mdm-data-quality/                  # Data Quality Rules
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── data-quality-domain/
│   │       │   └── src/main/kotlin/
│   │       │       └── com.erp.mdm.quality.domain/
│   │       │           ├── model/
│   │       │           │   ├── QualityRule.kt
│   │       │           │   ├── Validation.kt
│   │       │           │   └── QualityScore.kt
│   │       │           └── services/
│   │       │               └── DataQualityEngine.kt
│   │       ├── data-quality-application/
│   │       └── data-quality-infrastructure/
│   │
│   ├── analytics/                             # 📈 ANALYTICS DOMAIN (ADR-016)
│   │   │
│   │   ├── analytics-warehouse/               # Data Warehouse
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── warehouse-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.analytics.warehouse.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── FactTable.kt
│   │   │   │           │   ├── DimensionTable.kt
│   │   │   │           │   └── ETLJob.kt
│   │   │   │           └── services/
│   │   │   │               └── CDCProcessor.kt  # Change Data Capture
│   │   │   ├── warehouse-application/
│   │   │   └── warehouse-infrastructure/
│   │   │
│   │   ├── analytics-olap/                    # OLAP Cube Engine
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── olap-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.analytics.olap.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── Cube.kt
│   │   │   │           │   ├── Dimension.kt
│   │   │   │           │   ├── Measure.kt
│   │   │   │           │   └── Hierarchy.kt
│   │   │   │           └── services/
│   │   │   │               └── CubeQueryExecutor.kt
│   │   │   ├── olap-application/
│   │   │   └── olap-infrastructure/
│   │   │
│   │   └── analytics-kpi/                     # KPI Engine
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── kpi-domain/
│   │       │   └── src/main/kotlin/
│   │       │       └── com.erp.analytics.kpi.domain/
│   │       │           ├── model/
│   │       │           │   ├── KPI.kt
│   │       │           │   ├── Threshold.kt
│   │       │           │   └── Calculation.kt
│   │       │           └── services/
│   │       │               └── KPICalculator.kt
│   │       ├── kpi-application/
│   │       └── kpi-infrastructure/
│   │
│   ├── platform-services/                     # 🛠️ PLATFORM SERVICES (ADR-044, 045, 046)
│   │   │
│   │   ├── api-gateway/                       # ADR-004 (API Gateway)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   └── src/main/kotlin/
│   │   │       └── com.erp.platform.gateway/
│   │   │           ├── routing/
│   │   │           │   └── GatewayRoutingConfig.kt
│   │   │           ├── security/
│   │   │           │   ├── JwtAuthenticationFilter.kt
│   │   │           │   └── TenantResolutionFilter.kt
│   │   │           ├── ratelimiting/
│   │   │           │   └── RateLimitFilter.kt
│   │   │           └── cors/
│   │   │               └── CorsConfig.kt
│   │   │
│   │   ├── configuration-engine/              # ADR-044 (Configuration Framework)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── config-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.platform.config.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── ConfigurationKey.kt
│   │   │   │           │   ├── ConfigurationValue.kt
│   │   │   │           │   ├── ConfigurationScope.kt  # Tenant/User/Global
│   │   │   │           │   └── ConfigurationVersion.kt
│   │   │   │           └── services/
│   │   │   │               └── ConfigurationResolver.kt
│   │   │   ├── config-application/
│   │   │   └── config-infrastructure/
│   │   │
│   │   ├── org-model-service/                 # ADR-045 (Organizational Model)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── org-model-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.platform.orgmodel.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── CompanyCode.kt
│   │   │   │           │   ├── Plant.kt
│   │   │   │           │   ├── Division.kt
│   │   │   │           │   ├── SalesOrganization.kt
│   │   │   │           │   └── PurchasingOrganization.kt
│   │   │   │           └── services/
│   │   │   │               └── OrgHierarchyService.kt
│   │   │   ├── org-model-application/
│   │   │   └── org-model-infrastructure/
│   │   │
│   │   ├── workflow-engine/                   # ADR-046 (Workflow & Approval)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── workflow-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.platform.workflow.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── WorkflowDefinition.kt
│   │   │   │           │   ├── WorkflowInstance.kt
│   │   │   │           │   ├── WorkflowStep.kt
│   │   │   │           │   ├── ApprovalRule.kt
│   │   │   │           │   └── ApprovalRequest.kt
│   │   │   │           └── services/
│   │   │   │               └── WorkflowExecutor.kt
│   │   │   ├── workflow-application/
│   │   │   └── workflow-infrastructure/
│   │   │
│   │   ├── tax-engine/                        # ADR-030 (Tax Compliance)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── tax-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.platform.tax.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── TaxCode.kt
│   │   │   │           │   ├── TaxJurisdiction.kt
│   │   │   │           │   ├── TaxRule.kt
│   │   │   │           │   └── TaxCalculation.kt
│   │   │   │           └── services/
│   │   │   │               └── TaxCalculationEngine.kt
│   │   │   ├── tax-application/
│   │   │   └── tax-infrastructure/
│   │   │
│   │   ├── period-close-orchestrator/         # ADR-031 (Period Close)
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── period-close-domain/
│   │   │   │   └── src/main/kotlin/
│   │   │   │       └── com.erp.platform.periodclose.domain/
│   │   │   │           ├── model/
│   │   │   │           │   ├── PeriodCloseTask.kt
│   │   │   │           │   ├── TaskDependency.kt
│   │   │   │           │   └── CloseLock.kt
│   │   │   │           └── services/
│   │   │   │               └── PeriodCloseOrchestrator.kt
│   │   │   ├── period-close-application/
│   │   │   └── period-close-infrastructure/
│   │   │
│   │   ├── document-management/               # Document Attachments
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── document-domain/
│   │   │   │   └── [Document metadata, storage, versioning]
│   │   │   ├── document-application/
│   │   │   └── document-infrastructure/
│   │   │
│   │   ├── notification-service/              # Notifications & Alerts
│   │   │   ├── build.gradle.kts
│   │   │   ├── Dockerfile
│   │   │   ├── notification-domain/
│   │   │   │   └── [Email, SMS, push notifications]
│   │   │   ├── notification-application/
│   │   │   └── notification-infrastructure/
│   │   │
│   │   └── audit-log-service/                 # ADR-015 (Data Lifecycle)
│   │       ├── build.gradle.kts
│   │       ├── Dockerfile
│   │       ├── audit-domain/
│   │       │   └── [Audit trail, change history, retention policies]
│   │       ├── audit-application/
│   │       └── audit-infrastructure/
│   │
│   ├── industry-extensions/                   # 🏭 INDUSTRY-SPECIFIC MODULES
│   │   │
│   │   ├── banking/                           # Banking & Financial Services
│   │   │   ├── loan-management/
│   │   │   │   ├── build.gradle.kts
│   │   │   │   ├── Dockerfile
│   │   │   │   └── [Loan origination, servicing, collections]
│   │   │   ├── deposit-accounts/
│   │   │   │   └── [Checking, savings, CD accounts]
│   │   │   └── regulatory-reporting/
│   │   │       └── [Basel III, Dodd-Frank, IFRS 9]
│   │   │
│   │   ├── process-manufacturing/             # Process Manufacturing
│   │   │   ├── batch-management/
│   │   │   │   └── [Batch genealogy, lot traceability]
│   │   │   ├── formula-management/
│   │   │   │   └── [Recipes, formulations, yield]
│   │   │   └── process-execution/
│   │   │       └── [Campaigns, batch sequencing]
│   │   │
│   │   ├── utilities/                         # Utilities (Energy, Water, Telecom)
│   │   │   ├── meter-data-management/
│   │   │   │   └── [Meter readings, consumption, billing]
│   │   │   ├── outage-management/
│   │   │   │   └── [Incident tracking, crew dispatch]
│   │   │   └── asset-management/
│   │   │       └── [Grid infrastructure, maintenance]
│   │   │
│   │   ├── public-sector/                     # ADR-050 (Government)
│   │   │   ├── grant-management/
│   │   │   │   └── [Grant applications, tracking, reporting]
│   │   │   ├── fund-accounting/
│   │   │   │   └── [Fund-based GL, encumbrances]
│   │   │   └── procurement-compliance/
│   │   │       └── [RFP processes, vendor compliance]
│   │   │
│   │   ├── insurance/                         # Insurance
│   │   │   ├── policy-administration/
│   │   │   │   └── [Policy lifecycle, endorsements]
│   │   │   ├── claims-management/
│   │   │   │   └── [Claims processing, adjudication]
│   │   │   └── underwriting/
│   │   │       └── [Risk assessment, pricing]
│   │   │
│   │   ├── real-estate/                       # Real Estate & Property Management
│   │   │   ├── lease-management/
│   │   │   │   └── [Lease contracts, rent billing]
│   │   │   ├── property-maintenance/
│   │   │   │   └── [Work orders, tenant requests]
│   │   │   └── vacancy-management/
│   │   │       └── [Availability, showings, leasing]
│   │   │
│   │   ├── advanced-inventory/                # Advanced Inventory Features
│   │   │   ├── batch-management/
│   │   │   │   └── [Batch tracking, genealogy]
│   │   │   ├── serial-number-management/
│   │   │   │   └── [Serial tracking, warranties]
│   │   │   └── kitting/
│   │   │       └── [Kit assembly, disassembly]
│   │   │
│   │   └── retail-ai/                         # 🤖 RETAIL AI ENHANCEMENT (ADR-056, 057)
│   │       │
│   │       ├── demand-forecasting-service/    # ADR-056 (AI Demand Forecasting)
│   │       │   ├── build.gradle.kts
│   │       │   ├── Dockerfile
│   │       │   ├── requirements.txt           # Python dependencies
│   │       │   ├── forecasting-domain/
│   │       │   │   └── src/main/python/
│   │       │   │       └── com.erp.retail.forecasting/
│   │       │   │           ├── models/
│   │       │   │           │   ├── DemandForecast.py
│   │       │   │           │   ├── ForecastModel.py
│   │       │   │           │   ├── ReorderPoint.py
│   │       │   │           │   ├── PromotionPlan.py
│   │       │   │           │   ├── ExternalSignal.py
│   │       │   │           │   ├── ForecastAccuracy.py
│   │       │   │           │   └── ScenarioAnalysis.py
│   │       │   │           ├── ml/            # ML models
│   │       │   │           │   ├── time_series/
│   │       │   │           │   │   ├── arima_model.py
│   │       │   │           │   │   ├── prophet_model.py
│   │       │   │           │   │   ├── xgboost_model.py
│   │       │   │           │   │   └── lstm_model.py
│   │       │   │           │   ├── seasonality/
│   │       │   │           │   │   ├── stl_decomposition.py
│   │       │   │           │   │   └── fourier_transform.py
│   │       │   │           │   └── ensemble/
│   │       │   │           │       └── model_blending.py
│   │       │   │           └── services/
│   │       │   │               ├── ForecastingEngine.py
│   │       │   │               ├── ReorderPointCalculator.py
│   │       │   │               └── MultiEchelonOptimizer.py
│   │       │   │
│   │       │   ├── forecasting-application/
│   │       │   │   └── src/main/python/
│   │       │   │       └── [Commands, queries, handlers]
│   │       │   │
│   │       │   └── forecasting-infrastructure/
│   │       │       └── src/main/python/
│   │       │           ├── api/               # FastAPI REST endpoints
│   │       │           │   ├── forecast_api.py
│   │       │           │   └── reorder_point_api.py
│   │       │           ├── persistence/       # PostgreSQL + TimescaleDB
│   │       │           │   └── forecast_repository.py
│   │       │           └── messaging/         # Kafka integration
│   │       │               └── forecast_event_publisher.py
│   │       │
│   │       └── pricing-optimization-service/  # ADR-057 (Dynamic Pricing)
│   │           ├── build.gradle.kts
│   │           ├── Dockerfile
│   │           ├── requirements.txt           # Python dependencies
│   │           ├── pricing-domain/
│   │           │   └── src/main/python/
│   │           │       └── com.erp.retail.pricing/
│   │           │           ├── models/
│   │           │           │   ├── PriceElasticity.py
│   │           │           │   ├── MarkdownRecommendation.py
│   │           │           │   ├── CompetitorPrice.py
│   │           │           │   ├── PromotionROI.py
│   │           │           │   ├── ABTestExperiment.py
│   │           │           │   └── PriceHistory.py
│   │           │           ├── ml/            # ML models
│   │           │           │   ├── elasticity/
│   │           │           │   │   ├── log_log_regression.py
│   │           │           │   │   ├── xgboost_elasticity.py
│   │           │           │   │   └── hierarchical_model.py
│   │           │           │   ├── optimization/
│   │           │           │   │   ├── markdown_optimizer.py
│   │           │           │   │   └── clearance_accelerator.py
│   │           │           │   └── ab_testing/
│   │           │           │       └── statistical_validator.py
│   │           │           └── services/
│   │           │               ├── PricingEngine.py
│   │           │               ├── MarkdownOptimizer.py
│   │           │               └── CompetitiveIntelligence.py
│   │           │
│   │           ├── pricing-application/
│   │           │   └── src/main/python/
│   │           │       └── [Commands, queries, handlers]
│   │           │
│   │           └── pricing-infrastructure/
│   │               └── src/main/python/
│   │                   ├── api/               # FastAPI REST endpoints
│   │                   │   ├── pricing_api.py
│   │                   │   └── markdown_api.py
│   │                   ├── persistence/       # PostgreSQL
│   │                   │   └── pricing_repository.py
│   │                   └── messaging/         # Kafka integration
│   │                       └── pricing_event_publisher.py
│   │
│   └── localization/                          # 🌍 LOCALIZATION (ADR-047)
│       │                                      # Country packs as plugins
│       │
│       ├── country-packs/
│       │   ├── us/                            # United States
│       │   │   ├── build.gradle.kts
│       │   │   ├── chart-of-accounts/         # GAAP COA
│       │   │   ├── tax-codes/                 # Federal/State taxes
│       │   │   ├── legal-forms/               # LLC, Corp, etc.
│       │   │   └── regulatory-reports/        # 10-K, SOX
│       │   │
│       │   ├── de/                            # Germany
│       │   │   ├── build.gradle.kts
│       │   │   ├── chart-of-accounts/         # HGB/IFRS COA (SKR03/04)
│       │   │   ├── tax-codes/                 # MwSt, Umsatzsteuer
│       │   │   ├── legal-forms/               # GmbH, AG
│       │   │   └── regulatory-reports/        # GoBD, DATEV
│       │   │
│       │   ├── fr/                            # France
│       │   │   └── [COA, tax, legal forms, reports]
│       │   │
│       │   ├── gb/                            # United Kingdom
│       │   │   └── [COA, VAT, legal forms, MTD]
│       │   │
│       │   ├── cn/                            # China
│       │   │   └── [COA, Golden Tax, legal forms]
│       │   │
│       │   └── [additional countries...]
│       │
│       └── localization-engine/               # Localization Framework
│           ├── build.gradle.kts
│           ├── Dockerfile
│           └── src/main/kotlin/
│               └── com.erp.localization/
│                   ├── CountryPackLoader.kt
│                   ├── LocalizationResolver.kt
│                   └── RegulatoryReportGenerator.kt
│
├── frontend/                                   # 🖥️ FRONTEND APPLICATIONS (ADR-048)
│   │
│   ├── web-app/                               # Main Web Application (React)
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   ├── vite.config.ts                     # Vite build config
│   │   ├── Dockerfile
│   │   │
│   │   ├── public/
│   │   │   ├── index.html
│   │   │   └── assets/
│   │   │
│   │   └── src/
│   │       ├── main.tsx                       # Entry point
│   │       │
│   │       ├── modules/                       # Domain modules
│   │       │   ├── finance/
│   │       │   │   ├── gl/
│   │       │   │   │   ├── pages/
│   │       │   │   │   │   ├── TrialBalancePage.tsx
│   │       │   │   │   │   ├── JournalEntryPage.tsx
│   │       │   │   │   │   └── AccountHistoryPage.tsx
│   │       │   │   │   ├── components/
│   │       │   │   │   │   ├── JournalEntryForm.tsx
│   │       │   │   │   │   └── TrialBalanceTable.tsx
│   │       │   │   │   └── api/
│   │       │   │   │       └── glApiClient.ts
│   │       │   │   ├── ap/
│   │       │   │   │   └── [AP UI components]
│   │       │   │   └── ar/
│   │       │   │       └── [AR UI components]
│   │       │   │
│   │       │   ├── inventory/
│   │       │   │   ├── stock-overview/
│   │       │   │   ├── warehouse-management/
│   │       │   │   └── atp-check/
│   │       │   │
│   │       │   ├── sales/
│   │       │   │   ├── sales-orders/
│   │       │   │   ├── pricing/
│   │       │   │   └── shipping/
│   │       │   │
│   │       │   ├── manufacturing/
│   │       │   │   ├── shop-floor/
│   │       │   │   ├── bom-management/
│   │       │   │   └── mrp/
│   │       │   │
│   │       │   └── [other modules...]
│   │       │
│   │       ├── shared/                        # Shared UI components
│   │       │   ├── components/
│   │       │   │   ├── Layout/
│   │       │   │   │   ├── AppLayout.tsx
│   │       │   │   │   ├── Sidebar.tsx
│   │       │   │   │   └── Header.tsx
│   │       │   │   ├── DataTable/
│   │       │   │   │   └── DataTable.tsx
│   │       │   │   ├── Form/
│   │       │   │   │   ├── Input.tsx
│   │       │   │   │   ├── Select.tsx
│   │       │   │   │   └── DatePicker.tsx
│   │       │   │   └── Modals/
│   │       │   │       └── ConfirmDialog.tsx
│   │       │   │
│   │       │   ├── hooks/                     # Custom React hooks
│   │       │   │   ├── useAuth.ts
│   │       │   │   ├── useTenant.ts
│   │       │   │   └── useApi.ts
│   │       │   │
│   │       │   └── utils/
│   │       │       ├── api.ts                 # Axios client
│   │       │       ├── formatting.ts
│   │       │       └── validation.ts
│   │       │
│   │       ├── routing/
│   │       │   └── AppRoutes.tsx              # React Router config
│   │       │
│   │       └── store/                         # State management (Redux Toolkit)
│   │           ├── store.ts
│   │           ├── authSlice.ts
│   │           └── tenantSlice.ts
│   │
│   ├── mobile-app/                            # Mobile Application (React Native - Optional)
│   │   ├── package.json
│   │   ├── App.tsx
│   │   └── [mobile-specific components]
│   │
│   └── admin-portal/                          # Admin Portal (Tenant management)
│       ├── package.json
│       └── [admin-specific components]
│
├── infrastructure/                             # 🏗️ INFRASTRUCTURE & DEPLOYMENT
│   │
│   ├── kafka/                                 # Kafka Configuration (ADR-003)
│   │   ├── docker-compose.yml                 # Local Kafka cluster
│   │   ├── kafka-topics.sh                    # Topic creation script
│   │   ├── topics/
│   │   │   ├── finance-events.yaml
│   │   │   ├── inventory-events.yaml
│   │   │   ├── sales-events.yaml
│   │   │   └── [other topics]
│   │   └── schema-registry/
│   │       └── avro-schemas/                  # Avro schemas (mirrored from platform-events)
│   │
│   ├── postgres/                              # PostgreSQL Configuration (ADR-002)
│   │   ├── docker-compose.yml                 # Local PostgreSQL
│   │   ├── init-scripts/
│   │   │   ├── 01-create-databases.sql        # Database-per-context creation
│   │   │   ├── 02-create-users.sql
│   │   │   └── 03-grant-permissions.sql
│   │   └── timescaledb/                       # TimescaleDB for time-series (retail AI)
│   │       └── docker-compose.yml
│   │
│   ├── kubernetes/                            # Kubernetes Manifests (Enterprise deployment)
│   │   │
│   │   ├── namespaces/
│   │   │   ├── dev-namespace.yaml
│   │   │   ├── staging-namespace.yaml
│   │   │   └── production-namespace.yaml
│   │   │
│   │   ├── services/                          # Per-microservice deployments
│   │   │   ├── finance/
│   │   │   │   ├── finance-gl-deployment.yaml
│   │   │   │   ├── finance-gl-service.yaml
│   │   │   │   ├── finance-gl-configmap.yaml
│   │   │   │   └── finance-gl-secret.yaml
│   │   │   ├── inventory/
│   │   │   │   └── [inventory service manifests]
│   │   │   └── [other services...]
│   │   │
│   │   ├── databases/
│   │   │   ├── postgres-statefulset.yaml
│   │   │   ├── postgres-service.yaml
│   │   │   └── postgres-pvc.yaml
│   │   │
│   │   ├── kafka/
│   │   │   ├── kafka-statefulset.yaml
│   │   │   ├── kafka-service.yaml
│   │   │   └── zookeeper-statefulset.yaml
│   │   │
│   │   ├── monitoring/
│   │   │   ├── prometheus-deployment.yaml
│   │   │   ├── grafana-deployment.yaml
│   │   │   └── loki-deployment.yaml
│   │   │
│   │   ├── ingress/
│   │   │   ├── ingress-controller.yaml
│   │   │   └── ingress-routes.yaml
│   │   │
│   │   └── helm/                              # Helm charts (optional)
│   │       ├── chiroerp/
│   │       │   ├── Chart.yaml
│   │       │   ├── values.yaml
│   │       │   ├── values-dev.yaml
│   │       │   ├── values-staging.yaml
│   │       │   ├── values-production.yaml
│   │       │   └── templates/
│   │       │       └── [Helm templates]
│   │       └── [dependency charts]
│   │
│   ├── docker-compose/                        # Docker Compose (SMB deployment - ADR-018)
│   │   ├── docker-compose.yml                 # All-in-one SMB deployment
│   │   ├── docker-compose.dev.yml             # Development overrides
│   │   ├── docker-compose.monitoring.yml      # Monitoring stack
│   │   └── .env.example                       # Environment variables template
│   │
│   ├── terraform/                             # Infrastructure as Code (IaC)
│   │   ├── aws/
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   ├── outputs.tf
│   │   │   ├── eks.tf                         # EKS cluster
│   │   │   ├── rds.tf                         # RDS PostgreSQL
│   │   │   ├── msk.tf                         # MSK (Managed Kafka)
│   │   │   └── networking.tf                  # VPC, subnets
│   │   │
│   │   ├── azure/
│   │   │   ├── main.tf
│   │   │   ├── aks.tf                         # AKS cluster
│   │   │   ├── postgresql.tf                  # Azure Database for PostgreSQL
│   │   │   └── eventhub.tf                    # Event Hubs (Kafka alternative)
│   │   │
│   │   └── gcp/
│   │       ├── main.tf
│   │       ├── gke.tf                         # GKE cluster
│   │       └── cloudsql.tf                    # Cloud SQL for PostgreSQL
│   │
│   ├── monitoring/                            # Monitoring & Observability (ADR-017)
│   │   ├── prometheus/
│   │   │   ├── prometheus.yml                 # Prometheus config
│   │   │   └── alerts/
│   │   │       ├── service-alerts.yaml
│   │   │       └── infra-alerts.yaml
│   │   │
│   │   ├── grafana/
│   │   │   ├── dashboards/
│   │   │   │   ├── service-health-dashboard.json
│   │   │   │   ├── business-metrics-dashboard.json
│   │   │   │   └── sla-tracking-dashboard.json
│   │   │   └── provisioning/
│   │   │       └── datasources.yaml
│   │   │
│   │   └── loki/
│   │       └── loki-config.yaml               # Log aggregation
│   │
│   └── scripts/                               # Deployment & Utility Scripts
│       ├── setup-local-dev.sh                 # Local dev environment setup
│       ├── deploy-dev.sh                      # Deploy to dev environment
│       ├── deploy-staging.sh                  # Deploy to staging
│       ├── deploy-production.sh               # Production deployment
│       ├── backup-databases.sh                # Database backup (ADR-015)
│       ├── restore-databases.sh               # Database restore
│       └── seed-test-data.sh                  # Test data seeding
│
├── tests/                                      # 🧪 TESTING (ADR-019)
│   │
│   ├── unit/                                  # Unit tests (per service)
│   │   ├── finance-gl/
│   │   │   └── [JUnit tests for GL domain]
│   │   ├── inventory-core/
│   │   │   └── [JUnit tests for Inventory domain]
│   │   └── [other services...]
│   │
│   ├── integration/                           # Integration tests
│   │   ├── finance-integration-tests/
│   │   │   └── [Tests for GL ↔ AP ↔ AR integration]
│   │   └── [other integration tests...]
│   │
│   ├── contract/                              # Contract tests (Pact)
│   │   ├── consumer-contracts/
│   │   │   └── [Pact consumer contracts]
│   │   └── provider-contracts/
│   │       └── [Pact provider verifications]
│   │
│   ├── e2e/                                   # End-to-end tests (Playwright/Cypress)
│   │   ├── package.json
│   │   ├── playwright.config.ts
│   │   ├── tests/
│   │   │   ├── finance/
│   │   │   │   ├── journal-entry-e2e.spec.ts
│   │   │   │   └── trial-balance-e2e.spec.ts
│   │   │   ├── sales/
│   │   │   │   └── sales-order-e2e.spec.ts
│   │   │   └── [other E2E tests...]
│   │   └── fixtures/
│   │       └── test-data.json
│   │
│   ├── performance/                           # Performance tests (JMeter/Gatling) - ADR-017
│   │   ├── jmeter/
│   │   │   ├── load-test-plan.jmx
│   │   │   └── stress-test-plan.jmx
│   │   └── gatling/
│   │       └── LoadSimulation.scala
│   │
│   └── security/                              # Security tests (OWASP ZAP) - ADR-008
│       ├── zap-baseline-scan.sh
│       └── zap-full-scan.sh
│
├── migrations/                                 # 📊 DATABASE MIGRATIONS (Flyway/Liquibase)
│   │
│   ├── finance-gl/
│   │   ├── V001__create_journal_entry_table.sql
│   │   ├── V002__create_account_table.sql
│   │   └── [other migrations...]
│   │
│   ├── inventory-core/
│   │   ├── V001__create_stock_table.sql
│   │   ├── V002__create_storage_location_table.sql
│   │   └── [other migrations...]
│   │
│   └── [other services...]
│
├── build.gradle.kts                            # Root Gradle build (Kotlin DSL)
├── settings.gradle.kts                         # Gradle settings (multi-module)
├── gradle.properties                           # Gradle properties
├── gradlew                                     # Gradle wrapper (Unix)
├── gradlew.bat                                 # Gradle wrapper (Windows)
│
├── docker-compose.yml                          # Local development stack (all services)
├── docker-compose.override.yml                 # Local overrides (ports, env vars)
│
├── .gitignore                                  # Git ignore rules
├── .editorconfig                               # Editor config
├── .env.example                                # Environment variables template
│
├── README.md                                   # Project README
├── CONTRIBUTING.md                             # Contribution guidelines
├── LICENSE                                     # Project license
│
└── [configuration files...]
```

---

## Bounded Contexts & Microservices

> **Note**: This section shows target microservices structure. For **current module counts** (92 modules across 12 domains with actual port assignments), see [Architecture README](./README.md).

### 1. Finance Domain (ADR-009, 021, 022, 026, 029, 033)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **finance-gl** | 8081 | `finance_gl_db` | General Ledger, chart of accounts, journal entries |
| **finance-ap** | 8082 | `finance_ap_db` | Accounts Payable, vendor invoices, payments |
| **finance-ar** | 8083 | `finance_ar_db` | Accounts Receivable, customer invoices, collections |
| **finance-assets** | 8084 | `finance_fa_db` | Fixed asset accounting, depreciation (ADR-021) |
| **finance-treasury** | 8085 | `finance_treasury_db` | Cash management, bank accounts, FX (ADR-026) |
| **finance-intercompany** | 8086 | `finance_ic_db` | Intercompany transactions, netting (ADR-029) |
| **finance-lease-accounting** | 8087 | `finance_lease_db` | IFRS 16 lease accounting (ADR-033) |

**Integration**:
- **Publishes**: `JournalEntryPostedEvent`, `InvoiceCreatedEvent`, `PaymentReceivedEvent`
- **Consumes**: Events from Sales, Procurement, Inventory (for GL posting)

---

### 2. Controlling Domain (ADR-028, 032)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **controlling-cost-center** | 8091 | `controlling_cc_db` | Cost center accounting, variance analysis |
| **controlling-profitability** | 8092 | `controlling_pa_db` | Profitability analysis, contribution margins |
| **controlling-product-costing** | 8093 | `controlling_costing_db` | Standard/actual costing, variance |
| **controlling-budgeting** | 8094 | `controlling_budget_db` | Budget planning, rolling forecasts (ADR-032) |

**Integration**:
- **Consumes**: Cost events from Finance, Manufacturing
- **Publishes**: `VarianceDetectedEvent`, `BudgetExceededEvent`

---

### 3. Inventory Domain (ADR-024, 038)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **inventory-core** | 8101 | `inventory_core_db` | Stock management, storage locations, movements |
| **inventory-atp** | 8102 | `inventory_atp_db` | Available-to-Promise, allocations |
| **inventory-valuation** | 8103 | `inventory_val_db` | Inventory valuation (FIFO/LIFO/WAC) |
| **inventory-warehouse** | 8104 | `inventory_wms_db` | Warehouse Management System (ADR-038) |

**Integration**:
- **Publishes**: `StockMovementRecordedEvent`, `ReorderPointTriggeredEvent`, `GoodsReceivedEvent`
- **Consumes**: Events from Procurement, Sales, Manufacturing

---

### 4. Sales Domain (ADR-025)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **sales-core** | 8111 | `sales_core_db` | Sales orders, quotations, order management |
| **sales-pricing** | 8112 | `sales_pricing_db` | Price lists, promotions, discounts |
| **sales-credit** | 8113 | `sales_credits_db` | Credit memos, returns authorization |
| **sales-shipping** | 8114 | `sales_shipping_db` | Delivery documents, carrier integration |

**Integration**:
- **Publishes**: `SalesOrderCreatedEvent`, `OrderFulfilledEvent`, `InvoiceGeneratedEvent`
- **Consumes**: ATP events from Inventory, pricing events

---

### 5. Procurement Domain (ADR-023)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **procurement-core** | 8121 | `procurement_core_db` | Purchase requisitions, POs, goods receipts |
| **procurement-sourcing** | 8122 | `procurement_sourcing_db` | RFQs, quotations, contract agreements |

**Integration**:
- **Publishes**: `PurchaseOrderCreatedEvent`, `GoodsReceivedEvent`
- **Consumes**: Reorder events from Inventory, MRP events from Manufacturing

---

### 6. Manufacturing Domain (ADR-037)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **manufacturing-bom** | 8131 | `manufacturing_bom_db` | BOM management, routings, operations |
| **manufacturing-mrp** | 8132 | `manufacturing_mrp_db` | Material Requirements Planning |
| **manufacturing-shop-floor** | 8133 | `manufacturing_sfe_db` | Production orders, confirmations, capacity |
| **manufacturing-costing** | 8134 | `manufacturing_costing_db` | Production costing, variances |

**Integration**:
- **Publishes**: `ProductionOrderCreatedEvent`, `MaterialConsumedEvent`, `OperationCompletedEvent`
- **Consumes**: BOM events, inventory events

---

### 7. Quality Domain (ADR-039)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **quality-inspection-planning** | 8141 | `quality_planning_db` | Inspection plans, characteristics, sampling |
| **quality-execution** | 8142 | `quality_execution_db` | Inspection lots, results, usage decisions |
| **quality-capa** | 8143 | `quality_capa_db` | CAPA (Corrective & Preventive Actions) |

**Integration**:
- **Publishes**: `InspectionLotCreatedEvent`, `QualityDefectDetectedEvent`, `StockBlockedEvent`
- **Consumes**: Goods receipt events, production events

---

### 8. Maintenance Domain (ADR-040)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **maintenance-equipment** | 8151 | `maintenance_equipment_db` | Equipment master, technical objects |
| **maintenance-work-orders** | 8152 | `maintenance_wo_db` | Work orders, operations, notifications |
| **maintenance-preventive** | 8153 | `maintenance_pm_db` | Preventive maintenance plans, scheduling |

**Integration**:
- **Publishes**: `WorkOrderCreatedEvent`, `EquipmentDowntimeEvent`
- **Consumes**: Equipment events, spare parts events

---

### 9. CRM Domain (ADR-042, 043)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **crm-customer360** | 8161 | `crm_customer360_db` | Customer 360° view, accounts, contacts |
| **crm-contracts** | 8162 | `crm_contracts_db` | Service contracts, SLAs, entitlements |
| **crm-dispatch** | 8163 | `crm_dispatch_db` | Field service, technician dispatch (ADR-042) |

**Integration**:
- **Publishes**: `CustomerCreatedEvent`, `ServiceTicketClosedEvent`, `ContractRenewedEvent`
- **Consumes**: Sales events, maintenance work order events

---

### 10. Master Data Management (ADR-027)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **mdm-hub** | 8171 | `mdm_hub_db` | Master data hub (pub/sub for MDM changes) |
| **mdm-data-quality** | 8172 | `mdm_quality_db` | Data quality rules, validation, scoring |

**Integration**:
- **Publishes**: `MasterDataChangedEvent`, `DataQualityIssueDetectedEvent`
- **Consumed by**: All services requiring master data

---

### 11. Analytics Domain (ADR-016)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **analytics-warehouse** | 8181 | `analytics_dw_db` | Data warehouse (star schema, ETL/ELT) |
| **analytics-olap** | 8182 | `analytics_olap_db` | OLAP cube engine, MDX queries |
| **analytics-kpi** | 8183 | `analytics_kpi_db` | KPI engine, threshold alerts |

**Integration**:
- **Consumes**: All domain events for analytics pipeline
- **Publishes**: `KPIThresholdExceededEvent`

---

### 12. Human Capital Management (HCM) Domain (ADR-034, 052, 054, 055)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **hr-travel-expense** | 9901 | `hr_travelexpense_db` | Travel & Expense Management (ADR-054) |
| **hr-contingent-workforce** | 9902 | `hr_contingent_db` | Vendor Management System / Contingent Workforce (ADR-052) |
| **hr-workforce-scheduling** | 9903 | `hr_scheduling_db` | Workforce Scheduling & Time Management (ADR-055) |

**Integration**:
- **Publishes**: `ExpenseReportSubmittedEvent`, `ContingentWorkerOnboardedEvent`, `ShiftAssignedEvent`
- **Consumes**: Finance events (for payroll integration - ADR-034)

---

### 13. Fleet Management Domain (ADR-053)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **fleet-vehicle-lifecycle** | 10001 | `fleet_vehicle_db` | Vehicle master data, lifecycle management |
| **fleet-telematics** | 10002 | `fleet_telematics_db` | Telematics data, GPS tracking, diagnostics |
| **fleet-driver-management** | 10003 | `fleet_driver_db` | Driver profiles, licensing, compliance |
| **fleet-fuel-management** | 10004 | `fleet_fuel_db` | Fuel transactions, efficiency tracking |
| **fleet-compliance** | 10005 | `fleet_compliance_db` | Regulatory compliance, inspections |

**Integration**:
- **Publishes**: `VehicleAcquiredEvent`, `MaintenanceScheduledEvent`, `FuelTransactionRecordedEvent`
- **Consumes**: Maintenance events (for vehicle maintenance), Finance events (for asset accounting)

---

### 14. Platform Services (ADR-004, 044, 045, 046, 030, 031)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **api-gateway** | 8000 | N/A | API Gateway (routing, auth, rate limiting) - ADR-004 |
| **configuration-engine** | 8201 | `config_db` | Configuration management (ADR-044) |
| **org-model-service** | 8202 | `org_model_db` | Organizational model (ADR-045) |
| **workflow-engine** | 8203 | `workflow_db` | Workflow & approval engine (ADR-046) |
| **tax-engine** | 8204 | `tax_db` | Tax calculation engine (ADR-030) |
| **period-close-orchestrator** | 8205 | `period_close_db` | Period close orchestration (ADR-031) |
| **document-management** | 8206 | `document_db` | Document attachments, versioning |
| **notification-service** | 8207 | `notification_db` | Email, SMS, push notifications |
| **audit-log-service** | 8208 | `audit_db` | Audit trail, change history (ADR-015) |

**Integration**:
- **api-gateway**: Entry point for all external requests
- **configuration-engine**: Provides tenant/user configs to all services
- **org-model-service**: Provides organizational hierarchy to all services
- **workflow-engine**: Orchestrates approvals across domains
- **tax-engine**: Calculates taxes for Sales, Procurement, Finance
- **period-close-orchestrator**: Coordinates period close across Finance modules

---

### 15. Industry Extensions

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **banking-loan-management** | 8301 | `banking_loans_db` | Loan origination, servicing, collections |
| **process-mfg-batch** | 8311 | `process_batch_db` | Batch management, genealogy |
| **utilities-meter-data** | 8321 | `utilities_meter_db` | Meter data management, billing |
| **public-sector-grants** | 8331 | `public_grants_db` | Grant management (ADR-050) |
| **insurance-claims** | 8341 | `insurance_claims_db` | Claims processing, adjudication |
| **real-estate-lease** | 8351 | `realestate_lease_db` | Lease management, rent billing |
| **advanced-inventory-batch** | 8361 | `advinv_batch_db` | Batch tracking, serial numbers |

---

### 16. Retail AI Enhancement (ADR-056, 057)

| Service | Port | Database | Technology | Purpose |
|---------|------|----------|------------|---------|
| **demand-forecasting-service** | 8401 | `retail_forecast_db` | **Python + FastAPI** | AI Demand Forecasting (ADR-056) |
| **pricing-optimization-service** | 8402 | `retail_pricing_db` | **Python + FastAPI** | Dynamic Pricing (ADR-057) |

**Integration**:
- **Consumes**: Sales history, inventory levels, promotion events, competitor prices
- **Publishes**: `DemandForecastGeneratedEvent`, `ReorderPointAdjustedEvent`, `PriceRecommendationEvent`, `MarkdownOptimizationEvent`
- **ML Models**: ARIMA, Prophet, XGBoost, LSTM, Transformers (demand forecasting); Log-log regression, XGBoost, hierarchical models (price elasticity)

---

## Shared Libraries

All shared libraries reside in `platform-shared/` and follow **strict governance rules** (ADR-006):

1. **common-types**: Type-safe primitives (`TenantId`, `Money`, `Quantity`, `Currency`, `UnitOfMeasure`)
2. **common-api**: REST API standards (`ErrorResponse`, `PageRequest`, `PageResponse`, `RateLimiting`)
3. **common-security**: AuthN/AuthZ (`JwtTokenValidator`, `OAuth2Config`, `TenantContextHolder`, `PermissionChecker`, `SeparationOfDuties`)
4. **common-observability**: Monitoring (`CorrelationId`, `TraceContext`, `MetricsCollector`, `StructuredLogging`, `PerformanceMonitor`)
5. **common-events**: Event contracts (`DomainEvent`, `EventMetadata`, `EventEnvelope`, `EventPublisher`, `EventConsumer`)
6. **common-cqrs**: CQRS primitives (`Command`, `Query`, `CommandHandler`, `QueryHandler`, `CommandBus`, `QueryBus`)
7. **common-saga**: Saga orchestration (`SagaDefinition`, `SagaStep`, `CompensatingAction`, `SagaOrchestrator`, `SagaState`)
8. **common-testing**: Testing standards (`IntegrationTest`, `E2ETest`, `ContractTest`, `TestContainers`, `TestDataBuilder`)
9. **common-resilience**: Network resilience (`CircuitBreaker`, `RetryPolicy`, `Bulkhead`, `RateLimiter`, `Timeout`)

**Forbidden in platform-shared** (ADR-006):
- ❌ Domain models
- ❌ Business logic
- ❌ Shared DTOs
- ❌ Utility classes

---

## Infrastructure & Platform

### 1. Event Streaming (ADR-003)

**Kafka Topics** (one per domain):
- `finance.events` → Journal entries, invoices, payments
- `inventory.events` → Stock movements, goods receipts, reorder points
- `sales.events` → Sales orders, fulfillment, invoices
- `manufacturing.events` → Production orders, operations, material consumption
- `quality.events` → Inspection lots, defects, stock blocks
- `maintenance.events` → Work orders, equipment downtime
- `crm.events` → Customer events, service tickets, contracts
- `mdm.events` → Master data changes, data quality issues
- `retail-ai.events` → Forecasts, reorder point adjustments, pricing recommendations

**Schema Registry**: Avro schemas stored in `platform-events/` and mirrored to Kafka Schema Registry.

---

### 2. Database Architecture (ADR-002)

**Database-per-Context Pattern**:
- Each microservice owns its database schema
- No cross-service database queries
- All communication via events or REST APIs
- PostgreSQL for OLTP workloads
- TimescaleDB for time-series data (retail AI forecasting)

**Example Databases**:
```
finance_gl_db       → finance-gl service
finance_ap_db       → finance-ap service
inventory_core_db   → inventory-core service
sales_core_db       → sales-core service
```

---

### 3. Deployment Modes

#### A. SMB Mode (Docker Compose)

**Characteristics**:
- Single host deployment
- All services in one `docker-compose.yml`
- Bundled databases (single PostgreSQL instance with multiple databases)
- Suitable for: 1-50 users, single tenant

**File**: `infrastructure/docker-compose/docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:16
    volumes:
      - pgdata:/var/lib/postgresql/data
  
  kafka:
    image: confluentinc/cp-kafka:7.5.0
  
  api-gateway:
    build: ./bounded-contexts/platform-services/api-gateway
    ports:
      - "8000:8000"
  
  finance-gl:
    build: ./bounded-contexts/finance/finance-gl
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/finance_gl_db
  
  # ... all other services
```

---

#### B. Enterprise Mode (Kubernetes)

**Characteristics**:
- Multi-node cluster (AKS/EKS/GKE)
- Separate database instances per service (RDS/Cloud SQL)
- Managed Kafka (MSK/Event Hubs)
- Auto-scaling, high availability
- Suitable for: 100+ users, multi-tenant

**File**: `infrastructure/kubernetes/services/finance/finance-gl-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: finance-gl
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: finance-gl
  template:
    metadata:
      labels:
        app: finance-gl
    spec:
      containers:
      - name: finance-gl
        image: chiroerp/finance-gl:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: finance-gl-db-secret
              key: url
```

---

### 4. Monitoring & Observability (ADR-017)

**Stack**:
- **Prometheus**: Metrics collection (CPU, memory, request latency, throughput)
- **Grafana**: Dashboards (service health, business metrics, SLA tracking)
- **Loki**: Log aggregation
- **Jaeger**: Distributed tracing
- **AlertManager**: Alerting (PagerDuty, Slack integration)

**SLA Targets** (ADR-017):
- P95 latency: < 500ms (API calls)
- Availability: 99.9% uptime
- Error rate: < 0.1%

**Dashboards**:
- `service-health-dashboard.json`: Service uptime, request rates, error rates
- `business-metrics-dashboard.json`: Sales orders, invoice processing, stock movements
- `sla-tracking-dashboard.json`: P95/P99 latency, availability, error budgets

---

## Frontend Applications (ADR-048)

### 1. Web Application (React)

**Technology Stack**:
- **React 18** with TypeScript
- **Vite** for build tooling
- **Redux Toolkit** for state management
- **React Router 6** for routing
- **Axios** for API calls
- **Material-UI (MUI)** or **Ant Design** for UI components
- **React Query** for server state management

**Structure**:
- **Modular by domain**: Each ERP module (Finance, Inventory, Sales) has its own folder
- **Shared components**: Layout, forms, tables, modals
- **Custom hooks**: `useAuth`, `useTenant`, `useApi`
- **API clients**: One per microservice (`glApiClient.ts`, `inventoryApiClient.ts`)

---

### 2. Mobile Application (React Native - Optional)

**Use Cases**:
- Warehouse management (mobile barcode scanning)
- Field service (technician dispatch)
- Approvals (mobile approval workflows)

**Technology Stack**:
- **React Native** with TypeScript
- **Expo** for managed workflow (optional)

---

### 3. Admin Portal

**Purpose**:
- Tenant management (create/update/delete tenants)
- User administration (IAM)
- System configuration (feature flags, localization)
- Monitoring dashboards (tenant usage, resource consumption)

---

## Industry Extensions

All industry extensions follow the same microservices pattern but are **optional modules** loaded only when required:

1. **Banking**: Loan management, deposit accounts, regulatory reporting (Basel III, IFRS 9)
2. **Process Manufacturing**: Batch management, formula management, process execution
3. **Utilities**: Meter data management, outage management, asset management
4. **Public Sector (ADR-050)**: Grant management, fund accounting, procurement compliance
5. **Insurance**: Policy administration, claims management, underwriting
6. **Real Estate**: Lease management, property maintenance, vacancy management
7. **Advanced Inventory**: Batch tracking, serial number management, kitting
8. **Retail AI (ADR-056, 057)**: Demand forecasting, dynamic pricing, markdown optimization

---

## Technology Stack

### Backend

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin (JVM 21) |
| **Framework** | Spring Boot 3.2 |
| **Architecture** | Microservices, CQRS, Event Sourcing |
| **Event Streaming** | Kafka (Confluent Platform) |
| **Database** | PostgreSQL 16 (OLTP), TimescaleDB (time-series) |
| **API Gateway** | Spring Cloud Gateway |
| **Service Discovery** | Kubernetes (native), Consul (optional) |
| **Configuration** | Spring Cloud Config, Kubernetes ConfigMaps |
| **Security** | OAuth 2.0, JWT, Spring Security |
| **Testing** | JUnit 5, Testcontainers, Pact (contract testing) |
| **Build** | Gradle (Kotlin DSL) |
| **Containerization** | Docker, Kubernetes |

### Frontend

| Layer | Technology |
|-------|------------|
| **Language** | TypeScript |
| **Framework** | React 18 |
| **State Management** | Redux Toolkit, React Query |
| **Routing** | React Router 6 |
| **UI Library** | Material-UI (MUI) or Ant Design |
| **Build** | Vite |
| **Testing** | Jest, React Testing Library, Playwright (E2E) |

### Retail AI (ADR-056, 057)

| Layer | Technology |
|-------|------------|
| **Language** | Python 3.11+ |
| **Framework** | FastAPI |
| **ML Libraries** | scikit-learn, XGBoost, Prophet, LSTM (TensorFlow/PyTorch), Transformers |
| **Data Processing** | Pandas, NumPy |
| **Database** | PostgreSQL, TimescaleDB (time-series) |
| **Job Scheduler** | Celery, Airflow (optional) |

### Infrastructure

| Layer | Technology |
|-------|------------|
| **Container Orchestration** | Kubernetes (AKS/EKS/GKE) |
| **Service Mesh** | Istio (optional) |
| **CI/CD** | GitHub Actions, ArgoCD (GitOps) |
| **IaC** | Terraform, Helm |
| **Monitoring** | Prometheus, Grafana, Loki, Jaeger |
| **Security Scanning** | Trivy, OWASP ZAP, Snyk |

---

## Deployment Configurations

### 1. Local Development

**Command**:
```bash
docker-compose up -d
```

**Services Started**:
- PostgreSQL (all databases)
- Kafka (all topics)
- All microservices
- API Gateway
- Grafana/Prometheus

**Access**:
- API Gateway: `http://localhost:8000`
- Grafana: `http://localhost:3000`
- Kafka UI: `http://localhost:8080`

---

### 2. Development Environment (Cloud)

**Deployment**: Kubernetes (single node)
**CI/CD**: GitHub Actions → Deploy on push to `develop` branch
**Database**: Managed PostgreSQL (single instance)
**Kafka**: Managed Kafka (3 brokers)

---

### 3. Staging Environment

**Deployment**: Kubernetes (3 nodes)
**CI/CD**: GitHub Actions → Deploy on push to `staging` branch
**Database**: Managed PostgreSQL (separate instances per service)
**Kafka**: Managed Kafka (5 brokers)
**Purpose**: Pre-production testing, UAT (ADR-019)

---

### 4. Production Environment

**Deployment**: Kubernetes (10+ nodes, auto-scaling)
**CI/CD**: GitHub Actions → Manual approval → ArgoCD GitOps
**Database**: Managed PostgreSQL (HA, read replicas)
**Kafka**: Managed Kafka (7+ brokers, multi-AZ)
**Monitoring**: 24/7 on-call rotation
**Disaster Recovery**: Daily backups, cross-region replication (ADR-018)

---

## Next Steps

### Phase 1: Project Scaffolding (Week 1-2)

1. **Create root project structure**:
   ```bash
   mkdir chiroerp
   cd chiroerp
   # Create all top-level directories
   mkdir -p platform-shared platform-events bounded-contexts frontend infrastructure tests migrations
   ```

2. **Initialize Gradle multi-module project**:
   - Create `settings.gradle.kts` with all modules
   - Create root `build.gradle.kts` with shared dependencies

3. **Setup shared libraries** (`platform-shared/`):
   - Create all 9 common modules (types, api, security, observability, events, cqrs, saga, testing, resilience)
   - Implement base interfaces and abstract classes

4. **Setup event definitions** (`platform-events/`):
   - Create Avro schemas for all domain events
   - Configure Avro code generation in Gradle

---

### Phase 2: Core Microservices (Week 3-8)

**Priority Order** (based on 18-month roadmap):

1. **Phase 1 Services** (Months 1-6):
   - `finance-gl` (General Ledger)
   - `finance-ap` (Accounts Payable)
   - `finance-ar` (Accounts Receivable)
   - `inventory-core` (Core Inventory)
   - `sales-core` (Sales Orders)
   - `procurement-core` (Purchase Orders)

2. **Platform Services**:
   - `api-gateway` (ADR-004)
   - `configuration-engine` (ADR-044)
   - `org-model-service` (ADR-045)
   - `workflow-engine` (ADR-046)

---

### Phase 3: Advanced Modules (Week 9-16)

3. **Phase 2 Services** (Months 7-12):
   - `manufacturing-bom`, `manufacturing-mrp`, `manufacturing-shop-floor`
   - `quality-inspection-planning`, `quality-execution`
   - `maintenance-equipment`, `maintenance-work-orders`
   - `inventory-warehouse` (WMS - ADR-038)
   - `crm-customer360`, `crm-contracts`

---

### Phase 4: Industry Extensions (Week 17-20)

4. **Industry Extensions**:
   - Banking (loan management)
   - Process Manufacturing (batch management)
   - Utilities (meter data)
   - Public Sector (grants, fund accounting)
   - Insurance, Real Estate, Advanced Inventory

---

### Phase 5: Retail AI Enhancement (Week 21-24) - ADR-056, 057

5. **Retail AI Services** (Phase 3.5 - optional):
   - `demand-forecasting-service` (ADR-056)
   - `pricing-optimization-service` (ADR-057)
   - Python-based microservices with FastAPI
   - ML models (ARIMA, Prophet, XGBoost, LSTM, Transformers)
   - TimescaleDB for time-series storage

---

### Phase 6: Frontend Development (Week 25-32)

6. **Frontend Applications**:
   - React web application (all modules)
   - Admin portal (tenant management)
   - Mobile application (optional)

---

### Phase 7: Infrastructure & DevOps (Ongoing)

7. **Infrastructure**:
   - Kubernetes manifests (all services)
   - Helm charts (optional)
   - Terraform IaC (AWS/Azure/GCP)
   - CI/CD pipelines (GitHub Actions)
   - Monitoring (Prometheus, Grafana, Loki)

---

### Phase 8: Testing & Quality (Ongoing)

8. **Testing**:
   - Unit tests (JUnit 5)
   - Integration tests (Testcontainers)
   - Contract tests (Pact)
   - E2E tests (Playwright)
   - Performance tests (JMeter/Gatling) - ADR-017
   - Security tests (OWASP ZAP) - ADR-008

---

## Implementation Checklist

### ✅ Completed

- [x] 56+ ADRs covering all domains
- [x] Architecture documentation (retail AI, gap-to-SAP roadmap)
- [x] Workspace structure design (**THIS FILE**)

### 🔄 In Progress

- [ ] Project scaffolding (directories, Gradle setup)
- [ ] Shared libraries implementation
- [ ] Event schema definitions

### ⏳ Pending

- [ ] Core microservices implementation (Finance, Inventory, Sales)
- [ ] Platform services (API Gateway, Configuration Engine, Org Model, Workflow)
- [ ] Advanced modules (Manufacturing, Quality, Maintenance, CRM, MDG, Analytics)
- [ ] Industry extensions (Banking, Process Mfg, Utilities, Public Sector, etc.)
- [ ] Retail AI services (Demand Forecasting, Dynamic Pricing)
- [ ] Frontend applications (React web app, admin portal)
- [ ] Infrastructure (Kubernetes, Terraform, CI/CD)
- [ ] Testing (unit, integration, contract, E2E, performance, security)
- [ ] Documentation (API docs, runbooks, user guides)

---

## Additional Resources

- **Main Roadmap**: [`docs/architecture/gap-to-sap-grade-roadmap.md`](./gap-to-sap-grade-roadmap.md)
- **Retail AI Architecture**: [`docs/architecture/retail/retail-ai-architecture.md`](./retail/retail-ai-architecture.md)
- **ADRs**: [`docs/adr/`](../adr/)

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-01-XX | AI Agent | Initial comprehensive workspace structure based on 56+ ADRs |

---

**END OF DOCUMENT**
