# ADR-053: Fleet Management (Add-on)

**Status**: Accepted (Planned - Blueprint Defined)
**Date**: 2026-02-05
**Updated**: 2026-02-06 - Fixed port assignments (9761-9768) to avoid MDM conflict
**Deciders**: Architecture Team, Operations Team
**Priority**: P3 (Optional Add-on)
**Tier**: Add-on
**Tags**: fleet, vehicles, telematics, driver-management, fuel, maintenance, hexagonal-architecture

## Context
Organizations operating vehicle fleets (delivery, transportation, field service, sales, executive transport) require comprehensive fleet management capabilities beyond lease accounting. This includes vehicle master data, driver management, telematics integration, fuel card tracking, maintenance scheduling, utilization monitoring, and compliance management (inspections, registrations, insurance).

While **ADR-033** covers fleet lease accounting (finance/operating leases) and **ADR-040** handles maintenance workflows, neither provides fleet-specific operational capabilities like GPS tracking, driver behavior monitoring, fuel consumption analytics, or DOT compliance.

## Decision
Implement a **Fleet Management** add-on module that provides vehicle lifecycle management, driver assignment, telematics integration, fuel management, fleet maintenance scheduling, and compliance tracking integrated with fixed assets, procurement, controlling, and plant maintenance.

### Subdomain Architecture
Fleet Management is implemented as 8 subdomains within the `fleet/` bounded context, each following **hexagonal architecture** with clean separation of concerns:

```
fleet/                                                # Fleet Management Bounded Context (ADR-053)
├── fleet-shared/                                     # Shared fleet types (ADR-006 compliant)
├── fleet-vehicle-master/                             # Vehicle Master Data (Port 9761)
├── fleet-driver-management/                          # Driver Management (Port 9762)
├── fleet-telematics/                                 # Telematics Integration (Port 9763)
├── fleet-fuel-management/                            # Fuel Card Management (Port 9764)
├── fleet-maintenance/                                # Fleet Maintenance (Port 9765)
├── fleet-compliance/                                 # Compliance Management (Port 9766)
├── fleet-utilization/                                # Utilization & TCO (Port 9767)
└── fleet-lifecycle/                                  # Vehicle Lifecycle (Port 9768)
```

**Package Structure**: `com.chiroerp.fleet.*`
**Port Range**: 9761-9769 (avoids conflict with MDM 9701-9709)

#### 1. Vehicle Master Data (Port 9761)
**Package**: `com.chiroerp.fleet.vehiclemaster`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `Vehicle`, `VehicleStatus`, `VehicleType`, `FleetClassification`, `FleetPool`, `PoolType`, `VehicleHierarchy`, `TechnicalSpec`, `EngineType`, `AcquisitionDetails`, `HomeLocation`, `AssetLink` | Vehicle master entities |
| **Domain Events** | `VehicleCreatedEvent`, `VehicleUpdatedEvent`, `VehicleStatusChangedEvent`, `VehicleAssignedToPoolEvent`, `VehicleTransferredEvent`, `VehicleRetiredEvent` | Vehicle lifecycle events |
| **Input Ports** | `VehicleUseCase`, `FleetPoolUseCase` | Vehicle management use cases |
| **Output Ports** | `VehicleRepositoryPort`, `FleetPoolRepositoryPort`, `FixedAssetPort`, `VehicleMasterEventPublisherPort` | Persistence and integration |
| **Domain Services** | `VehicleRegistrationService`, `FleetHierarchyService`, `AssetIntegrationService` | Business logic |
| **REST Controllers** | `VehicleResource`, `FleetPoolResource` | API endpoints |

#### 2. Driver Management (Port 9762)
**Package**: `com.chiroerp.fleet.drivermanagement`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `Driver`, `DriverLicense`, `LicenseClass`, `Endorsement`, `MedicalCertification`, `DriverAssignment`, `AssignmentType`, `DriverSafetyScore`, `SafetyEvent`, `TrainingRecord`, `MVRRecord`, `DisciplinaryAction` | Driver management entities |
| **Domain Events** | `DriverCreatedEvent`, `DriverAssignedToVehicleEvent`, `DriverUnassignedEvent`, `LicenseExpiringSoonEvent`, `SafetyScoreUpdatedEvent`, `SafetyEventRecordedEvent`, `TrainingCompletedEvent` | Driver lifecycle events |
| **Input Ports** | `DriverUseCase`, `DriverAssignmentUseCase` | Driver management use cases |
| **Output Ports** | `DriverRepositoryPort`, `AssignmentRepositoryPort`, `HRIntegrationPort`, `DriverManagementEventPublisherPort` | Persistence and integration |
| **Domain Services** | `DriverAssignmentService`, `SafetyScoreCalculationService`, `LicenseValidationService`, `ComplianceCheckService` | Business logic |
| **REST Controllers** | `DriverResource`, `DriverAssignmentResource` | API endpoints |

