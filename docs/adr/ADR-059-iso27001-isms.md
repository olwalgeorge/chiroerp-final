# ADR-059: ISO 27001 Information Security Management System (ISMS)

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Security Team, Executive Team  
**Priority**: P0 (Critical - Required for EU/Global Markets)  
**Tier**: Platform Core  
**Tags**: compliance, security, iso27001, isms, global-markets, enterprise-readiness

---

## Context

**Problem**: **International customers** (EU, APAC, LATAM) and **regulated industries** (healthcare, financial services, government) **require ISO 27001 certification** as a prerequisite for vendor onboarding. Without this certification, ChiroERP cannot:
- Compete in EU public sector tenders (mandatory requirement)
- Pass vendor security assessments from EU enterprises
- Meet regulatory requirements (GDPR, NIS2, DORA)
- Demonstrate systematic approach to information security
- Achieve global ERP credibility (SAP/Oracle/D365 all certified)

**Current State**: ChiroERP has **no formal Information Security Management System (ISMS)**, no documented risk treatment plan, and no independent certification.

**Market Reality**:
- **100% of EU enterprises** >€100M revenue require/prefer ISO 27001
- **90% of global Fortune 500** require ISO 27001 or equivalent
- **Mandatory** for EU public sector contracts (governments, healthcare)
- **Regulatory driver**: GDPR, NIS2 (Network & Information Security), DORA (Digital Operational Resilience Act)

**Competitive Position**:
- SAP: ISO 27001 certified (all cloud products)
- Oracle: ISO 27001 + ISO 27017/27018 (cloud security/privacy)
- Dynamics 365: ISO 27001 + ISO 27017/27018 + ISO 27701 (privacy)
- NetSuite: ISO 27001 certified
- Odoo: ❌ Not certified (open-source model)
- **ChiroERP**: ❌ Not certified (blocking EU enterprise deals)

---

## Decision

Implement a **comprehensive ISO 27001:2022 Information Security Management System (ISMS)** covering all 93 controls across 4 themes (Organizational, People, Physical, Technological) with target certification by **Q4 2026**.

### ISO 27001:2022 Structure

**Annex A Controls**: 93 controls across 4 themes
1. **Organizational Controls** (37 controls): Policies, procedures, governance
2. **People Controls** (8 controls): HR security, training, awareness
3. **Physical Controls** (14 controls): Premises, equipment, environmental
4. **Technological Controls** (34 controls): Network, access, cryptography, development

**ISMS Requirements** (Clauses 4-10):
- Clause 4: Context of the organization
- Clause 5: Leadership
- Clause 6: Planning (risk assessment, risk treatment)
- Clause 7: Support (resources, competence, awareness, documentation)
- Clause 8: Operation (risk treatment implementation)
- Clause 9: Performance evaluation (monitoring, internal audit, management review)
- Clause 10: Improvement (nonconformity, corrective action, continual improvement)

---

## ISMS Scope

### In-Scope Systems & Services

**ChiroERP SaaS Platform**:
- All 92 microservices (12 domains)
- Multi-tenant infrastructure (AWS, Azure hybrid)
- Customer data processing & storage
- Event streaming (Kafka)
- API Gateway & authentication (Keycloak)
- CI/CD pipelines (GitHub Actions, ArgoCD)
- Monitoring & observability (Prometheus, Grafana, ELK)

**Corporate Systems**:
- Corporate network & office infrastructure
- Employee workstations (laptops, desktops)
- Identity & access management (Okta, Azure AD)
- Communication platforms (Slack, email, video conferencing)
- Development tools (GitHub, Jira, Confluence)

**Excluded**:
- Customer-managed infrastructure (self-hosted deployments)
- Third-party SaaS tools (security delegated to vendors with assessments)

---

## ISO 27001:2022 Annex A Control Implementation

### Theme 1: Organizational Controls (37 controls)

#### 5.1: Policies for Information Security
**Control**: Information security policy and topic-specific policies shall be defined, approved by management, published, communicated, and acknowledged.

**ChiroERP Implementation**:
```yaml
Master Policy:
  - Information Security Policy (board-approved)
  - Review frequency: Annual
  - Owner: CISO
  - Approval: Board of Directors

Topic-Specific Policies (26 policies):
  - Access Control Policy
  - Acceptable Use Policy
  - Asset Management Policy
  - Backup Policy
  - Business Continuity Policy
  - Change Management Policy
  - Cryptography Policy
  - Data Classification Policy
  - Data Protection Policy (GDPR)
  - Disaster Recovery Policy
  - Incident Response Policy
  - Mobile Device Policy
  - Network Security Policy
  - Password Policy
  - Physical Security Policy
  - Remote Access Policy
  - Risk Management Policy
  - Secure Development Policy
  - Third-Party Security Policy
  - Vulnerability Management Policy
  - (+ 6 more, see ADR-058 Appendix A)

Communication:
  - Intranet publication (all employees)
  - Onboarding training (new hires)
  - Annual acknowledgment (electronic signature)
  - Policy updates (notification + re-acknowledgment)

Evidence:
  - Policy register (version control, approval records)
  - Acknowledgment records (100% completion)
  - Communication logs (announcements, training)
```

#### 5.2: Information Security Roles & Responsibilities
**Control**: Information security roles and responsibilities shall be defined and allocated.

**ChiroERP Roles**:
```yaml
Executive Level:
  CISO (Chief Information Security Officer):
    - Overall ISMS ownership
    - Security strategy & budget
    - Board reporting
    - Incident escalation (P0/P1)

  DPO (Data Protection Officer):
    - GDPR compliance
    - Privacy impact assessments
    - Data subject requests
    - Regulatory liaison

Management Level:
  Compliance Manager:
    - ISO 27001 ISMS operations
    - Internal audits
    - Certification management
    - Training coordination

  Security Operations Manager:
    - SOC operations (24/7)
    - Incident response coordination
    - Vulnerability management
    - Threat intelligence

  IT Manager:
    - Infrastructure security
    - Access provisioning
    - Patch management
    - Backup/recovery

Operational Level:
  Security Engineers (3 FTE):
    - Security monitoring (SIEM)
    - Threat hunting
    - Security tooling
    - Penetration testing

  Developers (all):
    - Secure coding practices
    - Code review (security focus)
    - Vulnerability remediation

  IT Operations (3 FTE):
    - System hardening
    - Access reviews
    - Change implementation
    - Backup validation

Evidence:
  - Org chart (security team)
  - Role descriptions (responsibilities documented)
  - RACI matrix (major processes)
  - Delegation of authority matrix
```

#### 5.3: Segregation of Duties
**Control**: Conflicting duties and areas of responsibility shall be segregated.

