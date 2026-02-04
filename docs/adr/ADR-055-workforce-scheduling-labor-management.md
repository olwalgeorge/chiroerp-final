# ADR-055: Workforce Scheduling & Labor Management (WFM)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, HR Team, Operations Team
**Priority**: P3 (Industry-Specific)
**Tier**: Add-on
**Tags**: workforce-management, scheduling, shift-planning, time-tracking, labor-optimization, retail, hospitality, healthcare

## Context
While **ADR-042 (Field Service)** provides technician dispatch scheduling and **ADR-040 (Plant Maintenance)** handles maintenance crew scheduling, neither addresses comprehensive workforce scheduling needs for industries like retail, hospitality, healthcare, manufacturing, and call centers.

These industries require:
- **Shift planning** and rotation management for hourly workers.
- **Labor demand forecasting** based on sales, occupancy, patient volume, or production targets.
- **Compliance enforcement** (break rules, overtime limits, union contracts, labor laws).
- **Employee availability** and shift bidding/swapping.
- **Real-time attendance** tracking (time clocks, mobile check-in, geofencing).
- **Fatigue management** and rest period compliance.
- **Skills-based scheduling** (certifications, training, seniority).

## Decision
Implement a **Workforce Scheduling & Labor Management (WFM)** add-on module that provides shift planning, labor demand forecasting, schedule optimization, time & attendance tracking, and compliance management integrated with HR, Payroll, Controlling, and operational systems (POS, PMS, EHR).

### Scope
- **Shift Templates**: Define shift patterns and rotations.
- **Labor Demand Forecasting**: Predict staffing needs based on business drivers.
- **Schedule Creation**: Auto-generate optimized schedules (manual override).
- **Employee Self-Service**: View schedules, request time off, bid for shifts, swap shifts.
- **Time & Attendance**: Clock in/out, geofencing, biometric integration.
- **Compliance Engine**: Break rules, overtime, rest periods, union contracts.
- **Real-Time Adjustments**: Call-outs, last-minute changes, emergency coverage.
- **Labor Cost Analytics**: Actual vs budgeted labor, productivity tracking.

### Out of Scope (Future/External)
- Payroll calculation (handled by ADR-034).
- Benefits administration (handled by external HRIS).
- Recruiting and applicant tracking (external ATS).
- Advanced AI/ML-driven schedule optimization (phase 2 for large enterprises).
- Deep ML forecasting models (phase 2 after historical data accumulation).

## Core Capabilities

### Shift Templates & Patterns

#### Shift Definition
- **Shift Attributes**:
  - Shift name (Morning, Evening, Night, Weekend).
  - Start time, end time, duration.
  - Paid vs unpaid breaks.
  - Skill requirements (cashier, nurse, forklift operator).
  - Department, location, workstation.

- **Shift Types**:
  - Fixed shifts (same start/end time daily).
  - Rotating shifts (alternating weeks: Mon-Wed morning, Thu-Fri evening).
  - Split shifts (work, break, work again).
  - On-call shifts (standby with call-in).

- **Shift Patterns**:
  - 4x10 (4 days × 10 hours).
  - 3x12 (3 days × 12 hours, common in nursing).
  - Continental (rotating 8-hour shifts: day, swing, night).
  - DuPont (12-hour rotating: 4 on, 2 off, 3 on, 2 off, 2 on, 3 off).
  - Pitman (2-2-3 schedule: 2 days on, 2 off, 3 on, repeat).

#### Schedule Templates
- **Weekly Templates**:
  - Define typical week schedule by department/role.
  - Monday-Friday 9-5, weekends off.
  - 24/7 coverage with rotating crews.

- **Recurring Schedules**:
  - Copy previous week or template week.
  - Seasonal adjustments (holiday staffing, summer peak).

- **Template Customization**:
  - Per location, department, or role.
  - Override for special events (Black Friday, conference, election day).

### Labor Demand Forecasting

#### Demand Drivers
- **Sales-Based** (Retail, Restaurants):
  - Historical sales data (daily, hourly granularity).
  - Forecasted sales (promotions, seasonality, weather).
  - Transactions per labor hour (TPLH) target.

- **Occupancy-Based** (Hotels, Hospitality):
  - Room occupancy percentage.
  - Guest arrivals/departures (front desk staffing).
  - Restaurant covers and banquet events.

