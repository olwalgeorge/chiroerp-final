# Architecture Compliance Report

This repo enforces key ADR guardrails via **ArchUnit** in the `architecture-tests` module.

## ✅ How Enforcement Works

- **Fast local checks**: `./gradlew preCommit`
- **Full ADR suite**: `./gradlew architectureTest`
- **CI**: runs `./gradlew architectureTest`

## 🔍 What’s Covered

- Layering / Hexagonal boundaries
- REST adapters use use-cases (no repository coupling)
- Event naming conventions
- Outbox usage for event publishing
- `@Since` nullable fields
- `@DeprecatedSince` requires migration guidance
- Tenant context usage in repositories (heuristic)

## 📂 Source of Truth

- Rules live in:
  - `architecture-tests/src/test/kotlin/com/chiroerp/arch/ArchitectureRulesTest.kt`

## 🧪 Run Locally

```bash
./gradlew architectureTest
```

## Notes

The tenant isolation rule is heuristic (it looks for a `Tenant*` type reference in repositories). If your convention differs (e.g., raw UUID), update the rule accordingly.