#### 3. Telematics Integration (Port 9763)
**Package**: `com.chiroerp.fleet.telematics`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `TelematicsEvent`, `EventType`, `GPSLocation`, `VehicleDiagnostics`, `DiagnosticTroubleCode`, `EngineMetrics`, `DriverBehavior`, `SpeedingEvent`, `HarshBrakingEvent`, `RapidAccelerationEvent`, `IdleTimeEvent`, `Geofence`, `GeofenceViolation`, `TripRecord` | Telematics entities |
| **Domain Events** | `TelematicsEventReceivedEvent`, `GPSLocationUpdatedEvent`, `DiagnosticAlertRaisedEvent`, `SpeedingDetectedEvent`, `HarshBrakingDetectedEvent`, `GeofenceViolationEvent`, `TripStartedEvent`, `TripEndedEvent` | Telematics events |
| **Input Ports** | `TelematicsIngestionUseCase`, `GeofenceUseCase` | Telematics use cases |
| **Output Ports** | `TelematicsEventRepositoryPort`, `GPSLocationRepositoryPort`, `TripRepositoryPort`, `TelematicsEventPublisherPort` | Persistence and integration |
| **Domain Services** | `TelematicsIngestionService`, `GPSTrackingService`, `DiagnosticsMonitoringService`, `DriverBehaviorAnalysisService`, `GeofenceMonitoringService` | Business logic |
| **REST Controllers** | `TelematicsResource`, `GPSTrackingResource`, `GeofenceResource` | API endpoints |
| **Webhook Adapters** | `TelematicsWebhookReceiver` | Real-time event ingestion |
| **Vendor Adapters** | `GeotabAdapter`, `SamsaraAdapter`, `VerizonConnectAdapter` | Telematics vendor integration |

#### 4. Fuel Card Management (Port 9764)
**Package**: `com.chiroerp.fleet.fuelmanagement`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `FuelTransaction`, `FuelCard`, `CardProvider`, `CardRestriction`, `FuelPurchase`, `FuelType`, `FuelEfficiency`, `FuelCostAllocation`, `FuelAnomaly`, `FuelBudget`, `FuelTaxRecovery` | Fuel management entities |
| **Domain Events** | `FuelTransactionRecordedEvent`, `FuelCardIssuedEvent`, `FuelAnomalyDetectedEvent`, `FuelEfficiencyCalculatedEvent`, `FuelCostAllocatedEvent`, `FuelBudgetExceededEvent` | Fuel events |
| **Input Ports** | `FuelTransactionUseCase`, `FuelCardUseCase` | Fuel management use cases |
| **Output Ports** | `FuelTransactionRepositoryPort`, `FuelCardRepositoryPort`, `ControllingPort`, `FuelManagementEventPublisherPort` | Persistence and integration |
| **Domain Services** | `FuelTransactionProcessingService`, `FuelEfficiencyCalculationService`, `FuelAnomalyDetectionService`, `FuelCostAllocationService`, `FuelTaxRecoveryService` | Business logic |
| **REST Controllers** | `FuelTransactionResource`, `FuelCardResource` | API endpoints |
| **Vendor Adapters** | `WEXAdapter`, `VoyagerAdapter`, `ComdataAdapter`, `ShellFleetAdapter` | Fuel card provider integration |

#### 5. Fleet Maintenance (Port 9765)
**Package**: `com.chiroerp.fleet.maintenance`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `MaintenanceSchedule`, `ScheduleType`, `MaintenanceRecord`, `ServiceType`, `ServiceProvider`, `InspectionRecord`, `InspectionType`, `InspectionDefect`, `RecallCampaign`, `WarrantyTracking`, `DowntimeRecord`, `LoanerVehicle` | Fleet maintenance entities |
| **Domain Events** | `MaintenanceDueEvent`, `MaintenanceCompletedEvent`, `InspectionCompletedEvent`, `InspectionFailedEvent`, `RecallNotificationEvent`, `WarrantyClaimSubmittedEvent`, `VehicleDowntimeStartedEvent`, `VehicleReturnedToServiceEvent` | Maintenance events |
| **Input Ports** | `MaintenanceScheduleUseCase`, `InspectionUseCase` | Fleet maintenance use cases |
| **Output Ports** | `MaintenanceScheduleRepositoryPort`, `MaintenanceRecordRepositoryPort`, `InspectionRepositoryPort`, `PlantMaintenancePort`, `FleetMaintenanceEventPublisherPort` | Persistence and integration |
| **Domain Services** | `MaintenanceSchedulingService`, `InspectionManagementService`, `RecallTrackingService`, `WarrantyManagementService`, `DowntimeTrackingService` | Business logic |
| **REST Controllers** | `MaintenanceScheduleResource`, `InspectionResource` | API endpoints |