- **Volume-Based** (Call Centers, Healthcare):
  - Call volume (AHT × calls = labor hours).
  - Patient census (nurses per X patients).
  - Procedure schedule (OR staffing, radiology).

- **Production-Based** (Manufacturing, Warehouses):
  - Production orders and output targets.
  - Units per labor hour (UPH).
  - Order fulfillment volume (pick, pack, ship).

#### Forecasting Methods
- **Phase 1 (MVP): Historical Averaging + Regression**:
  - Average labor hours for same day of week (last 4, 8, 13 weeks).
  - Adjust for holidays and anomalies.
  - Linear regression (labor hours = f(sales, day, season)).
  - External factors (weather, events, promotions).

- **Phase 2 (Enterprise): AI/ML Forecasting**:
  - Time series forecasting (ARIMA, Prophet) for 1000+ employee operations.
  - Multi-variate models (sales, weather, holidays, local events).
  - Requires 6-12 months of historical data for training.

> **Implementation Note**: Phase 1 uses **deterministic forecasting** (historical averaging, linear regression) which works well for SMBs with <500 employees. Phase 2 adds ML-based forecasting for large-scale operations (1000+ employees) after accumulating sufficient historical data. This positions ChiroERP WFM as **lightweight and integration-friendly** vs. enterprise WFM systems (Kronos, UKG Pro, ADP Workforce Now) which require dedicated implementation teams.

#### Demand-to-Schedule Translation
- **Labor Budget**:
  - Forecasted labor hours by department, shift, day.
  - Labor cost budget (hours × hourly rate).

- **Staffing Requirements**:
  - Convert labor hours to shift counts.
  - Example: 120 labor hours / 8-hour shifts = 15 employees needed.
  - Skill mix requirements (3 cashiers, 2 stockers, 1 supervisor).

### Schedule Creation & Optimization

#### Auto-Scheduling Engine (Phase 2)
- **Optimization Objectives**:
  - **Primary**: Meet labor demand (avoid under/over-staffing).
  - **Secondary**: Minimize labor cost (avoid unnecessary overtime).
  - **Tertiary**: Maximize employee satisfaction (preferences, work-life balance).

- **Constraints**:
  - **Hard Constraints** (must satisfy):
    - Employee availability (time off, blackout dates).
    - Skill requirements (certified nurse for ICU).
    - Legal compliance (max hours, rest periods, breaks).
    - Union rules (seniority, shift bidding).
  - **Soft Constraints** (prefer to satisfy):
    - Employee preferences (preferred shifts, weekends off).
    - Consecutive days off.
    - Balanced workload distribution.

- **Optimization Algorithms**:
  - Integer Linear Programming (ILP).
  - Constraint satisfaction problem (CSP) solvers.
  - Heuristic algorithms (greedy, genetic, simulated annealing).

> **Implementation Note**: Auto-scheduling is **Phase 2** for enterprises with complex scheduling needs (100+ employees, 24/7 operations). Phase 1 provides **manual scheduling with conflict detection** (see below), which is sufficient for most SMBs. This aligns with the lightweight WFM strategy.

#### Manual Schedule Creation (Phase 1 MVP)
- **Drag-and-Drop Interface**:
  - Visual schedule board (rows = employees, columns = days/shifts).
  - Drag employee to shift slot.
  - Color-coded status (scheduled, available, unavailable, conflict).

- **Conflict Detection**:
  - Double-booking (employee scheduled twice at same time).
  - Skill mismatch (unqualified for shift).
  - Compliance violation (no break, too many hours).
  - Under/over-staffing (shift needs 5, only 3 scheduled).

- **Copy & Adjust**:
  - Copy last week's schedule and adjust for time off, demand changes.
  - Bulk actions (shift all shifts forward 1 hour, add extra shift).

#### Schedule Approval
- **Review Workflow**:
  - Scheduler creates draft schedule.
  - Manager reviews for coverage and cost.
  - HR reviews for compliance.
  - Publish schedule to employees.

- **Publication Rules**:
  - Minimum advance notice (e.g., 2 weeks before start).
  - Locked periods (cannot change within X days of shift).
  - Change notifications (SMS, email, app push).

### Employee Self-Service

#### Schedule Viewing
- **Employee Portal**:
  - View personal schedule (web, mobile app).
  - Calendar view (daily, weekly, monthly).
  - Shift details (location, role, break times, co-workers).