**ChiroERP SOD Matrix**:
```yaml
Critical Segregations:
  1. Development vs Production Access:
     - Developers: Cannot deploy to production
     - Operations: Cannot modify code

  2. Authorization vs Review:
     - Manager: Can approve access requests
     - Manager: Cannot approve own access changes

  3. Initiator vs Approver:
     - Initiator: Can request change
     - Initiator: Cannot approve own change

  4. Security Configuration vs Audit:
     - Security Engineer: Can configure firewalls
     - Security Engineer: Cannot audit own changes

  5. Backup vs Restoration:
     - IT Operations: Can perform backups
     - IT Manager: Must approve production restoration

SOD Controls:
  - RBAC enforcement (Keycloak)
  - Workflow approvals (Jira, ServiceNow)
  - Audit trails (all privileged actions)
  - Quarterly SOD reviews (compliance team)

Evidence:
  - SOD matrix (updated quarterly)
  - SOD violations report (zero tolerance)
  - Access reviews (SOD focus)
  - (Integration with ADR-014: Authorization Objects & SOD)
```

#### 5.7: Threat Intelligence
**Control**: Information relating to information security threats shall be collected and analyzed.

**ChiroERP Threat Intel Program**:
```yaml
Intelligence Sources:
  - Open Source: CISA, US-CERT, NVD, OWASP
  - Commercial: Recorded Future, CrowdStrike Threat Graph
  - Community: ISACs (Information Sharing & Analysis Centers)
  - Vendor: AWS Security Bulletins, PostgreSQL Security Advisories

Threat Analysis:
  - Threat modeling (STRIDE, PASTA) - per domain
  - Attack surface analysis (quarterly)
  - Vulnerability prioritization (CVSS + exploit availability)
  - Threat actor profiling (ransomware, APT, insider)

Operationalization:
  - SIEM correlation rules (IOC integration)
  - Vulnerability scanning (threat-based prioritization)
  - Security advisories (customer notifications)
  - Incident response (threat-informed playbooks)

Evidence:
  - Threat intelligence reports (monthly)
  - Threat models (per domain, updated annually)
  - IOC integration logs (SIEM)
  - Security advisories (published)
```

#### 5.10: Acceptable Use of Information
**Control**: Rules for acceptable use of information shall be established.

**ChiroERP Acceptable Use Policy (AUP)**:
```yaml
Permitted Uses:
  - Business purposes (job responsibilities)
  - Authorized personal use (reasonable, incidental)
  - Training & development

Prohibited Uses:
  - Illegal activities (piracy, hacking, fraud)
  - Unauthorized disclosure (confidential data)
  - Personal gain (side businesses using company resources)
  - Harassment or discrimination
  - Malicious code (malware, ransomware)

Specific Rules:
  - No sharing of credentials (passwords, API keys)
  - No installation of unauthorized software
  - No circumvention of security controls (VPN, firewall)
  - No exfiltration of data (USB, personal cloud)
  - Encryption required (laptops, mobile devices)

Enforcement:
  - Monitoring (DLP, endpoint detection)
  - Investigation (security incidents)
  - Disciplinary action (up to termination)

Evidence:
  - AUP document (employee handbook)
  - Acknowledgment records (onboarding + annual)
  - DLP alerts (policy violations)
  - Investigation reports (violations)
```

#### 5.14: Information Transfer
**Control**: Rules, procedures, or agreements for information transfer shall be established.

**ChiroERP Data Transfer Controls**:
```yaml
Transfer Methods:
  Secure Channels:
    - HTTPS/TLS 1.3 (web traffic)
    - SFTP/SCP (file transfers)
    - S3 with encryption (object storage)
    - VPN (site-to-site, remote access)

  Prohibited Channels:
    - HTTP (unencrypted web)
    - FTP (unencrypted file transfer)
    - Email attachments (PII, credentials)
    - USB drives (unencrypted)
    - Personal cloud storage (Dropbox, Google Drive)

Transfer Agreements:
  - Data Processing Agreements (DPA) - GDPR requirement
  - Business Associate Agreements (BAA) - HIPAA requirement
  - Non-Disclosure Agreements (NDA) - confidential data
  - Standard Contractual Clauses (SCC) - EU-US transfers

Cross-Border Transfers:
  - Data residency requirements (EU data stays in EU)
  - SCCs for EU-US transfers (GDPR Article 46)
  - Transfer Impact Assessments (TIA)

Evidence:
  - Data transfer policy
  - DPA/BAA/NDA templates
  - SCC documentation (EU transfers)
  - TIA reports (high-risk transfers)
```

#### 5.23: Information Security for Cloud Services
**Control**: Processes for acquisition, use, management, and exit from cloud services shall be established.

**ChiroERP Cloud Security**:
```yaml
Cloud Providers:
  Primary: AWS (us-east-1, eu-west-1)
  Secondary: Azure (failover, hybrid workloads)

Due Diligence:
  - Vendor security assessment (SOC 2, ISO 27001)
  - Data residency verification
  - Encryption capabilities (at-rest, in-transit)
  - Access control model (IAM)
  - Incident response SLA

Configuration Management:
  - Infrastructure as Code (Terraform)
  - Security baselines (CIS Benchmarks)
  - Configuration drift detection (Checkov)
  - Security groups (least privilege)

Monitoring:
  - CloudTrail (AWS audit logs)
  - Azure Monitor (Azure audit logs)
  - GuardDuty (threat detection)
  - Security Hub (compliance dashboard)

Exit Strategy:
  - Data portability (export APIs)
  - Multi-cloud architecture (avoid lock-in)
  - Backup retention (30 days post-termination)
  - Data deletion verification

Evidence:
  - Cloud provider security assessments
  - IaC repository (Terraform)
  - Cloud security posture reports
  - Exit plan documentation
```

#### 5.30: ICT Readiness for Business Continuity
**Control**: ICT readiness shall be planned, implemented, maintained, and tested.

**ChiroERP BC/DR Strategy**:
```yaml
Recovery Objectives:
  Tier 1 (Critical - Finance, Auth):
    - RPO: 1 hour (maximum data loss)
    - RTO: 4 hours (maximum downtime)

  Tier 2 (Important - Inventory, Sales):
    - RPO: 4 hours
    - RTO: 24 hours

  Tier 3 (Standard - Reporting, Analytics):
    - RPO: 24 hours
    - RTO: 72 hours

DR Architecture:
  - Multi-region: AWS us-east-1 (primary) + eu-west-1 (DR)
  - Active-active: Authentication, API Gateway
  - Active-passive: Databases (replication), Kafka (mirroring)
  - Backup strategy: Continuous (WAL), hourly snapshots, daily full

Testing:
  - Quarterly: Failover test (planned, off-hours)
  - Annual: Full DR drill (unannounced, business hours)
  - Post-test: After-action review, plan updates

Evidence:
  - BC/DR plan (detailed runbooks)
  - Failover test reports (quarterly)
  - DR drill reports (annual)
  - RTO/RPO achievement metrics
  - (Integration with ADR-017: Performance Standards)
```

#### 5.37: Documented Operating Procedures
**Control**: Operating procedures for information processing facilities shall be documented.

