# ADR-042: Field Service Operations (Add-on)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-05
**Updated**: 2026-02-06 - Promoted to independent bounded context (separated from Plant Maintenance)
**Deciders**: Architecture Team, Operations Team
**Priority**: Medium
**Tier**: Add-on
**Tags**: field-service, dispatch, work-orders, SLA, mobile, hexagonal-architecture

## Context
Field Service Operations handle **customer-facing** on-site service delivery, including service orders, technician dispatch, parts consumption, and SLA tracking. This is fundamentally different from **Plant Maintenance (ADR-040)**, which focuses on **internal asset maintenance**.

### Key Distinction from Plant Maintenance (ADR-040)

| Aspect | Field Service (ADR-042) | Plant Maintenance (ADR-040) |
|--------|-------------------------|------------------------------|
| **Focus** | Customer-facing service delivery | Internal asset upkeep |
| **Workers** | Field technicians at customer sites | Maintenance technicians on internal assets |
| **Billing** | Revenue-generating (billable to customer) | Cost center (internal expense) |
| **SLA** | Customer SLA/contract compliance | Internal availability targets |
| **Assets** | Customer equipment (external) | Company-owned equipment (internal) |
| **Triggers** | Customer service requests, contracts | Equipment failures, preventive schedules |

This separation justifies Field Service as an **independent bounded context** rather than a subdomain of Plant Maintenance.

## Decision
Adopt **Field Service Operations** as an **independent top-level bounded context** with its own deployment units, databases, and port range. The FSM domain follows hexagonal architecture with three distinct subdomains.

### Bounded Context Architecture

```
field-service/                     # Independent Bounded Context (ADR-042)
├── fsm-shared/                    # Shared identifiers and value objects (ADR-006 compliant)
├── fsm-service-orders/            # Service Orders Subdomain (Port 9601)
├── fsm-dispatch/                  # Technician Dispatch Subdomain (Port 9602)
├── fsm-parts-consumption/         # Parts Consumption Subdomain (Port 9603)
└── fsm-repair-depot/              # Customer RMA/Repair Services Subdomain (Port 9604)
```

**Package Structure**: `com.chiroerp.fieldservice.*`

#### FSM Shared Module (`fsm-shared/`)
ADR-006 compliant shared module containing only identifiers, value objects, and enums:

| Component | Type | Purpose |
|-----------|------|---------|
| `ServiceOrderId.kt` | Identifier | Service order identifier |
| `FieldTechnicianId.kt` | Identifier | Field technician identifier |
| `DispatchId.kt` | Identifier | Dispatch assignment identifier |
| `PartsConsumptionId.kt` | Identifier | Parts consumption record identifier |
| `RmaRequestId.kt` | Identifier | Return Merchandise Authorization ID |
| `RepairOrderId.kt` | Identifier | Customer repair order ID |
| `RepairQuoteId.kt` | Identifier | Repair quote identifier |
| `CustomerEquipmentId.kt` | Identifier | Customer-owned equipment ID |
| `DiagnosisResultId.kt` | Identifier | Diagnosis result identifier |
| `DepotTechnicianId.kt` | Identifier | Depot repair technician ID |
| `ServicePriority.kt` | Value Object | Priority (emergency, high, normal, low) |
| `ServiceLocation.kt` | Value Object | Customer site location |
| `ServiceWindowVO.kt` | Value Object | Preferred time window |
| `RepairPricingVO.kt` | Value Object | Labor rates, parts markup |
| `WarrantyTermsVO.kt` | Value Object | Warranty coverage terms |
| `ShippingAddressVO.kt` | Value Object | Customer return shipping address |
| `ServiceOrderStatus.kt` | Enum | Status enumeration |
| `DispatchStatus.kt` | Enum | Dispatch state enum |
| `RmaStatus.kt` | Enum | RMA workflow states |
| `RepairOrderStatus.kt` | Enum | Repair order workflow states |
| `RepairDecisionType.kt` | Enum | Repair, scrap, return-as-is |
| `WarrantyType.kt` | Enum | In-warranty, out-of-warranty, extended |

