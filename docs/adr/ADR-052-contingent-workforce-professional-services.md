# ADR-052: Contingent Workforce & Professional Services Management (Add-on)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Procurement Team, HR Team
**Priority**: P2 (Medium-High)
**Tier**: Add-on
**Parent Module**: HR Bounded Context
**Tags**: contingent-workforce, consultants, professional-services, vms, sow, staffing

## Context
While ADR-023 (Procurement) supports basic services procurement and ADR-036 (Project Accounting) tracks project labor, the current architecture lacks specialized capabilities for managing contingent workers, consultants, and professional services firms. Enterprise organizations require Vendor Management System (VMS) capabilities including SOW management, skills-based sourcing, consultant lifecycle tracking, rate card negotiations, compliance management (certifications, background checks, right-to-work), and staffing supplier performance management. This gap creates inefficiencies in sourcing specialized talent, compliance risks, and missed cost optimization opportunities.

## Decision
Implement **two distinct subdomains** under the HR bounded context to address different engagement models:

1. **Contingent Workforce** (Port 9904) - T&M contractors via staffing agencies
2. **Professional Services** (Port 9907) - SOW-based consulting engagements

### Subdomain Architecture

```
hr/
├── hr-shared/                           # ADR-006 COMPLIANT: Identifiers and enums
├── hr-contingent-workforce/             # Port 9904 - T&M Contractors
│   ├── contingent-domain/
│   ├── contingent-application/
│   └── contingent-infrastructure/
└── hr-professional-services/            # Port 9907 - SOW Management
    ├── professional-services-domain/
    ├── professional-services-application/
    └── professional-services-infrastructure/
```

**Package Structure**: `com.chiroerp.hr.contingent.*` and `com.chiroerp.hr.professionalservices.*`

### Key Distinction Between Subdomains

| Aspect | Contingent Workforce (9904) | Professional Services (9907) |
|--------|----------------------------|------------------------------|
| **Engagement Type** | Time & Materials (T&M) | Fixed Price / Milestone / Retainer |
| **Billing** | Hourly/daily rates | Deliverable-based |
| **Supplier** | Staffing agencies | Consulting firms |
| **Worker** | Individual contractor | Engagement team |
| **Approval** | Requisition → Fill | SOW → Deliverable acceptance |
| **Tracking** | Timesheets | Milestones/deliverables |
| **Compliance** | Co-employment, I-9, certs | SLA, IP rights, confidentiality |

### Scope
- Statement of Work (SOW) and Master Service Agreement (MSA) management.
- Consultant/contractor requisition and approval workflow.
- Skills taxonomy and competency-based matching.
- Rate card management by role, skill, geography, and tenure.
- Consultant onboarding, compliance, and offboarding.
- Time and expense approval workflows.
- Staffing supplier (vendor) management and scorecards.
- Co-employment risk mitigation and classification controls.

### Out of Scope (Handled Elsewhere)
- Benefits administration for full-time staff (external HRIS).
- Payroll processing (integrate with ADR-034).
- Advanced workforce planning (separate WFM module).

---

## Contingent Workforce Subdomain (Port 9904)

**Focus**: Time & Materials (T&M) contractors, staffing agencies, VMS workflows.

### Core Capabilities
- **Contractor lifecycle**: Requisition → sourcing → onboarding → assignment → time tracking → offboarding.
- **VMS (Vendor Management System)**: Centralized contractor sourcing and management.
- **Rate card management**: Standard, negotiated, geographic, peak rates.
- **Staffing supplier management**: Agencies, MSPs, freelance platforms.
- **Time & expense tracking**: Weekly timesheets, expense reports, approval workflows.
- **Compliance tracking**: Background checks, certifications, work authorization.
- **Contractor performance**: Periodic reviews, talent pool management, re-engagement.
- **Co-employment risk controls**: Classification, duration limits, supervision guidelines.

#### Contractor Requisition
- **Requisition workflow**: Business unit request → approval → sourcing.
- **Role definition**:
  - Job title and role description.
  - Required skills and certifications.
  - Duration (start date, estimated end date).
  - Work location (onsite, remote, hybrid).
  - Rate budget and approval limits.
- **Approval routing**: Hiring manager → budget approver → HR/procurement.
- **Supplier selection**: Direct hire, staffing agency, MSP, freelance platform.
- **Multi-supplier bidding**: RFP for consultant assignments.

