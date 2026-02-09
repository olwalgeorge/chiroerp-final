# ChiroERP - Cloud-Native Multi-Tenant ERP Platform

> **Enterprise-Grade ERP System for Africa and Emerging Markets**
> **Technology**: Quarkus 3.29.0 + Kotlin 2.2.0 + PostgreSQL + Kafka
> **Architecture**: Microservices + Event-Driven + CQRS + Multi-Tenancy
> **Status**: 🚧 Active Development | 📐 Architecture Complete | 🏗️ Build System Ready

---

## 📋 Quick Links

- [Architecture Overview](docs/architecture/README.md) - 12 domains, 92 modules, 57+ ADRs
- [Build System Guide](docs/architecture/BUILD_SYSTEM_ADOPTION.md) - Gradle 9.0 + Convention Plugins
- [Deployment Guide](docs/architecture/DEPLOYMENT-GUIDE.md) - 3-tier topology (SMB → Enterprise)
- [MVP Infra Prep](docs/MVP-INFRA-PREIMPLEMENTATION.md) - Local pre-implementation runtime baseline
- [MVP Implementation Roadmap](docs/MVP-IMPLEMENTATION-ROADMAP.md) - Trackable tenancy-identity + finance execution plan
- [Config Engine Design](docs/architecture/CONFIG-ENGINE-IMPLEMENTATION.md) - Adaptability framework
- [ADR Index](docs/adr/README.md) - All architectural decisions
- [Workspace Structure](docs/architecture/WORKSPACE-STRUCTURE.md) - Target file tree

---

## 🎯 What is ChiroERP?

ChiroERP is a **cloud-native, microservices-based ERP system** designed for **multi-tenant SaaS deployment** with optional on-premise support. Built specifically for **Africa and emerging markets**, with first-class support for local regulatory requirements (Kenya eTIMS, URA e-invoicing, etc.).

### Key Differentiators

✅ **60-83% cheaper than SAP** across all deployment tiers
✅ **Multi-region deployment** (Primary: Africa, DR: Europe, Backup: Asia)
✅ **Configurable Business Rules** (Drools engine - ADR-044)
✅ **Event-Driven Architecture** (Kafka - ADR-003, ADR-020)
✅ **Database-per-Context** (Independent scaling - ADR-002)
✅ **Modular CQRS** (Command/Query separation - ADR-001)
✅ **API-First Design** (REST + GraphQL via gateway - ADR-004)

---

## 🏗️ Architecture Highlights

### Domain Coverage (12 Domains, 92 Modules)

| Domain | Services | Key Features | ADRs |
|--------|----------|--------------|------|
| **Finance** | 7 | GL, AP, AR, Assets, Treasury, Intercompany, Lease | ADR-009, 021, 022, 026, 029, 033 |
| **Controlling** | 4 | Cost Center, Profitability, Product Costing, Budgeting | ADR-028, 032 |
| **Inventory** | 4 | Core, ATP, Valuation, Warehouse | ADR-024, 038 |
| **Sales** | 4 | Core, Pricing, Credit, Shipping | ADR-025, 057 |
| **Procurement** | 2 | Core, Sourcing | ADR-023 |
| **Manufacturing** | 4 | BOM, MRP, Shop Floor, Costing | ADR-037 |
| **Quality** | 3 | Planning, Execution, CAPA | ADR-039 |
| **Maintenance** | 3 | Equipment, Work Orders, Preventive | ADR-040 |
| **CRM** | 3 | Customer360, Contracts, Dispatch | ADR-043 |
| **MDM** | 2 | Hub, Data Quality | ADR-027 |
| **Analytics** | 3 | Warehouse, OLAP, KPI | ADR-016 |
| **HCM** | 3 | T&E, Contingent, Scheduling | ADR-034, 052, 054, 055 |
| **Fleet** | 5 | Lifecycle, Telematics, Driver, Fuel, Compliance | ADR-053 |

### Technology Stack

```yaml
Framework: Quarkus 3.29.0 (cloud-native, native image support)
Language: Kotlin 2.2.0 (progressive mode, strict null-safety)
JVM: Java 21 (Eclipse Temurin)
Database: PostgreSQL 16 (database-per-context)
Messaging: Kafka 3.8.0 + Avro (event-driven integration)
Cache: Redis (config engine, session management)
Rules Engine: Drools 8.44.0 (ADR-044 config framework)
API Gateway: Quarkus + routing + rate limiting
Observability: OpenTelemetry + Micrometer + Prometheus
Build: Gradle 9.0 + Kotlin DSL + Convention Plugins
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (auto-downloaded by Gradle if not found)
- **Docker** (for PostgreSQL, Kafka, Redis)
- **Git** (version control)

### 1. Clone Repository

```bash
git clone https://github.com/your-org/chiroerp.git
cd chiroerp
```

### 2. Build Project

```bash
# Build all modules
./gradlew build

# List all discovered modules
./gradlew projects