> **Note**: Events are NOT in shared module per ADR-006 governance. Events belong in their respective subdomain domain modules.

#### Service Orders Subdomain (`fsm-service-orders/` - Port 9601)

**Domain Model:**
| Entity | Description |
|--------|-------------|
| `ServiceOrder.kt` | Aggregate root - service order lifecycle |
| `ServiceOrderLine.kt` | Line items for service activities/tasks |
| `ServiceRequest.kt` | Incoming customer service request |
| `CustomerAsset.kt` | Customer equipment being serviced |
| `ServiceContract.kt` | SLA/warranty reference |
| `BillingDetails.kt` | Billable hours, materials, travel |
| `TimeAndMaterials.kt` | T&M billing model |
| `FixedPriceBilling.kt` | Fixed price contracts |

**Domain Events:**
- `ServiceOrderCreatedEvent`, `ServiceOrderScheduledEvent`, `ServiceOrderCompletedEvent`
- `ServiceOrderBilledEvent`, `ServiceOrderCancelledEvent`

**Use Cases (Input Ports):**
- `CreateServiceOrderUseCase`, `ScheduleServiceOrderUseCase`, `CompleteServiceOrderUseCase`
- `GenerateInvoiceUseCase`, `ServiceOrderQueryPort`

**Output Ports:**
- `ServiceOrderRepositoryPort`, `ContractRepositoryPort`, `BillingPort`, `ServiceOrderEventPublisherPort`

**Domain Services:**
- `ServiceOrderCreationService` - Core service order business logic
- `ContractValidationService` - Contract/warranty validation
- `BillingCalculationService` - Service billing calculations
- `SLAComplianceService` - SLA breach detection and compliance

**Infrastructure Adapters:**
- REST: `ServiceOrderResource` (Port 9601)
- Persistence: `ServiceOrderJpaRepository`, `ServiceOrderJpaEntity`, `ContractJpaRepository`
- Integration: `FinanceARAdapter` (invoice generation), `KafkaEventPublisher`

#### Dispatch Subdomain (`fsm-dispatch/` - Port 9602)

**Domain Model:**
| Entity | Description |
|--------|-------------|
| `Dispatch.kt` | Aggregate root - dispatch assignment |
| `FieldTechnician.kt` | Field technician with skills & availability |
| `TechnicianSkills.kt` | Skill matrix for matching |
| `TechnicianAvailability.kt` | Calendar/schedule |
| `ServiceTerritory.kt` | Geographic territory |
| `RouteOptimization.kt` | Optimal routing |
| `ScheduleSlot.kt` | Available time slots |
| `TravelTime.kt` | Estimated travel between sites |

**Domain Events:**
- `TechnicianDispatchedEvent`, `DispatchRescheduledEvent`, `TechnicianEnRouteEvent`
- `TechnicianArrivedEvent`, `DispatchCompletedEvent`

**Use Cases (Input Ports):**
- `AssignTechnicianUseCase`, `RescheduleDispatchUseCase`, `UpdateTechnicianStatusUseCase`
- `OptimizeRouteUseCase`, `DispatchQueryPort`

**Output Ports:**
- `DispatchRepositoryPort`, `TechnicianRepositoryPort`, `RouteOptimizationPort`, `DispatchEventPublisherPort`

**Domain Services:**
- `DispatchAssignmentService` - Core dispatch business logic
- `SkillMatchingService` - Technician skill matching
- `RouteOptimizationService` - Route optimization algorithms
- `TerritoryManagementService` - Territory assignment

**Infrastructure Adapters:**
- REST: `DispatchResource`, `TechnicianResource`, `ScheduleResource` (Port 9602)
- Persistence: `DispatchJpaRepository`, `TechnicianJpaRepository`, `TerritoryJpaRepository`
- External: `GoogleMapsRouteAdapter` (route optimization), `KafkaEventPublisher`

#### Parts Consumption Subdomain (`fsm-parts-consumption/` - Port 9603)

