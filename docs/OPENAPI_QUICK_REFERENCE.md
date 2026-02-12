# OpenAPI Quick Reference Card

## 📋 Most Common Annotations

### Resource Class
```kotlin
@Path("/api/v1/resource")
@Tag(name = "Resource", description = "Resource operations")
@SecurityRequirement(name = "jwt")
```

### Endpoint Method
```kotlin
@GET
@Path("/{id}")
@Operation(
    summary = "Get resource by ID",
    description = "Returns a single resource"
)
@APIResponse(responseCode = "200", description = "Success")
@APIResponse(responseCode = "404", description = "Not found")
```

### Parameters
```kotlin
@Parameter(description = "Resource ID", required = true)
@PathParam("id") id: String

@Parameter(description = "Page number", example = "1")
@QueryParam("page") page: Int = 1

@Parameter(description = "Tenant ID", required = true)
@HeaderParam("X-Tenant-Id") tenantId: String
```

### DTOs
```kotlin
@Schema(description = "Represents a resource")
data class ResourceDTO(
    @Schema(description = "Unique ID", example = "123")
    val id: String,
    
    @field:NotBlank
    @Schema(description = "Name", example = "Example")
    val name: String
)
```

## 🚀 Common Commands

```bash
# One-time setup (recommended for faster runs)
npm install -g @redocly/cli

# Generate OpenAPI specs
./gradlew generateOpenApiSpecs

# Lint all specs
./gradlew lintApiSpecs

# Generate HTML docs
./gradlew generateApiDocs

# Complete workflow
./gradlew apiGovernance

# Optional: fallback when Redocly is not installed globally
./gradlew lintApiSpecs -PallowNpxRedoclyFallback=true

# View in Swagger UI (dev mode)
./gradlew :your-module:quarkusDev
# → http://localhost:8080/q/swagger-ui
```

## ✅ Pre-Commit Checklist

- [ ] Add `@Operation` with summary/description
- [ ] Add `@APIResponse` for 2xx success
- [ ] Document all `@Parameter` annotations
- [ ] Add `@Schema` to DTOs with examples
- [ ] Run `./gradlew lintApiSpecs` (must pass)
- [ ] Preview docs: `./gradlew generateApiDocs`

## 📁 Key Files

| File | Purpose |
|------|---------|
| `config/openapi/application-openapi.properties` | Copy to your module |
| `.redocly.yaml` | Linting rules |
| `docs/API_GOVERNANCE.md` | Full documentation |
| `**/build/openapi/openapi.yaml` | Generated spec |
| `docs/api/*.html` | Generated docs |

## 🔧 Configuration Template

Add to `src/main/resources/application.yml` (or `.properties`):

```properties
quarkus.smallrye-openapi.store-schema-directory=build/openapi
quarkus.smallrye-openapi.store-schema-file-name=openapi
mp.openapi.extensions.smallrye.info.title=Your API Title
mp.openapi.extensions.smallrye.info.version=1.0.0
mp.openapi.extensions.smallrye.info.description=Your API description
```

## 🐛 Quick Fixes

**"operation-operationId: error"**
→ Add `operationId = "getResource"` to `@Operation`

**"operation-2xx-response: error"**
→ Add `@APIResponse(responseCode = "200", description = "Success")`

**"parameter-description: error"**
→ Add `description = "..."` to `@Parameter`

**"info-description: error"**
→ Add to config: `mp.openapi.extensions.smallrye.info.description=...`

## 📚 Learn More

See `docs/API_GOVERNANCE.md` for complete guide
