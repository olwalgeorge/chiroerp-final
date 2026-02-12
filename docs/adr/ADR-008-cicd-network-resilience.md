# ADR-008: CI/CD Pipeline Architecture and Network Resilience

**Status**: Accepted (Partially Implemented)
**Date**: 2025-11-09
**Updated**: 2026-02-12
**Deciders**: Development Team, DevOps
**Tier**: Core
**Technical Story**: GitHub Actions pipeline optimization and reliability improvements

## Context
### Context and Problem Statement

The ERP Platform's CI/CD pipeline needed improvements to address:

1. **Network Reliability**: Transient network failures (ETIMEDOUT, ENETUNREACH) causing ~15% of CI runs to fail and require manual re-runs
2. **Build Performance**: Sequential job execution taking ~50-60 minutes per run
3. **Quality Assurance**: No automated scanning for unexpected runtime errors in build outputs
4. **Security**: No vulnerability scanning integrated into CI pipeline
5. **Reproducibility**: Floating action versions (`@v3`) causing potential breaking changes
6. **Cache Management**: Cold starts downloading dependencies repeatedly, wasting time and bandwidth

### Decision Drivers

* Need for 99%+ CI reliability (minimize manual interventions)
* Fast feedback cycles for developers (<30 minutes)
* Production-grade quality gates (log scanning, security scanning)
* Cost optimization (reduce wasted compute on failed runs)
* Maintainability (reproducible builds, clear documentation)
* Industry best practices alignment

## Decision
### Decision Outcome

**Chosen Option:** Implement production-grade CI/CD pipeline with network resilience (v3.0)

### Planned Implementation Components

#### 1. Network Resilience Layer (Planned)
```yaml
# Planned: Automatic retry wrapper using nick-fields/retry@v3.0.0
- uses: nick-fields/retry@v3.0.0
  with:
    timeout_minutes: <job-specific>
    max_attempts: 3
    retry_wait_seconds: 10-15
    command: <gradle-command>
```

**To Be Applied to:**
- ⬜ Cache warmup (dependency resolution)
- ⬜ Lint checks (ktlint)
- ⬜ Build operations (full build)
- ⬜ Integration tests (with service dependencies)
- ⬜ Architecture tests (ArchUnit)

**Planned Retry Strategy:**
- 3 attempts per operation
- Exponential backoff (10-15 seconds)
- Per-operation timeout limits
- Will handle: ETIMEDOUT, ENETUNREACH, DNS failures, repository timeouts

#### 2. Dependency Cache Warmup
```yaml
cache-warmup:
  steps:
    - uses: nick-fields/retry@v3.0.0
      with:
        command: |
          ./gradlew resolveAndLockAll --write-locks --no-daemon --stacktrace ||
          ./gradlew dependencies --no-daemon --stacktrace
```

**Benefits:**
- Eliminates cold-start race conditions
- Shares cache with all downstream jobs
- Optimized dependency resolution (saves 1-2 minutes)
- Reduces network bandwidth consumption

#### 3. Log Gate Scanning
```bash
# scripts/ci/log-gate.sh
# Scans build outputs for unexpected ERROR/Exception patterns
scripts/ci/log-gate.sh scripts/ci/error-allowlist.txt
```

**Applied to:**
- Build job (after compilation)
- Integration tests (after test execution)
- Architecture tests (after ArchUnit tests)

#### 4. Security Scanning
```yaml
- uses: aquasecurity/trivy-action@0.28.0  # Pinned version
  with:
    scan-type: 'fs'
    format: 'sarif'
    output: 'trivy-results.sarif'
```

#### 5. Version Pinning Strategy
All GitHub Actions pinned to specific semantic versions:
- `nick-fields/retry@v3.0.0` (was `@v3`)
- `gradle/actions/wrapper-validation@v3.5.0` (was `@v3`)
- `gradle/actions/setup-gradle@v3.5.0` (was `@v3`)
- `aquasecurity/trivy-action@0.28.0` (was `@master`)

### Technical Details

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub Actions Trigger                   │
│              (PR/Push to main/develop branches)              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Cache Warmup Job (10min)                   │
│  • Pre-fetch all dependencies (with retry)                   │
│  • Optimized resolution: resolveAndLockAll + fallback        │
│  • Share Gradle cache with downstream jobs                   │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
         ▼                               ▼
┌──────────────────┐          ┌──────────────────┐
│  Lint (10min)    │          │ Architecture     │
│  • ktlint retry  │          │ (15min)          │
│  • Style reports │          │ • ArchUnit retry │
└────────┬─────────┘          │ • Log gate       │
         │                    └──────────────────┘
         ▼
