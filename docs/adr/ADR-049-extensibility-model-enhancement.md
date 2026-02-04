# ADR-049: Extensibility Model Enhancement

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Product Team
**Priority**: P1 (High - Competitive Requirement)
**Tier**: Core
**Tags**: extensibility, customization, hooks, plugins, versioning, upgrades

## Context
ChiroERP supports tenant customization and clear domain boundaries. To enable partner/customer extensions while preserving upgradeability, performance, and security, we standardize on a **multi-layer extensibility model** with explicit hook points, versioned contracts, and runtime sandboxing. World-class ERPs (SAP, Salesforce, Odoo) provide well-documented extension mechanisms:

- **SAP**: BAdI (Business Add-Ins), User Exits, Enhancement Spots
- **Salesforce**: Apex Triggers, Lightning Web Components, Platform Events
- **Odoo**: Inheritance (models, views, controllers), Hooks

### Design Inputs
ADR-012 establishes baseline principles (branding, field extensions, API customization). This ADR specifies concrete implementation mechanisms:
- Explicit hook point catalog for domain/application workflows
- Extension contract versioning and compatibility rules
- Sandboxing and resource limits for customer/partner code
- Optional marketplace/certification model for extensions

### Problem Statement
How do we enable customers and partners to extend ChiroERP without:
1. Breaking on upgrades (API stability)
2. Impacting performance (resource limits)
3. Compromising security (sandboxing)
4. Requiring ChiroERP source code access (black-box extensions)

## Decision
Implement a **multi-layered extensibility model** with explicit hook points, versioned APIs, and runtime sandboxing.

---

## Extensibility Layers

### Layer 1: Configuration Extensions (No Code)
**Covered by ADR-044**: Pricing rules, posting rules, approval schemas, tolerances.

**When to Use**: 80-90% of customizations should be configuration, not code.

---

### Layer 2: Domain Logic Extensions (Low Code)

#### 2.1 Hook Points (Pre/Post Event Handlers)

```kotlin
// Domain services expose explicit hook points
@ApplicationScoped
class SalesOrderService {

    @Inject
    lateinit var extensionRegistry: ExtensionRegistry

    fun createSalesOrder(command: CreateSalesOrderCommand): SalesOrder {
        // PRE-HOOK: Allow extensions to validate/modify command
        val modifiedCommand = extensionRegistry.executeHook(
            hookPoint = "sales.order.pre_create",
            context = HookContext(
                tenantId = command.tenantId,
                input = command,
                userId = command.userId
            )
        )

        // Core business logic
        val order = SalesOrder.create(modifiedCommand)
        salesOrderRepository.save(order)

        // POST-HOOK: Allow extensions to perform additional actions
        extensionRegistry.executeHook(
            hookPoint = "sales.order.post_create",
            context = HookContext(
                tenantId = command.tenantId,
                input = order,
                userId = command.userId
            )
        )

        return order
    }
}
```

#### 2.2 Hook Registry

```kotlin
data class HookPoint(
    val hookId: String, // "sales.order.pre_create"
    val domain: String, // "sales"
    val entity: String, // "order"
    val phase: HookPhase, // PRE, POST, REPLACE
    val version: SemanticVersion, // API version
    val inputSchema: JsonSchema,
    val outputSchema: JsonSchema?,
    val documentation: String,
    val deprecated: Boolean = false,
    val deprecationMessage: String? = null
)

enum class HookPhase {
    PRE,     // Before operation (can modify input, can block)
    POST,    // After operation (can trigger side effects, cannot block)
    REPLACE, // Replace entire operation (advanced)
    VALIDATE // Validation only (can add errors)
}

@ApplicationScoped
class ExtensionRegistry {

    private val hookPoints = mutableMapOf<String, List<Extension>>()

    fun registerExtension(extension: Extension) {
        val hookPoint = extension.hookPoint
        hookPoints[hookPoint] = hookPoints.getOrDefault(hookPoint, emptyList()) + extension
    }

    fun executeHook(hookPoint: String, context: HookContext): Any? {
        val extensions = hookPoints[hookPoint] ?: return context.input

        var result = context.input

        for (extension in extensions.sortedBy { it.priority }) {
            try {
                result = when (extension.type) {
                    ExtensionType.GROOVY_SCRIPT -> executeGroovyScript(extension, context.copy(input = result))
                    ExtensionType.JAVASCRIPT -> executeJavaScript(extension, context.copy(input = result))
                    ExtensionType.WEBHOOK -> callWebhook(extension, context.copy(input = result))
                    ExtensionType.KOTLIN_PLUGIN -> executeKotlinPlugin(extension, context.copy(input = result))
                }
            } catch (e: Exception) {
                // Log error but continue (fail-safe)
                logger.error("Extension ${extension.extensionId} failed at hook $hookPoint", e)
                if (extension.failureMode == FailureMode.HALT) {
                    throw ExtensionExecutionException("Extension failed: ${e.message}", e)
                }
            }
        }

        return result
    }
}
```

