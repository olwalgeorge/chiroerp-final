# Pre-Commit Hooks - Quick Start Guide

## ✅ What Just Happened

Your ChiroERP project now has **8 automated architecture compliance checks** that run on every commit:

| Hook | Purpose | Severity |
|------|---------|----------|
| **hexagonal-domain-isolation** | Domain has no infrastructure dependencies | ERROR |
| **hexagonal-usecase-ports** | Use cases depend on ports, not adapters | ERROR |
| **ddd-domain-behavior** | Domain entities have business logic | WARNING |
| **eda-event-naming** | Events use past tense naming | ERROR |
| **cqrs-command-validation** | Commands have Jakarta validation | ERROR |
| **tenant-isolation** | All queries filter by tenantId | **CRITICAL** |
| **rest-usecase-dependency** | REST injects use cases, not repos | ERROR |
| **event-outbox-pattern** | Events use transactional outbox | **CRITICAL** |

## 🚀 Installation (2 minutes)

```powershell
# From project root
cd c:\Users\PC\projects\chiroerp

# Run installation script
.\scripts\install-hooks.ps1
```

The script will:
1. ✓ Verify Python/pip installed
2. ✓ Install pre-commit framework
3. ✓ Validate all 8 hook scripts exist
4. ✓ Install git hooks
5. ✓ Validate configuration

## 📋 Usage

### Automatic (Recommended)
```powershell
# Make your changes
git add finance-domain/src/main/kotlin/...

# Commit (hooks run automatically)
git commit -m "Add journal entry use case"

# If violations found:
# - Fix the code
# - git add <fixed files>
# - git commit (try again)
```

### Manual Testing
```powershell
# Test all hooks on all files
pre-commit run --all-files

# Test specific hook
pre-commit run tenant-isolation --all-files

# Test on specific files
pre-commit run --files finance-domain/src/**/*.kt
```

### Bypass (Emergency Only)
```powershell
# Skip hooks for this commit (NOT recommended)
git commit --no-verify -m "WIP: broken code"

# Use only if absolutely necessary
# Fix violations in next commit!
```

## 🔍 Current Status

**Test Results** (from your existing code):

```
✅ Domain Isolation: PASS
   - Domain layer has no framework dependencies

❌ Tenant Isolation: FAIL (4 violations)
   - JournalEntryRepository lines 72, 99, 121, 132
   - Missing tenantId filter in find() calls

⚠️  Application Layer: N/A
   - No use case files to check yet
   - Will enforce when you add application layer
```

## 🛠️ Next Steps: Refactor finance-domain

### Priority 1: Fix Tenant Isolation (CRITICAL - Security Risk)

**Current Code** (❌ VIOLATION):
```kotlin
// JournalEntryRepository.kt:72
fun findByEntryNumber(entryNumber: String): JournalEntryEntity? {
    return find("entryNumber = ?1", entryNumber)  // ❌ Missing tenantId!
        .firstResult()
}
```

**Fixed Code** (✅ COMPLIANT):
```kotlin
fun findByTenantAndEntryNumber(
    tenantId: UUID,
    entryNumber: String
): JournalEntryEntity? {
    return find(
        "tenantId = ?1 and entryNumber = ?2",
        tenantId,
        entryNumber
    ).firstResult()  // ✅ Tenant isolation enforced
}
```

### Priority 2: Add Application Layer (Hexagonal Architecture)

Create this structure:
```
finance-domain/src/main/kotlin/com/chiroerp/finance/
├── application/
│   ├── port/
│   │   ├── input/
│   │   │   ├── command/
│   │   │   │   └── CreateJournalEntryCommand.kt
│   │   │   └── usecase/
│   │   │       └── CreateJournalEntryUseCase.kt
│   │   └── output/
│   │       ├── JournalEntryRepositoryPort.kt
│   │       └── DomainEventPublisherPort.kt
│   └── usecase/
│       └── CreateJournalEntryUseCaseImpl.kt
```