**ChiroERP Operating Procedures**:
```yaml
Critical Procedures (25+ documented):
  Infrastructure:
    - System startup/shutdown
    - Backup procedures (automated + manual)
    - Restoration procedures
    - Failover procedures
    - Patch management

  Security:
    - Access provisioning/deprovisioning
    - Incident response playbooks
    - Vulnerability remediation
    - Security monitoring (SIEM)

  Development:
    - Code deployment (CI/CD)
    - Database migrations
    - Configuration changes
    - Rollback procedures

  Support:
    - Customer onboarding
    - Data migration (customer import)
    - Troubleshooting guides
    - Escalation procedures

Documentation Standards:
  - Format: Markdown (version controlled in Git)
  - Structure: Purpose, Scope, Responsibilities, Steps, Validation
  - Review: Annual (or after incidents)
  - Access: Confluence wiki (role-based access)

Evidence:
  - Procedure repository (Git)
  - Review logs (annual sign-offs)
  - Procedure adherence audits
  - Incident postmortems (procedure gaps)
```

---

### Theme 2: People Controls (8 controls)

#### 6.1: Screening
**Control**: Background verification checks on candidates shall be carried out prior to joining.

**ChiroERP Screening Process**:
```yaml
All Employees:
  - Identity verification (government-issued ID)
  - Employment history (previous 5 years)
  - Education verification (claimed degrees)
  - Reference checks (2 professional references)
  - Criminal background check (national database)

Enhanced Screening (Security, Finance, Exec):
  - Credit check (financial roles)
  - International criminal check (as applicable)
  - Social media screening
  - Continuous monitoring (annual re-checks)

Contractors/Vendors:
  - Background check (if on-site access)
  - Security awareness training
  - NDA execution
  - Escorted access (if no background check)

Evidence:
  - Background check reports (HR confidential files)
  - Reference check forms
  - Screening completion rate (100% employees)
  - Contractor screening logs
```

#### 6.2: Terms & Conditions of Employment
**Control**: Employment agreements shall include responsibilities for information security.

**ChiroERP Employment Terms**:
```yaml
Contractual Clauses:
  - Confidentiality obligations (during + post-employment)
  - Acceptable use policy acknowledgment
  - Security incident reporting duty
  - Return of assets upon termination
  - Non-compete / non-solicitation (as applicable)
  - Intellectual property assignment

Security Responsibilities:
  - Password protection (no sharing)
  - Device security (encryption, screen lock)
  - Data protection (no unauthorized disclosure)
  - Incident reporting (immediate notification)

Consequences:
  - Policy violations (disciplinary action)
  - Data breaches (termination, legal action)
  - Intellectual property theft (legal action)

Evidence:
  - Employment contract template (security clauses)
  - Signed contracts (all employees)
  - Acknowledgment forms (onboarding)
```

#### 6.3: Information Security Awareness, Education, Training
**Control**: Personnel shall receive appropriate awareness, education, and training.

**ChiroERP Security Training Program**:
```yaml
All Employees (Annual):
  - Security awareness training (1 hour, mandatory)
    - Phishing recognition
    - Password security
    - Social engineering
    - Physical security
    - Incident reporting
  - Privacy training (GDPR, CCPA)
  - Acceptable use policy review

Role-Based Training:
  Developers (Quarterly):
    - Secure coding (OWASP Top 10)
    - Dependency management
    - Code review (security focus)

  IT Operations (Quarterly):
    - System hardening
    - Patch management
    - Access control

  Security Team (Monthly):
    - Threat intelligence
    - Incident response
    - Forensics

  Executives (Annual):
    - Risk management
    - Compliance obligations
    - Board reporting

Phishing Simulations:
  - Frequency: Monthly (randomized)
  - Remedial training: Click rate >10%

Evidence:
  - Training completion records (100% compliance)
  - Phishing simulation results
  - Training materials (updated annually)
  - Training feedback surveys
```

#### 6.4: Disciplinary Process
**Control**: A formal disciplinary process shall be in place for information security breaches.

**ChiroERP Disciplinary Framework**:
```yaml
Violation Levels:
  Minor (Inadvertent):
    - Example: Single phishing click, policy misunderstanding
    - Action: Remedial training, written warning

  Moderate (Negligent):
    - Example: Repeated violations, weak passwords
    - Action: Formal warning, performance improvement plan

  Major (Reckless):
    - Example: Sharing credentials, disabling security
    - Action: Suspension, final warning

  Severe (Intentional):
    - Example: Data theft, sabotage, fraud
    - Action: Immediate termination, legal action

Investigation Process:
  1. Incident detection (SIEM, DLP, report)
  2. Preliminary assessment (Security team)
  3. Formal investigation (Security + HR)
  4. Determination (severity, intent)
  5. Disciplinary action (manager + HR)
  6. Closure (documentation, training)

Evidence:
  - Disciplinary policy (employee handbook)
  - Investigation reports (confidential)
  - Disciplinary action records (HR files)
  - Trend analysis (violation types, frequency)
```

---

### Theme 3: Physical Controls (14 controls)

#### 7.1: Physical Security Perimeters
**Control**: Security perimeters shall be defined and used to protect areas containing information.

**ChiroERP Physical Security**:
```yaml
Data Centers:
  - Cloud providers: AWS, Azure (ISO 27001 certified)
  - Physical security: Provider responsibility
  - Attestation: AWS/Azure compliance reports (SOC 2, ISO 27001)

Office Locations:
  HQ (San Francisco):
    - Perimeter: Badge-controlled entry
    - Reception: 24/7 security desk
    - Server room: Separate badge access (IT only)
    - Visitor policy: Sign-in, escort, badge surrender

  Remote Offices (if any):
    - Badge-controlled entry
    - Visitor log (electronic)
    - No on-premise servers (cloud-only)

Evidence:
  - Cloud provider security reports
  - Badge access logs (office entry)
  - Visitor logs (all locations)
  - Physical security policy
```

#### 7.2: Physical Entry
**Control**: Secure areas shall be protected by appropriate entry controls.

**ChiroERP Entry Controls**:
```yaml
Badge System:
  - Employees: Unique badge (RFID)
  - Contractors: Temporary badge (expiration)
  - Visitors: Visitor badge (escort required)
  - Lost badges: Immediate deactivation

Server Room Access:
  - Authorized personnel: IT team (4 people)
  - Access log: Badge swipe (timestamp, user)
  - Two-person rule: Server maintenance (witness)

After-Hours Access:
  - Pre-authorization required (manager approval)
  - Security escort (if requested)
  - Access log review (monthly)

Evidence:
  - Badge access logs (entry/exit timestamps)
  - Server room access logs (all entries)
  - After-hours access approvals
  - Badge inventory (active/deactivated)
```

#### 7.4: Physical Security Monitoring
**Control**: Premises shall be continuously monitored for unauthorized physical access.

**ChiroERP Monitoring**:
```yaml
CCTV Surveillance:
  - Locations: Reception, hallways, server room, parking
  - Retention: 90 days (rolling)
  - Access: Security manager, executives (incident investigation)

Intrusion Detection:
  - Alarm system (after-hours)
  - Glass break sensors (windows)
  - Motion detectors (server room)
  - Security company monitoring (24/7 response)

Evidence:
  - CCTV footage (incident investigation)
  - Alarm event logs
  - Security company reports (false alarms, incidents)
```

#### 7.7: Clear Desk & Clear Screen
**Control**: Clear desk and clear screen rules shall be adopted.