#### Skills Taxonomy & Competency Management
- **Skills catalog**: Hierarchical taxonomy (e.g., Technology → Programming → Java → Spring Boot).
- **Proficiency levels**: Beginner, Intermediate, Advanced, Expert.
- **Certifications**: PMP, CPA, AWS Certified, Six Sigma, etc.
- **Competency profiles**: Role-based skill requirements.
- **Skills matching**: Automated recommendations based on requisition.
- **Skills gap analysis**: Available vs. required skills.

#### Rate Card Management
| Rate Type | Description | Use Case |
|-----------|-------------|----------|
| **Standard Rate** | Published rate card by role/skill | Staffing agency agreements |
| **Negotiated Rate** | Custom rates per engagement | High-volume or strategic suppliers |
| **Blended Rate** | Average rate for mixed teams | Fixed-price SOWs |
| **Geographic Rate** | Location-based adjustments | Offshore/nearshore/onshore |
| **Tenure Rate** | Loyalty discounts for long-term | Extended engagements |
| **Peak Rate** | Premium for urgent/short-notice | Emergency staffing |

- **Rate card versioning**: Effective dates, approval workflow.
- **Rate benchmarking**: Market rate comparison and variance alerts.
- **Markup transparency**: Bill rate vs. pay rate visibility (for staffing agencies).
- **Currency support**: Multi-currency rates with FX handling.

#### Consultant Onboarding & Compliance
- **Onboarding checklist**:
  - Background check verification.
  - Right-to-work documentation (I-9, visa status).
  - Certification validation (professional licenses).
  - Confidentiality and IP assignment agreements.
  - IT access provisioning.
  - Safety training (for onsite roles).
  - Badge/access card issuance.