#### 2.3 Extension Model

```kotlin
data class Extension(
    val extensionId: ExtensionId,
    val tenantId: TenantId,
    val extensionName: String,
    val hookPoint: String, // "sales.order.pre_create"
    val type: ExtensionType,
    val code: String?, // For script-based extensions
    val webhookUrl: String?, // For webhook extensions
    val priority: Int, // Execution order (lower = earlier)
    val enabled: Boolean,
    val version: SemanticVersion,
    val author: String,
    val failureMode: FailureMode,
    val resourceLimits: ResourceLimits,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class ExtensionType {
    GROOVY_SCRIPT,  // Sandboxed Groovy (JVM scripting)
    JAVASCRIPT,     // GraalVM JavaScript
    WEBHOOK,        // HTTP POST to external service
    KOTLIN_PLUGIN   // Compiled Kotlin plugin (JAR)
}

enum class FailureMode {
    CONTINUE, // Log error, continue processing
    HALT      // Throw exception, rollback transaction
}

data class ResourceLimits(
    val maxExecutionTimeMs: Long = 5000, // 5 seconds
    val maxMemoryMB: Long = 128,
    val maxHttpCallouts: Int = 5,
    val maxDbQueries: Int = 10
)
```

---

### Layer 3: Documented Hook Points Catalog

#### Sales Domain Hooks

| Hook Point | Phase | Input | Output | Use Case |
|------------|-------|-------|--------|----------|
| `sales.order.pre_create` | PRE | CreateSalesOrderCommand | Modified command | Add custom validation, set default values |
| `sales.order.post_create` | POST | SalesOrder | void | Send Slack notification, create task in external system |
| `sales.order.pre_approve` | PRE | ApproveSalesOrderCommand | Modified command | Check custom approval rules |
| `sales.order.post_approve` | POST | SalesOrder | void | Trigger custom workflow |
| `sales.order.validate` | VALIDATE | SalesOrder | List<ValidationError> | Add business-specific validation |
| `sales.pricing.calculate` | REPLACE | PricingContext | Price | Custom pricing logic (replaces standard) |

#### Finance Domain Hooks

| Hook Point | Phase | Input | Output | Use Case |
|------------|-------|-------|--------|----------|
| `finance.invoice.pre_create` | PRE | CreateInvoiceCommand | Modified command | Add custom fields, validation |
| `finance.invoice.post_create` | POST | Invoice | void | Send to external billing system |
| `finance.gl.pre_post` | PRE | JournalEntry | Modified entry | Modify GL posting logic |
| `finance.gl.post_post` | POST | JournalEntry | void | Archive to external system |
| `finance.payment.validate` | VALIDATE | Payment | List<ValidationError> | Custom payment validation |

#### Inventory Domain Hooks

| Hook Point | Phase | Input | Output | Use Case |
|------------|-------|-------|--------|----------|
| `inventory.reservation.pre_create` | PRE | ReserveStockCommand | Modified command | Custom allocation logic |
| `inventory.stock.post_movement` | POST | MaterialDocument | void | Update warehouse WMS |
| `inventory.counting.post_complete` | POST | StockCount | void | Generate custom reconciliation report |

