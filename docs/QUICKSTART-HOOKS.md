# Pre-Commit Hooks - Quick Start Guide

## ✅ What Runs Now

Architecture guardrails are enforced by a **Gradle/Kotlin** task (no PS1 scripts):

- `./gradlew preCommit` - fast, pre-commit friendly checks (ArchUnit)
- `./gradlew architectureTest` - full ADR guardrails

## 🚀 Install Hooks

### Option A: Git Hook (recommended)
```bash
./gradlew installGitHooks
```

This installs `.git/hooks/pre-commit` to run:
```
./gradlew preCommit -PchangedOnly
```

### Option B: pre-commit framework (optional)
If you already use pre-commit, it’s configured to call Gradle:
```bash
pre-commit install
pre-commit run --all-files
```

## 📋 Usage

```bash
# Fast, local checks
./gradlew preCommit

# Full ADR guardrails
./gradlew architectureTest
```

### Changed-only optimization
When running via pre-commit, `-PchangedOnly` skips the ArchUnit suite if there are no staged `.kt`/`.java` files.

## 🧭 What’s Enforced

- Hexagonal layering (domain/application/infrastructure boundaries)
- REST adapters don’t depend on repositories
- Event naming conventions (`..domain.events..` + `*Event`)
- Outbox usage in event publishers
- `@Since` fields must be nullable
- `@DeprecatedSince` must include meaningful `replaceWith`
- Repository tenant context heuristic

## 🔧 Troubleshooting

- If hooks don’t run: re-run `./gradlew installGitHooks`.
- If Gradle is slow: use `./gradlew preCommit` (fast path) instead of full `check`.
