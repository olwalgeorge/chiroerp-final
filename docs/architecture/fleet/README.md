# Fleet Management Domain Architecture

This directory contains hexagonal architecture specifications for Fleet Management subdomains including **Vehicle Lifecycle**, **Telematics Integration**, **Driver Management**, **Fuel Management**, and **Fleet Compliance**.

## Subdomain Index

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Vehicle Master** | ADR-053 | 10001 | Core | [fleet-vehicle-master.md](./fleet-vehicle-master.md) | Vehicle hierarchy, specs, assignments |
| **Telematics Integration** | ADR-053 | 10002 | Advanced | [fleet-telematics.md](./fleet-telematics.md) | GPS tracking, driving behavior, diagnostics |
| **Driver Management** | ADR-053 | 10003 | Core | [fleet-driver-management.md](./fleet-driver-management.md) | Licenses, certifications, safety scores |
| **Fuel Management** | ADR-053 | 10004 | Core | [fleet-fuel-management.md](./fleet-fuel-management.md) | Fuel cards, consumption, cost tracking |
| **Fleet Compliance** | ADR-053 | 10005 | Advanced | [fleet-compliance.md](./fleet-compliance.md) | Inspections, registrations, DOT compliance |

## Domain Overview (New: 2026-02-03)

### Fleet Management (ADR-053) - Add-on Tier
**Specialized module for organizations with significant vehicle fleets (100+ vehicles)**

**Scope:**
- Vehicle lifecycle management (acquisition → operations → disposal)
- Driver assignment and license tracking
- Telematics integration (GPS, driving behavior, vehicle diagnostics)
- Fuel management (card integration, consumption tracking, cost allocation)
- Fleet maintenance scheduling (preventive, corrective)
- Compliance tracking (inspections, registrations, DOT/FMCSA, emissions)
- Fleet cost accounting and TCO analysis
- Integration with Fixed Assets (ADR-021), Plant Maintenance (ADR-040), Controlling (ADR-028)

**Key Features:**
- Vehicle master data with specs, assignments, and lifecycle status
- Telematics integration for real-time GPS tracking and driving behavior monitoring
- Driver safety scoring (speeding, harsh braking, rapid acceleration, idling)
- Fuel card integration (WEX, FleetCor, Voyager) with transaction reconciliation
- Maintenance schedule generation based on mileage/hours/calendar
- DOT compliance tracking (hours-of-service, driver logs, inspections)
- Fleet analytics (utilization, fuel efficiency, maintenance costs, TCO)
- Mobile app for drivers (trip logging, fuel receipts, inspection checklists)

### Target Industries
- **Transportation & Logistics**: Trucking companies, delivery services, courier services
- **Field Service**: HVAC, plumbing, electrical, telecommunications
- **Utilities**: Electric, gas, water, waste management
- **Construction**: Equipment rental, general contractors
- **Government**: Municipal vehicles, public works, emergency services
- **Healthcare**: Ambulance services, home health care
- **Sales & Distribution**: Route sales, vending machine services

## Port Allocation

| Port | Service | Database | Tier | Status |
|------|---------|----------|------|--------|
| 10001 | fleet-vehicle-master | chiroerp_fleet_vehicles | Core | New (2026-02-03) |
| 10002 | fleet-telematics | chiroerp_fleet_telematics | Advanced | New (2026-02-03) |
| 10003 | fleet-driver-management | chiroerp_fleet_vehicles (shared) | Core | New (2026-02-03) |
| 10004 | fleet-fuel-management | chiroerp_fleet_operations | Core | New (2026-02-03) |
| 10005 | fleet-compliance | chiroerp_fleet_operations (shared) | Advanced | New (2026-02-03) |

## Integration Map