┌──────────────────┐
│  Build (30min)   │
│  • Build retry   │
│  • Log gate      │
│  • Exclude integ │
└────────┬─────────┘
         │
         ├───────────────┐
         ▼               ▼
┌─────────────────┐  ┌─────────────┐
│ Integration     │  │ Security    │
│ (20min)         │  │ (10min)     │
│ • PostgreSQL    │  │ • Trivy     │
│ • Tests retry   │  │ • SARIF     │
│ • Log gate      │  └─────────────┘
└─────────────────┘
         │
         ▼
┌──────────────────┐
│  Status Check    │
│  • PR comment    │
└──────────────────┘
```

### Configuration Values

```yaml
# Environment
JAVA_VERSION: '21'
JAVA_DISTRIBUTION: 'temurin'
GRADLE_OPTS: '-Dorg.gradle.configuration-cache=true -Dorg.gradle.daemon=false -Dorg.gradle.parallel=true'

# Retry Configuration
Cache Warmup:  3 attempts, 10s backoff, 6min timeout
Lint:          3 attempts, 10s backoff, 8min timeout
Build:         3 attempts, 15s backoff, 25min timeout
Integration:   3 attempts, 15s backoff, 15min timeout
Architecture:  3 attempts, 10s backoff, 12min timeout

# Permissions (Least Privilege)
contents: read
pull-requests: write
checks: write
security-events: write
```

### Metrics and Monitoring

### Success Criteria
- ✅ CI reliability >99%
- ✅ Pipeline duration <35min
- ✅ Manual interventions <1%
- ✅ Network failure auto-recovery >90%

### Key Performance Indicators
- Pipeline success rate
- Average pipeline duration (cold/warm)
- Retry success rate
- Cache hit rate
- Security vulnerabilities detected

### Monitoring Plan
- Daily: Review failed runs
- Weekly: Analyze retry patterns
- Monthly: Performance trend analysis
- Quarterly: Action version reviews

## Alternatives Considered
### Alternative 1: Manual Re-run on Failures
**Rejected:** Not scalable, wastes developer time, poor developer experience

**Pros:**
- No changes to workflows
- Simple to understand

**Cons:**
- 15% of runs require manual intervention
- Developer context switching
- Delayed feedback cycles
- Not production-grade

### Alternative 2: Increase Timeout Without Retry
**Rejected:** Doesn't solve root cause, wastes resources

**Pros:**
- Simple configuration change

**Cons:**
- Hung processes waste GitHub Actions minutes
- Network failures still cause total failures
- Poor resource utilization
- Doesn't improve reliability

### Alternative 3: Third-Party CI Service (Jenkins, CircleCI)
**Rejected:** Migration cost too high, GitHub Actions meets needs

**Pros:**
- More control over infrastructure
- Potentially better caching

**Cons:**
- High migration cost
- Additional infrastructure to maintain
- Loss of GitHub integration benefits
- Network issues exist on any platform

### Alternative 4: Cache All Dependencies in Repository
**Rejected:** Violates best practices, bloats repository

**Pros:**
- No network dependencies during build

**Cons:**
- Large repository size (hundreds of MB)
- Security update delays
- Violates Gradle/Maven best practices
- Still need network for wrapper/plugins

## Consequences
### Positive (Expected)

✅ **Planned Reliability Improvements**
- Target network failure recovery: 95% automated
- Target CI success rate: 99%+
- Target manual interventions: <1%

✅ **Planned Performance Optimization**
- Target parallel execution: ~30-32min (vs estimated ~50-60min sequential)
- Expected improvement: ~40% faster with parallelization + config cache
- Estimated cache warmup overhead: +1-2min on cold runs

✅ **Planned Quality Assurance**
- Log gate to catch unexpected runtime errors early
- Automated security vulnerability detection
- Test result uploads for analysis

✅ **Planned Maintainability**
- Reproducible builds (all actions pinned to specific versions)
- Comprehensive documentation
- Clear troubleshooting guides
- Version history tracking

✅ **Expected Cost Optimization**
- Reduced wasted compute on failed runs
- Concurrency controls to prevent redundant runs
- Faster builds to reduce GitHub Actions minutes

### Negative (Risks)

⚠️ **Complexity Considerations**
- More complex workflow configuration to manage
- Retry logic adds additional steps
- Will require team training on new patterns

⚠️ **Marginal Overhead**
- Cache warmup adds 1-2min on cold runs
- Retry attempts add time on failures (but prevent manual re-runs)
- Log gate scanning adds ~10-20s per job

⚠️ **Maintenance Burden**
- Quarterly action version reviews required
- Log gate allowlist needs periodic cleanup
- Performance metrics should be monitored

### Neutral

🔄 **Learning Curve**
- Team needs to understand retry patterns
- New troubleshooting procedures
- Documentation helps mitigate

## Compliance
### Compliance and Standards

### GitHub Actions Best Practices (Planned)
⬜ All actions pinned to specific versions
⬜ Least privilege permissions
⬜ Secrets handled securely
⬜ Concurrency controls planned
⬜ Timeout limits on all jobs

### Security Standards (Planned)
⬜ Vulnerability scanning (Trivy)
⬜ SARIF format reports
⬜ GitHub Security tab integration
⬜ Gradle wrapper validation

### Performance Standards
✅ <35min total pipeline time
✅ Parallel job execution
✅ Configuration cache enabled
✅ Test duplication eliminated

## Implementation Plan
### Implementation Status

🟡 **PARTIALLY IMPLEMENTED** - 2026-02-12

The CI governance baseline is implemented, especially for API contracts:

- ✅ OpenAPI workflow: `.github/workflows/api-lint.yml`
  - Generates OpenAPI specs
  - Lints with Redocly and Spectral
  - Enforces breaking-change checks on PRs (`oasdiff`)
  - Generates static API docs on main/master
- ✅ AsyncAPI workflow: `.github/workflows/asyncapi-lint.yml`
  - Validates and lints AsyncAPI specs
  - Performs event contract breaking-change checks on PRs
  - Generates event docs artifacts
- ✅ Version pinning for API-governance CLIs in CI (reproducible builds)

Planned but not yet standardized across all CI lanes:
- ⬜ Retry wrappers for all build/test lanes (`nick-fields/retry`)
- ⬜ Global network resilience rollout for non-API workflows
- ⬜ Full pipeline convergence into a single reliability policy

### Implementation Timeline

- **Phase 1 (v1.0):** Basic CI setup - Sequential execution (~2024)
- **Phase 2 (v2.0):** API governance rollout - OpenAPI/AsyncAPI validation + breaking-change checks (Implemented Feb 2026)
- **Phase 3 (v3.0):** Network resilience rollout - retry logic, cache warmup, version pinning across all jobs (In Progress)
- **Future:** Potential nightly.yml optimization if network issues increase

### Decision Review

**Next Review Date:** 2026-02-09 (Quarterly)

**Review Criteria:**
- Pipeline reliability metrics
- Team feedback on developer experience
- Cost analysis (GitHub Actions minutes)
- Technology updates (new GitHub Actions features)
- Alternative solutions emerging

**Last Updated:** 2026-02-12
**Status:** 🟡 PARTIALLY IMPLEMENTED
**Version:** 3.1 (API governance baseline live; resilience rollout continuing)

## References

### Related ADRs
- [ADR-001: Modular CQRS](./ADR-001-modular-cqrs.md) - Architecture testing patterns
- [ADR-006: Platform Shared Governance](./ADR-006-platform-shared-governance.md) - Quality standards and ArchUnit enforcement
- [ADR-020: Event-Driven Architecture](./ADR-020-event-driven-architecture-hybrid-policy.md) - EDA patterns tested in CI

### Internal Documentation
- [API Governance Guide](../API_GOVERNANCE.md)
- [CI Evolution Changelog](../CI_EVOLUTION_CHANGELOG.md)
- [CI Troubleshooting Guide](../CI_TROUBLESHOOTING.md) - Detailed troubleshooting procedures
- [Developer Advisory - Section 7](../DEVELOPER_ADVISORY.md#7-cicd--quality-gates-adr-008) - Local development workflow, CI failure interpretation, and quality gates
- [CI/CD Quick Reference](../GITHUB_ACTIONS_QUICKREF.md) - One-page cheat sheet
- [GitHub Actions Upgrade Guide](../GITHUB_ACTIONS_UPGRADE.md)
- [Local Quality Gates](../LOCAL_QUALITY_GATES.md)
- [Implementation Roadmap - Phase 1](../ROADMAP.md#2-phase-1---platform-bootstrap--complete) - CI/CD evolution timeline and performance metrics

### External References
- [GitHub Actions Best Practices](https://docs.github.com/en/actions/learn-github-actions/security-hardening-for-github-actions)
- [Trivy Scanner](https://github.com/aquasecurity/trivy)
- [Gradle Build Action](https://github.com/gradle/actions)
- [nick-fields/retry](https://github.com/nick-fields/retry)