**Domain Model:**
| Entity | Description |
|--------|-------------|
| `PartsConsumption.kt` | Aggregate root - consumption record |
| `FieldInventory.kt` | Technician truck/van inventory |
| `PartUsage.kt` | Part consumed on service order |
| `ReplenishmentRequest.kt` | Request for restocking |
| `TruckStock.kt` | Mobile stock levels |
| `WarrantyPart.kt` | Parts under warranty claim |

**Domain Events:**
- `PartConsumedEvent`, `InventoryDepletedEvent`, `ReplenishmentRequestedEvent`
- `TruckRestockedEvent`, `WarrantyClaimCreatedEvent`

**Use Cases (Input Ports):**
- `ConsumePartUseCase`, `RequestReplenishmentUseCase`, `RestockTruckUseCase`
- `CreateWarrantyClaimUseCase`, `PartsConsumptionQueryPort`

**Output Ports:**
- `PartsConsumptionRepositoryPort`, `TruckInventoryRepositoryPort`, `WarehouseIntegrationPort`, `PartsEventPublisherPort`

**Domain Services:**
- `PartsConsumptionService` - Parts consumption business logic
- `TruckInventoryService` - Truck/van inventory management
- `WarrantyClaimService` - Warranty claim processing

**Infrastructure Adapters:**
- REST: `PartsConsumptionResource`, `TruckInventoryResource`, `WarrantyClaimResource` (Port 9603)
- Persistence: `PartsConsumptionJpaRepository`, `TruckInventoryJpaRepository`, `WarrantyClaimJpaRepository`
- Integration: `InventoryWarehouseAdapter`, `KafkaEventPublisher`

#### Repair Depot Subdomain (`fsm-repair-depot/` - Port 9604)

This subdomain handles **customer-facing RMA and out-of-warranty repair services**. Unlike Plant Maintenance (ADR-040) which manages internal company assets as a cost center, Repair Depot is a **revenue-generating** service that repairs customer equipment for a fee.

**Key Distinctions from Plant Maintenance (ADR-040):**
| Aspect | Repair Depot (ADR-042) | Repair Center (ADR-040) |
|--------|------------------------|------------------------|
| **Owner** | Customer | Company |
| **Billing** | Revenue (billable to customer) | Cost (internal expense) |
| **Equipment** | Customer equipment via RMA | Company-owned assets |
| **Workflow** | RMA → Quote → Approval → Repair | Work Order → Repair → Close |
| **Warranty** | Out-of-warranty paid service | N/A (internal assets) |

**Domain Model:**
| Entity | Description |
|--------|-------------|
| `RepairOrder.kt` | Aggregate root - customer repair order lifecycle |
| `RepairOrderLine.kt` | Line items (labor, parts, services) |
| `RepairOrderStatus.kt` | Received, diagnosing, quoted, approved, repairing, testing, complete, shipped |
| `RmaRequest.kt` | Return Merchandise Authorization request |
| `RmaStatus.kt` | RMA workflow: requested, approved, received, processed |
| `CustomerEquipment.kt` | Customer-owned equipment received for repair |
| `EquipmentCondition.kt` | Condition assessment on receipt |
| `DiagnosisResult.kt` | Fault found, root cause, repair recommendations |
| `RepairQuote.kt` | Quote for customer approval |
| `QuoteLineItem.kt` | Labor, parts, shipping costs |
| `RepairDecision.kt` | Repair, scrap, or return-as-is decision |
| `RepairTechnician.kt` | Depot repair technician |
| `WarrantyEvaluation.kt` | Check if warranty applies |
| `OutOfWarrantyRepair.kt` | Paid repair services |
| `TestResult.kt` | Post-repair testing and validation |
| `QualityCertification.kt` | QA sign-off before shipping |
| `ShipmentTracking.kt` | Return shipping to customer |

**Domain Events:**
- `RmaRequestedEvent`, `RmaApprovedEvent`, `EquipmentReceivedEvent`
- `DiagnosisCompletedEvent`, `QuoteSentEvent`, `QuoteApprovedEvent`, `QuoteRejectedEvent`
- `RepairStartedEvent`, `RepairCompletedEvent`, `TestPassedEvent`, `TestFailedEvent`
- `EquipmentShippedEvent`, `RepairBilledEvent`