```
+------------------------------------------------------------------------------------+
|                             FLEET MANAGEMENT                                        |
|------------------------------------------------------------------------------------|
|                                                                                    |
|  Vehicle Master -> Driver Assignment -> Trip Planning -> Telematics                 |
|         |              |                     |                |                   |
|         v              v                     v                v                   |
|  Fixed Assets    License Tracking      Route Optimize    GPS Tracking             |
|  (ADR-021)       Certifications                           Driving Behavior         |
|         |              |                                        |                   |
|         v              v                                        v                   |
|  Depreciation    Safety Scores                          Safety Alerts              |
|  Acquisition     Compliance                              Maintenance Triggers       |
|                                                                 |                   |
|                                                                 v                   |
|  Fuel Management -> Card Transactions -> Cost Allocation -> Controlling (ADR-028)  |
|         |                    |                    |                                |
|         v                    v                    v                                |
|  Consumption           Reconciliation        Project/Cost Center                   |
|  MPG Tracking          Fraud Detection       Driver Allocation                     |
|                                                                                    |
|  Maintenance Scheduling -> Work Orders -> Plant Maintenance (ADR-040)              |
|         |                       |                                                   |
|         v                       v                                                   |
|  Preventive Plans         Spare Parts (Inventory ADR-024)                          |
|  Service History          Repair Costs (ADR-028)                                   |
|                                                                                    |
|  Fleet Compliance -> Inspections -> Registrations -> DOT/FMCSA -> Audit Reports    |
|         |                |               |                  |                       |
|         v                v               v                  v                       |
|  State Laws        Daily Checks    Renewals           Hours-of-Service            |
|  Emissions         Safety Items    Title/Tags         Driver Logs                 |
|                                                                                    |
+------------------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)

**Fixed Assets (ADR-021):**
- `AssetAcquiredEvent` -> Create vehicle master record
- `AssetDisposedEvent` -> Retire vehicle from fleet
- `AssetTransferredEvent` -> Transfer vehicle between locations/cost centers

**HR (ADR-034):**
- `EmployeeHiredEvent` -> Create driver record
- `EmployeeTerminatedEvent` -> Deactivate driver, reassign vehicles
- `LicenseCertificationExpiredEvent` -> Prevent vehicle assignment

**Plant Maintenance (ADR-040):**
- `MaintenanceWorkOrderCompletedEvent` -> Update vehicle service history
- `MaintenanceScheduleCreatedEvent` -> Trigger fleet maintenance workflow
- `SparePartIssuedEvent` -> Track fleet maintenance parts consumption

**Procurement (ADR-023):**
- `PurchaseOrderApprovedEvent` -> Vehicle acquisition, fuel card procurement
- `GoodsReceivedEvent` -> Vehicle delivery, add to fleet

### Downstream Consumers (Publish Events)

**Fixed Assets (ADR-021):**
- `VehicleAcquiredEvent` -> Create fixed asset record, start depreciation
- `VehicleDisposedEvent` -> Trigger asset disposal posting
- `VehicleMileageUpdatedEvent` -> Update usage-based depreciation

**Plant Maintenance (ADR-040):**
- `FleetMaintenanceDueEvent` -> Create preventive maintenance work order
- `VehicleBreakdownEvent` -> Create corrective maintenance work order
- `TelematicsAlertRaisedEvent` -> Trigger diagnostic maintenance

**Controlling (ADR-028):**
- `FuelCostAllocatedEvent` -> Allocate fuel costs to cost center/project/driver
- `FleetMaintenanceCostPostedEvent` -> Post maintenance costs
- `VehicleUtilizationRecordedEvent` -> Track fleet utilization for cost allocation

**HR (ADR-034):**
- `DriverSafetyScoreUpdatedEvent` -> Update driver performance record
- `DriverComplianceViolationDetectedEvent` -> Trigger HR review (speeding, hours violations)

**Finance/GL (ADR-009):**
- `FuelTransactionPostedEvent` -> Post fuel costs to GL
- `VehicleOperatingCostPostedEvent` -> Monthly fleet cost posting

### Telematics Event Flow
```
Telematics Device -> Telematics Platform (Geotab, Samsara, Verizon Connect)
  -> FleetTelematics.TelematicsDataReceivedEvent
  -> FleetTelematics.ProcessGPSTracking (location, speed, idling)
  -> FleetTelematics.ProcessDrivingBehavior (harsh braking, rapid acceleration, speeding)
  -> FleetTelematics.CalculateSafetyScore
  -> DriverManagement.SafetyScoreUpdatedEvent
  -> (If critical violation) HR.ComplianceViolationEvent

  -> FleetTelematics.ProcessVehicleDiagnostics (engine codes, fuel level, odometer)
  -> (If maintenance alert) PlantMaintenance.MaintenanceDueEvent
```

### Fuel Card Integration Flow
```
Fuel Card Provider (WEX, FleetCor, Voyager)
  -> FuelManagement.FuelTransactionReceivedEvent
  -> FuelManagement.ValidateTransaction (vehicle, driver, location, amount)
  -> (If fraud detected) FuelManagement.FraudAlertRaisedEvent
  -> FuelManagement.ReconcileTransaction
  -> FuelManagement.AllocateCost (cost center/project/driver)
  -> Controlling.CostAllocationEvent
  -> FinanceGL.PostFuelCostEvent