#### Manufacturing Domain Hooks

| Hook Point | Phase | Input | Output | Use Case |
|------------|-------|-------|--------|----------|
| `manufacturing.order.pre_create` | PRE | CreateProductionOrderCommand | Modified command | Custom capacity check |
| `manufacturing.order.post_complete` | POST | ProductionOrder | void | Update MES system |
| `manufacturing.bom.validate` | VALIDATE | BillOfMaterial | List<ValidationError> | Engineering rules validation |

#### Procurement Domain Hooks

| Hook Point | Phase | Input | Output | Use Case |
|------------|-------|-------|--------|----------|
| `procurement.po.pre_create` | PRE | CreatePurchaseOrderCommand | Modified command | Add custom fields |
| `procurement.po.post_approve` | POST | PurchaseOrder | void | Send to supplier EDI |
| `procurement.supplier.validate` | VALIDATE | Supplier | List<ValidationError> | Custom supplier qualification |

**Total Hook Points**: 50+ across all domains (comprehensive catalog in documentation).

---

### Layer 4: Scripting Extension Example

#### Groovy Script Extension
```groovy
// Extension: sales.order.pre_create
// Purpose: Enforce custom credit limit per customer category

def order = input as CreateSalesOrderCommand
def customerCategory = api.getCustomerCategory(order.customerId)

if (customerCategory == "HIGH_RISK") {
    def creditLimit = api.getCreditLimit(order.customerId)
    def outstandingBalance = api.getOutstandingBalance(order.customerId)

    if (outstandingBalance + order.totalAmount > creditLimit) {
        api.addError("Credit limit exceeded for high-risk customer")
        return null // Block order creation
    }
}

// Modify command: add custom field
order.customFields["risk_checked"] = true
order.customFields["checked_at"] = new Date()

return order // Return modified command
```

#### Sandboxed API Provided to Scripts
```kotlin
@ScriptAPI
class SalesOrderScriptAPI(private val context: HookContext) {

    fun getCustomerCategory(customerId: CustomerId): String {
        // Read-only access to customer data
        val customer = customerRepository.findById(customerId)
        return customer.category
    }

    fun getCreditLimit(customerId: CustomerId): BigDecimal {
        return creditRepository.getCreditLimit(customerId)
    }

    fun getOutstandingBalance(customerId: CustomerId): BigDecimal {
        return arRepository.getOutstandingBalance(customerId)
    }

    fun addError(message: String) {
        context.errors.add(ValidationError(message))
    }

    // LIMITED: No access to delete operations, admin APIs, etc.
}
```

---

### Layer 5: Webhook Extensions

#### Webhook Extension Configuration
```json
{
  "extensionId": "ext-webhook-001",
  "extensionName": "Send order to Shopify",
  "hookPoint": "sales.order.post_create",
  "type": "WEBHOOK",
  "webhookUrl": "https://customer-system.com/api/orders",
  "headers": {
    "Authorization": "Bearer ${secret:shopify_token}",
    "Content-Type": "application/json"
  },
  "retryPolicy": {
    "maxRetries": 3,
    "backoffMs": 1000
  },
  "timeout": 5000
}
```

#### Webhook Payload
```json
POST https://customer-system.com/api/orders
{
  "hookPoint": "sales.order.post_create",
  "timestamp": "2026-02-03T10:30:00Z",
  "tenantId": "tenant-123",
  "userId": "user-456",
  "data": {
    "orderId": "SO-2026-001",
    "customerId": "CUST-001",
    "totalAmount": 1500.00,
    "currency": "USD",
    "orderDate": "2026-02-03",
    "lineItems": [
      {
        "materialId": "MAT-001",
        "quantity": 10,
        "unitPrice": 150.00
      }
    ]
  }
}
```

---

### Layer 6: Kotlin Plugin Extensions (Advanced)