**Use Cases (Input Ports):**
- `RmaUseCase` - RMA request and approval workflow
- `RepairIntakeUseCase` - Equipment receipt and inspection
- `DiagnosisUseCase` - Fault finding and root cause analysis
- `QuotingUseCase` - Quote generation and customer approval
- `RepairExecutionUseCase` - Repair work coordination
- `ReturnShippingUseCase` - Ship repaired equipment back

**Output Ports:**
- `RepairOrderRepositoryPort`, `RmaRepositoryPort`, `CustomerEquipmentRepositoryPort`
- `InventoryPartsPort` - Parts consumption from inventory
- `WarrantyCheckPort` - CRM contract/warranty lookup
- `BillingPort` - Finance AR integration for invoicing
- `ShippingIntegrationPort` - Carrier integration (FedEx/UPS/DHL)
- `RepairDepotEventPublisherPort`

**Domain Services:**
- `RmaCreationService` - RMA request & approval workflow
- `RepairIntakeService` - Equipment receipt & inspection
- `DiagnosisService` - Fault finding & root cause analysis
- `QuotingService` - Quote generation & pricing
- `WarrantyValidationService` - Warranty check & claim
- `RepairExecutionService` - Repair work coordination
- `QualityTestingService` - Post-repair testing & QA
- `ReturnShippingService` - Ship repaired equipment

**Infrastructure Adapters:**
- REST: `RmaResource`, `RepairOrderResource`, `DiagnosisResource`, `QuoteResource`, `RepairMetricsResource` (Port 9604)
- Persistence: `RepairOrderJpaRepository`, `RmaJpaRepository`, `CustomerEquipmentJpaRepository`, `DiagnosisResultJpaRepository`
- Integration: `InventoryPartsAdapter`, `CrmWarrantyAdapter`, `FinanceARAdapter`, `ShippingCarrierAdapter`

**Repair Depot Workflow:**
```
Customer Request → RMA Created → RMA Approved → Equipment Shipped (by customer)
    → Equipment Received → Inspection/Diagnosis → Quote Sent
    → Customer Approves/Rejects Quote
        → If Approved: Repair Execution → Testing → QA Sign-off → Ship Back → Invoice
        → If Rejected: Return As-Is → Ship Back (shipping charge only)
        → If Scrap Decision: Customer consent → Dispose → Credit/Refund handling
```

### Scope
- Service orders with SLA tracking and priority rules.
- Technician scheduling and dispatch with route optimization hooks.
- Parts consumption and service inventory usage.
- Service billing and warranty/contract handling.
- **Customer RMA and out-of-warranty repair depot services.**

### Key Capabilities
- **Service Orders**: intake, triage, assignment, execution, closure.
- **Dispatch**: schedule boards, technician availability, skills matching.
- **Parts & Inventory**: reserving and consuming parts in the field.
- **Service Contracts**: entitlements, warranty coverage, billing rules.
- **Repair Depot/RMA**: RMA processing, diagnosis, quoting, out-of-warranty repairs, return shipping.

### Integration Points
- **Inventory (ADR-024)**: parts reservation, issuance, and stock adjustments via `WarehouseIntegrationPort`.
- **Finance AR (ADR-009)**: service invoicing and revenue recognition via `BillingPort`.
- **Finance Revenue (ADR-009)**: revenue recognition for service jobs.
- **CRM Contracts (ADR-043)**: customer entitlements, warranty coverage, SLA definitions.
- **CRM Account Health (ADR-043)**: service interaction tracking for customer health scoring.
- **Plant Maintenance (ADR-040)**: **Peer integration** - field service may trigger internal maintenance work orders for complex repairs, but operates as a **separate bounded context** with distinct billing models, workers, and SLAs.
- **Workforce Scheduling (ADR-055)**: technician shift management and labor costing.
- **Shipping/Logistics (ADR-027)**: carrier integration for RMA return shipping.

