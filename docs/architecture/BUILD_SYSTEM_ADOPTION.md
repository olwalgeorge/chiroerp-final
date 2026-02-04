# ChiroERP Build System - Complete Implementation Guide

> **Status**: ✅ Implemented
> **Date**: February 3, 2026
> **Source**: Adapted from erp-platform BUILD_SYSTEM_UPDATE.md
> **Technology Stack**: Gradle 9.0 + Kotlin DSL + Quarkus 3.29.0 + Java 21

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [What's Been Created](#whats-been-created)
3. [Quick Start](#quick-start)
4. [Convention Plugins](#convention-plugins)
5. [Version Catalog](#version-catalog)
6. [Module Creation Guide](#module-creation-guide)
7. [Common Workflows](#common-workflows)
8. [Architecture Compliance](#architecture-compliance)
9. [Next Steps](#next-steps)

---

## Overview

ChiroERP now has a **production-ready, enterprise-grade build system** following the patterns from erp-platform's BUILD_SYSTEM_UPDATE.md. The build system supports:

### Key Features

✅ **Gradle 9.0 with Kotlin DSL** - Modern, type-safe build configuration
✅ **Automatic Module Discovery** - No manual `include()` statements needed
✅ **Convention Plugins** - Shared build logic (kotlin, quarkus, native-image)
✅ **Version Catalog** - Centralized dependency management (gradle/libs.versions.toml)
✅ **Type-Safe Project Accessors** - Compile-time safe inter-module dependencies
✅ **Java 21 Toolchain** - Automatic JDK download and configuration
✅ **Quarkus 3.29.0** - Cloud-native framework for microservices
✅ **Multi-Project Structure** - Ready for 92 modules across 12 domains
✅ **Parallel Builds** - Fast compilation with parallel execution
✅ **Architecture Validation** - Automated ADR compliance checks

### Architecture Alignment

| ADR | Principle | Build System Support |
|-----|-----------|---------------------|
| **ADR-001** | Modular CQRS | ✅ Convention plugins enforce CQRS structure |
| **ADR-002** | Database-per-Context | ✅ Each module has independent dependencies |
| **ADR-006** | Platform Shared Governance | ✅ Architecture validation task prevents violations |
| **ADR-044** | Configuration Framework | ✅ Drools dependencies in version catalog |

---

## What's Been Created

### 🏗️ Root Build Files

```
chiroerp/
├── build.gradle.kts                    ✅ Root build configuration
├── settings.gradle.kts                 ✅ Multi-project setup with auto-discovery
├── gradle.properties                   ✅ Global Gradle configuration
└── gradle/
    └── libs.versions.toml              ✅ Version catalog (Quarkus 3.29.0, Kotlin 2.2.0)
```

### 🔧 Convention Plugins (build-logic/)

```
build-logic/
├── build.gradle.kts                    ✅ Convention plugin project config
├── settings.gradle.kts                 ✅ Composite build setup
└── src/main/kotlin/chiroerp/buildlogic/
    ├── KotlinConventionsPlugin.kt      ✅ Base Kotlin/JVM + Java 21 + Testing
    ├── QuarkusConventionsPlugin.kt     ✅ Quarkus 3.29.0 microservices
    └── NativeImageConventionsPlugin.kt ✅ GraalVM native executable builds
```

### 📦 Sample Module (platform-shared/common-types)

```
platform-shared/
└── common-types/
    ├── build.gradle.kts                ✅ Module build with architecture validation
    └── src/main/kotlin/com/chiroerp/shared/types/
        ├── valueobjects/
        │   └── Money.kt                ✅ Money/Currency value objects (DDD)
        └── cqrs/
            └── CQRS.kt                 ✅ Command/Query/Handler interfaces
```

---

## Quick Start

### Prerequisites

- **Java 21** (auto-downloaded by Gradle if not found)
- **Gradle 9.0** (included via wrapper - use `./gradlew`)
- **Git** (for version control)

### Initial Setup

1. **Generate Gradle Wrapper** (if not present)
   ```powershell
   gradle wrapper --gradle-version=9.0 --distribution-type=all
   ```

2. **Verify Setup**
   ```powershell
   ./gradlew --version
   # Should show: Gradle 9.0, Kotlin DSL
   ```

3. **List Discovered Modules**
   ```powershell
   ./gradlew projects
   # Shows all modules found by auto-discovery
   ```

4. **Build Sample Module**
   ```powershell
   ./gradlew :platform-shared:common-types:build
   ```

### Current Module Structure

As of now, ChiroERP has:
- ✅ **1 module**: `platform-shared:common-types` (sample)
- 🔄 **91 modules to create**: Finance, Inventory, Sales, etc.

---

## Convention Plugins

ChiroERP provides **3 hierarchical convention plugins** that encapsulate shared build logic.

### 1. `chiroerp.kotlin-conventions`

**Purpose**: Base layer for all Kotlin modules

**Features**:
- Kotlin JVM plugin (2.2.0)
- Java 21 toolchain (Adoptium/Eclipse Temurin)
- Progressive Kotlin mode
- Strict null-safety (JSR-305)
- JUnit 5 + MockK + Kotest testing stack
- Parallel test execution

**When to Use**: Domain modules, libraries without framework dependencies

**Example** (`platform-shared/common-types/build.gradle.kts`):
```kotlin
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(libs.vavr)
    implementation(libs.commons.lang3)
}
```

### 2. `chiroerp.quarkus-conventions`

**Purpose**: Quarkus microservices (extends kotlin-conventions)

**Features**:
- Inherits all kotlin-conventions features
- Quarkus 3.29.0 application plugin
- Quarkus BOM for dependency management
- Core extensions: REST, Jackson, Hibernate Validator, Arc (CDI)
- Kotlin support (Jackson module, allopen/jpa plugins)
- Quarkus testing framework (JUnit5, REST Assured)
- Dev mode support (live reload)

**When to Use**: Application services, REST APIs, microservices

**Example** (`bounded-contexts/finance/finance-gl/build.gradle.kts`):
```kotlin
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    // Domain layer
    implementation(projects.boundedContexts.finance.financeGlDomain)

    // Platform utilities
    implementation(projects.platformShared.commonTypes)
    implementation(projects.platformShared.commonMessaging)

    // Database
    implementation(libs.bundles.quarkus.persistence)

    // Messaging
    implementation(libs.bundles.quarkus.messaging)
}
```

### 3. `chiroerp.native-image-conventions`

**Purpose**: Production-ready native executables (extends quarkus-conventions)

**Features**:
- Inherits all quarkus-conventions features
- Elytron security configuration
- OpenTelemetry observability
- PostgreSQL native driver
- Redis cache support
- Container-based build support (no local GraalVM required)

**When to Use**: Production deployments requiring fast startup and low memory

**Example**:
```kotlin
plugins {
    id("chiroerp.native-image-conventions")
}

// Build native executable
// ./gradlew build -Dquarkus.package.type=native

// Build in container (recommended - no local GraalVM needed)
// ./gradlew buildNativeContainer
```

---

## Version Catalog

All dependencies are centralized in `gradle/libs.versions.toml`.

### Key Versions

```toml
[versions]
kotlin = "2.2.0"
quarkus = "3.29.0"
java = "21"
junit = "5.11.2"
mockk = "1.13.12"
kotest = "5.9.1"
postgresql = "42.7.3"
kafka = "3.8.0"
drools = "8.44.0.Final"  # ADR-044 Config Engine
```

### Using Dependencies

#### Individual Libraries
```kotlin
dependencies {
    implementation(libs.quarkus.rest)
    implementation(libs.postgresql.driver)
    testImplementation(libs.mockk)
}
```

#### Bundles (Grouped Dependencies)
```kotlin
dependencies {
    implementation(libs.bundles.quarkus.core)          // Arc, REST, Jackson, Validator
    implementation(libs.bundles.quarkus.persistence)   // Hibernate ORM, JDBC, Flyway
    implementation(libs.bundles.testing.core)          // JUnit, MockK, AssertJ
}
```

#### Platform BOM (Bill of Materials)
```kotlin
dependencies {
    implementation(platform(libs.quarkus.bom))  // Quarkus version alignment
}
```

### Available Bundles

| Bundle | Contents |
|--------|----------|
| `quarkus-core` | Arc, REST, Jackson, Validator |
| `quarkus-persistence` | Hibernate ORM, PostgreSQL JDBC, Flyway |
| `quarkus-messaging` | Kafka, Avro, SmallRye Reactive Messaging |
| `quarkus-observability` | Micrometer, Prometheus, OpenTelemetry, JSON logging |
| `quarkus-security` | Security, OIDC, Elytron JDBC |
| `kotlin-core` | Stdlib, Reflect |
| `kotlin-coroutines` | Coroutines Core, Reactor |
| `testing-core` | JUnit 5, MockK, AssertJ |
| `testing-kotest` | Kotest Runner, Assertions, Property |
| `testing-quarkus` | Quarkus JUnit5, REST Assured |
| `testing-containers` | Testcontainers (PostgreSQL, Kafka) |
| `drools` | Drools Core, Compiler, MVEL, Engine |
| `jackson` | Jackson Kotlin module, JSR-310 dates |
| `security` | JJWT, BCrypt |

---

## Module Creation Guide

### Step 1: Create Directory Structure

```powershell
# Example: Create a new Finance GL service
mkdir -p bounded-contexts/finance/finance-gl/src/main/kotlin/com/chiroerp/finance/gl
mkdir -p bounded-contexts/finance/finance-gl/src/main/resources
mkdir -p bounded-contexts/finance/finance-gl/src/test/kotlin/com/chiroerp/finance/gl
```

### Step 2: Create `build.gradle.kts`

```kotlin
/*
 * Finance - General Ledger Service
 *
 * Core accounting module for journal entries, chart of accounts, and period management.
 * Architecture: ADR-001 (CQRS), ADR-002 (Database-per-Context), ADR-009 (Finance Domain)
 * Port: 8081
 */

plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    // Platform shared
    implementation(projects.platformShared.commonTypes)
    implementation(projects.platformShared.commonMessaging)
    implementation(projects.platformShared.commonSecurity)

    // Database
    implementation(libs.bundles.quarkus.persistence)
    implementation(libs.flyway.database.postgresql)

    // Messaging (Kafka)
    implementation(libs.bundles.quarkus.messaging)

    // Observability
    implementation(libs.bundles.quarkus.observability)

    // Testing
    testImplementation(libs.bundles.testing.quarkus)
    testImplementation(libs.bundles.testing.containers)
}

// Service configuration
quarkus {
    quarkusDev {
        // Dev mode runs on port 8081
        jvmArgs = listOf("-Dquarkus.http.port=8081")
    }
}
```

### Step 3: Verify Module Discovery

```powershell
./gradlew projects

# Should show:
# :bounded-contexts:finance:finance-gl
```

### Step 4: Create Source Code

```kotlin
// src/main/kotlin/com/chiroerp/finance/gl/FinanceGLResource.kt
package com.chiroerp.finance.gl

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/api/v1/finance/gl")
class FinanceGLResource {

    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    fun health(): String = "Finance GL service is running"
}
```

### Step 5: Build and Test

```powershell
# Build the module
./gradlew :bounded-contexts:finance:finance-gl:build

# Run tests
./gradlew :bounded-contexts:finance:finance-gl:test

# Run in dev mode (live reload)
./gradlew :bounded-contexts:finance:finance-gl:quarkusDev

# Access: http://localhost:8081/api/v1/finance/gl/health
```

---

## Common Workflows

### Build Everything

```powershell
./gradlew build
# or
./gradlew buildAll
```

### Run All Tests

```powershell
./gradlew test
# or
./gradlew testAll
```

### List Modules by Domain

```powershell
./gradlew listModules

# Output:
# === ChiroERP Module Structure ===
#
# 📦 Finance Domain (7 modules)
#    └─ :bounded-contexts:finance:finance-gl
#    └─ :bounded-contexts:finance:finance-ap
#    ... (91 more to create)
```

### Check Architecture Compliance

```powershell
./gradlew checkArchitecture

# Validates:
# - Domain modules don't depend on frameworks (ADR-001)
# - Platform-shared doesn't depend on bounded contexts (ADR-006)
```

### Dev Mode (Live Reload)

```powershell
# Run specific service in dev mode
./gradlew :bounded-contexts:finance:finance-gl:quarkusDev

# Access Quarkus Dev UI: http://localhost:8081/q/dev/
```

### Build Native Executable

```powershell
# Option 1: With local GraalVM
./gradlew :module-name:build -Dquarkus.package.type=native

# Option 2: In container (no local GraalVM required - RECOMMENDED)
./gradlew :module-name:buildNativeContainer

# Output: module-name/build/module-name-runner (native executable)
```

### Clean Build

```powershell
./gradlew clean build
```

### Dependency Analysis

```powershell
# Show all dependencies for a module
./gradlew :platform-shared:common-types:dependencies

# Check for dependency updates
./gradlew dependencyUpdates  # Requires gradle-versions plugin
```

---

## Architecture Compliance

The build system enforces ChiroERP's architectural principles through automated validation.

### Automated Checks

#### 1. Domain Purity (ADR-001)

**Rule**: Domain modules must not depend on infrastructure frameworks

```kotlin
// ❌ VIOLATION: Domain depends on Quarkus
plugins {
    id("chiroerp.kotlin-conventions")
}
dependencies {
    implementation("io.quarkus:quarkus-rest")  // ❌ Not allowed in domain
}

// ✅ CORRECT: Domain uses only pure Kotlin
plugins {
    id("chiroerp.kotlin-conventions")
}
dependencies {
    implementation(projects.platformShared.commonTypes)  // ✅ Shared kernel OK
    implementation(libs.vavr)                            // ✅ Utilities OK
}
```

#### 2. Platform Shared Governance (ADR-006)

**Rule**: Platform-shared must not depend on bounded contexts

```kotlin
// ❌ VIOLATION: Platform-shared depends on domain
// platform-shared/common-security/build.gradle.kts
dependencies {
    implementation(projects.boundedContexts.finance.financeGl)  // ❌ Not allowed
}

// ✅ CORRECT: Platform-shared is independent
dependencies {
    implementation(projects.platformShared.commonTypes)  // ✅ Peer dependency OK
    implementation(libs.bundles.security)                // ✅ External library OK
}
```

### Running Validation

```powershell
# Validate architecture rules
./gradlew checkArchitecture

# Output (if violations found):
# ❌ VIOLATION: Domain module :bounded-contexts:finance:finance-gl-domain depends on framework: quarkus-rest
# See ADR-001 (CQRS), ADR-002 (Database-per-Context), ADR-006 (Platform Governance)
```

### Enforcing in CI

Add to `.github/workflows/ci-build.yml`:

```yaml
- name: Validate Architecture
  run: ./gradlew checkArchitecture
```

---

## Next Steps

### Phase 1: Core Platform Infrastructure (Weeks 1-2)

Create foundation modules:

```
✅ platform-shared/common-types         (Created)
⬜ platform-shared/common-messaging     (Kafka abstractions)
⬜ platform-shared/common-security      (Auth/AuthZ)
⬜ platform-shared/common-observability (Tracing, metrics)
⬜ platform-shared/config-engine        (ADR-044 - Drools integration)
⬜ platform-infrastructure/cqrs         (Command/Query buses)
⬜ platform-infrastructure/eventing     (Event sourcing)
⬜ platform-infrastructure/monitoring   (Health checks)
```

**Command to create each**:
```powershell
mkdir -p platform-shared/common-messaging/src/main/kotlin/com/chiroerp/shared/messaging
# Then add build.gradle.kts with chiroerp.quarkus-conventions
```

### Phase 2: Finance Domain (Weeks 3-6)

Implement 7 Finance services (ADR-009):

```
⬜ bounded-contexts/finance/finance-gl         (Port 8081)
⬜ bounded-contexts/finance/finance-ap         (Port 8082)
⬜ bounded-contexts/finance/finance-ar         (Port 8083)
⬜ bounded-contexts/finance/finance-assets     (Port 8084)
⬜ bounded-contexts/finance/finance-treasury   (Port 8085)
⬜ bounded-contexts/finance/finance-intercompany (Port 8086)
⬜ bounded-contexts/finance/finance-lease      (Port 8087)
```

**Template for each service**:
```kotlin
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(projects.platformShared.commonTypes)
    implementation(libs.bundles.quarkus.persistence)
    implementation(libs.bundles.quarkus.messaging)
}
```

### Phase 3: Remaining Domains (Weeks 7-18)

Create 85 more modules across 11 domains:
- Inventory (4 services)
- Sales (4 services)
- Procurement (2 services)
- Manufacturing (4 services)
- Quality (3 services)
- Maintenance (3 services)
- CRM (3 services)
- Master Data (2 services)
- Analytics (3 services)
- HCM (3 services)
- Fleet (5 services)

### Phase 4: API Gateway & Portal (Weeks 19-20)

```
⬜ api-gateway/          (Routing, rate limiting, auth)
⬜ portal/               (React/Vue frontend)
```

### Phase 5: CI/CD Integration (Week 21)

Update `.github/workflows/ci-build.yml`:

```yaml
name: Build All Services

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2

      - name: Build All Modules
        run: ./gradlew build --no-daemon --parallel

      - name: Validate Architecture
        run: ./gradlew checkArchitecture

      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: '**/build/reports/tests/'
```

---

## Summary

### What You Now Have

✅ **Enterprise-Grade Build System**: Gradle 9.0 + Kotlin DSL + Quarkus 3.29.0
✅ **Convention Plugins**: Reusable build logic for all 92 modules
✅ **Version Catalog**: Centralized dependency management
✅ **Automatic Module Discovery**: No manual includes needed
✅ **Architecture Validation**: Automated ADR compliance checks
✅ **Sample Module**: `platform-shared/common-types` with Money value object
✅ **Type-Safe Accessors**: Compile-time safe inter-module dependencies
✅ **Parallel Builds**: Fast compilation across all modules
✅ **Dev Mode Support**: Live reload for rapid development
✅ **Native Image Support**: Production-ready fast startup builds

### What's Next

🔄 **Create 91 more modules** following the patterns established
🔄 **Implement Config Engine MVP** (ADR-044) - 6-week roadmap in CONFIG-ENGINE-IMPLEMENTATION.md
🔄 **CI/CD integration** with architecture validation
🔄 **Docker/Kubernetes deployment** configurations

---

## References

- **Source Document**: `c:\Users\PC\coding\complete\erp-platform\docs\BUILD_SYSTEM_UPDATE.md`
- **ChiroERP ADRs**: `docs/adr/` (57+ architectural decisions)
- **Architecture Overview**: `docs/architecture/README.md`
- **Deployment Guide**: `docs/architecture/DEPLOYMENT-GUIDE.md`
- **Workspace Structure**: `docs/architecture/WORKSPACE-STRUCTURE.md`
- **Config Engine**: `docs/architecture/CONFIG-ENGINE-IMPLEMENTATION.md`
- **Gradle Docs**: https://docs.gradle.org/9.0/userguide/userguide.html
- **Quarkus Guides**: https://quarkus.io/guides/
- **Kotlin DSL**: https://docs.gradle.org/current/userguide/kotlin_dsl.html

---

> **Last Updated**: February 3, 2026
> **Author**: GitHub Copilot (based on BUILD_SYSTEM_UPDATE.md patterns)
> **Status**: ✅ Build system fully implemented, ready for module creation