#### Plugin Interface
```kotlin
// ChiroERP Extension SDK
interface ExtensionPlugin {
    val extensionId: String
    val version: SemanticVersion
    val supportedHooks: List<String>

    fun execute(hookPoint: String, context: HookContext): Any?
    fun initialize(config: ExtensionConfig)
    fun shutdown()
}

// Customer implementation (compiled JAR)
class CustomPricingExtension : ExtensionPlugin {

    override val extensionId = "acme-custom-pricing"
    override val version = SemanticVersion.parse("1.0.0")
    override val supportedHooks = listOf("sales.pricing.calculate")

    override fun execute(hookPoint: String, context: HookContext): Any? {
        return when (hookPoint) {
            "sales.pricing.calculate" -> {
                val pricingContext = context.input as PricingContext
                calculateCustomPrice(pricingContext)
            }
            else -> context.input
        }
    }

    private fun calculateCustomPrice(context: PricingContext): Price {
        // Customer-specific pricing logic
        val basePrice = context.basePrice
        val customerTier = getCustomerTier(context.customerId)

        val discount = when (customerTier) {
            "GOLD" -> 0.15
            "SILVER" -> 0.10
            "BRONZE" -> 0.05
            else -> 0.0
        }

        return Price(
            amount = basePrice * (1 - discount),
            currency = context.currency
        )
    }

    override fun initialize(config: ExtensionConfig) {
        // Load configuration
    }

    override fun shutdown() {
        // Cleanup resources
    }
}
```

#### Plugin Deployment
```bash
# Customer builds plugin
./gradlew buildExtension

# Generates: acme-custom-pricing-1.0.0.jar

# Upload to ChiroERP
curl -X POST https://chiroerp.com/api/extensions/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@acme-custom-pricing-1.0.0.jar" \
  -F "tenantId=tenant-123"

# ChiroERP validates plugin (security scan, API version check)
# Deploys to isolated classloader
# Registers hook points
```

---

## Extension Versioning & Upgrade Compatibility

### API Versioning Strategy

```kotlin
data class HookPointVersion(
    val hookPoint: String,
    val version: SemanticVersion,
    val status: VersionStatus,
    val introducedIn: String, // "ChiroERP 1.5.0"
    val deprecatedIn: String?,
    val removedIn: String?
)

enum class VersionStatus {
    CURRENT,     // Active, recommended
    DEPRECATED,  // Still works, but will be removed
    REMOVED      // No longer available
}

// Example lifecycle
val salesOrderPreCreateV1 = HookPointVersion(
    hookPoint = "sales.order.pre_create",
    version = SemanticVersion.parse("1.0.0"),
    status = DEPRECATED,
    introducedIn = "ChiroERP 1.0.0",
    deprecatedIn = "ChiroERP 2.0.0",
    removedIn = "ChiroERP 3.0.0" // 2-year deprecation period
)

val salesOrderPreCreateV2 = HookPointVersion(
    hookPoint = "sales.order.pre_create",
    version = SemanticVersion.parse("2.0.0"),
    status = CURRENT,
    introducedIn = "ChiroERP 2.0.0",
    deprecatedIn = null,
    removedIn = null
)
```

### Upgrade Compatibility Check

```kotlin
@ApplicationScoped
class ExtensionUpgradeService {

    fun checkCompatibility(
        tenantId: TenantId,
        targetVersion: SemanticVersion
    ): CompatibilityReport {
        val extensions = extensionRepository.findByTenant(tenantId)
        val incompatible = mutableListOf<IncompatibleExtension>()

        extensions.forEach { extension ->
            val hookPointVersion = hookRegistry.getHookPointVersion(
                extension.hookPoint,
                targetVersion
            )

            if (hookPointVersion.status == REMOVED) {
                incompatible.add(
                    IncompatibleExtension(
                        extensionId = extension.extensionId,
                        hookPoint = extension.hookPoint,
                        reason = "Hook point removed in $targetVersion",
                        action = "Migrate to ${hookPointVersion.replacementHook}"
                    )
                )
            }
        }

        return CompatibilityReport(
            compatible = incompatible.isEmpty(),
            incompatibleExtensions = incompatible
        )
    }
}
```