### Inter-Subdomain Communication
| Source Subdomain | Target Subdomain | Event/Integration |
|-----------------|------------------|-------------------|
| Service Orders | Dispatch | `ServiceOrderCreatedEvent` → triggers dispatch assignment |
| Dispatch | Service Orders | `TechnicianArrivedEvent` → updates service order status |
| Service Orders | Parts Consumption | `ServiceOrderStartedEvent` → enables parts consumption |
| Parts Consumption | Service Orders | `PartsConsumedEvent` → updates service order parts list |
| Parts Consumption | Inventory | `PartsConsumedEvent` → stock adjustment (ADR-024) |
| Repair Depot | Inventory | `PartsConsumedEvent` → stock adjustment for repair parts |
| Repair Depot | Finance AR | `RepairBilledEvent` → invoice generation |
| Repair Depot | CRM | `RmaRequestedEvent` → customer interaction tracking |
| Repair Depot | Shipping | `EquipmentShippedEvent` → carrier tracking integration |
| Service Orders | Repair Depot | `ServiceOrderEscalatedEvent` → if field repair not possible, escalate to depot |

### Non-Functional Constraints / KPIs
- **Dispatch assignment latency**: p95 < 2 minutes.
- **On-time arrival rate**: >= 95%.
- **First-time-fix rate**: >= 85%.
- **Parts availability accuracy**: >= 99.0%.
- **RMA turnaround time**: p95 < 10 business days (depot repair).
- **Repair quote approval rate**: >= 70%.

## Alternatives Considered
- **Embed in PM module**: rejected due to broader customer-facing workflows.
- **External FSM vendor**: rejected for weaker ERP accounting integration.
- **Lightweight CRM extension**: insufficient for SLA/dispatch needs.

## Consequences
### Positive
- Adds full service lifecycle support for equipment/service-based businesses.
- Improves revenue capture for service and warranty workflows.

### Negative / Risks
- Requires strong mobile/offline support for technicians.
- Adds scheduling complexity and SLA exposure.

### Neutral
- Optional add-on; not required for core ERP adoption.

---

## Utilities Extension (Electric, Gas, Water Industries)

### Context
Utility companies (electric, gas, water) require industry-specific capabilities beyond standard field service: meter-to-cash (meter reading, consumption billing, tariff management), network asset management (poles, transformers, lines, substations), outage management, and crew dispatch optimization for service connections/disconnections.

### Scope Extension
- **Meter-to-Cash**: Meter reading cycles, consumption calculations, tariff/rate schedules, tiered pricing, estimated billing.
- **Billing Engine**: Utility bill generation (consumption-based, fixed charges, demand charges), bill format compliance.
- **Network Asset Management**: Poles, transformers, power lines, substations, pipelines, GIS integration.
- **Outage Management**: Outage detection, crew dispatch, restoration tracking, customer notifications.
- **Service Orders**: Connect/disconnect/transfer service, meter installation/replacement, load surveys.
- **Asset Maintenance**: Preventive maintenance schedules for critical infrastructure (transformers, switches, valves).

### Additional Capabilities
- **Meter Data Management**:
  - Meter master data (meter ID, type, location, installation date, last reading).
  - Reading cycles (monthly, bi-monthly, quarterly).
  - Manual readings, AMR (automated meter reading), AMI (advanced metering infrastructure).
  - Consumption calculations (current reading - previous reading, multiplier adjustments).
  - Estimated bills (when meter not accessible, based on historical consumption).

- **Tariff & Rate Management**:
  - Rate schedules (residential, commercial, industrial).
  - Tiered pricing (e.g., first 500 kWh @ $0.10, next 500 kWh @ $0.12).
  - Time-of-use rates (peak, off-peak, shoulder).
  - Demand charges for commercial customers (kW vs kWh).
  - Seasonal rate adjustments.
  - Regulatory rate case tracking and approvals.

- **Utility Billing**:
  - Bill calculation engine (consumption × tariff + fixed charges + taxes).
  - Bill formatting per regulatory requirements (itemized charges, usage graphs).
  - Budget billing (average monthly payment plans).
  - Payment plans and installment agreements.
  - Late fees and disconnect notices.

