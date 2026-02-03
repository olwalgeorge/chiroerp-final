# org-model

Organizational hierarchy value objects (OrgUnit, AuthorizationContext)

## Purpose

This module defines **interfaces and value objects only** - no implementations.

Domain modules (finance, sales, etc.) depend on these interfaces. Implementations can be swapped:
- **Phase 0**: Hardcoded implementations in each domain module
- **Phase 1**: Drools-based configuration engine in platform-infrastructure
- **Phase 2**: AI-powered rule suggestions and validation

## Architecture Pattern

```kotlin
// This module (platform-shared/org-model)
interface PostingRulesEngine {
    fun determineAccounts(context: PostingContext): AccountMapping
}

// Domain module (finance-domain) - depends on interface
class GLPostingService(
    private val postingRules: PostingRulesEngine  // Injected via CDI
) {
    fun post(transaction: Transaction) {
        val accounts = postingRules.determineAccounts(context)
        // ...
    }
}

// Phase 0: Hardcoded implementation in finance-domain
@ApplicationScoped
class HardcodedPostingRules : PostingRulesEngine {
    override fun determineAccounts(context: PostingContext): AccountMapping {
        // Hardcoded rules for Kenya MVP
    }
}

// Phase 1: Config-driven implementation in platform-infrastructure
@ApplicationScoped
class DroolsPostingRules : PostingRulesEngine {
    override fun determineAccounts(context: PostingContext): AccountMapping {
        // Load rules from config database
    }
}
```

**Zero domain code changes** when swapping implementations!

## Dependencies

- platform-shared/common-types - Shared value objects (Money, UUID extensions, etc.)
- No other dependencies

## Usage

Add to your domain module's \uild.gradle.kts\:

```kotlin
dependencies {
    implementation(project(":platform-shared:org-model"))
}
```

Then inject interfaces via CDI:

```kotlin
@ApplicationScoped
class YourService(
    private val someEngine: SomeEngine  // From this module
) {
    // Use the interface
}
```

## See Also

- [ADR-044: Configuration Rules Framework](../../docs/adr/ADR-044-configuration-rules-framework.md)
- [ADR-045: Enterprise Organizational Model](../../docs/adr/ADR-045-enterprise-organizational-model.md)
- [ADR-046: Workflow & Approval Engine](../../docs/adr/ADR-046-workflow-approval-engine.md)