```

### Fleet Maintenance Flow
```
VehicleMaster.MileageUpdatedEvent (from telematics or manual entry)
  -> MaintenanceScheduling.CheckPreventivePlan
  -> (If due) MaintenanceScheduling.MaintenanceDueEvent
  -> PlantMaintenance.CreateWorkOrderEvent
  -> PlantMaintenance.WorkOrderCompletedEvent
  -> VehicleMaster.UpdateServiceHistoryEvent
  -> FuelManagement.UpdateMPGPostMaintenance
```

## Related ADRs

- [ADR-053: Fleet Management](../../adr/ADR-053-fleet-management.md) - **New: Add-on Tier (2026-02-03)**
- [ADR-021: Fixed Asset Accounting](../../adr/ADR-021-fixed-asset-accounting.md) - Vehicle as fixed asset
- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md) - Fleet maintenance scheduling
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md) - Fleet cost allocation
- [ADR-034: HR Integration](../../adr/ADR-034-hr-integration-payroll-events.md) - Driver management
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Spare parts for fleet maintenance
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - Vehicle acquisition, fuel card procurement

## Related Domains

- [Finance Architecture](../finance/README.md) - GL posting, cost allocation
- [Maintenance Architecture](../maintenance/README.md) - Fleet maintenance workflows
- [Procurement Architecture](../procurement/README.md) - Vehicle procurement

## Implementation Roadmap

### Phase 1: Foundation (Core) - Months 1-3
- Vehicle master data management
- Driver license and certification tracking
- Fuel card integration and transaction reconciliation
- Basic maintenance scheduling
- Vehicle assignment and transfer workflows
- Integration with ADR-021 (Fixed Assets)

### Phase 2: Telematics (Advanced) - Months 4-6
- Telematics platform integration (Geotab, Samsara, Verizon Connect)
- Real-time GPS tracking and geofencing
- Driving behavior monitoring (speeding, harsh braking, rapid acceleration)
- Driver safety scoring and alerts
- Vehicle diagnostics and maintenance alerts
- Trip history and route replay

### Phase 3: Compliance (Advanced) - Months 7-9
- DOT/FMCSA compliance tracking
- Hours-of-service (HOS) monitoring
- Driver qualification files (DQF)
- Vehicle inspection tracking (pre-trip, post-trip, annual)
- Registration and title renewals
- Emissions compliance (DEF, smog checks)
- Automated compliance alerts and reporting

### Phase 4: Analytics & Optimization (Advanced) - Months 10-12
- Fleet utilization dashboards
- Fuel efficiency analysis and MPG tracking
- Maintenance cost analysis and TCO reporting
- Driver performance scorecards
- Route optimization recommendations
- Fleet rightsizing analysis (underutilized vehicles)
- Predictive maintenance based on telematics data

### Phase 5: Advanced Features (Future) - Months 13+
- ELD (Electronic Logging Device) integration for HOS compliance
- Video telematics (dashcam integration) for safety and incident management
- Fuel optimization (preferred vendor routing, fuel price tracking)
- Electric vehicle (EV) management (charging schedules, range optimization)
- Autonomous vehicle readiness (AV fleet management)
- Carbon footprint tracking and emissions reporting

---

## Implementation Status

### Current Status: Specification Complete (2026-02-03)

**5 modules specified:**
- Vehicle Master (Core) - Vehicle lifecycle, specs, assignments
- Telematics Integration (Advanced) - GPS, driving behavior, diagnostics
- Driver Management (Core) - Licenses, certifications, safety scores
- Fuel Management (Core) - Card integration, consumption, cost allocation
- Fleet Compliance (Advanced) - Inspections, registrations, DOT compliance

**Target Customers:**
- Organizations with 100+ vehicles
- Transportation & logistics companies
- Field service operations
- Utilities and construction
- Government/municipal fleets

**Next Steps:**
1. Pilot Phase 1 (Core) with 2-3 customers (500+ vehicle fleets)
2. Partner with telematics providers (Geotab, Samsara, Verizon Connect) for Phase 2
3. Partner with fuel card providers (WEX, FleetCor, Voyager) for card integration
4. Build compliance library for Phase 3 (state laws, DOT regulations)
5. Develop ML models for Phase 4 (predictive maintenance, route optimization)