- **Network Asset Registry**:
  - Asset hierarchy (substation → feeder → transformer → service line → meter).
  - GIS integration (asset location, service territory mapping).
  - Asset condition tracking (age, inspection results, failure history).
  - Load analysis (transformer capacity, feeder loading).

- **Outage Management System (OMS)**:
  - Outage detection (customer calls, AMI last-gasp signals, SCADA alarms).
  - Outage prediction models (weather, asset condition).
  - Crew dispatch optimization (location, skills, equipment availability).
  - Restoration tracking and customer ETR (estimated time to restore).
  - Outage analytics (frequency, duration, SAIDI/SAIFI/CAIDI metrics).

- **Service Order Types**:
  - Connect service (new customer, turn-on).
  - Disconnect service (non-payment, customer request).
  - Transfer service (change of occupancy).
  - Meter exchange (upgrade, malfunction).
  - Load survey (voltage quality, demand analysis).

### Data Model Extensions
- `Meter`: meter ID, meter type (electric/gas/water), serial number, multiplier, installation date, location (lat/long), service account.
- `MeterReading`: meter ID, reading date, reading value, reading type (actual/estimated), reader ID.
- `TariffSchedule`: rate code, effective date, customer class, rate structure (tiered, TOU, demand).
- `RateTier`: tier number, from quantity, to quantity, rate per unit.
- `UtilityBill`: service account, bill period, consumption, tariff applied, total charges, due date.
- `NetworkAsset`: asset ID, asset type (pole, transformer, line, substation), GIS coordinates, voltage level, capacity, install date, condition.
- `Outage`: outage ID, start time, affected assets, estimated customers, assigned crew, restoration time.
- `ServiceConnection`: service account, premise, meter, connect/disconnect status, service type (overhead, underground).

### Integration Points
- **Inventory/MM-IM**: Meter inventory, transformer spare parts, materials for service orders.
- **Finance/FI-AR**: Utility billing receivables, payment processing, delinquency collections.
- **Fixed Assets/FI-AA**: Network infrastructure assets (poles, transformers, substations), depreciation.
- **GIS Systems**: Asset locations, service territory mapping, outage visualization.
- **SCADA/AMI**: Real-time meter data, outage signals, load monitoring.
- **CRM/ADR-043**: Customer service account, contact info, service history.

### KPIs / SLOs
- **Meter reading accuracy**: >= 99.5% (actual reads vs estimates).
- **Billing cycle completion**: 100% bills generated within 5 business days of cycle end.
- **Tariff application accuracy**: >= 99.9% (correct rate applied).
- **Outage response**: Crew dispatched within 15 minutes of outage detection.
- **Restoration time**: SAIDI (System Average Interruption Duration Index) < regulatory target.
- **Service order completion**: Connect/disconnect within 24 hours of request.
- **Asset inspection compliance**: >= 95% of critical assets inspected per schedule.

### Implementation Phasing
- **Phase 5A**: Meter data management and reading cycles (4 months).
- **Phase 5B**: Tariff management and billing engine (5 months).
- **Phase 5C**: Network asset registry and GIS integration (4 months).
- **Phase 5D**: Outage management and crew dispatch (5 months).

---

## Advanced Inventory Operations Integration (ADR-024 Extension)

### Overview
Field Service Operations leverage **Advanced Inventory Operations** (ADR-024 extension) for sophisticated parts management beyond basic stock movements. Service industries require kitting, catch weight handling, repack operations, and packaging hierarchies for field inventory management.

### Key Capabilities from Advanced Inventory

#### 1. Service Kit Management
- **Static Kits**: Pre-built maintenance/repair kits (HVAC Tune-Up Kit, Electrical Repair Kit)
- **Dynamic Kits**: Assembled on-demand based on service order type
- **Virtual Kits**: Logical grouping for multi-item service orders
- **Kit ATP**: Real-time component availability check before technician dispatch
- **Kit Assembly/Disassembly**: Warehouse operations with cost rollup