**ChiroERP Clear Desk Policy**:
```yaml
Clear Desk Requirements:
  - No confidential documents left on desks (end of day)
  - Locked drawers/cabinets (confidential storage)
  - Shredding bins (secure disposal)
  - No passwords on sticky notes

Clear Screen Requirements:
  - Screen lock (5 minutes idle)
  - Password-protected (screensaver)
  - Privacy screens (optional, provided)

Enforcement:
  - Quarterly audits (random desk checks)
  - Violations: Remedial training, escalation

Evidence:
  - Clear desk policy (employee handbook)
  - Audit reports (compliance rate)
  - Screen lock configuration (enforced via GPO)
```

#### 7.14: Secure Disposal of Equipment
**Control**: Equipment shall be securely disposed of when no longer required.

**ChiroERP Asset Disposal**:
```yaml
Laptops/Desktops:
  - Data wiping: DoD 5220.22-M (7-pass overwrite)
  - Certificate of destruction (vendor-provided)
  - Asset decommissioning log

Hard Drives/SSDs:
  - Physical destruction (shredding, degaussing)
  - Certified disposal vendor (NAID AAA certified)
  - Certificate of destruction

Mobile Devices:
  - Factory reset + MDM wipe
  - Physical destruction (if sensitive data)

Disposal Methods:
  - Recycling: Certified e-waste vendor
  - Donation: Only after secure wipe + verification
  - Destruction: Hard drives, storage media

Evidence:
  - Asset disposal log (device ID, method, date)
  - Certificates of destruction
  - Vendor certifications (NAID AAA)
```

---

### Theme 4: Technological Controls (34 controls)

#### 8.1: User Endpoint Devices
**Control**: Information on user endpoint devices shall be protected.

**ChiroERP Endpoint Security**:
```yaml
Mandatory Controls:
  - Full disk encryption (BitLocker, FileVault)
  - Endpoint detection & response (EDR) - CrowdStrike, SentinelOne
  - Antivirus (real-time scanning)
  - Firewall (enabled, configured)
  - Auto-updates (OS patches)
  - Screen lock (5 min idle, password-protected)

MDM (Mobile Device Management):
  - Device enrollment (all corporate devices)
  - Remote wipe capability
  - App whitelisting (approved apps only)
  - Encryption enforcement
  - Compliance monitoring (configuration drift)

BYOD (Bring Your Own Device):
  - Prohibited for production access
  - If allowed: MDM enrollment, containerization

Evidence:
  - EDR deployment status (100% compliance)
  - Encryption status reports (BitLocker, FileVault)
  - MDM compliance reports
  - Non-compliant device alerts (remediation tracking)
```

#### 8.2: Privileged Access Rights
**Control**: Allocation and use of privileged access rights shall be restricted and managed.

**Implementation**: See ADR-058 (SOC 2) CC6.3 Privileged Access
- Just-in-time (JIT) access (max 4 hours)
- Break-glass procedures (emergency only)
- Session recording (all privileged sessions)
- Monthly access reviews (privileged accounts)

#### 8.3: Information Access Restriction
**Control**: Access to information and assets shall be restricted in accordance with access control policy.

**Implementation**: See ADR-058 (SOC 2) CC6.1 Logical Access + ADR-007 (AuthN/AuthZ)
- Role-based access control (RBAC)
- Principle of least privilege
- Quarterly access reviews

#### 8.5: Secure Authentication
**Control**: Secure authentication technologies shall be implemented.

**ChiroERP Authentication**:
```yaml
Authentication Methods:
  - Primary: Username + password + MFA (TOTP, SMS, hardware token)
  - Fallback: Security questions (account recovery only)
  - SSO: SAML/OIDC (enterprise customers)

Password Requirements:
  - Length: 12+ characters
  - Complexity: Upper, lower, number, special character
  - History: Cannot reuse last 10 passwords
  - Rotation: 90 days (prompted)
  - Lockout: 5 failed attempts (30 min lockout)

MFA Enforcement:
  - All users: 100% MFA enrollment
  - Privileged users: Hardware token (Yubikey)
  - API access: Service accounts (API keys + IP whitelist)

SSO Integration:
  - Keycloak (OIDC/OAuth2 provider)
  - Support: SAML, OIDC, LDAP
  - Just-in-time provisioning (JIT)

Evidence:
  - Authentication policy
  - MFA enrollment reports (100% compliance)
  - Password policy configuration (Keycloak)
  - SSO integration documentation
```

#### 8.9: Configuration Management
**Control**: Configurations shall be established, documented, implemented, monitored, and reviewed.

**ChiroERP Configuration Management**:
```yaml
Infrastructure as Code (IaC):
  - Tool: Terraform (v1.6+)
  - Repository: GitHub (version-controlled)
  - Review: PR approval (2+ reviewers)
  - Drift detection: Daily scans (Checkov, Terraform Cloud)

Security Baselines:
  - Operating Systems: CIS Benchmarks (Level 1)
  - Databases: PostgreSQL hardening guide
  - Kubernetes: CIS Kubernetes Benchmark
  - Cloud: CIS AWS/Azure Foundations Benchmark

Configuration Standards:
  - Least privilege (IAM roles, security groups)
  - Encryption enforced (at-rest, in-transit)
  - Logging enabled (CloudTrail, Azure Monitor)
  - Public access blocked (S3, databases)

Change Control:
  - All config changes: Git commit + PR + approval
  - Production changes: CAB approval (Change Advisory Board)
  - Rollback: Terraform state versioning

Evidence:
  - IaC repository (Terraform)
  - Configuration drift reports (daily)
  - CAB meeting minutes (production changes)
  - Compliance scan reports (CIS Benchmarks)
```

#### 8.10: Information Deletion
**Control**: Information in systems shall be deleted when no longer required.

**Implementation**: See ADR-015 (Data Lifecycle Management)
```yaml
Retention Policies:
  - Audit logs: 1 year (compliance requirement)
  - Customer data: Per contract (default: duration + 30 days)
  - Backup data: 30 days (rolling)
  - Soft-deleted data: 90 days (recoverable)

Deletion Methods:
  - Logical deletion: Soft delete (flag + retention)
  - Physical deletion: Secure erase (after retention)
  - Cryptographic erasure: Key deletion (encrypted data)

Data Subject Rights (GDPR):
  - Right to erasure ("right to be forgotten")
  - Deletion request: 30-day SLA
  - Verification: Audit trail (deletion confirmation)

Evidence:
  - Data retention policy
  - Deletion logs (automated + manual)
  - Data subject request logs (GDPR)
  - Retention compliance reports
```

#### 8.16: Monitoring Activities
**Control**: Networks, systems, applications shall be monitored for anomalous behavior.

