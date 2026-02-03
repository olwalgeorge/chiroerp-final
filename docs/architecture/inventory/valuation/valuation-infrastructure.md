# Inventory Valuation Infrastructure Layer

> Part of [Inventory Valuation & Costing](../inventory-valuation.md)

## Directory Structure

```
valuation-infrastructure/
└── src/main/kotlin/com.erp.inventory.valuation.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       └── messaging/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

### Valuation Resource

```
adapter/input/rest/
├── ValuationResource.kt
│   ├── POST   /api/v1/inventory/valuation/runs          -> runValuation()
│   ├── POST   /api/v1/inventory/valuation/runs/{id}/post -> postValuation()
│   └── GET    /api/v1/inventory/valuation/runs/{id}     -> getValuationRun()
```

### Landed Cost Resource

```
├── LandedCostResource.kt
│   ├── POST   /api/v1/inventory/valuation/landed-costs   -> createLandedCost()
│   └── POST   /api/v1/inventory/valuation/landed-costs/{id}/allocate -> allocateLandedCost()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── RunValuationRequest.kt
│   ├── period: String
│   ├── method: String
│   └── includeLandedCost: Boolean
│
├── PostValuationRequest.kt
│   └── postingDate: LocalDate
│
└── CreateLandedCostRequest.kt
    ├── sourceDocument: String
    ├── allocationRule: String
    └── charges: List<CostChargeRequest>
```

### Response DTOs

```
dto/response/
├── ValuationRunDto.kt
│   ├── id, period, method, status
│   ├── totalValue, currency
│
└── LandedCostDto.kt
    ├── id, totalCharge, allocationRule
```

---

## Event Consumers

```
adapter/input/event/
├── StockMovementEventConsumer.kt
│   └── Consumes: StockReceivedEvent, StockIssuedEvent -> Update cost layers
│
└── FinancialPeriodClosedEventConsumer.kt
    └── Consumes: FinancialPeriodClosedEvent -> Lock valuation
```

---

## Persistence Adapters (Secondary/Driven)

### JPA Adapters (Write Model)

```
adapter/output/persistence/jpa/
├── CostLayerJpaAdapter.kt        -> implements CostLayerRepository
├── ValuationRunJpaAdapter.kt     -> implements ValuationRunRepository
├── LandedCostJpaAdapter.kt       -> implements LandedCostRepository
└── FxRevaluationJpaAdapter.kt    -> implements FxRevaluationRepository
```

### JPA Entities

```
adapter/output/persistence/jpa/entity/
├── CostLayerEntity.kt
├── ValuationRunEntity.kt
├── LandedCostEntity.kt
└── FxRevaluationEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
├── InventoryCoreAdapter.kt
├── GLPostingAdapter.kt
└── FxRateAdapter.kt
```

```
adapter/output/messaging/
├── kafka/
│   ├── ValuationEventPublisher.kt
│   └── schema/
│       ├── ValuationRunCompletedSchema.avro
│       └── LandedCostAllocatedSchema.avro
└── outbox/
    └── ValuationOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
├── ValuationDependencyInjection.kt
├── PersistenceConfiguration.kt
└── MessagingConfiguration.kt
```

```
resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_valuation_schema.sql
    ├── V2__create_cost_layer_tables.sql
    ├── V3__create_valuation_run_tables.sql
    └── V4__create_landed_cost_tables.sql
```
