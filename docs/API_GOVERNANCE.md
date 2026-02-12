# API Governance & OpenAPI Automation

**Status:** ✅ Implemented (February 12, 2026)  
**Related ADR:** ADR-010 (REST Validation Standard)

---

## Overview

ChiroERP uses **automated OpenAPI specification generation** to ensure consistent REST API design across all bounded contexts. This document explains the 85–95% automated workflow for API documentation, linting, and governance.

### Why OpenAPI?

- **Industry standard** for REST API contracts
- **Automatic generation** from JAX-RS annotations (no manual YAML writing)
- **CI enforcement** of design rules (blocks PRs on violations)
- **Client code generation** (TypeScript, Kotlin, Java)
- **Interactive documentation** (Swagger UI, Redoc, Scalar)

### What About Swagger?

- **OpenAPI 3.x** is the specification standard (maintained by OpenAPI Initiative)
- **Swagger UI** is one viewer tool (aging UX)
- We use **Redoc** and **Scalar** for better documentation experience
- "Swagger" refers to the legacy Swagger 2.0 spec — we use OpenAPI 3.0+

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│ Developer writes JAX-RS resources with annotations              │
│   @Path, @GET, @Operation, @Schema, @Parameter, etc.           │
└────────────────────┬─────────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────────┐
│ Quarkus SmallRye OpenAPI Extension                              │
│ - Scans JAX-RS endpoints at build time                          │
│ - Generates OpenAPI 3.0 spec (YAML/JSON)                        │
│ - Exports to build/openapi/openapi.yaml                         │
└────────────────────┬─────────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────────┐
│ CI Linting (Redocly + Spectral)                                 │
│ - Validates spec structure                                       │
│ - Enforces design rules (descriptions, operation IDs, etc.)     │
│ - Fails PR if errors found                                      │
└────────────────────┬─────────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────────┐
│ Static Documentation Generation                                  │
│ - Redoc builds static HTML                                       │
│ - Published to docs/api/*.html                                   │
│ - Optionally deployed to GitHub Pages                           │
└──────────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### 1. Enable OpenAPI in Your Module

The `quarkus-smallrye-openapi` dependency is **already included** via `chiroerp.quarkus-conventions` plugin.

Add configuration to your module's `src/main/resources/application.yml` (or `.properties`):

```properties
# Copy from config/openapi/application-openapi.properties template
quarkus.smallrye-openapi.store-schema-directory=build/openapi
quarkus.smallrye-openapi.store-schema-file-name=openapi
quarkus.smallrye-openapi.path=/q/openapi

# Customize for your module
mp.openapi.extensions.smallrye.info.title=ChiroERP Finance GL API
mp.openapi.extensions.smallrye.info.version=1.0.0
mp.openapi.extensions.smallrye.info.description=General Ledger REST API
```

### 2. Annotate Your REST Resources

```kotlin
@Path("/api/v1/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Accounts", description = "General Ledger account management")
class AccountResource {

    @GET
    @Operation(
        summary = "List all accounts",
        description = "Returns chart of accounts for the current tenant"
    )
    @APIResponse(
        responseCode = "200",
        description = "Successful operation",
        content = [Content(schema = Schema(implementation = Array<AccountDTO>::class))]
    )
    @APIResponse(responseCode = "401", description = "Unauthorized")
    fun listAccounts(
        @Parameter(description = "Tenant ID", required = true)
        @HeaderParam("X-Tenant-Id") tenantId: String
    ): List<AccountDTO> {
        // ...
    }
}

data class AccountDTO(
    @Schema(description = "Unique account identifier", example = "1000")
    val accountNumber: String,
    
    @Schema(description = "Account name", example = "Cash in Bank")
    val name: String,
    
    @Schema(description = "Account type", example = "ASSET")
    val type: AccountType,
    
    @Schema(description = "Current balance in cents", example = "150000")
    val balance: Long
)
```

### 3. Build & Generate Spec

```bash
# Generate specs for modules that opted into OpenAPI export
./gradlew generateOpenApiSpecs

# View generated spec
cat bounded-contexts/tenancy-identity/identity-core/build/openapi/openapi.yaml
```

### 4. Lint & Generate Docs Locally

```bash
# Lint all OpenAPI specs
./gradlew lintApiSpecs

# Generate static Redoc documentation
./gradlew generateApiDocs

# Run complete governance workflow
./gradlew apiGovernance
```

Open `docs/api/finance-gl.html` in your browser to view the documentation.

---

## CI/CD Integration

### Pull Request Workflow

When you push code that changes REST APIs:

1. **GitHub Actions** triggers `.github/workflows/api-lint.yml`
2. **Build step** runs `./gradlew generateOpenApiSpecs` (generates OpenAPI specs)
3. **Lint step** runs Redocly CLI on all generated specs
4. **PR fails** if any design rule violations are found
5. **Comment posted** to PR with specific errors

### Main Branch Workflow

On merge to `master`:

1. Specs are linted (must pass)
2. **Static Redoc docs** are generated
3. Docs artifact is uploaded (retention: 90 days)
4. *Optional:* Deploy to GitHub Pages (currently disabled)

---

## Governance Rules

All APIs must comply with rules defined in `.redocly.yaml`:

### Required (Blocks PRs)

- ✅ Valid OpenAPI 3.0+ structure
- ✅ API must have description, contact info
- ✅ All operations must have:
  - Summary and description
  - Unique operation ID
  - At least one 2xx response
  - Defined security scheme
- ✅ All parameters must be documented
- ✅ No broken `$ref` references
- ✅ Boolean parameters use prefixes (`is`, `has`, `can`)

### Recommended (Warns but Allows Merge)

- ⚠️ License information
- ⚠️ Tag descriptions
- ⚠️ 4xx error responses
- ⚠️ Response examples
- ⚠️ No unused components
- ⚠️ kebab-case for paths
- ⚠️ No HTTP verbs in paths (e.g., `/getAccounts` is bad)

### Custom Rules (Future)

- 🔮 Tenant-scoped endpoints must include `X-Tenant-Id` header or `tenantId` path param
- 🔮 Multi-tenant isolation validation

---

## Best Practices

### 1. Rich Documentation

```kotlin
@Schema(
    description = "Represents a general ledger account in the chart of accounts",
    example = """{"accountNumber": "1000", "name": "Cash", "type": "ASSET"}"""
)
data class AccountDTO(
    @Schema(
        description = "Account number (4-digit code)",
        example = "1000",
        minLength = 4,
        maxLength = 4
    )
    val accountNumber: String
)
```

### 2. Error Responses

```kotlin
@APIResponse(responseCode = "400", description = "Invalid request")
@APIResponse(responseCode = "401", description = "Authentication required")
@APIResponse(responseCode = "403", description = "Insufficient permissions")
@APIResponse(responseCode = "404", description = "Account not found")
@APIResponse(responseCode = "409", description = "Account number already exists")
```

### 3. Security Annotations

```kotlin
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@ApplicationScoped
class SecurityConfiguration

@Path("/api/v1/accounts")
@SecurityRequirement(name = "jwt")
class AccountResource {
    // All operations require JWT by default
}
```

### 4. Examples & Validation

```kotlin
data class CreateAccountRequest(
    @field:NotBlank
    @Schema(description = "Account number", example = "1000")
    val accountNumber: String,
    
    @field:Size(min = 1, max = 100)
    @Schema(description = "Account name", example = "Cash in Bank")
    val name: String,
    
    @field:NotNull
    @Schema(description = "Account type", example = "ASSET")
    val type: AccountType
)
```

---

## For AsyncAPI (Event Documentation)

OpenAPI documents **REST** APIs. For **Kafka/outbox events**, we'll add **AsyncAPI**:

### Phase 2 (Upcoming)

- Add AsyncAPI specs for event channels
- Document message schemas (e.g., `TenantProvisioningEvent`)
- Integrate AsyncAPI linting into CI
- Generate event documentation

See `ADR-003-event-driven-integration.md` for event architecture.

---

## Tools Reference

### Redocly CLI

```bash
# Install globally
npm install -g @redocly/cli

# Lint single spec
redocly lint bounded-contexts/finance/finance-gl/build/openapi/openapi.yaml

# Build static docs
redocly build-docs openapi.yaml --output=docs.html

# Preview docs locally
redocly preview-docs openapi.yaml
```

### Spectral (Alternative Linter)

```bash
# Install
npm install -g @stoplight/spectral-cli

# Lint with default rules
spectral lint openapi.yaml

# Custom ruleset
spectral lint openapi.yaml --ruleset=.spectral.yaml
```

### Swagger UI (Dev Mode)

Access at `http://localhost:8080/q/swagger-ui` in dev mode:

```bash
./gradlew :your-module:quarkusDev
```

---

## Troubleshooting

### "No OpenAPI specs found"

**Problem:** Gradle tasks can't find generated specs.

**Solution:**
1. Ensure `quarkus.smallrye-openapi.store-schema-directory=build/openapi` is set
2. Run `./gradlew clean build`
3. Check `**/build/openapi/openapi.yaml` exists

### "Redocly CLI not found"

**Problem:** `lintApiSpecs` or `generateApiDocs` fails before linting/doc generation.

**Solution:**
1. Install Redocly once: `npm install -g @redocly/cli`
2. Re-run `./gradlew lintApiSpecs` or `./gradlew generateApiDocs`
3. If global install is blocked, use temporary fallback:
   `./gradlew lintApiSpecs -PallowNpxRedoclyFallback=true`

### "Operation ID missing"

**Problem:** Redocly lint fails with "operation-operationId: error".

**Solution:** Add `@Operation(operationId = "...")` or enable auto-generation:
```properties
quarkus.smallrye-openapi.auto-add-tags=true
```

### "No 2xx response defined"

**Problem:** Missing success response.

**Solution:** Add `@APIResponse`:
```kotlin
@APIResponse(responseCode = "200", description = "Success")
```

### "Parameter description missing"

**Problem:** Undocumented query/path/header parameters.

**Solution:** Add `@Parameter(description = "...")`:
```kotlin
fun getAccount(@Parameter(description = "Account ID") @PathParam("id") id: String)
```

---

## Gradle Tasks Reference

```bash
# Generate OpenAPI specs
./gradlew generateOpenApiSpecs