#### 6. Compliance Management (Port 9766)
**Package**: `com.chiroerp.fleet.compliance`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `VehicleRegistration`, `RegistrationState`, `IRPRegistration`, `InsurancePolicy`, `InsuranceCoverage`, `InsuranceClaim`, `DOTCompliance`, `FMCSACarrier`, `HoursOfService`, `ELDRecord`, `DriverQualificationFile`, `DrugAlcoholTest`, `EnvironmentalCompliance`, `COIGeneration` | Compliance entities |
| **Domain Events** | `RegistrationExpiringSoonEvent`, `RegistrationRenewedEvent`, `InsuranceExpiringSoonEvent`, `InsuranceClaimFiledEvent`, `DOTViolationRecordedEvent`, `HOSViolationDetectedEvent`, `DQFileUpdatedEvent`, `EmissionsTestDueEvent` | Compliance events |
| **Input Ports** | `RegistrationUseCase`, `InsuranceUseCase`, `DOTComplianceUseCase` | Compliance use cases |
| **Output Ports** | `RegistrationRepositoryPort`, `InsuranceRepositoryPort`, `DOTComplianceRepositoryPort`, `ELDIntegrationPort`, `ComplianceEventPublisherPort` | Persistence and integration |
| **Domain Services** | `RegistrationTrackingService`, `InsuranceManagementService`, `DOTComplianceService`, `HOSMonitoringService`, `DQFileManagementService`, `EnvironmentalComplianceService` | Business logic |
| **REST Controllers** | `RegistrationResource`, `InsuranceResource`, `DOTComplianceResource` | API endpoints |

#### 7. Utilization & TCO (Port 9767)
**Package**: `com.chiroerp.fleet.utilization`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `UtilizationMetric`, `MileageTracking`, `EngineHours`, `TripCount`, `IdleTimeMetric`, `UtilizationRate`, `CostTracking`, `FuelCostPerMile`, `MaintenanceCostRatio`, `TotalCostOfOwnership`, `TCOComponent`, `FleetKPI`, `AccidentRate`, `ReplacementCycle` | Utilization & TCO entities |
| **Domain Events** | `UtilizationCalculatedEvent`, `TCOCalculatedEvent`, `LowUtilizationAlertEvent`, `HighTCOAlertEvent`, `ReplacementRecommendedEvent`, `FleetKPIUpdatedEvent` | Utilization events |
| **Input Ports** | `UtilizationUseCase`, `TCOUseCase` | Utilization use cases |
| **Output Ports** | `UtilizationRepositoryPort`, `TCORepositoryPort`, `FleetKPIRepositoryPort`, `UtilizationEventPublisherPort` | Persistence and integration |
| **Domain Services** | `UtilizationCalculationService`, `TCOCalculationService`, `FleetKPIService`, `BenchmarkingService`, `ReplacementPlanningService` | Business logic |
| **REST Controllers** | `UtilizationResource`, `TCOResource`, `FleetKPIResource` | API endpoints |

#### 8. Vehicle Lifecycle (Port 9768)
**Package**: `com.chiroerp.fleet.lifecycle`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `VehicleAcquisition`, `AcquisitionType`, `AcquisitionApproval`, `VehicleDeployment`, `TelematicsInstallation`, `FuelCardIssuance`, `TollTagIssuance`, `VehicleTransfer`, `TransferType`, `VehicleDisposal`, `DisposalMethod`, `DisposalApproval`, `DisposalAccounting` | Lifecycle entities |
| **Domain Events** | `AcquisitionRequestedEvent`, `AcquisitionApprovedEvent`, `VehicleReceivedEvent`, `VehicleDeployedEvent`, `VehicleTransferredEvent`, `DisposalRequestedEvent`, `DisposalApprovedEvent`, `VehicleDisposedEvent`, `AssetDisposalPostedEvent` | Lifecycle events |
| **Input Ports** | `AcquisitionUseCase`, `DeploymentUseCase`, `DisposalUseCase` | Lifecycle use cases |
| **Output Ports** | `AcquisitionRepositoryPort`, `DeploymentRepositoryPort`, `DisposalRepositoryPort`, `ProcurementPort`, `FixedAssetPort`, `LifecycleEventPublisherPort` | Persistence and integration |
| **Domain Services** | `AcquisitionWorkflowService`, `DeploymentService`, `TransferService`, `DisposalWorkflowService`, `AssetDisposalPostingService` | Business logic |
| **REST Controllers** | `AcquisitionResource`, `DeploymentResource`, `DisposalResource` | API endpoints |