# Build specific module
./gradlew :platform-shared:common-types:build
```

### 3. Run Sample Module (when available)

```bash
# Run Finance GL service in dev mode (live reload)
./gradlew :bounded-contexts:finance:finance-gl:quarkusDev

# Access Quarkus Dev UI: http://localhost:8081/q/dev/
```

### 4. Run Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :platform-shared:common-types:test
```

---

## 📁 Repository Structure

```
chiroerp/
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Multi-project setup (auto-discovery)
├── gradle.properties                   # Global Gradle settings
├── gradle/
│   └── libs.versions.toml              # Version catalog (Quarkus 3.29.0, Kotlin 2.2.0)
│
├── build-logic/                        # Convention plugins (composite build)
│   ├── src/main/kotlin/chiroerp/buildlogic/
│   │   ├── KotlinConventionsPlugin.kt  # Base Kotlin + Java 21 + Testing
│   │   ├── QuarkusConventionsPlugin.kt # Quarkus microservices
│   │   └── NativeImageConventionsPlugin.kt # Native executables
│   └── build.gradle.kts
│
├── platform-shared/                    # Cross-cutting concerns (ADR-006)
│   ├── common-types/                   # ✅ CREATED: Value objects, CQRS interfaces
│   ├── common-messaging/               # 🔄 TODO: Kafka abstractions
│   ├── common-security/                # 🔄 TODO: Auth/AuthZ
│   ├── common-observability/           # 🔄 TODO: Tracing, metrics
│   └── config-engine/                  # 🔄 TODO: Drools integration (ADR-044)
│
├── platform-infrastructure/            # Infrastructure services
│   ├── cqrs/                           # 🔄 TODO: Command/Query buses
│   ├── eventing/                       # 🔄 TODO: Event sourcing
│   └── monitoring/                     # 🔄 TODO: Health checks
│
├── bounded-contexts/                   # Domain services (92 modules)
│   ├── finance/                        # 🔄 TODO: 7 services (Ports 8081-8087)
│   ├── inventory/                      # 🔄 TODO: 4 services
│   ├── sales/                          # 🔄 TODO: 4 services
│   ├── procurement/                    # 🔄 TODO: 2 services
│   ├── manufacturing/                  # 🔄 TODO: 4 services
│   ├── quality/                        # 🔄 TODO: 3 services
│   ├── maintenance/                    # 🔄 TODO: 3 services
│   ├── crm/                            # 🔄 TODO: 3 services
│   ├── mdm/                            # 🔄 TODO: 2 services
│   ├── analytics/                      # 🔄 TODO: 3 services
│   ├── hcm/                            # 🔄 TODO: 3 services
│   └── fleet/                          # 🔄 TODO: 5 services
│
├── api-gateway/                        # 🔄 TODO: API Gateway (Port 8080)
├── portal/                             # 🔄 TODO: Frontend (React/Vue)
│
├── docs/                               # ✅ COMPLETE: 57+ ADRs, architecture docs
│   ├── adr/                            # Architecture Decision Records
│   ├── architecture/                   # Architecture documentation
│   │   ├── README.md                   # ✅ Architecture index
│   │   ├── BUILD_SYSTEM_ADOPTION.md    # ✅ Build system guide
│   │   ├── DEPLOYMENT-GUIDE.md         # ✅ 3-tier deployment topology
│   │   ├── CONFIG-ENGINE-IMPLEMENTATION.md # ✅ Config engine MVP design
│   │   └── WORKSPACE-STRUCTURE.md      # ✅ Target file tree
│
├── COMPLETE_STRUCTURE.txt              # Canonical blueprint and target structure
│
├── scripts/                            # ✅ CI validation scripts
│   └── validate-docs.ps1               # Documentation validation
│
└── .github/                            # ✅ CI/CD workflows
    └── workflows/
        └── ci-docs.yml                 # Documentation CI pipeline
```

---

## 📚 Documentation

### Architecture Documentation

- **[Architecture README](docs/architecture/README.md)** - Complete overview of 12 domains, 92 modules, port assignments
- **[ADR Index](docs/adr/README.md)** - 57+ architectural decision records
- **[Workspace Structure](docs/architecture/WORKSPACE-STRUCTURE.md)** - Target directory tree for full implementation
- **[Deployment Guide](docs/architecture/DEPLOYMENT-GUIDE.md)** - 3-tier topology with cost analysis
- **[Config Engine Design](docs/architecture/CONFIG-ENGINE-IMPLEMENTATION.md)** - Drools-based adaptability framework
- **[Build System Guide](docs/architecture/BUILD_SYSTEM_ADOPTION.md)** - Gradle 9.0 setup and usage

### Key ADRs