# Lint all OpenAPI specs
./gradlew lintApiSpecs

# Generate static Redoc HTML docs
./gradlew generateApiDocs

# Run complete governance workflow
./gradlew apiGovernance

# Clean all generated specs
./gradlew clean
```

---

## Configuration Files

| File | Purpose |
|------|---------|
| `config/openapi/application-openapi.properties` | Template for module OpenAPI config |
| `.redocly.yaml` | Redocly linting rules and API definitions |
| `.github/workflows/api-lint.yml` | CI workflow for automated linting |
| `build.gradle.kts` | Root Gradle tasks for docs generation |
| `build-logic/.../QuarkusConventionsPlugin.kt` | Adds `quarkus-smallrye-openapi` dependency |

---

## Migration Checklist

For each bounded context with REST APIs:

- [ ] Copy `config/openapi/application-openapi.properties` to module's `src/main/resources/`
- [ ] Customize `info-title`, `info-description`, `servers` for your module
- [ ] Add `@Operation`, `@Schema`, `@Parameter` annotations to existing resources
- [ ] Run `./gradlew :your-module:build`
- [ ] Run `./gradlew lintApiSpecs` to check compliance
- [ ] Fix any linting errors
- [ ] Run `./gradlew generateApiDocs` to preview documentation
- [ ] Commit generated spec to trigger CI validation

---

## References

- **Quarkus OpenAPI Guide:** https://quarkus.io/guides/openapi-swaggerui
- **SmallRye OpenAPI Spec:** https://github.com/smallrye/smallrye-open-api
- **OpenAPI Specification:** https://spec.openapis.org/oas/v3.1.0
- **Redocly CLI:** https://redocly.com/docs/cli/
- **Redoc (viewer):** https://redocly.com/docs/redoc
- **Spectral:** https://github.com/stoplightio/spectral
- **Scalar (alternative viewer):** https://scalar.com/products/api-references/openapi
- **AsyncAPI:** https://www.asyncapi.com/docs/tutorials/getting-started

---

## Questions?

- Review `ADR-010-rest-validation-standard.md` for design decisions
- Check existing bounded contexts for annotation examples
- Run `./gradlew help --task lintApiSpecs` for task details
- See `.github/workflows/api-lint.yml` for CI configuration

**Next Steps:**
1. Apply to `finance-gl` module first (pilot)
2. Validate workflow and refine rules
3. Roll out to remaining bounded contexts
4. Add AsyncAPI for event documentation (Phase 2)