### Inter-Subdomain Communication

| Source Subdomain | Target Subdomain | Communication | Purpose |
|------------------|------------------|---------------|---------|
| Vehicle Master | Driver Management | Event/Query | Provide vehicle info for driver assignment |
| Driver Management | Telematics | Event | Link driver to telematics behavior events |
| Telematics | Driver Management | Event | Update driver safety scores |
| Telematics | Fuel Management | Event | Validate fuel transaction odometer |
| Telematics | Fleet Maintenance | Event | Trigger maintenance alerts from diagnostics |
| Fuel Management | Utilization | Event | Provide fuel cost data for TCO |
| Fleet Maintenance | Utilization | Event | Provide maintenance cost data for TCO |
| Compliance | Driver Management | Query | Validate driver license/certification |
| Utilization | Lifecycle | Event | Recommend vehicle replacement |
| Lifecycle | Vehicle Master | Event | Create/update/retire vehicles |

### Port Assignments

| Subdomain | Port | Package |
|-----------|------|---------|
| Vehicle Master Data | 9761 | `com.chiroerp.fleet.vehiclemaster` |
| Driver Management | 9762 | `com.chiroerp.fleet.drivermanagement` |
| Telematics Integration | 9763 | `com.chiroerp.fleet.telematics` |
| Fuel Card Management | 9764 | `com.chiroerp.fleet.fuelmanagement` |
| Fleet Maintenance | 9765 | `com.chiroerp.fleet.maintenance` |
| Compliance Management | 9766 | `com.chiroerp.fleet.compliance` |
| Utilization & TCO | 9767 | `com.chiroerp.fleet.utilization` |
| Vehicle Lifecycle | 9768 | `com.chiroerp.fleet.lifecycle` |

## Core Capabilities

### Vehicle Master Data
- **Vehicle Registry**:
  - VIN, make, model, year, color, license plate.
  - Vehicle type (sedan, van, truck, trailer, specialty).
  - Fleet classification (delivery, sales, executive, service, rental).
  - Acquisition details (purchase/lease, date, cost, supplier).
  - Current status (active, idle, maintenance, retired, sold).
  - Home location/garage assignment.
  - Ownership entity (legal entity, cost center).

- **Vehicle Hierarchy**:
  - Fleet → Pool (delivery, sales, service) → Vehicle → Component.
  - Multi-level classification for reporting and allocation.

- **Technical Specifications**:
  - Engine type (gasoline, diesel, hybrid, electric, CNG).
  - Fuel capacity, fuel efficiency (MPG, L/100km).
  - Payload capacity, towing capacity, seating.
  - Tire specifications and replacement cycles.

- **Asset Integration**:
  - Link to Fixed Asset register (ADR-021) for depreciation.
  - Link to Lease Accounting (ADR-033) if leased vehicle.
  - Capitalized vs expense treatment for modifications.

### Driver Management
- **Driver Registry**:
  - Driver master data (employee or contractor).
  - Driver's license number, class, state, expiration.
  - Endorsements and certifications (CDL, hazmat, passenger).
  - Medical certification status (DOT physical).
  - Background check and MVR (motor vehicle record) status.

- **Driver Assignments**:
  - Assign driver to vehicle (permanent or temporary).
  - Multi-driver vehicles (pool vehicles).
  - Driver availability calendar and time off.
  - Driver home location and territory.

- **Driver Safety**:
  - Safety score (based on telematics: speeding, harsh braking, rapid acceleration).
  - Incident history (accidents, violations, claims).
  - Training records (defensive driving, safety courses).
  - Disciplinary actions and improvement plans.

### Telematics Integration
- **GPS Tracking**:
  - Real-time vehicle location and map view.
  - Historical trip playback and route analysis.
  - Geofencing (alerts for unauthorized zones).
  - Mileage capture (odometer reading sync).

- **Vehicle Diagnostics**:
  - Engine hours, idle time, fuel level.
  - Diagnostic trouble codes (DTCs) and alerts.
  - Battery voltage, tire pressure (TPMS).
  - Oil life, brake wear, maintenance reminders.

- **Driver Behavior**:
  - Speeding events (over posted limit or threshold).
  - Harsh braking, rapid acceleration, sharp cornering.
  - Seatbelt usage, distracted driving (if supported).
  - Idling time and fuel waste.

- **Telematics Vendor Integration**:
  - API connectors for Geotab, Samsara, Verizon Connect, Teletrac Navman.
  - Standardized telematics event schema.
  - Webhook ingestion for real-time alerts.
  - Batch import for historical data (daily odometer, fuel).