| ADR | Title | Summary |
|-----|-------|---------|
| **ADR-001** | [Modular CQRS](docs/adr/ADR-001-modular-cqrs.md) | Command/Query separation per bounded context |
| **ADR-002** | [Database-per-Context](docs/adr/ADR-002-database-per-context.md) | Each microservice owns its database schema |
| **ADR-003** | [Event-Driven Integration](docs/adr/ADR-003-event-driven-integration.md) | Kafka for async communication |
| **ADR-004** | [API Gateway Pattern](docs/adr/ADR-004-api-gateway-pattern.md) | Single entry point for all services |
| **ADR-005** | [Multi-Tenancy Isolation](docs/adr/ADR-005-multi-tenancy-isolation.md) | Tenant discriminator in all aggregates |
| **ADR-044** | [Configuration Framework](docs/adr/ADR-044-configuration-rules-framework.md) | Drools-based business rules engine |

---

## 🔧 Development Workflow

### Creating a New Module

See [Build System Guide](docs/architecture/BUILD_SYSTEM_ADOPTION.md#module-creation-guide) for detailed instructions.

**Quick Example**:

```bash
# 1. Create directory
mkdir -p bounded-contexts/finance/finance-gl/src/main/kotlin/com/chiroerp/finance/gl

# 2. Create build.gradle.kts
cat > bounded-contexts/finance/finance-gl/build.gradle.kts << 'EOF'
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(projects.platformShared.commonTypes)
    implementation(libs.bundles.quarkus.persistence)
}
EOF

# 3. Verify discovery
./gradlew projects  # Should show :bounded-contexts:finance:finance-gl

# 4. Build
./gradlew :bounded-contexts:finance:finance-gl:build
```

### Running in Dev Mode

```bash
# Start service with live reload
./gradlew :bounded-contexts:finance:finance-gl:quarkusDev

# Access:
# - Service: http://localhost:8081
# - Dev UI: http://localhost:8081/q/dev/
```

### Building Native Executable

```bash
# Build native executable (requires GraalVM)
./gradlew :module:build -Dquarkus.package.type=native

# Build in container (no local GraalVM required - RECOMMENDED)
./gradlew :module:buildNativeContainer
```

---

## 🧪 Testing

### Test Stack

- **JUnit 5** (unit testing)
- **MockK** (Kotlin mocking)
- **Kotest** (assertions and property-based testing)
- **Quarkus Test** (integration testing)
- **REST Assured** (API testing)
- **Testcontainers** (PostgreSQL, Kafka)

### Running Tests

```bash
# All tests
./gradlew test

# Specific module tests
./gradlew :platform-shared:common-types:test

# With coverage
./gradlew test jacocoTestReport
```

---

## 📊 Current Status

### ✅ Completed

- **Architecture Design**: 57+ ADRs, 12 domains, 92 modules planned
- **Build System**: Gradle 9.0 + Convention Plugins + Version Catalog
- **CI Infrastructure**: Documentation validation pipeline
- **Deployment Planning**: 3-tier topology with cost analysis
- **Config Engine Design**: Drools-based framework (6-week MVP roadmap)
- **Sample Module**: `platform-shared/common-types` with Money value object

### 🔄 In Progress

- **Platform Infrastructure**: CQRS, eventing, monitoring modules
- **Finance Domain**: 7 services (GL, AP, AR, Assets, Treasury, Intercompany, Lease)
- **Config Engine MVP**: Implementation (database, Drools integration, admin UI)

### 📅 Roadmap

- **Q1 2026**: Platform infrastructure + Finance domain (7 services)
- **Q2 2026**: Inventory, Sales, Procurement domains (10 services)
- **Q3 2026**: Manufacturing, Quality, Maintenance, CRM domains (13 services)
- **Q4 2026**: MDM, Analytics, HCM, Fleet domains (13 services) + API Gateway + Portal

---

## 🤝 Contributing

### Before Contributing

1. Read [Architecture README](docs/architecture/README.md)
2. Review [Build System Guide](docs/architecture/BUILD_SYSTEM_ADOPTION.md)
3. Understand [ADR-001 (CQRS)](docs/adr/ADR-001-modular-cqrs.md)
4. Follow Kotlin coding conventions

### Development Process

1. Create feature branch: `feature/finance-gl-journal-posting`
2. Follow module creation guide
3. Run tests: `./gradlew test`
4. Validate architecture: `./gradlew checkArchitecture`
5. Create pull request

---

## 📞 Contact & Support

- **Documentation**: [docs/](docs/)
- **Issues**: GitHub Issues (coming soon)
- **Architecture Questions**: See [Architecture README](docs/architecture/README.md)
- **Build Issues**: See [Build System Guide](docs/architecture/BUILD_SYSTEM_ADOPTION.md)

---

## 📜 License

[To be determined - likely Apache 2.0 or similar]

---

## 🎯 Vision

**"Enterprise-grade ERP for Africa and emerging markets at 1/5th the cost of SAP"**

ChiroERP aims to democratize access to world-class ERP systems by providing:
- Modern cloud-native architecture
- Regulatory compliance for African markets
- Configurable business rules (no code changes)
- Multi-tenant SaaS deployment
- Predictable, affordable pricing

---

> **Status**: 🚧 Active Development
> **Last Updated**: February 3, 2026
> **Build System**: ✅ Ready
> **Architecture**: ✅ Complete
> **Implementation**: 🔄 In Progress (1/92 modules)