- **Notifications**:
  - New schedule published.
  - Schedule changes (shift added, removed, time changed).
  - Reminders (shift starts in 1 hour).

#### Availability Management
- **Recurring Availability**:
  - Standing availability (e.g., never work Sundays).
  - Preferred shifts (mornings preferred, nights avoid).

- **Time Off Requests**:
  - Request vacation, sick leave, personal days.
  - Manager approval workflow.
  - Blackout dates (e.g., no time off during Black Friday week).
  - Accrual balance visibility.

#### Shift Bidding
- **Bid Process**:
  - Open shifts posted for bidding.
  - Employees bid based on seniority or qualifications.
  - Highest seniority (or highest bid) wins shift.
  - Auto-assignment after bidding window closes.

- **Union Compliance**:
  - Seniority-based shift selection (union contracts).
  - Overtime distribution (rotate OT opportunities fairly).

#### Shift Swapping
- **Swap Request**:
  - Employee A requests to swap shift with Employee B.
  - Employee B accepts or declines.
  - Manager approval (optional or auto-approve if qualified).

- **Swap Eligibility**:
  - Both employees qualified for each other's shifts.
  - No compliance violations (max hours, rest periods).
  - No impact to coverage (same skills/headcount).

- **Trade Board**:
  - Post shift to trade board (anyone can pick up).
  - First-come, first-served or bid-based.

### Time & Attendance

#### Clock In/Out
- **Time Clock Methods**:
  - Physical time clock (biometric: fingerprint, face recognition).
  - Mobile app check-in (GPS geofencing validation).
  - Web portal (for remote/office workers).
  - Badge swipe (RFID, magnetic stripe).

- **Punch Types**:
  - Shift start, shift end, break start, break end.
  - Meal periods (paid vs unpaid).
  - Job transfer (switch departments mid-shift).

- **Geofencing**:
  - Validate employee location on mobile clock-in.
  - Allow clock-in only within X meters of work site.
  - Alert for off-site clock attempts (potential fraud).

#### Attendance Tracking
- **Attendance Status**:
  - **Present**: Clocked in on time.
  - **Late**: Clocked in after grace period (e.g., > 5 min late).
  - **Absent**: No-show, no clock-in.
  - **Partial**: Left early or extended break.

- **Exception Handling**:
  - Missed punch (forgot to clock out).
  - Manager correction (manual time entry with approval).
  - Disputed time (employee contests manager edit).

- **Attendance Scoring**:
  - Attendance points system (tardy = 0.5 pts, absent = 1 pt).
  - Cumulative points trigger warnings, disciplinary action.
  - Points reset after clean period (e.g., 90 days).

#### Real-Time Labor Tracking
- **Live Dashboard**:
  - Who's clocked in now (by department, location).
  - Scheduled vs actual headcount.
  - Projected labor cost for day (actuals + forecasted remainder).

- **Alerts**:
  - Under-staffed (fewer employees than scheduled).
  - Over-time approaching (employee nearing 40 hours).
  - Missed break (employee worked > 5 hours without break).

### Compliance Engine

#### Labor Law Compliance
- **Federal/State Laws** (US):
  - **FLSA**: Overtime (> 40 hrs/week = 1.5× pay for non-exempt).
  - **Break Rules**: Meal break after 5 hours (CA), rest breaks every 4 hours.
  - **Minor Labor Laws**: Max hours, prohibited times (school nights).
  - **Predictive Scheduling**: Minimum advance notice, change penalties (SF, NYC, OR).

- **International Regulations**:
  - **EU Working Time Directive**: Max 48 hours/week, 11-hour rest period, 4 weeks paid leave.
  - **Canada**: Overtime rules vary by province.
  - **Australia**: Minimum rest periods, penalty rates for nights/weekends.

#### Union Contract Compliance
- **Collective Bargaining Agreements (CBAs)**:
  - Seniority rules (shift assignments, layoff order).
  - Overtime distribution (fair rotation, voluntary first).
  - Shift differentials (night shift +$2/hr, weekend +$3/hr).
  - Grievance procedures (schedule disputes).

- **Union Reporting**:
  - Seniority roster updates.
  - Overtime distribution reports.
  - Schedule posting compliance.

#### Fatigue Management
- **Rest Period Rules**:
  - Minimum hours off between shifts (e.g., 8-hour rest).
  - Maximum consecutive days (e.g., 6 days on, 1 day off).
  - Maximum hours per week/month.