**ChiroERP Monitoring & Alerting**:
```yaml
Monitoring Layers:
  Infrastructure:
    - Metrics: CPU, memory, disk, network (Prometheus)
    - Logs: System logs, application logs (ELK)
    - Availability: Uptime checks (Pingdom)

  Security:
    - SIEM: Wazuh, Splunk (log correlation)
    - IDS/IPS: Suricata (network intrusion)
    - EDR: CrowdStrike (endpoint threats)
    - Threat detection: AWS GuardDuty

  Application:
    - APM: New Relic, Datadog (performance)
    - Error tracking: Sentry (exceptions)
    - Distributed tracing: Jaeger (request flow)

  Behavioral Analytics:
    - User behavior: Anomaly detection (failed logins, unusual access)
    - Entity behavior: Lateral movement detection

Alerting:
  - Critical: PagerDuty (immediate page)
  - High: Slack (security channel)
  - Medium: Email (security team)
  - Low: Dashboard (review daily)

Evidence:
  - Monitoring dashboards (Grafana)
  - SIEM correlation rules
  - Alert history (response times)
  - Anomaly detection reports
```

#### 8.19: Web Filtering
**Control**: Access to external websites shall be managed.

**ChiroERP Web Filtering**:
```yaml
Web Filter Categories:
  Blocked:
    - Malicious sites (phishing, malware)
    - Adult content
    - Illegal content (piracy, gambling)
    - Anonymizers (proxies, VPNs)

  Monitored:
    - Social media (allowed, logged)
    - Webmail (allowed, DLP scanning)
    - Cloud storage (allowed for approved services)

  Allowed:
    - Business applications (SaaS)
    - Development resources (GitHub, Stack Overflow)
    - News and research

Implementation:
  - Office network: Firewall-based filtering (Palo Alto)
  - Remote workers: Endpoint-based filtering (Cisco Umbrella)
  - DNS filtering: Block malicious domains

Evidence:
  - Web filter policy
  - Blocked site logs (category, user)
  - Override requests (approved/denied)
```

#### 8.23: Web-Based Applications
**Control**: Information in web applications shall be protected.

**ChiroERP Web Application Security**:
```yaml
Security Controls:
  - Input validation (all user input)
  - Output encoding (prevent XSS)
  - Parameterized queries (prevent SQL injection)
  - CSRF protection (tokens)
  - Security headers (CSP, HSTS, X-Frame-Options)
  - Rate limiting (API abuse prevention)
  - WAF (Web Application Firewall) - Cloudflare, ModSecurity

OWASP Top 10 Mitigation:
  - A01 Broken Access Control: RBAC, authorization checks
  - A02 Cryptographic Failures: TLS 1.3, AES-256
  - A03 Injection: Prepared statements, input validation
  - A04 Insecure Design: Threat modeling, secure architecture
  - A05 Security Misconfiguration: IaC, drift detection
  - A06 Vulnerable Components: SCA (Snyk), Dependabot
  - A07 Authentication Failures: MFA, password policy
  - A08 Data Integrity Failures: Code signing, checksums
  - A09 Logging Failures: Centralized logging, audit trails
  - A10 SSRF: URL validation, network segmentation

Security Testing:
  - SAST: SonarQube (code analysis)
  - DAST: OWASP ZAP (runtime testing)
  - Penetration testing: Annual (external firm)

Evidence:
  - Secure coding standards (OWASP)
  - SAST/DAST scan reports
  - Penetration test reports
  - WAF logs (blocked attacks)
```

#### 8.28: Secure Coding
**Control**: Secure coding principles shall be applied to software development.

**ChiroERP Secure Development Lifecycle (SDL)**:
```yaml
Design Phase:
  - Threat modeling (STRIDE, per domain)
  - Security requirements (functional + non-functional)
  - Architecture review (security team)

Development Phase:
  - Secure coding standards (OWASP, language-specific)
  - Code review (security checklist)
  - Pair programming (critical components)
  - Static analysis (SonarQube, Checkmarx)

Testing Phase:
  - Security test cases (OWASP ASVS)
  - Dependency scanning (Snyk, Dependabot)
  - Dynamic analysis (OWASP ZAP)
  - Penetration testing (annual, external)

Deployment Phase:
  - Security sign-off (no critical findings)
  - Configuration review (IaC scanning)
  - Secrets management (Vault, not hardcoded)

Post-Deployment:
  - Vulnerability monitoring (Snyk, GitHub alerts)
  - Patch management (critical: 7 days)
  - Bug bounty program (HackerOne)

Evidence:
  - Secure coding training (developers, annual)
  - Threat models (per domain)
  - Code review logs (security findings)
  - SAST/DAST scan reports
  - Penetration test reports
```

#### 8.32: Change Management
**Control**: Changes to information processing facilities shall be subject to change management procedures.

**Implementation**: See ADR-058 (SOC 2) CC5.2 Change Management

#### 8.34: Test Information
**Control**: Test information shall be appropriately selected, protected, and managed.

**ChiroERP Test Data Management**:
```yaml
Test Data Sources:
  Preferred:
    - Synthetic data (generated, realistic but fake)
    - Anonymized data (from production, PII removed)
    - Subset data (non-sensitive records only)

  Prohibited:
    - Production data (with PII, PHI, financial data)
    - Customer data (without explicit consent)

Anonymization Techniques:
  - Masking: Replace with random values
  - Pseudonymization: Consistent replacement (testable)
  - Aggregation: Summary data (no individuals)
  - Data synthesis: AI-generated (GPT, GANs)

Test Environment Security:
  - Segregated network (no production access)
  - Access control (developers only)
  - Expiration: 90 days (refresh required)
  - Destruction: Secure deletion (after testing)

Evidence:
  - Test data policy
  - Anonymization procedures
  - Test data access logs
  - Test data destruction logs
```

---

## ISMS Processes (Clauses 4-10)

### Clause 4: Context of the Organization

#### 4.1: Understanding the Organization & Context
**ChiroERP Context**:
```yaml
Business Context:
  - Industry: Enterprise software (ERP, SaaS)
  - Target markets: SMB, mid-market, enterprise (global)
  - Revenue model: Subscription (SaaS), implementation services

External Issues:
  - Regulatory: GDPR, CCPA, SOX, HIPAA (sector-specific)
  - Competitive: SAP, Oracle, D365 (security as differentiator)
  - Technological: Cloud adoption, AI/ML, zero-trust
  - Threat landscape: Ransomware, supply chain attacks, APTs

Internal Issues:
  - Growth stage: Scale-up (revenue growth, customer acquisition)
  - Resources: Limited security team (CISO + 3 FTE)
  - Technical debt: Legacy components, modernization needed
  - Culture: Engineering-first, security awareness improving
```

#### 4.2: Understanding Needs of Interested Parties
**ChiroERP Stakeholders**:
```yaml
Customers:
  - Needs: Data protection, availability, confidentiality
  - Requirements: SOC 2, ISO 27001, GDPR compliance
  - Communication: Trust Center, security bulletins

Employees:
  - Needs: Secure work environment, clear policies
  - Requirements: Training, resources, support
  - Communication: Security awareness training, intranet

Executives:
  - Needs: Risk visibility, compliance assurance
  - Requirements: Quarterly risk reports, certification status
  - Communication: Executive dashboard, board presentations

Regulators:
  - Needs: Compliance evidence, incident notifications
  - Requirements: GDPR DPO, breach reporting (72 hours)
  - Communication: DPO contact, regulatory filings

Investors:
  - Needs: Risk management, business continuity
  - Requirements: Insurance coverage, incident disclosure
  - Communication: Board reports, due diligence

Partners/Vendors:
  - Needs: Secure integration, data protection
  - Requirements: API security, data processing agreements
  - Communication: Security documentation, assessments
```