- **Compliance tracking**:
  - Certification expiration monitoring.
  - Visa/work permit renewal alerts.
  - Annual re-verification workflows.
  - Insurance coverage (liability, workers' comp).
- **Co-employment risk controls**:
  - Independent contractor vs. employee classification.
  - Duration limits and extension approval.
  - Supervision and direction guidelines.
  - Equipment and workspace provisions.

#### Time & Expense Management
- **Timesheet capture**:
  - Weekly/bi-weekly time entry.
  - Project/task code allocation.
  - Overtime and holiday tracking.
  - Approval workflow (manager → project owner → finance).
- **Expense reporting**:
  - Travel, meals, and incidentals.
  - Receipt capture and validation.
  - Per diem and mileage reimbursement.
  - Client-billable vs. non-billable classification.
- **Validation rules**:
  - SOW rate card enforcement.
  - Budget vs. actual variance alerts.
  - Anomaly detection (excessive hours, duplicate entries).
- **Integration**: Feed to Project Accounting (ADR-036) and Payroll (ADR-034).

#### Staffing Supplier Management
- **Supplier types**:
  - Staffing agencies (IT, engineering, healthcare, finance).
  - Consulting firms (strategy, technology, operations).
  - Managed Service Providers (MSPs).
  - Freelance platforms (Upwork, Toptal, Catalant).
  - Direct contractors (1099/sole proprietor).
- **Supplier onboarding**:
  - Business registration and tax ID.
  - Insurance certificates (liability, E&O).
  - Service categories and specializations.
  - Geographic coverage.
  - Preferred supplier agreements.
- **Supplier scorecards**:
  - Time-to-fill (days to present candidates).
  - Submittal-to-hire ratio (quality of candidates).
  - Retention rate (consultant tenure).
  - Performance ratings (manager feedback).
  - Compliance score (document completeness).
  - Invoice accuracy and timeliness.
- **Supplier tiering**: Preferred, approved, restricted, blocked.
- **Spend visibility**: Supplier spend analytics and consolidation opportunities.

#### Consultant Performance & Lifecycle
- **Performance reviews**:
  - Periodic evaluations (quarterly, end-of-assignment).
  - Manager ratings (technical skills, soft skills, deliverables).
  - 360-degree feedback (peers, clients).
  - Performance improvement plans (PIPs).
- **Lifecycle events**:
  - Extension requests with justification.
  - Conversion to full-time employee (FTE).
  - Early termination (performance, budget cuts).
  - End-of-assignment offboarding.
- **Talent pool management**:
  - Past consultant profiles for re-engagement.
  - Skills and performance history.
  - Blacklist/do-not-rehire registry.

#### AI-Powered Talent Matching (Advanced)
- **Machine Learning Models**:
  - **Skills matching algorithm**: NLP-based resume parsing and competency extraction.
  - **Success prediction**: Historical data (past assignments, performance ratings, tenure) to predict consultant-project fit.
  - **Recommendation engine**: Top 5 consultant recommendations per requisition based on:
    - Skills match score (required vs. available skills).
    - Performance history (ratings, successful projects).
    - Availability and location fit.
    - Rate competitiveness vs. budget.
  - **Candidate ranking**: Multi-factor scoring (skills, experience, ratings, cost, availability).

- **Natural Language Processing (NLP)**:
  - **Resume parsing**: Extract skills, certifications, work history from unstructured CVs.
  - **Job description analysis**: Auto-generate skill requirements from SOW descriptions.
  - **Semantic search**: Find consultants by natural language queries ("senior Java developer with AWS and microservices experience").

- **Predictive Analytics** (Phase 2):
  - **Time-to-fill prediction**: Estimate days to fill based on role difficulty, location, rate.
  - **Attrition risk**: Identify consultants likely to end assignments early.
  - **Rate optimization**: Suggest optimal rate based on market data, skills, and urgency.
  - **Supplier performance prediction**: Forecast supplier fill rates and quality scores.
  - **Data Requirements**: Requires 6-12 months of historical requisition, assignment, and performance data.

> **Phased AI Strategy**: Phase 1 (MVP) includes **resume parsing**, **skills matching**, and **bias mitigation** as core VMS differentiators. Phase 2 adds **predictive analytics** (time-to-fill, attrition, rate optimization) after accumulating sufficient historical data. This ensures AI features deliver measurable value from day one (resume parsing saves hours per requisition) while avoiding predictive models trained on insufficient data.

- **Learning & Improvement**:
  - **Feedback loops**: Manager ratings feed back into matching algorithm.
  - **A/B testing**: Test different matching algorithms and measure hire success.
  - **Model retraining**: Quarterly model updates with new performance data.

- **Bias Mitigation**:
  - **Fairness constraints**: Ensure recommendations are free from demographic bias.
  - **Explainability**: Show why each consultant was recommended (feature importance).
  - **Audit trails**: Log all AI decisions for compliance review.

#### Applicant Tracking System (ATS) for Permanent Hires (Integrated)
- **Requisition Management**:
  - **Permanent hire requisitions**: Separate workflow from contingent workforce.
  - **Job posting**: Internal career site, external job boards (LinkedIn, Indeed, Glassdoor).
  - **Requisition approval**: Budget approval, headcount control, hiring manager assignment.

- **Candidate Sourcing**:
  - **Resume upload**: Bulk import from job boards or email.
  - **Employee referrals**: Referral tracking with bonus eligibility.
  - **Talent pool**: Past candidates, silver medalists, passive candidates.
  - **Social recruiting**: LinkedIn integration, Twitter sourcing.

- **Candidate Screening**:
  - **Resume parsing**: Auto-populate candidate profiles (NLP extraction).
  - **Knockout questions**: Automated screening (location, salary expectations, work authorization).
  - **Skills assessment integration**: HackerRank, Codility, TestGorilla.
  - **Video screening**: One-way video interviews (HireVue, Spark Hire).

- **Interview Management**:
  - **Interview scheduling**: Calendar integration (Outlook, Google Calendar).
  - **Interview kits**: Structured interview guides, scorecards.
  - **Panel interviews**: Multi-interviewer coordination and feedback collection.
  - **Interview feedback**: Structured ratings (technical, cultural fit, communication).
  - **Scorecard aggregation**: Weighted average across interviewers.

- **Offer Management**:
  - **Offer letter generation**: Templates by role, level, location.
  - **Compensation approval**: Compensation band validation, executive approval for exceptions.
  - **Offer negotiation**: Counter-offer tracking and approval workflow.
  - **E-signature integration**: DocuSign, Adobe Sign for offer acceptance.
  - **Background check initiation**: Trigger background check on offer acceptance.

- **Onboarding Handoff**:
  - **HR system integration**: Push hired candidate to HRIS (ADR-034).
  - **Onboarding checklist**: I-9, tax forms, benefits enrollment, IT provisioning.
  - **New hire portal**: Pre-boarding tasks (paperwork, training videos).

- **Compliance & Reporting**:
  - **EEO/OFCCP compliance**: Applicant flow logs, adverse impact analysis.
  - **GDPR/CCPA**: Candidate data retention policies, right to erasure.
  - **Audit trails**: All hiring decisions logged with timestamps and users.
  - **Hiring metrics**: Time-to-fill, source effectiveness, offer acceptance rate, diversity metrics.

#### Vendor Collaboration Portal (Supplier Self-Service)
- **Supplier Portal Access**:
  - **Role-based login**: Staffing agency recruiters, consulting firm partners, freelancers.
  - **Multi-tenant isolation**: Each supplier sees only their data.
  - **SSO integration**: SAML, OAuth for enterprise suppliers.

- **Requisition Visibility**:
  - **Open requisitions**: Suppliers see requisitions they're invited to fill.
  - **RFP management**: Download RFP documents, submit proposals, Q&A forums.
  - **Bidding**: Submit rate bids, availability commitments, consultant profiles.
  - **Requisition status**: Real-time updates (under review, filled, cancelled).

- **Candidate Submission**:
  - **Candidate profiles**: Upload resumes, skills, certifications, references.
  - **Resume parsing**: Auto-populate candidate data (AI-assisted).
  - **Video introductions**: Upload candidate intro videos.
  - **Availability calendar**: Mark consultant availability dates.
  - **Submission tracking**: Status updates (submitted, shortlisted, interview, offer, rejected).

- **Timesheet & Invoice Management**:
  - **Timesheet submission**: Suppliers submit consultant timesheets on behalf.
  - **Expense submission**: Upload receipts and expense reports.
  - **Invoice generation**: Auto-generate invoices from approved timesheets.
  - **Invoice status tracking**: Submitted, approved, paid, disputed.
  - **Payment history**: View payment dates and amounts.

- **Performance Dashboards**:
  - **Supplier scorecard**: Real-time view of performance metrics (time-to-fill, quality scores, retention).
  - **Fill rate trends**: Monthly/quarterly trends.
  - **Consultant ratings**: Aggregated performance ratings for submitted consultants.
  - **Compliance status**: Document completeness, expiration alerts.

- **Communication & Collaboration**:
  - **Messaging**: In-app messaging with hiring managers and procurement.
  - **Notifications**: Email/SMS alerts for new requisitions, status changes, invoice approvals.
  - **Document sharing**: SOWs, MSAs, rate cards, compliance docs.
  - **Feedback forms**: Supplier feedback on requisition clarity, process improvements.

- **Self-Service Actions**:
  - **Profile updates**: Update supplier company info, contacts, certifications.
  - **Rate card submissions**: Propose rate updates for annual negotiations.
  - **Consultant roster management**: Add/remove consultants from talent pool.
  - **Compliance document uploads**: Insurance certificates, business licenses, tax forms.

- **Analytics & Reporting**:
  - **Revenue reports**: Supplier's revenue by period, project, client department.
  - **Utilization reports**: Consultant utilization rates (billable hours / available hours).
  - **Pipeline visibility**: Forecasted demand (upcoming requisitions, renewal opportunities).

### Contingent Workforce Data Model
- `ContractorRequisition`, `ContractorAssignment`, `ContractorProfile`.
- `RateCard`, `RateLine`, `RateCardVersion`.
- `Skill`, `SkillCategory`, `Certification`, `CompetencyProfile`.
- `Timesheet`, `TimesheetEntry`, `ExpenseReport`, `ExpenseItem`.
- `StaffingSupplier`, `SupplierScorecard`, `SupplierContract`.
- `ComplianceDocument`, `BackgroundCheck`, `WorkAuthorization`.
- `PerformanceReview`, `FeedbackEntry`, `TalentPool`.
- **ATS Entities**: `JobRequisition`, `Candidate`, `CandidateApplication`, `Interview`, `InterviewFeedback`, `OfferLetter`, `JobPosting`.
- **AI/ML Entities**: `SkillMatchScore`, `CandidateRecommendation`, `PredictionModel`, `ModelTrainingRun`, `BiasAuditLog`.
- **Portal Entities**: `SupplierPortalUser`, `SupplierSession`, `CandidateSubmission`, `PortalNotification`, `SupplierMessage`.

---

## Professional Services Subdomain (Port 9907)

**Focus**: Statement of Work (SOW) based consulting, deliverable-based billing, milestone tracking.

### Core Capabilities
- **SOW lifecycle**: Draft → Approval → Active → Amendment → Closed.
- **MSA tracking**: Master Service Agreements with rate schedules and terms.
- **Deliverable management**: Scope definition, acceptance criteria, sign-off workflows.
- **Milestone billing**: Fixed-fee, milestone-based, and retainer payment models.
- **Multi-tier engagements**: Prime contractor with sub-tier consulting partners.
- **SLA/KPI tracking**: Service level agreements and performance metrics.
- **IP/Confidentiality**: Rights management, NDA tracking, non-compete provisions.

#### SOW Management
- **SOW components**:
  - Scope of work and deliverables definition.
  - Duration, start/end dates, extension options.
  - Resource requirements (roles, skills, quantities).
  - Rate cards and fee structures.
  - Payment terms (milestone, fixed fee, retainer).
  - Service level agreements (SLAs) and KPIs.
  - IP rights, confidentiality, non-compete clauses.
- **Change management**: SOW amendments with approval workflow.
- **Version control**: SOW revision history and audit trail.

#### Master Service Agreement (MSA) Management
- **MSA lifecycle**: Negotiation → Execution → Active → Renewal/Termination.
- **Terms management**:
  - Standard terms and conditions.
  - Rate schedules and annual escalations.
  - Liability caps and indemnification.
  - Termination clauses and notice periods.
- **MSA-SOW hierarchy**: Multiple SOWs under a single MSA.
- **Renewal tracking**: Expiration alerts and renewal workflows.

#### Deliverable & Milestone Tracking
- **Deliverable definition**:
  - Deliverable name and description.
  - Acceptance criteria and quality standards.
  - Due date and dependencies.
  - Assigned consulting team.
- **Milestone states**: Not Started → In Progress → Submitted → Under Review → Accepted → Invoiced.
- **Acceptance workflow**: Client review → feedback → rework (if needed) → sign-off.
- **Milestone billing triggers**: Acceptance triggers invoice generation.

#### Consulting Firm Management
- **Partner types**:
  - Strategy consulting (McKinsey, BCG, Bain).
  - Technology consulting (Accenture, Deloitte, Capgemini).
  - Specialized boutique firms.
  - Independent consultants/advisors.
- **Partner onboarding**:
  - Business registration and tax ID.
  - Insurance certificates (E&O, liability).
  - Service specializations and case studies.
  - Geographic coverage and delivery centers.
- **Partner performance scorecards**:
  - Deliverable quality ratings.
  - On-time delivery percentage.
  - Client satisfaction scores.
  - Budget adherence.

#### Billing & Payment Models
| Model | Description | Use Case |
|-------|-------------|----------|
| **Fixed Fee** | Lump sum for defined scope | Well-defined projects |
| **Milestone** | Payment upon deliverable acceptance | Phased engagements |
| **Retainer** | Monthly/quarterly fixed fee | Ongoing advisory |
| **Capped T&M** | Hourly rates with ceiling | Discovery/scoping phases |
| **Gain Share** | % of savings/revenue generated | Transformation projects |
| **Risk/Reward** | Base + performance bonus | Outcome-based deals |

#### Project Governance
- **Steering committee tracking**: Meeting schedules, decisions, action items.
- **RAID log integration**: Risks, Assumptions, Issues, Dependencies.
- **Status reporting**: Weekly/monthly progress reports.
- **Escalation management**: Issue escalation paths and resolution tracking.
- **Change request management**: Scope changes with cost/schedule impact.

### Professional Services Data Model
- `MasterServiceAgreement`, `MSATerms`, `MSARenewal`.
- `StatementOfWork`, `SOWVersion`, `SOWAmendment`.
- `Deliverable`, `AcceptanceCriteria`, `DeliverableReview`.
- `Milestone`, `MilestoneInvoice`, `MilestonePayment`.
- `ConsultingFirm`, `ConsultingPartner`, `PartnerScorecard`.
- `ProjectGovernance`, `SteeringCommittee`, `ChangeRequest`.
- `SLA`, `KPI`, `PerformanceMetric`.

### Key Workflows
- **SOW Lifecycle**: Draft SOW → approval → consulting firm assignment → execution → milestone tracking → deliverable acceptance → invoicing → close.
- **Deliverable Acceptance**: Submit deliverable → client review → feedback/rework → acceptance sign-off → invoice trigger.
- **MSA Renewal**: Expiration alert → terms review → negotiation → renewal approval → updated MSA.

---

## Cross-Subdomain Workflows

The following workflows span both subdomains or integrate with other HR/Finance modules:

- **Requisition to Assignment (Contingent - Port 9904)**: Create requisition → approval → supplier selection → candidate submission → interview → offer → onboarding.
- **Requisition to Hire (Permanent - Port 9904)**: Post job → source candidates → screen/assess → interview → offer → background check → onboard → HR system handoff.
- **AI Talent Matching (Port 9904)**: Requisition created → AI model scores candidates → top recommendations surfaced → hiring manager reviews → feedback loop to retrain model.
- **Supplier Portal Workflow (Port 9904)**: Supplier logs in → views open requisitions → submits candidates → tracks status → submits timesheets → generates invoices → views payments.
- **Time & Expense (Port 9904)**: Contractor enters time/expenses → manager approval → project owner approval → invoice matching → payment.
- **Compliance (Port 9904)**: Document collection → verification → expiration monitoring → renewal alerts → re-verification.
- **Extension/Conversion (Both)**: Extension request → budget check → approval → SOW amendment (9907) OR contractor extension (9904) → conversion to FTE if applicable.
- **Offboarding (Port 9904)**: End date trigger → access revocation → equipment return → final timesheet → knowledge transfer → exit survey.

### Integration Points
- **Procurement (ADR-023)**: SOW-based PO generation, supplier master integration, invoice matching.
- **Project Accounting (ADR-036)**: Time and cost allocation to WBS, resource planning, project billing.
- **HR Integration (ADR-034)**: Contractor vs. employee classification, payroll for W-2 contractors, benefits eligibility checks.
- **Finance/AP (ADR-009)**: Invoice processing, accruals, cost center allocation, budget controls.
- **Finance/GL (ADR-009)**: Consultant expense GL postings, project cost capitalization.
- **Authorization (ADR-014)**: Role-based access for requisitions, approvals, and sensitive data (SSN, background checks).
- **Master Data Governance (ADR-027)**: Supplier master, skills taxonomy, rate card governance.
- **Analytics (ADR-016)**: Contingent workforce spend analytics, supplier performance dashboards, compliance reporting.

### Non-Functional Constraints
- **Accuracy**: Rate card application 99.9% correct, timesheet to GL reconciliation within 0.01%.
- **Timeliness**: Requisition approval within 48 hours, onboarding within 5 business days.
- **Auditability**: Full trail from requisition to payment, compliance document versioning.
- **Scalability**: Support 10,000+ active contractors per tenant.
- **Data Privacy**: PII protection for SSN, background checks, performance reviews (GDPR, CCPA).
- **Co-employment Risk**: Automated controls to prevent misclassification.

### KPIs and SLOs
| Metric | Target |
|--------|--------|
| Requisition approval time | p95 < 2 business days |
| Time-to-fill (requisition to start) | p95 < 14 days (contingent), < 30 days (permanent) |
| Onboarding completion | p95 < 5 business days |
| Timesheet approval latency | p95 < 3 business days |
| Compliance document completeness | >= 99% |
| Rate card adherence | >= 98% (with approved exceptions) |
| Supplier scorecard accuracy | >= 95% data quality |
| Invoice matching accuracy | >= 99.5% (PO/timesheet/invoice) |
| Co-employment risk incidents | 0 per year |
| **AI matching accuracy** | **>= 80% (top-3 recommendations hired)** |
| **ATS candidate conversion rate** | **>= 25% (application → offer)** |
| **Supplier portal adoption** | **>= 90% of suppliers active monthly** |
| **Resume parsing accuracy** | **>= 95% (key fields extracted correctly)** |
| **Offer acceptance rate** | **>= 85%** |

## Alternatives Considered
- **Use Procurement only**: Rejected (lacks skills matching, compliance, time tracking, ATS workflows).
- **Use Project Accounting only**: Rejected (no requisition workflow, rate cards, supplier scorecards, candidate management).
- **Standalone VMS vendor (Fieldglass, Beeline)**: Rejected (integration overhead, data silos, higher TCO, no permanent hire support).
- **Standalone ATS (Greenhouse, Lever, Workday Recruiting)**: Partially considered (can integrate for permanent hires, but keeping unified talent platform offers better candidate-to-FTE conversion and single skills database).
- **Spreadsheet tracking**: Rejected (no controls, no audit trail, compliance risk, no AI matching).
- **External staffing MSP**: Partially accepted (can integrate as supplier type via vendor portal, but need internal controls).

## Consequences
### Positive
- Centralized contingent workforce visibility and control.
- Reduced maverick spend on contractors and consultants.
- Improved compliance and reduced co-employment risk.
- Better cost management through rate card enforcement and supplier consolidation.

## Nice-to-Have Domains (Covered / Planned)

### Real Estate Management (Covered in ADR-033)
- **Coverage**: Property portfolio, space/occupancy, lease administration (lessor extension).
- **Impact**: Low — niche use case.

### Grants Management (Covered in ADR-050)
- **Coverage**: Grant lifecycle (application → award → compliance), grant accounting, drawdowns, reporting.
- **Impact**: Low — niche (universities, NGOs).

### Subscription Management (Covered via ADR-022 Extension)
- **Coverage**: Subscription lifecycle, usage metering, tiered pricing, proration, dunning, RevRec linkage.
- **Impact**: Medium — important for SaaS businesses.

### Travel & Expense (Covered in ADR-054)
- **Coverage**: Full T&E workflow (booking, receipts OCR, corporate cards, policy enforcement).
- **Impact**: Medium — can integrate with Concur/Expensify if desired.

### Workforce Planning & Scheduling (Covered in ADR-055)
- **Coverage**: Shift planning, labor demand forecasting, compliance engine, time & attendance.
- **Impact**: Low — industry-specific (retail, hospitality).

### Application Lifecycle Management (ALM) (≈70% covered)
- **Coverage**: ADR-021 + ADR-040 cover **physical ALM** (fixed asset lifecycle + maintenance operations).
- **Out of Scope**: **Software ALM** (SDLC, CI/CD, release management) is not part of ERP scope.
- **Impact**: Medium — asset lifecycle is covered; software ALM can be handled by DevOps platforms.
- Faster time-to-fill with skills-based matching.
- Enhanced supplier performance management.
- Audit-ready trails for SOX, labor law, and tax compliance.

### Negative
- Configuration complexity (skills taxonomy, rate cards, approval workflows).
- Change management for hiring managers and procurement.
- Requires strong integration with HR for payroll and classification.
- Data privacy sensitivity (background checks, SSN, performance reviews).

### Neutral
- Module is optional; enabled per tenant based on contingent workforce volume.
- Can coexist with external MSP providers via integration.

## Compliance
- **Labor Law**: Independent contractor vs. employee classification (IRS 20-factor test, AB5 in California).
- **Immigration**: I-9 compliance, visa status tracking, work authorization verification.
- **SOX**: Approval controls for contractor spend, segregation of duties.
- **GDPR/CCPA**: PII protection for personal data, right to erasure, data minimization.
- **FCRA**: Fair Credit Reporting Act compliance for background checks.
- **Equal Employment**: Non-discrimination in contingent workforce management.
- **Data Residency**: Cross-border data handling for global contractors.

## Industry-Specific Extensions

### IT Services & Technology
- **Offshore/nearshore rate differentials**: India, Eastern Europe, Latin America.
- **Technology stack specialization**: Java, .NET, AWS, Salesforce, SAP.
- **Security clearances**: Government contractor tracking.
- **Remote work provisions**: Equipment, connectivity stipends.

### Healthcare
- **Clinical credentials**: MD, RN, NP, PA licenses with state-by-state tracking.
- **Hospital privileges**: Credentialing and privileging workflows.
- **Malpractice insurance**: Coverage verification and renewal.
- **Locum tenens**: Temporary physician staffing with shift scheduling.

### Engineering & Construction
- **Professional Engineer (PE) licenses**: State-specific registrations.
- **Safety certifications**: OSHA 30, confined space, fall protection.
- **Union rates**: Prevailing wage compliance for government projects.
- **Equipment operators**: Crane operator, forklift, CDL certifications.

### Finance & Accounting
- **CPA licenses**: State board verification.
- **Big 4 alumni**: Preferred sourcing from major accounting firms.
- **Sarbanes-Oxley experience**: SOX audit and remediation skills.
- **Regulatory reporting**: SEC, IFRS, tax compliance expertise.

### Legal Services
- **Bar admission**: State bar active status verification.
- **Practice area specialization**: Litigation, M&A, IP, labor law.
- **Conflict checks**: Client conflict of interest screening.
- **Billable hour tracking**: Detailed time entry by matter code.

## Add-on Activation
- **Tenant feature flags**: 
  - `contingent_workforce_enabled` (Port 9904)
  - `professional_services_enabled` (Port 9907)
- **Licensing**: Optional modules, priced per active contractor/SOW or % of spend.
- **Activation prerequisites**: Procurement (ADR-023) and Project Accounting (ADR-036) must be active.

## Implementation Plan

### Contingent Workforce (Port 9904)
- **Phase 1**: Requisition workflow, basic rate cards, supplier onboarding (3 months).
- **Phase 2**: Skills taxonomy, contractor onboarding, compliance tracking (3 months).
- **Phase 3**: Time & expense approval, staffing supplier scorecards (2 months).
- **Phase 4**: AI talent matching, ATS integration, vendor portal (3 months).
- **Phase 5**: Performance reviews, talent pool, advanced analytics (2 months).

### Professional Services (Port 9907)
- **Phase 1**: MSA management, SOW lifecycle, basic deliverable tracking (3 months).
- **Phase 2**: Milestone billing, acceptance workflows, change management (2 months).
- **Phase 3**: Consulting firm scorecards, project governance, SLA/KPI tracking (2 months).
- **Phase 4**: Industry-specific extensions (legal, healthcare, engineering) (2 months per vertical).

**Total Timeline**: 
- Contingent Workforce: 13 months
- Professional Services: 9-11 months
- Parallel development possible with shared team.

## References
### Related ADRs
- ADR-023: Procurement (MM-PUR)
- ADR-036: Project Accounting (PS)
- ADR-034: HR Integration & Payroll Events
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-027: Master Data Governance

### Internal Documentation
- `docs/contingent-workforce/cw_requirements.md`
- `docs/contingent-workforce/vms_integration_guide.md`

### External References
- Gartner Magic Quadrant for Vendor Management Systems (VMS)
- SAP Fieldglass VMS capabilities
- IRS Independent Contractor Classification Guidelines
- Contingent Workforce Management Best Practices (SHRM)
- California AB5 Worker Classification Law

## Success Metrics (12 Months Post-Launch)

### Contingent Workforce (Port 9904)
- **Adoption**: >= 80% of contractor spend managed through system.
- **Cost Savings**: 10-15% reduction in contingent workforce spend through rate optimization.
- **Compliance**: 100% compliant onboarding documentation.
- **Time-to-Fill**: 20% improvement vs. manual processes.
- **Supplier Consolidation**: 30% reduction in staffing supplier count.
- **User Satisfaction**: >= 4.0/5.0 NPS from hiring managers.

### Professional Services (Port 9907)
- **SOW Coverage**: >= 90% of consulting engagements managed through system.
- **Deliverable On-Time Rate**: >= 85% of milestones delivered on schedule.
- **Invoice Accuracy**: >= 99% milestone-to-invoice accuracy.
- **Partner Scorecard Adoption**: 100% of consulting firms with active scorecards.
- **User Satisfaction**: >= 4.0/5.0 NPS from project sponsors.

---

**Notes for Implementers**:

### Contingent Workforce (Port 9904)
1. Start with high-volume use cases (IT contractors, temp staffing).
2. Integrate tightly with Procurement for PO/invoice flows.
3. Ensure HR partnership for co-employment risk mitigation.
4. Build strong supplier onboarding and scorecard capabilities early.
5. Prioritize compliance automation (expiration alerts, classification rules).
6. Design for multi-tenancy: different industries have different requirements.
7. Consider white-label freelance platform integration (Upwork, Fiverr).

### Professional Services (Port 9907)
1. Start with high-value consulting engagements (strategy, technology).
2. Integrate with Project Accounting (ADR-036) for WBS and cost tracking.
3. Build MSA repository before SOW management (MSA is foundation).
4. Implement deliverable acceptance workflows with clear SLAs.
5. Enable consulting firm self-service for status updates and document uploads.
6. Connect milestone acceptance to AP invoice generation.