- **High-Risk Industries** (Transportation, Healthcare, Aviation):
  - Fatigue risk scoring (hours worked, circadian rhythm, sleep patterns).
  - Mandatory rest after long shifts (12+ hours).
  - Fitness-for-duty checks.

#### Compliance Alerts
- **Pre-Schedule Validation**:
  - Block schedule publication if violations detected.
  - Warn if soft violations (e.g., no weekend off in 3 weeks).

- **Real-Time Alerts**:
  - Employee approaching overtime threshold.
  - Break required (worked 5 hours, no break yet).
  - Rest period violation (less than 8 hours since last shift).

- **Audit Reports**:
  - Compliance violations by type, frequency, department.
  - Manager accountability (who approved violating schedule).
  - Corrective actions and remediation.

### Labor Cost Management

#### Labor Budgeting
- **Budget Creation**:
  - Set labor hour budget by department, week, period.
  - Labor cost budget (hours × standard rate + benefits).
  - Allocate to cost centers (ADR-028 integration).

- **Budget Tracking**:
  - Actual labor hours vs budget (daily, weekly, period).
  - Variance analysis (over/under budget).
  - Forecasted full-period labor cost.

#### Cost Allocation
- **Cost Centers**:
  - Allocate labor hours to department, project, customer.
  - Multi-dimensional (location, product, service).

- **Job Costing**:
  - Employee switches jobs mid-shift (transfer punch).
  - Time captured by activity (cashier, stocking, cleaning).
  - Direct vs indirect labor allocation.

#### Productivity Tracking
- **Productivity Metrics**:
  - **Retail**: Sales per labor hour (SPLH).
  - **Hospitality**: Revenue per available room per labor hour.
  - **Manufacturing**: Units per hour (UPH), output per labor dollar.
  - **Call Center**: Calls handled per hour, AHT.
  - **Healthcare**: Patients per nurse, procedures per labor hour.

- **Benchmarking**:
  - Compare productivity across locations, shifts, employees.
  - Identify top/bottom performers.
  - Best practice sharing.

#### Labor Analytics
- **Dashboards**:
  - Scheduled vs actual labor hours (daily, weekly trend).
  - Labor cost as % of revenue.
  - Overtime hours and cost.
  - Attendance and tardiness rates.

- **Reports**:
  - Labor distribution (full-time, part-time, overtime).
  - Shift coverage heatmaps (under/over-staffed hours).
  - Employee utilization (scheduled hours / available hours).
  - Turnover impact on scheduling (open shifts, overtime).

## Data Model (Conceptual)

### Core Entities
- **ShiftTemplate**: shift_id, name, start_time, end_time, duration, break_minutes (paid, unpaid), skill_requirements, department_id.
- **LaborForecast**: forecast_id, department_id, date, day_of_week, forecasted_hours, demand_driver (sales, volume, occupancy), confidence_level.
- **Schedule**: schedule_id, week_start_date, status (draft, published, locked), created_by, published_date.
- **ScheduleAssignment**: assignment_id, schedule_id, employee_id, shift_id, date, start_time, end_time, location_id, role, status (scheduled, confirmed, swapped, cancelled).
- **AvailabilityRule**: rule_id, employee_id, day_of_week, time_range, availability_type (available, preferred, unavailable), recurring.
- **TimeOffRequest**: request_id, employee_id, start_date, end_date, time_off_type (vacation, sick, personal), status (pending, approved, denied), approved_by.
- **TimePunch**: punch_id, employee_id, punch_type (in, out, break_start, break_end), punch_datetime, location_lat_lng, device_id, method (clock, mobile, badge).
- **AttendanceRecord**: attendance_id, employee_id, schedule_assignment_id, scheduled_start, actual_start, scheduled_end, actual_end, status (present, late, absent, partial), tardiness_minutes, attendance_points.
- **ShiftSwapRequest**: swap_id, requester_employee_id, target_employee_id, shift_assignment_id, status (pending, approved, denied), manager_approval_required.
- **ComplianceViolation**: violation_id, employee_id, schedule_assignment_id, violation_type (overtime, no_break, rest_period, minor_hours), violation_datetime, resolution_status.
- **LaborBudget**: budget_id, department_id, cost_center_id, period_start, period_end, budgeted_hours, budgeted_cost, actual_hours, actual_cost.