#### 4.3: ISMS Scope
See "ISMS Scope" section above (all ChiroERP SaaS platform + corporate systems).

---

### Clause 5: Leadership

#### 5.1: Leadership & Commitment
**Executive Commitment**:
```yaml
CEO Responsibilities:
  - ISMS policy approval
  - Resource allocation (budget, headcount)
  - Board reporting (quarterly)
  - Tone from the top (security culture)

CISO Responsibilities:
  - ISMS implementation & operations
  - Risk management
  - Security strategy & roadmap
  - Incident escalation (P0/P1)

Board Oversight:
  - Audit Committee: InfoSec oversight
  - Quarterly security briefings
  - Risk register review
  - Certification status monitoring
```

#### 5.2: Information Security Policy
See Control 5.1 (Organizational Controls) above.

#### 5.3: Organizational Roles & Responsibilities
See Control 5.2 (Organizational Controls) above.

---

### Clause 6: Planning

#### 6.1: Risk Assessment
**ChiroERP Risk Assessment Process**:
```yaml
Frequency:
  - Comprehensive: Annual
  - Review: Quarterly (risk register update)
  - Ad-hoc: Material changes (new services, incidents)

Methodology:
  - Asset identification (systems, data, processes)
  - Threat identification (STRIDE, threat intel)
  - Vulnerability assessment (scans, pen tests)
  - Impact analysis (confidentiality, integrity, availability)
  - Likelihood estimation (historical, threat intel)
  - Risk calculation: Risk = Likelihood × Impact

Risk Scoring: See ADR-058 CC3.2 (5x5 matrix)

Risk Register:
  - Risk ID, description, owner
  - Asset, threat, vulnerability
  - Existing controls
  - Likelihood, impact, risk level
  - Risk treatment decision
  - Target risk level (after treatment)
  - Review date

Evidence:
  - Annual risk assessment report
  - Quarterly risk register (updated)
  - Risk treatment plans
  - Executive risk acceptance (for accepted risks)
```

#### 6.2: Risk Treatment
**ChiroERP Risk Treatment Options**:
```yaml
Treatment Decisions:
  - Avoid: Eliminate activity (e.g., discontinue high-risk feature)
  - Mitigate: Implement controls (most common)
  - Transfer: Cyber insurance, vendor contracts
  - Accept: No action (low risk only, executive sign-off)

Risk Treatment Plans:
  For each mitigate/transfer decision:
    - Control selection (preventive, detective, corrective)
    - Implementation timeline
    - Responsibility assignment
    - Budget allocation
    - Monitoring & testing plan

Evidence:
  - Risk treatment register
  - Control implementation status (monthly updates)
  - Risk acceptance forms (executive signatures)
  - Cyber insurance policy
```

#### 6.3: ISMS Objectives
**ChiroERP ISMS Objectives (2026-2027)**:
```yaml
Security Objectives:
  1. Zero critical vulnerabilities unpatched >7 days (100% target)
  2. 100% MFA enrollment (all employees, contractors)
  3. Mean time to detect (MTTD) <15 minutes (security incidents)
  4. Mean time to respond (MTTR) <4 hours (P0/P1 incidents)
  5. Security training completion: 100% (annual)

Compliance Objectives:
  6. ISO 27001 certification: Q4 2026
  7. SOC 2 Type II certification: Q4 2026
  8. GDPR compliance: 100% (ongoing)
  9. Zero data breaches (year-over-year)

Availability Objectives:
  10. 99.9% uptime (Enterprise SLA)
  11. RTO: 4 hours (Tier 1 services)
  12. RPO: 1 hour (Tier 1 services)

Measurement:
  - Quarterly: Objectives review (Security Committee)
  - Annual: ISMS management review (executives + board)
```

---

### Clause 7: Support

#### 7.2: Competence
See Control 6.3 (People Controls) - Security awareness, training.

#### 7.3: Awareness
Security awareness program (all employees, annual training, phishing simulations).

#### 7.4: Communication
See ADR-058 CC2.2 (Internal Communication), CC2.3 (External Communication).

#### 7.5: Documented Information
**ChiroERP ISMS Documentation**:
```yaml
Mandatory Documents:
  - ISMS Policy (master document)
  - ISMS Scope Statement
  - Risk Assessment Methodology
  - Risk Treatment Plan
  - Statement of Applicability (SoA) - 93 controls
  - ISMS Objectives

Supporting Documents:
  - 26 topic-specific policies
  - 25+ operating procedures
  - Risk register
  - Control catalog
  - Incident response plan
  - BC/DR plan

Document Management:
  - Repository: Confluence wiki (version-controlled)
  - Approval: CISO (policies), managers (procedures)
  - Review: Annual (or after material changes)
  - Access: Role-based (confidential documents restricted)

Evidence:
  - Document register (all ISMS documents)
  - Version control logs (changes tracked)
  - Approval records (signatures)
  - Review logs (annual sign-offs)
```

---

### Clause 8: Operation

#### 8.1: Operational Planning & Control
- Risk treatment plans execution
- Control implementation (per SoA)
- Operational procedures (documented, followed)

#### 8.2: Information Security Risk Assessment
Annual risk assessment + quarterly reviews.

#### 8.3: Information Security Risk Treatment
Control implementation, monitoring, effectiveness testing.

---

### Clause 9: Performance Evaluation

#### 9.1: Monitoring, Measurement, Analysis, Evaluation
**ChiroERP ISMS Metrics**:
```yaml
Security Metrics (Monthly):
  - Vulnerability metrics: Critical/high open, mean time to patch
  - Incident metrics: MTTD, MTTR, incident count by severity
  - Access metrics: Privileged access usage, access review completion
  - Training metrics: Completion rate, phishing click rate

Compliance Metrics (Quarterly):
  - Control effectiveness: % controls tested, % passing
  - Policy compliance: Acknowledgment rate, violations
  - Audit findings: Open findings, remediation status

Availability Metrics (Real-time):
  - Uptime: % per service tier
  - Performance: Latency, error rate
  - Capacity: CPU, memory, storage utilization

Dashboard:
  - Security dashboard: Grafana (real-time)
  - Executive dashboard: Power BI (weekly refresh)
  - Board dashboard: PDF report (quarterly)
```

#### 9.2: Internal Audit
**ChiroERP Internal Audit Program**:
```yaml
Audit Frequency:
  - Full ISMS audit: Annual
  - Focused audits: Quarterly (specific controls/domains)

Audit Scope:
  - All 93 ISO 27001 controls (annual)
  - High-risk areas (quarterly): Access control, change management, incident response

Auditor Independence:
  - Internal auditor: Compliance Manager (reports to CISO)
  - External auditor: Hired annually (rotation every 3 years)

Audit Process:
  1. Audit planning (scope, criteria, schedule)
  2. Evidence collection (documentation, interviews, testing)
  3. Findings documentation (nonconformities, observations)
  4. Report issuance (audit report, findings summary)
  5. Corrective action (management response, remediation)
  6. Follow-up (verify corrective actions)

Evidence:
  - Audit schedule (annual calendar)
  - Audit reports (findings, recommendations)
  - Corrective action plans (ownership, timelines)
  - Follow-up reports (verification)
```