### Fuel Card Management
- **Fuel Card Integration**:
  - Supported providers: WEX, Voyager, Comdata, Shell Fleet, BP.
  - Import fuel transactions (date, location, gallons/liters, cost, vehicle, driver).
  - Card assignment to vehicles and drivers.
  - Card limits and restrictions (fuel type, merchant category).

- **Fuel Transaction Processing**:
  - Match transaction to vehicle (by card or VIN).
  - Validate odometer reading and location.
  - Flag anomalies (duplicate purchases, off-hours, geographic outliers).
  - Calculate fuel efficiency (MPG = miles / gallons).

- **Fuel Cost Allocation**:
  - Allocate fuel cost to cost center, project, or customer job.
  - Fuel surcharge calculation for billing.
  - Budget tracking and variance analysis.
  - Tax recovery (fuel tax credits, off-road use).

### Fleet Maintenance
- **Maintenance Scheduling**:
  - Preventive maintenance (PM) by mileage or time intervals.
  - Oil changes, tire rotations, inspections, brake service.
  - Telematics-triggered maintenance alerts (engine hours, DTC codes).
  - Integration with Plant Maintenance (ADR-040) for work orders.

- **Service History**:
  - Maintenance records (date, mileage, service type, vendor, cost).
  - Parts replaced and warranty tracking.
  - Recalls and safety campaigns.
  - Service provider network (dealerships, fleet shops, mobile mechanics).

- **Inspections**:
  - Pre-trip and post-trip inspections (DVIR - Driver Vehicle Inspection Report).
  - Annual safety inspections (state requirements).
  - DOT inspections (commercial vehicles).
  - Failed inspection follow-up and out-of-service tracking.

- **Downtime Tracking**:
  - Vehicle availability status (in service, maintenance, repair, awaiting parts).
  - Downtime duration and impact on fleet capacity.
  - Loaner/rental vehicle provisioning.

### Utilization & Performance
- **Utilization Metrics**:
  - Miles/km driven (daily, monthly, annual).
  - Hours operated and engine hours.
  - Trips and stops per vehicle.
  - Idle time percentage.
  - Utilization rate (actual use / available time).

- **Cost Tracking**:
  - Fuel cost per vehicle, per mile, per trip.
  - Maintenance cost (scheduled + unscheduled).
  - Insurance, registration, tolls, parking.
  - Total Cost of Ownership (TCO) per vehicle.
  - Cost per mile/km benchmarking.

- **Fleet KPIs**:
  - Fleet size and composition.
  - Average vehicle age and replacement cycle.
  - Fuel efficiency trends (MPG over time).
  - Maintenance cost ratio (maintenance $ / vehicle value).
  - Accident rate and claim frequency.
  - Driver safety score distribution.

### Compliance Management
- **Registration & Licensing**:
  - Vehicle registration expiration tracking.
  - License plate renewals and state fees.
  - Multi-state registration (IRP - International Registration Plan).
  - Title and lien tracking.

- **Insurance**:
  - Policy master data (carrier, policy number, coverage limits).
  - Coverage by vehicle (comprehensive, collision, liability).
  - Insurance expiration alerts.
  - Claims tracking (date, description, cost, status).
  - Certificate of Insurance (COI) generation for customers.

- **DOT Compliance** (Commercial Fleets):
  - FMCSA carrier number and safety rating.
  - Hours of Service (HOS) tracking for drivers.
  - Electronic Logging Device (ELD) integration.
  - Drug and alcohol testing program.
  - Driver qualification files (DQ files).
  - Annual vehicle inspections and documentation.

- **Environmental Compliance**:
  - Emissions testing and certification.
  - CARB compliance (California Air Resources Board).
  - Idle reduction mandates.
  - Integration with ESG Reporting (ADR-035) for Scope 1 emissions.

### Vehicle Lifecycle
- **Acquisition**:
  - Purchase requisition and approval.
  - Vendor selection (dealership, auction, manufacturer).
  - Purchase order integration (ADR-023).
  - Fixed asset capitalization (ADR-021).
  - Lease setup if applicable (ADR-033).

- **Deployment**:
  - Vehicle assignment to pool, driver, or location.
  - Initial inspection and documentation.
  - Telematics device installation.
  - Fuel card and toll tag issuance.

- **Transfer**:
  - Inter-location transfers (branch to branch).
  - Re-assignment to different pool or driver.
  - Cost center reallocation.

- **Disposal**:
  - Retirement decision (age, mileage, condition, TCO).
  - Disposal method (sell, trade-in, auction, scrap, donate).
  - Asset disposal accounting (ADR-021).
  - Title transfer and deregistration.
  - Final inspection and documentation.

## Data Model (Conceptual)