### Relationships
- Schedule → ScheduleAssignment (one-to-many)
- ScheduleAssignment → Employee (many-to-one)
- ScheduleAssignment → ShiftTemplate (many-to-one)
- Employee → AvailabilityRule (one-to-many)
- Employee → TimeOffRequest (one-to-many)
- Employee → TimePunch (one-to-many)
- ScheduleAssignment → AttendanceRecord (one-to-one)
- ScheduleAssignment → ShiftSwapRequest (one-to-many)

## Key Workflows

### Schedule Creation & Publication
1. **Forecast Demand**: Calculate labor hours needed by department/day.
2. **Generate Schedule**: Auto-scheduler creates draft schedule (or manual creation).
3. **Conflict Resolution**: Fix under/over-staffing, compliance violations.
4. **Approval**: Manager reviews and approves schedule.
5. **Publication**: Publish schedule to employees (2 weeks in advance).
6. **Notifications**: Employees receive schedule notifications (email, SMS, app).

### Shift Swap Workflow
1. **Employee A** requests to swap shift with **Employee B**.
2. **Employee B** reviews and accepts swap.
3. **System** validates: both qualified, no conflicts, no compliance violations.
4. **Manager** approves (if required by policy).
5. **Schedule** updated, both employees notified.

### Time & Attendance Workflow
1. **Employee** clocks in via mobile app (geofence validated).
2. **System** records punch, matches to schedule assignment.
3. **Real-Time Dashboard** updates (headcount, labor cost).
4. **Employee** takes break (punch break start/end).
5. **Employee** clocks out at shift end.
6. **System** calculates total hours, overtime, attendance status.
7. **Payroll Integration**: Export hours to payroll system (ADR-034).

## Integration Points

### HR Integration (ADR-034)
- Employee master data (name, hire date, position, skills, availability).
- Time off balances (vacation, sick leave accruals).
- Payroll integration (export time punches for payroll processing).
- Disciplinary actions (attendance violations).

### Controlling (ADR-028)
- Labor cost allocation to cost centers, projects, internal orders.
- Budget creation and tracking (labor hour budgets).
- Variance analysis (actual vs budgeted labor).

### Project Accounting (ADR-036)
- Project labor tracking (time captured by project/phase).
- Billable vs non-billable hours.
- Customer billing for labor.

### Field Service (ADR-042)
- Technician scheduling and dispatch.
- Skills-based assignment.
- Mobile time capture (start/end service call).

### Plant Maintenance (ADR-040)
- Maintenance crew scheduling.
- Work order labor assignment.
- Skills and certifications tracking.

### Contingent Workforce (ADR-052)
- Temporary worker scheduling.
- Vendor timesheet integration.
- Shift fill-in from staffing agency.

### Workflow Engine (ADR-046)
- Time off request approval workflows.
- Schedule approval workflows.
- Shift swap approvals.

### Analytics (ADR-016)
- Labor productivity dashboards.
- Schedule adherence reporting.
- Turnover and retention analytics.

### Master Data Governance (ADR-027)
- Employee data quality and validation.
- Skills taxonomy and certification tracking.
- Location and department hierarchies.

## Non-Functional Requirements

### Performance
- **Schedule generation**: p95 < 30 seconds for 500 employees, 1-week schedule.
- **Auto-scheduling**: p95 < 5 minutes for large facilities (2,000+ employees).
- **Time clock response**: p95 < 2 seconds from punch to confirmation.
- **Dashboard refresh**: Real-time updates within 30 seconds of punch.

### Scalability
- Support 50K+ employees across multi-site operations.
- Handle 10M+ time punches per month.
- 100K+ schedule assignments per week.

### Reliability
- **System availability**: 99.95% uptime (critical for time clocks).
- **Data accuracy**: 99.99% for time punch capture.
- **Schedule integrity**: No lost or corrupted schedules.

### Security
- Employee PII protection (SSN, address, phone).
- Biometric data encryption (fingerprints, face scans).
- Role-based access control (employees, managers, schedulers, HR, payroll).

### Compliance
- **Data retention**: 7 years for time records (FLSA, tax).
- **Audit trail**: Immutable log of schedule changes, time edits, approvals.
- **Privacy**: Geolocation data use disclosure and consent.

## KPIs and SLOs