---

## Security & Sandboxing

### Resource Limits Enforcement

```kotlin
@ApplicationScoped
class SandboxedScriptExecutor {

    fun executeGroovyScript(
        script: String,
        context: HookContext,
        limits: ResourceLimits
    ): Any? {
        val sandbox = GroovySandbox(limits)

        return try {
            sandbox.execute(script, context)
        } catch (e: TimeoutException) {
            throw ExtensionExecutionException("Script exceeded ${limits.maxExecutionTimeMs}ms timeout")
        } catch (e: OutOfMemoryError) {
            throw ExtensionExecutionException("Script exceeded ${limits.maxMemoryMB}MB memory limit")
        }
    }
}

class GroovySandbox(private val limits: ResourceLimits) {

    private val shell = GroovyShell(createSecureBinding())

    fun execute(script: String, context: HookContext): Any? {
        // Enforce execution time limit
        val future = CompletableFuture.supplyAsync {
            shell.setVariable("input", context.input)
            shell.setVariable("api", createSandboxedAPI(context))
            shell.evaluate(script)
        }

        return future.get(limits.maxExecutionTimeMs, TimeUnit.MILLISECONDS)
    }

    private fun createSecureBinding(): Binding {
        val binding = Binding()

        // Whitelist allowed classes
        val secureConfig = CompilerConfiguration()
        secureConfig.addCompilationCustomizers(
            SecureASTCustomizer().apply {
                // Block dangerous operations
                setImportsBlacklist(listOf(
                    "java.io.*",
                    "java.nio.*",
                    "java.lang.System",
                    "java.lang.Runtime"
                ))

                // Block direct database access
                setStarImportsBlacklist(listOf("java.sql.*"))
            }
        )

        return binding
    }
}
```

### Authorization

```kotlin
// Extensions respect tenant isolation
@ApplicationScoped
class ExtensionAuthorizationService {

    fun canExecute(extension: Extension, context: HookContext): Boolean {
        // 1. Tenant isolation
        if (extension.tenantId != context.tenantId) {
            return false
        }

        // 2. Extension enabled
        if (!extension.enabled) {
            return false
        }

        // 3. User has permission to trigger extension (if required)
        if (extension.requiresUserConsent) {
            return userConsentRepository.hasConsent(
                context.userId,
                extension.extensionId
            )
        }

        return true
    }
}
```

---

## Extension Marketplace

### Extension Metadata
```kotlin
data class ExtensionListing(
    val listingId: String,
    val extensionName: String,
    val publisher: Publisher,
    val category: ExtensionCategory,
    val description: String,
    val pricing: PricingModel,
    val supportedVersions: List<SemanticVersion>,
    val hookPoints: List<String>,
    val certifications: List<Certification>,
    val installCount: Int,
    val rating: Double,
    val reviews: List<Review>,
    val screenshots: List<String>,
    val documentation: String
)

enum class ExtensionCategory {
    FINANCE, SALES, INVENTORY, MANUFACTURING,
    INTEGRATION, REPORTING, LOCALIZATION, VERTICAL_SPECIFIC
}

data class PricingModel(
    val type: PricingType, // FREE, ONE_TIME, SUBSCRIPTION
    val price: Money?,
    val billingFrequency: BillingFrequency? // MONTHLY, ANNUALLY
)

data class Certification(
    val certificationId: String,
    val certificationName: String, // "ChiroERP Certified", "Security Audited"
    val issuedBy: String,
    val issuedDate: LocalDate,
    val expiresDate: LocalDate?
)
```

### Extension Installation Flow
```
1. User browses extension marketplace
2. User selects extension (e.g., "Shopify Integration")
3. ChiroERP shows required hook points + permissions
4. User approves installation
5. ChiroERP downloads extension code
6. Security scan (code analysis, vulnerability check)
7. Extension deployed to tenant's isolated namespace
8. Extension enabled
9. User configures extension (API keys, settings)
10. Extension active
```

