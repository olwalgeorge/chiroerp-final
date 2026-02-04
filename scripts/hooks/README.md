# Architecture Compliance Pre-Commit Hooks

This directory contains pre-commit hooks that enforce architectural standards across the ChiroERP codebase.

## 🎯 Purpose

These hooks ensure compliance with:
- **ADR-001**: CQRS with Use Cases
- **ADR-005**: Multi-Tenancy Isolation
- **ADR-009**: Financial Accounting Domain (DDD)
- **ADR-020**: Event-Driven Architecture

## 📋 Hooks Overview

### 1. `check-hexagonal-architecture.ps1`
**Enforces**: Domain layer isolation (Hexagonal Architecture)

**Validates**:
- ✅ Domain layer has NO infrastructure dependencies
- ✅ No Jakarta, Quarkus, Hibernate imports in domain
- ✅ No JPA annotations in domain models
- ✅ Domain depends only on standard library + shared kernel

**Example Violation**:
```kotlin
// ❌ BAD: domain/JournalEntry.kt
import jakarta.persistence.Entity  // FORBIDDEN!

@Entity
data class JournalEntry(...)
```

**How to Fix**: Move JPA entities to `infrastructure/adapter/output/persistence/`

---

### 2. `check-usecase-dependencies.ps1`
**Enforces**: Use cases depend on ports, not adapters (Dependency Inversion)

**Validates**:
- ✅ Use cases inject port interfaces
- ✅ Use cases do NOT inject concrete repositories
- ✅ Use cases do NOT inject Kafka emitters directly

**Example Violation**:
```kotlin
// ❌ BAD: application/usecase/CreateJournalEntryUseCaseImpl.kt
@Inject
lateinit var repository: JournalEntryRepository  // Should be JournalEntryRepositoryPort
```

**How to Fix**: Define port interface, inject port instead of adapter

---

### 3. `check-domain-behavior.ps1`
**Enforces**: Rich domain models (not anemic)

**Validates**:
- ✅ Domain entities have behavior methods
- ✅ Validation logic in domain model
- ✅ Business rules encapsulated

**Example Warning**:
```kotlin
// ⚠️  ANEMIC MODEL
data class JournalEntry(
    val entryId: String,
    val lines: List<JournalEntryLine>,
    val status: String
    // No methods - just data!
)
```

**How to Fix**: Add validation, business rule methods

---

### 4. `check-event-naming.ps1`
**Enforces**: Domain event conventions

**Validates**:
- ✅ Events use past tense (JournalEntryPosted, not JournalEntryPost)
- ✅ Events implement DomainEvent interface
- ✅ Events include required metadata (eventId, occurredAt, tenantId)
- ✅ Events are immutable data classes

**Example Violation**:
```kotlin
// ❌ BAD: Present tense
data class JournalEntryPost(...)

// ✅ GOOD: Past tense
data class JournalEntryPosted(...)
```

---

### 5. `check-command-validation.ps1`
**Enforces**: CQRS commands have validation

**Validates**:
- ✅ Commands have Jakarta Bean Validation annotations
- ✅ Commands are immutable data classes
- ✅ Commands end with 'Command' suffix
- ✅ Commands include tenant context

**Example Violation**:
```kotlin
// ❌ BAD: No validation
data class CreateJournalEntryCommand(
    val companyCode: String  // Should be @field:NotBlank
)
```

**How to Fix**: Add `@field:NotNull`, `@field:NotBlank`, `@field:Valid`

---

### 6. `check-tenant-isolation.ps1` 🚨 CRITICAL
**Enforces**: Multi-tenancy data isolation

**Validates**:
- ✅ Repository queries accept `tenantId: UUID` parameter
- ✅ All queries filter by `tenantId` in WHERE clause
- ✅ No `findAll()` / `listAll()` without tenant filter

**Example Critical Violation**:
```kotlin
// 🚨 CRITICAL: Data leak risk!
fun findAll(): List<JournalEntryEntity> {
    return listAll()  // Returns ALL tenants' data!
}
```

**How to Fix**: Always filter by tenantId
```kotlin
fun findByTenant(tenantId: UUID): List<JournalEntryEntity> {
    return find("tenantId", tenantId).list()
}
```

---

### 7. `check-rest-dependencies.ps1`
**Enforces**: REST resources depend on use cases, not repositories

**Validates**:
- ✅ REST resources inject use cases
- ✅ REST resources do NOT inject repositories
- ✅ REST resources have `@Valid` on request parameters
- ✅ No business logic in REST layer