### Operational KPIs
| KPI | Target | Measurement |
|-----|--------|-------------|
| **Schedule adherence** | >= 95% | Actual vs scheduled hours match |
| **Labor cost variance** | <= 5% | (Actual - Budget) / Budget |
| **Overtime %** | <= 8% of total hours | OT hours / Total hours |
| **Attendance rate** | >= 97% | Present / Scheduled shifts |
| **On-time rate** | >= 95% | On-time clock-ins / Total shifts |
| **Schedule change rate** | <= 10% | Changes after publication / Total shifts |
| **Compliance violation rate** | <= 1% | Violations / Total shifts |
| **Employee schedule satisfaction** | >= 4.0/5 | Survey score |

### Technical SLOs
| Service | SLO | Measurement |
|---------|-----|-------------|
| **Time clock availability** | 99.95% | Uptime |
| **Punch processing** | p95 < 2s | Punch to confirmation |
| **Schedule generation** | p95 < 30s | Draft schedule creation |
| **Mobile app responsiveness** | p95 < 3s | Screen load time |

## Alternatives Considered

### 1. Extend ADR-042 (Field Service) or ADR-040 (Plant Maintenance)
- **Rejected**: Different use cases (hourly shift workers vs salaried technicians), compliance complexity (break rules, union contracts), employee self-service needs.

### 2. Use Standalone WFM System (Kronos, ADP, WorkForce Software)
- **Rejected**: Weak ERP integration (labor cost allocation, project time tracking). Duplicate employee master data.

### 3. Manual Scheduling (Spreadsheets, Google Sheets)
- **Rejected**: Not scalable, no compliance enforcement, error-prone, no employee self-service.

### 4. Embed in ADR-034 (HR Integration)
- **Rejected**: Workforce scheduling is operationally complex (forecasting, optimization, real-time adjustments) and warrants dedicated module.

## Consequences

### Positive
- Optimizes labor costs (reduce overtime, right-size staffing).
- Improves compliance (automate break rules, rest periods, labor laws).
- Enhances employee experience (self-service, shift preferences, mobile access).
- Provides real-time visibility (who's working, labor cost tracking).
- Integrates with ERP financials (labor cost allocation to cost centers/projects).

### Negative / Risks
- High implementation complexity (optimization algorithms, compliance rules).
- Requires change management (shift from manual to automated scheduling).
- Union pushback (perceived loss of seniority rights, shift control).
- Dependency on time clock hardware and mobile devices.

### Neutral
- Add-on tier (optional for industries requiring shift-based scheduling).
- Can be phased: Basic Scheduling → Time & Attendance → Auto-Optimization → Advanced Analytics.

## Implementation Phases

### Phase 1: Foundation (Core Scheduling)
- Shift templates and manual schedule creation.
- Employee availability and time off requests.
- Schedule publication and notifications.
- Basic time clock integration (in/out punches).

### Phase 2: Compliance & Self-Service (Advanced)
- Compliance engine (break rules, overtime, rest periods).
- Employee self-service portal (schedule viewing, shift swaps).
- Shift bidding and trade boards.
- Geofencing and mobile clock-in.

### Phase 3: Optimization & Forecasting (Advanced)
- Labor demand forecasting (sales, volume-based).
- Auto-scheduling with optimization engine.
- Real-time labor tracking dashboards.
- Alerts and notifications (under-staffed, overtime approaching).

### Phase 4: Analytics & AI (Future)
- Labor productivity analytics and benchmarking.
- Predictive scheduling (AI/ML demand forecasting).
- Fatigue management and wellness tracking.
- Integration with workforce planning (ADR-034 extension).

## References
- **ADR-034**: HR Integration & Payroll Events (employee master, payroll integration).
- **ADR-028**: Controlling (labor cost allocation, budget management).
- **ADR-036**: Project Accounting (project labor tracking, billable hours).
- **ADR-042**: Field Service Operations (technician scheduling, dispatch).
- **ADR-040**: Plant Maintenance (maintenance crew scheduling).
- **ADR-052**: Contingent Workforce (temporary worker scheduling).
- **ADR-046**: Workflow & Approval Engine (time off approvals, schedule approvals).
- **ADR-016**: Analytics & Reporting Architecture (labor dashboards).

## Decision Log
- **2026-02-03**: Initial draft created to fill workforce scheduling gap (50% coverage → 95% coverage) for shift-based hourly workforce industries (retail, hospitality, healthcare, manufacturing).