#### 9.3: Management Review
**ChiroERP ISMS Management Review**:
```yaml
Frequency: Quarterly (minimum annually per ISO 27001)

Attendees:
  - CEO, CTO, CISO
  - Compliance Manager
  - IT Manager
  - Board representative (Audit Committee chair)

Agenda:
  1. Previous review: Action item status
  2. ISMS performance: Metrics, objectives achievement
  3. Risk register: New risks, changes, treatment status
  4. Audit findings: Internal audits, external audits
  5. Incidents: Summary, trends, corrective actions
  6. Changes: Organizational, regulatory, technological
  7. Improvement opportunities: Lessons learned, best practices
  8. Resource needs: Budget, headcount, tooling

Outputs:
  - Management review report
  - Action items (ownership, deadlines)
  - ISMS improvement decisions
  - Resource allocation approvals

Evidence:
  - Meeting minutes (quarterly)
  - Action item tracker
  - Decisions log (changes approved)
```

---

### Clause 10: Improvement

#### 10.1: Nonconformity & Corrective Action
**ChiroERP Corrective Action Process**:
```yaml
Nonconformity Sources:
  - Internal audits (findings)
  - External audits (certification body findings)
  - Incidents (security breaches, outages)
  - Monitoring (control failures, policy violations)

Corrective Action Steps:
  1. Nonconformity identification (detection, documentation)
  2. Root cause analysis (5-Whys, fishbone diagram)
  3. Corrective action plan (address root cause)
  4. Implementation (owner, timeline)
  5. Effectiveness review (verify resolution)
  6. Closure (sign-off)

Tracking:
  - Nonconformity register (centralized)
  - Status: Open, in progress, closed
  - Escalation: Overdue actions (weekly review)

Evidence:
  - Nonconformity register
  - Root cause analysis reports
  - Corrective action plans
  - Effectiveness review reports
```

#### 10.2: Continual Improvement
**ChiroERP Improvement Program**:
```yaml
Improvement Sources:
  - Audit findings (internal, external)
  - Incident postmortems (lessons learned)
  - Metrics analysis (trends, anomalies)
  - Threat intelligence (emerging risks)
  - Industry best practices (benchmarking)

Improvement Process:
  1. Opportunity identification
  2. Feasibility assessment (cost, benefit, risk)
  3. Approval (management review)
  4. Implementation (project plan)
  5. Effectiveness measurement (KPIs)
  6. Standardization (update ISMS documentation)

Examples:
  - Automate access reviews (reduce manual effort)
  - Implement SOAR (Security Orchestration & Automated Response)
  - Upgrade to behavioral analytics (anomaly detection)

Evidence:
  - Improvement register (opportunities, status)
  - Business case documents (cost/benefit)
  - Implementation reports (outcomes)
```

---

## ISO 27001:2022 Certification Process

### Timeline (Q2-Q4 2026)

**Q2 2026: ISMS Implementation (April-June)**
- Week 1-4: ISMS project kick-off, certification body selection
- Week 5-8: Policies & procedures documentation (26 policies)
- Week 9-12: Control implementation (93 controls, prioritize gaps)

**Q3 2026: Internal Audit & Readiness (July-September)**
- Month 1: Internal audit (full ISMS scope)
- Month 2: Gap remediation (audit findings)
- Month 3: Pre-assessment (optional, certification body)

**Q4 2026: Certification Audit (October-December)**
- October: Stage 1 audit (documentation review)
- November: Stage 2 audit (on-site/virtual, control testing)
- December: Certification decision (certificate issuance)

**Deliverable**: ISO 27001:2022 Certificate (December 2026)

---

### Certification Body Selection

