# ADR-013: External Integration Patterns (B2B / EDI)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Integration Team
**Tier**: Advanced
**Tags**: integration, b2b, edi, idoc, odata, mapping, canonical-model

## Context
ChiroERP must integrate with external partners and legacy systems (suppliers, customers, banks, logistics, tax providers). SAP-grade ERPs provide robust B2B integrations (EDI, IDoc, SOAP/OData, file-based) plus monitoring and retry semantics. This ADR defines a standardized approach for partner onboarding, message formats, mapping, transport, and operational visibility.

## Decision
Adopt a **tiered external integration standard** using a **canonical integration model**, controlled adapters, and explicit transport policies. Implementation is **not started**; this ADR defines the standard.

### Integration Tiers
1. **Tier A – Real-time APIs**: REST/OData for synchronous partner calls and queries.
2. **Tier B – Event/Messaging**: Asynchronous integration via Kafka-compatible broker for near-real-time flows.
3. **Tier C – B2B/EDI & Batch**: EDI, file-based (SFTP), and batch interfaces for external trading partners.

### Canonical Integration Model
- Define **Canonical Business Messages (CBM)** per domain (Orders, Invoices, Shipments, Payments, Inventory).
- Each context exposes **mapping contracts** from domain models to CBM and back.
- CBM versioning is strict; schema registry required for evolution.

### Standard Patterns

### B2B / EDI Support
- Support **X12** (US) and **EDIFACT** (global) message sets for common flows:
  - Orders (850/ORDERS), Order Acknowledgement (855/ORDRSP)
  - Advance Ship Notice (856/DESADV)
  - Invoice (810/INVOIC), Payment (820/REMADV)
- EDI handled via adapters that translate to/from CBM.
- Partner-specific mapping rules stored in versioned configuration.

### File-Based Integration
- SFTP/HTTPS drop for CSV/JSON/XML.
- Strict file naming conventions and checksum verification.
- Inbound processing uses a staging area with validation and quarantine on failures.

### API & Event Integration
- REST/OData for synchronous interactions with idempotency keys.
- Event-driven for asynchronous workflows (outbox/inbox required).

### Mapping & Transformation

- Mapping rules are **declarative** and versioned.
- Transformations must be deterministic and testable.
- Support reference data translation (units, currency, tax codes, partner IDs).

### Validation & Error Handling

- Validate inbound messages against schema + business rules.
- Reject invalid messages with explicit error codes and partner feedback.
- Retry with backoff for transient transport failures.
- Dead-letter queue (DLQ) for poison messages with manual replay tooling.

### Security & Compliance

- Mutual TLS for partner APIs; token-based auth for SaaS integrations.
- PGP encryption for file/EDI payloads where required.
- Audit log of all inbound/outbound messages and mapping versions.
- GDPR: PII redaction in logs; retention policy per tenant/region.

### Observability

- Metrics: message throughput, error rates, retries, DLQ depth, processing latency.
- Tracing: correlation IDs across inbound/outbound transactions.
- Dashboards: partner-level SLA tracking and failure alerts.

## Alternatives Considered
- **Point-to-point custom integrations** per partner. Rejected due to high maintenance cost and inconsistent governance.
- **REST-only integration** for all partners. Rejected because many B2B partners require EDI/file-based workflows.
- **Single monolithic integration service**. Rejected due to scalability and coupling concerns.

## Consequences
### Positive
- SAP-grade integration capabilities with clear standards
- Predictable partner onboarding and mapping governance
- Robust monitoring, retry, and auditability

### Negative / Risks
- Added integration platform complexity and operational overhead
- Requires investment in mapping tooling and partner management
- Potential performance costs for large batch processing

### Neutral
- Some partners may remain on legacy file-based flows while APIs mature

## Compliance
Enforce via partner onboarding checklists, schema validation, encryption requirements (mTLS/PGP), and audit logging. EDI mappings and transformations must be versioned and reviewed before production release.

## Implementation Plan
### Implementation Plan (Not Started)

- Phase 1: Define CBM schemas and schema registry; baseline partner onboarding process.
- Phase 2: Build EDI adapter for X12 850/810 and EDIFACT ORDERS/INVOIC.
- Phase 3: Add SFTP staging + validation + quarantine workflows.
- Phase 4: Implement monitoring dashboards, DLQ tooling, and audit exports.
- Phase 5: Expand message set coverage and partner self-service portal.

## References

### Related ADRs
- ADR-003 (Event-Driven Integration), ADR-010 (Validation), ADR-011 (Saga Pattern)

### External References
- SAP analogs: IDoc, ALE/EDI, PI/PO, Integration Suite