---

## Observability & Monitoring

### Extension Metrics

```kotlin
@ApplicationScoped
class ExtensionMetricsService {

    @Inject
    lateinit var meterRegistry: MeterRegistry

    fun recordExtensionExecution(
        extension: Extension,
        hookPoint: String,
        executionTimeMs: Long,
        success: Boolean
    ) {
        meterRegistry.counter(
            "extension.executions",
            "extension_id", extension.extensionId.value,
            "hook_point", hookPoint,
            "status", if (success) "success" else "failure"
        ).increment()

        meterRegistry.timer(
            "extension.execution.duration",
            "extension_id", extension.extensionId.value,
            "hook_point", hookPoint
        ).record(executionTimeMs, TimeUnit.MILLISECONDS)
    }
}
```

### Extension Dashboard
```
Extension Performance Dashboard
├─ Total Executions (last 24h)
├─ Average Execution Time (per hook point)
├─ Error Rate (per extension)
├─ Resource Usage (CPU, memory)
└─ Top 10 Slowest Extensions
```

---

## Testing Extensions

### Extension Test Harness
```kotlin
@QuarkusTest
class ExtensionTestHarness {

    @Inject
    lateinit var extensionRegistry: ExtensionRegistry

    @Test
    fun testCustomPricingExtension() {
        // Load extension
        val extension = Extension(
            extensionId = ExtensionId.generate(),
            tenantId = TenantId("test-tenant"),
            extensionName = "Custom Pricing",
            hookPoint = "sales.pricing.calculate",
            type = ExtensionType.GROOVY_SCRIPT,
            code = """
                def context = input as PricingContext
                def basePrice = context.basePrice
                return new Price(amount: basePrice * 0.9, currency: "USD")
            """.trimIndent(),
            priority = 100,
            enabled = true,
            version = SemanticVersion.parse("1.0.0"),
            author = "test",
            failureMode = FailureMode.HALT,
            resourceLimits = ResourceLimits(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        extensionRegistry.registerExtension(extension)

        // Execute hook
        val result = extensionRegistry.executeHook(
            hookPoint = "sales.pricing.calculate",
            context = HookContext(
                tenantId = TenantId("test-tenant"),
                input = PricingContext(
                    basePrice = BigDecimal("100.00"),
                    currency = "USD"
                ),
                userId = UserId("test-user")
            )
        ) as Price

        // Verify
        assertEquals(BigDecimal("90.00"), result.amount)
    }
}
```

---

## Migration from ADR-012

### ADR-012 (Current) vs ADR-049 (Enhanced)

| Feature | ADR-012 | ADR-049 |
|---------|---------|---------|
| Field Extensions | ✅ Custom fields | ✅ Same + validation hooks |
| API Customization | ✅ Custom endpoints | ✅ Same + hook points |
| Domain Logic | ✅ High-level guidance | ✅ Standardized hook point catalog (50+) |
| Versioning | ✅ Principles | ✅ Semantic versioning + deprecation |
| Sandboxing | ✅ Principles | ✅ Resource limits + isolation |
| Marketplace | ✅ Optional | ✅ Marketplace + certifications |

**Migration Path**: ADR-012 remains valid for general principles. ADR-049 adds explicit implementation details.

---

## Performance Standards

### Extension Execution SLOs

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Hook Execution (PRE) | < 50ms p95 | > 100ms |
| Hook Execution (POST) | < 100ms p95 | > 200ms |
| Webhook Callout | < 500ms p95 | > 1s |
| Script Compilation (Groovy) | < 50ms | > 100ms |
| Extension Load Time | < 200ms | > 500ms |

### Caching Strategy
```kotlin
// Compiled scripts cached per tenant
@CacheResult(cacheName = "compiled-extensions")
fun getCompiledExtension(extensionId: ExtensionId): CompiledScript {
    return compileScript(extensionRepository.findById(extensionId))
}
```