**Service Order → Kit Workflow**:
```
1. Service Order Created → Check Kit ATP (component availability)
2. Kit Available → Reserve kit, dispatch technician
3. Kit Unavailable → Trigger kit assembly work order
4. Kit Assembly → Post receipt to service inventory
5. Kit Issued → Technician dispatched with kit
6. Service Completed → Post component consumption, return unused parts
```

**KPIs**:
- Kit availability: >= 95% (at time of service order creation)
- First-time-fix rate: >= 85% (correct components in kit)
- Kit assembly cycle time: p95 < 4 hours

#### 2. Field Inventory Management (Service Van/Truck Stock)
- **Packaging Hierarchy**: Warehouse bulk (pallet/case) → van stock (each-level)
- **Break Bulk**: Split warehouse inventory for van replenishment
- **Repackaging**: Repack damaged materials returned from field
- **Multi-Location Tracking**: Warehouse, van, consignment, RMA quarantine

**Van Replenishment Workflow**:
```
1. Van Replenishment Request → Break bulk from warehouse stock
2. Warehouse issues pallet → Repack to van quantities
3. Transfer to van location → Van inventory updated
4. Technician consumes parts → Issue from van stock
5. End-of-day reconciliation → Variance posting
6. Return unused materials → Receive to warehouse (repack if needed)
```

**KPIs**:
- Van stock accuracy: >= 99%
- Replenishment cycle time: p95 < 24 hours
- Shrinkage rate: < 0.5%

#### 3. Utilities-Specific Inventory Operations
- **Meter Installation Kits**: Meter + fittings + seals + mounting hardware
- **Catch Weight Materials**: Cable spools, wire reels, pipe sections (variable length/weight)
- **Emergency Parts Network**: ATP across all locations (warehouse + van + consignment)
- **Asset Tracking**: Serial number tracking for transformers, meters, network equipment

**Cable/Wire Consumption (Catch Weight)**:
```
1. Service order requires 150 feet of cable (nominal)
2. Reserve cable spool (500 feet available, average)
3. Technician cuts 152 feet (actual catch weight)
4. Post consumption: Cost = (152 ft / 500 ft) × spool cost
5. Remaining spool: 348 feet available
```

**Utilities KPIs**:
- Emergency parts availability: >= 99.5%
- Meter kit accuracy: >= 99.9%
- Cable waste (catch weight variance): < 2%
- Outage response parts ready: < 15 minutes

#### 4. Warranty/RMA Processing
- **Repackaging**: Repack defective parts for vendor return
- **VAS Operations**: Inspection, cleaning, labeling, photography
- **Kit Returns**: Disassemble returned service kits, receive components individually

**RMA Workflow**:
```
1. Technician returns defective part → Create RMA
2. Receive to quarantine → Inspection (repackable vs scrap)
3. Repackable → Repack order (per vendor specs)
4. VAS → Inspection report, photos, compliance labeling
5. Ship to vendor → Track warranty claim
6. Vendor credit → Post to Finance (ADR-009)
```

**KPIs**:
- RMA processing time: p95 < 48 hours
- Repack quality (vendor acceptance): >= 99%
- Warranty recovery rate: >= 80%

#### 5. Repair Center Integration (ADR-040)
**Field Service Role**: Trigger repair operations and facilitate loaner exchanges

**Field Technician Triggers Repair**:
```
1. Technician identifies defective equipment in field
2. Create field service work order with "Requires Repair" flag
3. Defective unit returned to service center
4. Field service work order triggers repair work order (ADR-040)
5. Inventory receives unit into REPAIR_QUEUE (ADR-024)
```

**Loaner Exchange at Customer Site**:
```
1. Customer needs immediate replacement during repair
2. Field technician checks loaner availability (ADR-024 LOANER_POOL)
3. Issue loaner to customer (loaner work order in ADR-040)
4. Collect defective unit from customer
5. Defective unit → Repair work order (ADR-040)
6. When repair complete → Schedule return visit
7. Exchange loaner for repaired unit at customer site
```

**Integration Points**:
- **Plant Maintenance (ADR-040)**: Repair work orders, loaner work orders, repair scheduling
- **Inventory (ADR-024)**: Loaner inventory movements, repair location stock
- **Service Orders**: Link field service order to repair work order