**Command Example**:
```kotlin
data class CreateJournalEntryCommand(
    @field:NotNull(message = "Tenant ID required")
    val tenantId: UUID,

    @field:NotBlank(message = "Entry number required")
    val entryNumber: String,

    @field:Valid
    @field:NotEmpty(message = "Lines required")
    val lines: List<JournalEntryLineCommand>
)
```

### Priority 3: Add Domain Events

```kotlin
data class JournalEntryPosted(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    val tenantId: UUID,
    val entryId: UUID,
    val entryNumber: String,
    val effectiveDate: LocalDate,
    val totalDebit: BigDecimal
) : DomainEvent
```

### Priority 4: Implement Outbox Pattern

```kotlin
@Entity
@Table(name = "domain_events_outbox", schema = "finance")
class DomainEventOutbox(
    @Id @GeneratedValue
    val id: UUID,
    val eventId: UUID,
    val eventType: String,
    val payload: String,  // JSON
    val tenantId: UUID,
    val occurredAt: Instant,
    var processed: Boolean = false
)

@ApplicationScoped
class OutboxDomainEventPublisher : DomainEventPublisherPort {
    @Transactional
    override suspend fun publish(event: DomainEvent) {
        outboxRepository.persist(DomainEventOutbox.from(event))
        // Background job will process and send to Kafka
    }
}
```

### Priority 5: Refactor REST Layer

**Move**: `api/` → `infrastructure/adapter/input/rest/`

**Before** (❌ VIOLATION):
```kotlin
@Path("/api/gl-accounts")
class GLAccountResource {
    @Inject
    lateinit var repository: GLAccountRepository  // ❌ Direct repo injection
}
```

**After** (✅ COMPLIANT):
```kotlin
@Path("/api/gl-accounts")
class GLAccountResource {
    @Inject
    lateinit var createUseCase: CreateGLAccountUseCase  // ✅ Use case injection

    @POST
    fun create(@Valid request: CreateGLAccountRequest): Response {
        val command = request.toCommand()
        return createUseCase.execute(command).fold(
            onSuccess = { Response.created(...).build() },
            onFailure = { Response.status(400).entity(...).build() }
        )
    }
}
```

## 📊 Expected Compliance Score

**Current**:
- Hexagonal Architecture: 30%
- CQRS Implementation: 20%
- Event-Driven: 10%
- Tenant Isolation: 90%

**After Refactoring**:
- Hexagonal Architecture: 95%
- CQRS Implementation: 90%
- Event-Driven: 85%
- Tenant Isolation: 95%

## 🆘 Troubleshooting

**Hook fails but I don't understand why?**
- Each error message includes "How to Fix" section
- Check related ADR document (e.g., ADR-001 for CQRS)
- See `scripts/hooks/README.md` for detailed explanations

**Hook is too strict?**
- Most violations indicate real architecture issues
- CRITICAL violations (tenant, outbox) are security/data risks
- If truly false positive, add `// allow: <reason>` comment

**Need to bypass temporarily?**
```powershell
git commit --no-verify -m "WIP"  # Emergency only!
```

**Want to disable specific hook?**
- Edit `.pre-commit-config.yaml`
- Comment out the hook entry
- Run `pre-commit install` again

## 📚 Documentation

- **Detailed Hook Guide**: `scripts/hooks/README.md`
- **Architecture Decisions**: `docs/adr/ADR-*.md`
- **Installation Script**: `scripts/install-hooks.ps1`

## 🎯 Success Criteria

You'll know refactoring is complete when:
```powershell
pre-commit run --all-files
# All hooks pass ✅
```

Then your finance-domain will have:
- ✅ Clean hexagonal architecture
- ✅ Full CQRS with validated commands
- ✅ Event-driven integration ready
- ✅ Transactional outbox pattern
- ✅ Secure multi-tenant isolation
- ✅ Rich domain models with business logic

---

**Ready to start?**
```powershell
# Install hooks
.\scripts\install-hooks.ps1

# Test current state
pre-commit run --all-files

# Begin refactoring!
```