---

## Alternatives Considered

### 1. No Extensibility (Configuration Only)
**Pros**: Simple, secure
**Cons**: Cannot handle complex business logic
**Decision**: Rejected—insufficient for enterprise customers

### 2. Direct Database Access for Extensions
**Pros**: Maximum flexibility
**Cons**: Security risk, no API stability
**Decision**: Rejected—violates encapsulation

### 3. Microservice-Only Extensions
**Pros**: Full isolation
**Cons**: High latency (network calls), complex deployment
**Decision**: Partial—webhooks support this pattern

### 4. Plugin-Only (No Scripts)
**Pros**: Type-safe, compiled
**Cons**: High barrier to entry, requires JVM knowledge
**Decision**: Rejected—hybrid approach (scripts + plugins) better

---

## Consequences

### Positive
- ✅ **Competitive Parity**: Matches SAP BAdI, Salesforce Triggers
- ✅ **API Stability**: Semantic versioning prevents breakage
- ✅ **Security**: Sandboxing prevents malicious code
- ✅ **Ecosystem**: Extension marketplace enables partner ecosystem
- ✅ **Flexibility**: Script, webhook, and plugin options cover all use cases

### Negative
- ❌ **Complexity**: Extension framework is complex to build/maintain
- ❌ **Performance**: Extensions add latency (50-100ms per hook)
- ❌ **Support Burden**: Debugging customer extensions is difficult
- ❌ **Versioning Overhead**: Must maintain backward compatibility

### Neutral
- Extension certification process TBD
- Pricing model for marketplace TBD (revenue share?)
- Low-code visual extension builder (future enhancement)

---

## Compliance

### Code Review
- All marketplace extensions undergo security audit
- Static analysis (SonarQube, Checkmarx)
- Dependency vulnerability scanning

### Data Access
- Extensions only access data within tenant boundary
- No cross-tenant data leakage
- Audit log for all extension executions

---

## Implementation Plan

### Phase 1: Foundation (Months 1-2)
- ✅ Extension registry and hook point catalog
- ✅ Groovy script executor with sandboxing
- ✅ Webhook extension support
- ✅ Resource limits enforcement

### Phase 2: Core Hook Points (Months 3-4)
- ✅ Sales domain hooks (10+ hook points)
- ✅ Finance domain hooks (10+ hook points)
- ✅ Inventory domain hooks (5+ hook points)
- ✅ Extension testing harness

### Phase 3: Advanced Features (Months 5-6)
- ✅ Kotlin plugin support (compiled JARs)
- ✅ Extension marketplace (listing, installation)
- ✅ Versioning and upgrade compatibility checks
- ✅ Extension performance dashboard

### Phase 4: Ecosystem (Months 7-12)
- ✅ Partner certification program
- ✅ Extension SDK documentation
- ✅ Sample extensions (templates)
- ✅ Low-code extension builder (visual)

---

## References

### Related ADRs
- ADR-012: Tenant Customization Framework (general principles)
- ADR-044: Configuration & Rules Framework (configuration-first approach)
- ADR-046: Workflow & Approval Engine (workflow extensibility)
- ADR-014: Authorization Objects & SoD (extension authorization)

### Technology References
- [SAP BAdI](https://help.sap.com/doc/saphelp_nw75/7.5.5/en-US/8b/e6b7a84be22e0ce10000000a11402f/content.htm)
- [Salesforce Apex](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/)
- [Odoo Inheritance](https://www.odoo.com/documentation/16.0/developer/tutorials/backend.html)
- [Groovy Sandbox](https://github.com/kohsuke/groovy-sandbox)

### Industry References
- SAP Enhancement Framework
- Salesforce AppExchange
- Odoo Apps Store
- Shopify App Store

---

**Status**: This ADR is **CRITICAL for competitive positioning**. World-class ERPs require extensibility. Without explicit hook points, customers will fork code (maintenance nightmare).
