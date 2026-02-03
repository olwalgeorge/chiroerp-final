# Quality Certificates - ADR-039

> **Bounded Context:** `quality-certificates`  
> **Port:** `9506` (logical, part of quality-execution service)  
> **Database:** `chiroerp_quality_execution`  
> **Kafka Consumer Group:** `quality-execution-cg`

## Overview

Quality Certificates produces **Certificates of Analysis (CoA) and Certificates of Conformance (CoC)** for lots and shipments. It supports regulatory submissions and customer-specific certificate requirements.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | CoA/CoC generation, regulatory submissions |
| **Aggregates** | QualityCertificate, CertificateTemplate, LotTrace |
| **Key Events** | CertificateGeneratedEvent, RegulatorySubmissionCreatedEvent |
| **Integration** | Sales fulfillment, Inventory lots |
| **Compliance** | Digital signatures, audit trail |

## Key Capabilities

- Certificate generation per lot or shipment
- Template-driven CoA/CoC formatting
- Digital signatures and audit trail
- Regulatory submission packaging

## Domain Model

```
QualityCertificate
|-- certificateId
|-- lotNumber
|-- certificateType (COA, COC)
`-- issuedAt

CertificateTemplate
|-- templateId
|-- format
`-- locale
```

## Workflows

1. Inspection completion triggers certificate eligibility.
2. Certificate is generated for lot or shipment.
3. Sales attaches certificate to shipment documents.
4. Regulatory submissions packaged as required.

## Domain Events Published

```json
{
  "eventType": "CertificateGeneratedEvent",
  "payload": {
    "certificateId": "CERT-200",
    "lotNumber": "LOT-2026-01",
    "certificateType": "COA"
  }
}
```

```json
{
  "eventType": "RegulatorySubmissionCreatedEvent",
  "payload": {
    "submissionId": "SUB-800",
    "authority": "FDA",
    "certificateCount": 3
  }
}
```

## Domain Events Consumed

```
- InspectionCompletedEvent (from Quality Execution) -> Generate certificate
- ShipmentConfirmedEvent (from Sales) -> Attach certificate
```

## Integration Points

- **Sales**: shipment documentation and customer delivery.
- **Inventory**: lot traceability and genealogy.
- **Compliance**: regulatory submissions.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| CertificateGeneratedEvent | `quality.certificate.generated` | 6 | 30d |
| RegulatorySubmissionCreatedEvent | `quality.certificate.submission.created` | 6 | 30d |
| InspectionCompletedEvent (consumed) | `quality.execution.inspection.completed` | 6 | 30d |
| ShipmentConfirmedEvent (consumed) | `sales.fulfillment.shipment.confirmed` | 6 | 30d |

**Consumer Group:** `quality-execution-cg`  
**Partition Key:** `lotNumber` / `certificateId` (ensures all events for a lot are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/quality/certificates
Content-Type: application/json

{
  "lotNumber": "LOT-2026-01",
  "certificateType": "COA"
}
```

## Error Responses

```json
{
  "errorCode": "CERTIFICATE_LOT_NOT_FOUND",
  "message": "Lot 'LOT-999' not found",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "lotNumber": "LOT-999"
  }
}
```

```json
{
  "errorCode": "CERTIFICATE_INSPECTION_NOT_COMPLETE",
  "message": "Cannot generate certificate: inspection not completed",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "lotNumber": "LOT-2026-01",
    "inspectionStatus": "IN_PROGRESS"
  }
}
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| API availability | 99.7% | < 99.5% |
| Command latency | < 1s p95 | > 3s |
| Event processing lag | < 30s p95 | > 2m |

## Compliance & Controls

- Digital signature for regulated certificates.
- Immutable certificate history.

## References

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