**Field Service KPIs for Repair Integration**:
- Loaner exchange time: < 30 minutes (at customer site)
- Repair request processing: < 2 hours (field to repair center)
- Customer notification: Within 1 hour of repair completion

#### 6. Consignment Inventory at Customer Sites
- **Multi-Location Tracking**: Track inventory at customer premises
- **Consumption Billing**: Trigger billing when customer consumes parts
- **Replenishment**: Break bulk from warehouse for consignment restocking
- **Reconciliation**: Cycle counting at consignment locations

**Consignment KPIs**:
- Consignment stock accuracy: >= 99%
- Replenishment timeliness: p95 < 24 hours
- Consumption billing accuracy: >= 99.9%

### Integration Architecture

**Field Service ↔ Advanced Inventory Events**:
- `ServiceOrderCreatedEvent` → Check Kit ATP
- `KitReservedEvent` → Update service order status
- `KitAssembledEvent` → Notify service order (kit ready for dispatch)
- `FieldConsumptionEvent` → Update van inventory, trigger replenishment
- `VanReplenishmentCompletedEvent` → Notify dispatch (van restocked)
- `ConsignmentConsumptionEvent` → Trigger customer billing (ADR-025)
- `WarrantyReturnEvent` → Create RMA, initiate repack workflow

**REST API Integration**:
- `GET /api/v1/inventory/kits/{kitId}/atp` → Real-time kit availability for service order
- `POST /api/v1/inventory/kits/{kitId}/reserve` → Reserve kit for service order
- `GET /api/v1/inventory/locations/{vanId}/stock` → Query van inventory for dispatch
- `POST /api/v1/inventory/catchweight/consume` → Post catch weight consumption from field
- `POST /api/v1/inventory/repair/orders` → Create repair order from field defect
- `GET /api/v1/inventory/loaner/{sku}/available` → Check loaner availability for exchange

### Service Industry Robustness

**Coverage Assessment**: 95/100 (improved from 92/100 with repair center addition)

**Strengths**:
- ✅ Complete service kit lifecycle (static/dynamic/virtual)
- ✅ Field inventory management with break bulk and repack
- ✅ Utilities-specific capabilities (catch weight, meter kits, emergency parts)
- ✅ Warranty/RMA processing with VAS
- ✅ **Repair center/depot operations** (in-house refurbishment, loaner programs, test/calibration)
- ✅ Consignment inventory tracking with consumption billing

**Recommended Phase 7+ Enhancements**:
- Loaner equipment tracking and swap transactions
- Field consumption forecasting for van restocking
- Service kit optimization based on first-time-fix analytics
- Real-time mobile app sync for field consumption posting
- Consignment analytics for optimal stock levels

### Documentation Reference
For detailed service industry integration patterns, workflows, and data model extensions, see:
- **Architecture Documentation**: `docs/architecture/inventory/inventory-advanced-ops.md` (Service Industry Integration section)
- **Related ADR**: ADR-024 Inventory Management (Advanced Operations Extension)

## Compliance
- SLA reporting requirements for customer contracts.
- Audit trail for parts usage and billing.

## Implementation Plan
1. Service order lifecycle and status model.
2. Dispatch and scheduling logic.
3. Parts consumption and inventory integration.
4. Service billing workflows and finance postings.
5. SLA dashboards and reporting.

## References
- ADR-024 Inventory Management (Advanced Operations Extension - Service Industry Integration, loaner inventory)
- ADR-025 Sales & Distribution
- ADR-009 Financial Accounting
- ADR-027 Master Data Governance (kit BOM approvals, GTIN validation)
- ADR-038 Warehouse Execution System (physical kit assembly, break bulk execution)
- ADR-040 Plant Maintenance (repair work orders, loaner programs, repair center integration)
- ADR-043 CRM & Customer Management
- ADR-055 Workforce Scheduling & Labor Management

### Internal Documentation
- `docs/architecture/inventory/inventory-advanced-ops.md` (Service Industry Integration)
