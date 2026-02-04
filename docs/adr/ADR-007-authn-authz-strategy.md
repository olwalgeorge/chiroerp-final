# ADR-007: Authentication & Authorization Strategy

**Status**: Draft (Not Implemented)
**Date**: 2025-11-08
**Tier**: Core
**Tags**: authn, authz, security, rbac, jwt
**Context**: Tenancy-Identity, API Gateway, Platform-Shared

## Context
ChiroERP is multi-tenant and spans multiple bounded contexts. We need a consistent authentication and authorization strategy that centralizes token validation, enforces tenant scoping, and provides uniform RBAC semantics while keeping services decoupled.

## Decision
Adopt a layered AuthN/AuthZ strategy:

- Authentication at API Gateway (JWT validation), service trusts claims via headers.
- Authorization enforced inside the bounded context using RBAC (roles) and fine-grained permissions (resource:action[:scope]).
- Tenant scoping enforced per request; SYSTEM_ADMIN role bypasses tenant scoping for operational tasks.
- Anti-enumeration and constant-time guards for login and registration flows.

### Rationale

- Keeps identity services focused on domain rules; gateway centralizes protocol concerns (tokens, rate limits).
- Consistent RBAC model across services via shared semantics; no cross-context coupling.
- Mitigates user enumeration and timing side channels.

### Details

### Gateway Responsibilities
- Validate JWT and set trusted headers for downstream (e.g., `X-User-Id`, `X-User-Roles`, `X-User-Permissions`, `X-Tenant-ID`).
- Enforce rate-limits on `/api/v1/identity/auth/*` and registration endpoints.
- Propagate correlation ID and sanitize gateway-level errors.

### Service Responsibilities (Tenancy-Identity)
- Parse trusted headers into a request principal (RequestPrincipalContext).
- Check tenant access: principal tenant must match path tenant unless role SYSTEM_ADMIN.
- RBAC checks:
  - Roles: SYSTEM_ADMIN, TENANT_ADMIN (baseline)
  - Permissions: `resource:action[:scope]` (e.g., `roles:manage`, `roles:read`)
- Login anti-enumeration: unknown user returns `AUTHENTICATION_FAILED` (401) with timing guard (~100ms minimum).

### Error Handling
- Map domain errors to HTTP status via ResultMapper.
- Sanitize all errors via platform-level ErrorSanitizer; attach `X-Error-ID`.
- Validation errors should also flow through the sanitizer (Phase 2 cleanup).

## Alternatives Considered
- **Service-only AuthN/AuthZ**: every service validates tokens independently. Rejected due to duplicated logic and inconsistent policies.
- **Centralized Auth Service for all checks**: a single service performs authz decisions. Rejected due to latency and tight coupling.
- **Opaque tokens with introspection**: increases operational complexity and adds a hard dependency on the identity provider.

## Consequences
### Positive
- Cohesive domain logic; consistent enforcement of security concerns.
- Easier gateway policy evolution without changing services.

### Negative
- All services must standardize on trusted header names and formats.
- Registration anti-enumeration to be completed (success message without leakage).
- Eliminate direct ErrorResponse usages in resources (Phase 2).

### Neutral
- Requires coordination across teams for shared header conventions.

## Compliance
Enforce via gateway policy tests, security review checklists, and audit logging of auth decisions. Periodic penetration testing validates tenant isolation and anti-enumeration safeguards.

## Implementation Plan
- Phase 1: Define trusted header contract and JWT validation policy at the gateway.
- Phase 2: Implement RBAC checks in service layer with shared permission semantics.
- Phase 3: Add anti-enumeration guards and structured error sanitization.
- Phase 4: Add metrics/audit logs for authz decisions and failures.

## References

### Related ADRs
- ADR-004: API Gateway Pattern
- ADR-005: Multi-Tenancy Data Isolation Strategy
- ADR-006: Platform-Shared Governance Rules