### Core Entities
- **Vehicle**: vehicle_id, VIN, make, model, year, license_plate, fleet_pool_id, status, home_location, acquisition_date, acquisition_cost, ownership_type, fuel_type, current_mileage.
- **FleetPool**: pool_id, name, type (delivery, sales, service, executive), legal_entity_id, cost_center_id.
- **Driver**: driver_id, employee_id, license_number, license_class, license_expiration, endorsements, medical_cert_expiration, safety_score.
- **DriverAssignment**: assignment_id, vehicle_id, driver_id, start_date, end_date, assignment_type (permanent, temporary, pool).
- **TelematicsEvent**: event_id, vehicle_id, timestamp, latitude, longitude, odometer, speed, fuel_level, engine_hours, event_type (location, speeding, harsh_brake, idle_start, idle_end).
- **FuelTransaction**: transaction_id, vehicle_id, driver_id, fuel_card_id, transaction_date, location, fuel_type, quantity, unit_price, total_cost, odometer, vendor.
- **MaintenanceRecord**: record_id, vehicle_id, service_date, service_type, odometer, vendor, labor_cost, parts_cost, total_cost, next_service_due.
- **InspectionRecord**: inspection_id, vehicle_id, driver_id, inspection_date, inspection_type (pre_trip, post_trip, annual, DOT), status (pass, fail), defects, corrective_actions.
- **Registration**: registration_id, vehicle_id, state, registration_number, issue_date, expiration_date, fee, renewal_status.
- **InsurancePolicy**: policy_id, carrier, policy_number, effective_date, expiration_date, premium, coverage_types.
- **VehicleInsurance**: vehicle_insurance_id, vehicle_id, policy_id, coverage_limits.
- **Incident**: incident_id, vehicle_id, driver_id, incident_date, incident_type (accident, violation, claim), description, cost, status (open, closed).

### Relationships
- Vehicle → FleetPool (many-to-one)
- Vehicle → Driver (many-to-many via DriverAssignment)
- Vehicle → TelematicsEvent (one-to-many)
- Vehicle → FuelTransaction (one-to-many)
- Vehicle → MaintenanceRecord (one-to-many)
- Vehicle → InspectionRecord (one-to-many)
- Vehicle → Registration (one-to-many, multi-state)
- Vehicle → VehicleInsurance → InsurancePolicy
- Driver → Incident (one-to-many)

## Key Workflows

### Vehicle Acquisition
1. **Initiate Purchase**:
   - Fleet manager creates vehicle requisition.
   - Approval workflow (budget, fleet size limits).

2. **Procure Vehicle**:
   - Create purchase order (ADR-023).
   - Receive vehicle and inspection.
   - Capitalize as fixed asset (ADR-021).

3. **Setup Vehicle**:
   - Register in fleet master data.
   - Install telematics device.
   - Issue fuel card and toll tag.
   - Assign to pool and/or driver.

### Fuel Transaction Processing
1. **Import Fuel Data**:
   - Daily import from fuel card provider.
   - Parse transactions and map to vehicles.

2. **Validation**:
   - Match odometer to telematics reading.
   - Check for duplicate or fraudulent transactions.
   - Flag anomalies for review.

3. **Cost Allocation**:
   - Post fuel expense to cost center or project.
   - Update fuel efficiency metrics.
   - Generate fuel usage reports.

### Preventive Maintenance
1. **Maintenance Schedule**:
   - Define PM schedule (e.g., every 5,000 miles or 6 months).
   - Monitor odometer and date triggers.

2. **Generate Work Order**:
   - Create maintenance work order (ADR-040 integration).
   - Schedule appointment with service provider.
   - Reserve vehicle (mark as unavailable).

3. **Complete Service**:
   - Record service completion and costs.
   - Update next service due mileage/date.
   - Return vehicle to service.

### Driver Safety Monitoring
1. **Capture Events**:
   - Ingest telematics events (speeding, harsh braking).
   - Calculate daily/weekly safety scores.

2. **Alert & Review**:
   - Alert fleet manager for severe events (excessive speeding, accident).
   - Driver coaching and remedial training.

3. **Performance Tracking**:
   - Monthly safety scorecards.
   - Recognition for safe drivers.
   - Corrective action plans for poor performers.

## Integration Points

### Internal Subdomain Integration
The 8 Fleet Management subdomains integrate through domain events and queries:

| Source → Target | Integration Pattern | Purpose |
|-----------------|---------------------|---------|
| Vehicle Master (9761) → Driver Management (9762) | Event/Query | Provide vehicle info for driver assignment |
| Driver Management (9762) → Telematics (9763) | Event | Link driver to telematics behavior events |
| Telematics (9763) → Driver Management (9762) | Event | Update driver safety scores |
| Telematics (9763) → Fuel Management (9764) | Event | Validate fuel transaction odometer |
| Telematics (9763) → Fleet Maintenance (9765) | Event | Trigger maintenance alerts from diagnostics |
| Fuel Management (9764) → Utilization (9767) | Event | Provide fuel cost data for TCO |
| Fleet Maintenance (9765) → Utilization (9767) | Event | Provide maintenance cost data for TCO |
| Compliance (9766) → Driver Management (9762) | Query | Validate driver license/certification |
| Utilization (9767) → Lifecycle (9768) | Event | Recommend vehicle replacement |
| Lifecycle (9768) → Vehicle Master (9761) | Event | Create/update/retire vehicles |

### External Module Integration

#### Fixed Assets (ADR-021)
- **Vehicle Master (9761)** → Vehicle capitalization and depreciation.
- **Lifecycle (9768)** → Asset disposal accounting (sale, trade-in, scrap).
- Transfer between cost centers.

#### Lease Accounting (ADR-033)
- **Vehicle Master (9761)** → Operating and finance lease setup for leased vehicles.
- Lease payment processing and ROU asset tracking.
- Lease return and disposition.

#### Procurement (ADR-023)
- **Lifecycle (9768)** → Vehicle purchase orders and supplier management.
- **Fleet Maintenance (9765)** → Parts procurement for repairs.
- Fuel card and toll tag ordering.

#### Plant Maintenance (ADR-040)
- **Fleet Maintenance (9765)** → Work order management for vehicle repairs.
- Spare parts inventory and usage.
- Maintenance task lists and checklists.
- Downtime tracking.

#### Controlling (CO, ADR-028)
- **Fuel Management (9764)** → Cost center allocation for fuel.
- **Fleet Maintenance (9765)** → Cost allocation for maintenance, insurance.
- Internal orders for vehicle repairs.
- Profitability analysis for delivery/service fleets.
- Fleet budget vs actual tracking.

#### Treasury (ADR-026)
- **Fuel Management (9764)** → Fuel card payment processing.
- **Compliance (9766)** → Insurance premium payments.
- Registration fee payments.

#### HR Integration (ADR-034)
- **Driver Management (9762)** → Driver master data (employee assignments).
- Driver training records and certifications.
- Payroll integration for mileage reimbursement.

#### Field Service (ADR-042)
- **Driver Management (9762)** → Technician vehicle assignments.
- **Telematics (9763)** → Trip tracking and customer visit verification.
- Parts inventory on service vehicles.

#### ESG Reporting (ADR-035)
- **Fuel Management (9764)** → Scope 1 emissions from fleet fuel consumption.
- **Utilization (9767)** → Fuel efficiency trends and sustainability KPIs.
- Electric vehicle (EV) adoption tracking.

#### Analytics (ADR-016)
- **Utilization (9767)** → Fleet performance dashboards (TCO, utilization, safety).
- **Fuel Management (9764)** → Fuel consumption analytics and trend analysis.
- **Driver Management (9762)** → Driver behavior scorecards.
- **Fleet Maintenance (9765)** → Maintenance cost benchmarking.

#### Master Data Governance (ADR-027)
- **Vehicle Master (9761)** → Vehicle master data stewardship.
- **Driver Management (9762)** → Driver data quality and validation.
- Vendor master (service providers, fuel stations).

## Non-Functional Requirements

### Performance
- **Telematics ingestion latency**: p95 < 5 minutes for real-time location.
- **Fuel transaction processing**: Daily batch import within 2 hours.
- **Dashboard load time**: < 2 seconds for fleet overview (1,000 vehicles).

### Scalability
- Support fleets from 10 to 10,000+ vehicles.
- Handle 1M+ telematics events per day.
- 100K+ fuel transactions per month.

### Reliability
- **Vehicle location availability**: 99.9% uptime for GPS tracking.
- **Fuel data accuracy**: 99.95% transaction match rate.
- **Maintenance alert delivery**: 100% on-time alerts for expiring registrations/inspections.

### Security
- Driver PII protection (license numbers, medical records).
- Telematics data encryption in transit and at rest.
- Role-based access control (fleet managers, drivers, auditors).

### Compliance
- **DOT compliance**: Maintain driver qualification files and HOS logs.
- **Privacy**: Limit driver tracking to business hours (configurable).
- **Data retention**: 7-year retention for tax and audit (fuel, maintenance).

## KPIs and SLOs

### Operational KPIs
| KPI | Target | Measurement |
|-----|--------|-------------|
| **Fleet utilization rate** | >= 85% | (Vehicles in active use / Total fleet) × 100 |
| **Fuel efficiency (MPG)** | Baseline +5% YoY | Total miles / Total gallons |
| **Maintenance cost per mile** | <= $0.15 | Total maintenance cost / Total miles |
| **Vehicle downtime** | <= 5% | (Downtime hours / Available hours) × 100 |
| **Driver safety score** | >= 90/100 | Weighted average of telematics safety events |
| **Accident rate** | <= 2 per 100 vehicles/year | Incidents / Fleet size |
| **On-time maintenance** | >= 95% | PM completed by due date |
| **Registration compliance** | 100% | Active vehicles with valid registration |