**Example Violation**:
```kotlin
// ❌ BAD: REST directly using repository
@Path("/api/journal-entries")
class JournalEntryResource {
    @Inject lateinit var repository: JournalEntryRepository  // WRONG!

    @POST
    fun create(request: CreateRequest): Response {
        repository.persist(...)  // Business logic in REST!
    }
}
```

**How to Fix**: Inject use case, delegate logic

---

### 8. `check-outbox-pattern.ps1` 🚨 CRITICAL
**Enforces**: Transactional outbox for events

**Validates**:
- ✅ Event publishers use outbox table
- ✅ No direct Kafka publish without transaction
- ✅ Publish methods are `@Transactional`

**Example Critical Violation**:
```kotlin
// 🚨 CRITICAL: Event loss risk!
override fun publish(event: DomainEvent) {
    kafkaEmitter.send(event)  // Lost if transaction rolls back!
}
```

**How to Fix**: Persist to outbox first, publish asynchronously

---

## 🚀 Installation

### 1. Install pre-commit framework
```powershell
pip install pre-commit
```

### 2. Install hooks
```powershell
cd c:\Users\PC\projects\chiroerp
pre-commit install
```

### 3. Test hooks
```powershell
# Run on all files
pre-commit run --all-files

# Run specific hook
pre-commit run hexagonal-domain-isolation --all-files

# Run on staged files (automatic before commit)
git add .
git commit -m "Your commit message"
```

## 🔧 Usage

### Automatic (on git commit)
```powershell
git add finance-domain/src/main/kotlin/com/chiroerp/finance/domain/JournalEntry.kt
git commit -m "Add journal entry validation"

# Pre-commit hooks run automatically
# If violations found, commit is blocked
```

### Manual
```powershell
# Run all hooks
pre-commit run --all-files

# Run specific hook
pre-commit run hexagonal-domain-isolation --all-files

# Run on specific files
pre-commit run --files finance-domain/**/*.kt
```

### Bypass (use sparingly!)
```powershell
# Skip hooks (NOT RECOMMENDED)
git commit -m "WIP" --no-verify
```

## 📊 Severity Levels

| Level | Meaning | Action |
|-------|---------|--------|
| ✅ **PASS** | No violations | Commit allowed |
| ⚠️ **WARNING** | Potential issue | Commit allowed, review recommended |
| ❌ **ERROR** | Architecture violation | Commit blocked |
| 🚨 **CRITICAL** | Security/data risk | Commit blocked, immediate fix required |

## 🎓 Learning from Violations

When a hook fails:

1. **Read the error message** - It explains what's wrong
2. **Check the "How to Fix" section** - Provides concrete examples
3. **Review the ADR** - Link provided for deeper understanding
4. **Ask for help** - Tag architecture team in PR

## 🛠️ Troubleshooting

### Hook not running
```powershell
# Reinstall hooks
pre-commit clean
pre-commit install

# Check configuration
pre-commit validate-config
```

### False positive
```powershell
# Add inline comment to suppress
// allow: cross-tenant query for admin dashboard
return listAll()
```

### Update hooks
```powershell
# Update to latest versions
pre-commit autoupdate
```

## 📚 Related Documentation

- [ADR-001: Pragmatic CQRS](../../docs/adr/ADR-001-modular-cqrs.md)
- [ADR-005: Multi-Tenancy Isolation](../../docs/adr/ADR-005-multi-tenancy-isolation.md)
- [ADR-009: Financial Accounting Domain](../../docs/adr/ADR-009-financial-accounting-domain.md)
- [ADR-020: Event-Driven Architecture](../../docs/adr/ADR-020-event-driven-architecture-hybrid-policy.md)

## 🤝 Contributing

To add a new architectural check:

1. Create hook script in `scripts/hooks/check-*.ps1`
2. Add entry to `.pre-commit-config.yaml`
3. Test with `pre-commit run <hook-id> --all-files`
4. Update this README
5. Submit PR with justification

## 🎯 Compliance Scorecard

Current architecture compliance status:

| Domain | Hook | Status |
|--------|------|--------|
| finance-domain | Hexagonal Architecture | ⚠️ In Progress |
| finance-domain | Use Case Ports | ❌ Not Implemented |
| finance-domain | Domain Behavior | ✅ Good |
| finance-domain | Event Naming | ❌ No Events Yet |
| finance-domain | Command Validation | ❌ Not Implemented |
| finance-domain | Tenant Isolation | ✅ Good |
| finance-domain | REST Dependencies | ❌ Direct Repository Use |
| finance-domain | Outbox Pattern | ❌ Not Implemented |

**Goal**: All ✅ before Phase 1 completion