**Accredited Certification Bodies** (Top 5):
1. **BSI (British Standards Institution)**: Global leader, premium pricing
2. **DNV (Det Norske Veritas)**: Strong reputation, reasonable pricing
3. **SGS**: Global presence, mid-market friendly
4. **LRQA (Lloyd's Register)**: UK-based, good for EU markets
5. **Schellman**: US-based, SaaS expertise

**Recommendation**: **DNV** or **Schellman** (balance of credibility + cost)

---

### Cost Estimate

| Item | Cost | Timeline |
|------|------|----------|
| **ISMS Implementation** (Compliance Manager time) | Included in ADR-058 | Q2-Q4 2026 |
| **Certification Body** (Stage 1 + Stage 2 audit) | $30K-50K | Q4 2026 |
| **Pre-Assessment** (optional readiness check) | $10K-15K | Q3 2026 |
| **Gap Analysis** (certification body) | Included | Q2 2026 |
| **Surveillance Audit** (annual, post-certification) | $15K-25K/year | Starting 2027 |
| **Re-Certification** (every 3 years) | $30K-50K | 2029 |
| **Total Estimate** | **$40K-65K** (first year) | |
| **Annual Cost** (surveillance) | $15K-25K/year | Starting 2027 |

**Note**: ISO 27001 leverages same controls as SOC 2 (ADR-058), so implementation costs are shared.

---

## ISO 27001 & SOC 2 Synergies

### Control Overlap (80%+ Common Controls)

| ISO 27001 Control | SOC 2 Control | Notes |
|-------------------|---------------|-------|
| 5.1 (Policies) | CC1.1 (Control Environment) | Same policies |
| 5.3 (Segregation of Duties) | CC6.1/6.2 (Access Control) | Same SOD matrix |
| 6.3 (Training) | CC1.4 (Competence) | Same training program |
| 8.1 (Endpoints) | CC6.6 (Physical Access) | Same EDR, encryption |
| 8.2 (Privileged Access) | CC6.3 (Privileged Access) | Same JIT, session recording |
| 8.5 (Authentication) | CC6.1 (Logical Access) | Same MFA, password policy |
| 8.9 (Config Mgmt) | CC8.1 (Change Management) | Same IaC, drift detection |
| 8.16 (Monitoring) | CC7.1 (Detection & Response) | Same SIEM, alerts |
| 8.23 (Web Apps) | CC5.2 (Technology Controls) | Same WAF, OWASP mitigations |
| 8.28 (Secure Coding) | CC5.2 (Technology Controls) | Same SDL, SAST/DAST |

**Benefit**: **Single control implementation satisfies both standards**, reducing cost and operational overhead.

---

## Implementation Plan (Integrated with ADR-058)

### Phase 1: Foundation (Q2 2026) - SHARED WITH SOC 2

**Week 1-4: Organization**
- [x] Compliance Manager hired (ADR-058)
- [ ] ISO 27001 certification body selection (this ADR)
- [ ] ISMS project kick-off (stakeholders, timeline)

**Week 5-8: Policies & Procedures**
- [x] 26 policies documented (ADR-058)
- [ ] ISO 27001-specific additions:
  - Information Security Policy (ISO 27001 format)
  - Statement of Applicability (SoA) - 93 controls
  - ISMS Scope Statement
  - Risk Assessment Methodology

**Week 9-12: Technical Controls**
- [x] MFA, logging, vuln scanning, access reviews (ADR-058)
- [ ] ISO 27001-specific:
  - Control effectiveness testing procedures
  - Internal audit schedule

### Phase 2: ISMS Operations (Q3 2026)

**Month 1: Internal Audit**
- [ ] Full ISMS audit (all 93 controls)
- [ ] Gap identification (vs ISO 27001 requirements)
- [ ] Audit report (findings, recommendations)

**Month 2: Gap Remediation**
- [ ] Corrective action plans (all findings)
- [ ] Control enhancements (medium/high findings)
- [ ] Documentation updates (policies, procedures)

**Month 3: Pre-Assessment (Optional)**
- [ ] Certification body readiness check
- [ ] Documentation review
- [ ] Final gap remediation

### Phase 3: Certification Audit (Q4 2026)

**October: Stage 1 Audit**
- [ ] Documentation review (certification body)
- [ ] ISMS maturity assessment
- [ ] Stage 2 readiness confirmation

**November: Stage 2 Audit**
- [ ] On-site/virtual audit (control testing)
- [ ] Evidence review (logs, reports, records)
- [ ] Management interviews

**December: Certification**
- [ ] Audit report (findings, recommendation)
- [ ] Corrective actions (if any non-conformities)
- [ ] Certificate issuance (ISO 27001:2022)
- [ ] Customer communication (Trust Center update)

---

## Success Criteria

### Technical Compliance
- ✅ All 93 ISO 27001 controls implemented (100%)
- ✅ Internal audit: Zero critical nonconformities
- ✅ Risk register: All high/critical risks treated
- ✅ ISMS documentation: Complete, approved

### Process Compliance
- ✅ Annual risk assessment completed
- ✅ Quarterly management reviews conducted
- ✅ ISMS objectives: ≥80% achievement
- ✅ Training: 100% completion (all employees)

### Audit Outcome
- ✅ ISO 27001:2022 certification (no nonconformities)
- ✅ Zero critical findings (Stage 2 audit)
- ✅ ≤5 minor findings (acceptable)

---

## Alternatives Considered

### 1. SOC 2 Only (No ISO 27001)
**Rejected**: EU market requires ISO 27001; SOC 2 not recognized in Europe.

### 2. ISO 27001:2013 (Not 2022)
**Rejected**: ISO 27001:2013 is obsolete (superseded by 2022 version in October 2022). New certifications must use 2022 standard.

### 3. ISO 27017/27018 (Cloud Security/Privacy)
**Deferred to Phase 2**: ISO 27017 (cloud security) and ISO 27018 (cloud privacy) are valuable but not required for initial market entry. Consider for 2027 (global expansion phase).

---

## Consequences

### Positive ✅
- Unlocks EU enterprise market
- Demonstrates systematic security approach
- Globally recognized (100+ countries)
- Competitive parity with SAP/Oracle/D365
- Regulatory compliance (GDPR, NIS2, DORA)
- Insurance premium reduction (cyber insurance)

### Negative ⚠️
- $40K-65K first-year certification cost
- $15K-25K annual surveillance audit
- Operational overhead (internal audits, management reviews)
- Re-certification every 3 years

### Risks 🚨
- Audit failure (inadequate ISMS implementation)
  - Mitigation: Internal audit first, pre-assessment (optional)
- Timeline delay (resource constraints)
  - Mitigation: Share resources with SOC 2 (ADR-058), same controls
- Certification body issues (slow response, poor auditor)
  - Mitigation: Select reputable body (DNV, Schellman), check references

---

## Compliance Mapping

### Integration with Other ADRs

- **ADR-058**: SOC 2 Type II Compliance (80%+ control overlap)
- **ADR-007**: Authentication & Authorization (MFA, RBAC)
- **ADR-014**: Authorization Objects & SOD (segregation of duties)
- **ADR-015**: Data Lifecycle Management (retention, deletion)
- **ADR-017**: Performance Standards & Monitoring (availability, MTTR/MTTD)
- **ADR-064**: GDPR Compliance (privacy controls, data subject rights)

---

## Appendix A: Statement of Applicability (SoA) Summary

**Total Controls**: 93 (ISO 27001:2022 Annex A)
- **Organizational Controls**: 37 (100% applicable)
- **People Controls**: 8 (100% applicable)
- **Physical Controls**: 14 (12 applicable, 2 not applicable*)
- **Technological Controls**: 34 (100% applicable)

**Not Applicable Controls** (2):
- 7.8 (Equipment Siting & Protection): Cloud-only infrastructure (provider responsibility)
- 7.12 (Cabling Security): No on-premise cabling (cloud-only)

**Note**: Full Statement of Applicability (93 controls with implementation details) available in separate document (ISMS-SoA.xlsx).

---

## Appendix B: ISO 27001 vs SOC 2 Comparison

| Aspect | ISO 27001 | SOC 2 |
|--------|-----------|-------|
| **Origin** | International (ISO/IEC) | US (AICPA) |
| **Market** | Global (EU, APAC, LATAM) | US, Canada |
| **Focus** | ISMS (systematic security management) | Trust Services (security, availability, confidentiality) |
| **Controls** | 93 prescriptive controls | 5 principles, flexible controls |
| **Certification** | Certificate issued (3-year validity) | Report issued (point-in-time or observation period) |
| **Audit** | External (accredited certification body) | External (CPA firm) |
| **Cost** | $40K-65K (certification) | $50K-80K (Type II) |
| **Renewal** | Surveillance (annual) + re-certification (every 3 years) | Annual Type II report |
| **Recognition** | Mandatory (EU public sector) | Preferred (US enterprises) |
| **Overlap** | 80%+ common controls | 80%+ common controls |

**ChiroERP Strategy**: **Pursue both certifications in parallel** (Q2-Q4 2026) to maximize global market access with minimal additional cost (shared controls).

---

## References

### Standards & Frameworks
- ISO/IEC 27001:2022: Information Security Management Systems
- ISO/IEC 27002:2022: Information Security Controls
- NIST Cybersecurity Framework (CSF)
- CIS Controls v8

### Related ADRs
- ADR-058: SOC 2 Type II Compliance Framework
- ADR-007: Authentication & Authorization Strategy
- ADR-014: Authorization Objects & SOD
- ADR-015: Data Lifecycle Management
- ADR-017: Performance Standards & Monitoring
- ADR-064: GDPR Compliance

### External Resources
- ISO 27001 Official: https://www.iso.org/standard/27001
- DNV Certification: https://www.dnv.com/assurance/management-systems/iso-27001/
- Schellman: https://www.schellman.com/certification/iso-27001

---

*Document Owner*: CISO  
*Review Frequency*: Annual (or upon material changes)  
*Next Review*: February 2027 (post-certification)  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q2 2026**