### Technical SLOs
| Service | SLO | Measurement |
|---------|-----|-------------|
| **GPS tracking availability** | 99.9% | Uptime of telematics API |
| **Fuel transaction import** | Daily by 6 AM | ETL completion time |
| **Maintenance alert delivery** | 100% on-time | Alerts sent 30 days before due date |
| **Dashboard performance** | p95 < 2s | Page load time |

## Alternatives Considered

### 1. Extend Plant Maintenance (ADR-040)
- **Rejected**: Fleet has unique workflows (driver management, fuel, telematics, DOT compliance) that don't fit generic equipment maintenance.

### 2. Use External Fleet Management System (FMS)
- **Rejected**: Weak integration with ERP financials (cost allocation, GL posting). Requires duplicate master data entry.

### 3. Manual Spreadsheet Tracking
- **Rejected**: Not scalable, error-prone, no real-time visibility, lacks compliance audit trail.

### 4. Embed in Field Service (ADR-042)
- **Rejected**: Fleet management serves broader use cases (executive, sales, delivery) beyond field service technicians.

## Consequences

### Positive
- Provides end-to-end fleet visibility (location, utilization, cost, compliance).
- Reduces fuel waste and improves driver safety through telematics.
- Automates compliance tracking (registrations, inspections, insurance).
- Enables data-driven fleet optimization (right-sizing, replacement cycles, TCO).
- Centralizes fleet financials in ERP (no separate FMS system).

### Negative / Risks
- Requires telematics hardware investment and vendor partnerships.
- Driver privacy concerns (GPS tracking, behavior monitoring).
- Complexity of multi-state registration and DOT compliance rules.
- Dependency on external fuel card and telematics APIs.

### Neutral
- Optional add-on for tenants with significant vehicle fleets (100+ vehicles).
- Can be phased: Core (vehicle master, fuel, maintenance) → Advanced (telematics, compliance).

## Implementation Phases
Implementation follows the subdomain architecture within `fleet/`:

### Phase 1: Foundation (Core)
- **fleet-vehicle-master** (Port 9761): Vehicle master data, hierarchy, pool management.
- **fleet-driver-management** (Port 9762): Driver registry, assignments, license tracking.
- **fleet-fuel-management** (Port 9764): Fuel card integration, transaction processing, efficiency.
- Integration with Fixed Assets (ADR-021) and Procurement (ADR-023).

### Phase 2: Telematics (Advanced)
- **fleet-telematics** (Port 9763): GPS tracking, real-time location, trip history.
- Odometer sync and mileage capture.
- Driver behavior monitoring (speeding, harsh braking).
- Geofencing and alerts.
- Vendor adapters: Geotab, Samsara, Verizon Connect.

### Phase 3: Maintenance & Compliance (Advanced)
- **fleet-maintenance** (Port 9765): PM scheduling, service history, inspections (DVIR, annual, DOT).
- **fleet-compliance** (Port 9766): Registration, insurance, DOT compliance, HOS, ELD integration.
- Integration with Plant Maintenance (ADR-040) for work orders.

### Phase 4: Analytics & Optimization (Advanced)
- **fleet-utilization** (Port 9767): Fleet performance dashboards, utilization metrics.
- TCO analysis and benchmarking.
- Predictive maintenance integration.
- Right-sizing recommendations.

### Phase 5: Lifecycle Management (Advanced)
- **fleet-lifecycle** (Port 9768): Acquisition workflow, deployment, transfer, disposal.
- Asset capitalization integration.
- Disposal accounting.

### Phase 6: Advanced Features (Future)
- Electric vehicle (EV) charging management.
- Route optimization integration.
- Connected car APIs (OEM telematics).
- Autonomous vehicle fleet management.

## References
- **ADR-021**: Fixed Asset Accounting (vehicle capitalization, depreciation).
- **ADR-033**: Lease Accounting (fleet lease accounting).
- **ADR-040**: Plant Maintenance (vehicle maintenance workflows).
- **ADR-042**: Field Service Operations (service vehicle assignments).
- **ADR-035**: ESG & Sustainability Reporting (fleet emissions).
- **ADR-023**: Procurement (vehicle purchasing, parts).
- **ADR-028**: Controlling (fleet cost allocation).
- **ADR-034**: HR Integration (driver master data).

## Decision Log
- **2026-02-03**: Initial draft created to fill fleet management gap identified in domain coverage analysis.
