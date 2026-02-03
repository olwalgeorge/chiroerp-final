# ADR-014: Authorization Objects & Segregation of Duties

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Security Team, Compliance Team  
**Tier**: Core  
**Tags**: security, authorization, rbac, abac, sod, compliance  

## Context
Role-based access control (RBAC) alone is insufficient for SAP-grade enterprise security. ERP systems require fine-grained authorization objects (object-level permissions) and segregation of duties (SoD) to prevent fraud and meet SOX/GDPR requirements. This ADR extends the JWT + RBAC baseline with authorization objects, SoD conflict detection, and policy governance.

## Decision
Adopt **Authorization Objects** with **Attribute-Based Access Control (ABAC)** and implement **Segregation of Duties (SoD)** enforcement across sensitive workflows. Implementation is **not started**; this ADR defines the standard.

### Feature Tiering (Core vs Advanced)
**Core**
- Basic RBAC roles, maker-checker approval, and audit logging.
- Baseline SoD rules for high-risk financial actions.

**Advanced**
- Full ABAC constraints, dynamic authorization, SoD matrices, and break-glass access.
- Access review automation, compensating controls, and compliance reporting.

### Authorization Objects
- Define domain-specific **authorization objects** (e.g., `FI_POSTING`, `FI_APPROVAL`, `AP_INVOICE`, `AR_COLLECTION`).
- Each object has **fields** (attributes) that constrain scope (e.g., company code, cost center, business unit, region, amount range).
- Policies are evaluated as `(object, action, attributes)` at runtime.

### Authorization Object Structure

### Example: FI_POSTING (Financial Posting)
```
Object: FI_POSTING
Actions: CREATE, MODIFY, REVERSE, DISPLAY
Fields:
  - BUKRS (Company Code): 1000, 2000, 3000, *
  - COST_CENTER: 100-*, 200-299, *
  - AMOUNT_RANGE: 0-1000, 1000-10000, 10000+, *
  - PERIOD_STATUS: OPEN, CLOSED
```

### Example: AP_PAYMENT (Accounts Payable Payment)
```
Object: AP_PAYMENT
Actions: CREATE, APPROVE, EXECUTE, CANCEL
Fields:
  - VENDOR_GROUP: DOMESTIC, FOREIGN, *
  - PAYMENT_METHOD: CHECK, WIRE, ACH, *
  - AMOUNT_RANGE: 0-5000, 5000-50000, 50000+, *
  - CURRENCY: USD, EUR, GBP, *
```

### Example: AR_INVOICE (Accounts Receivable Invoice)
```
Object: AR_INVOICE
Actions: CREATE, MODIFY, VOID, WRITE_OFF, DISPLAY
Fields:
  - CUSTOMER_GROUP: RETAIL, WHOLESALE, GOVERNMENT, *
  - INVOICE_TYPE: STANDARD, CREDIT_MEMO, DEBIT_MEMO, *
  - AMOUNT_RANGE: 0-1000, 1000-50000, 50000+, *
  - AGING_BUCKET: CURRENT, 30-60, 60-90, 90+
```

### Example: USER_ADMIN (User Administration)
```
Object: USER_ADMIN
Actions: CREATE, MODIFY, DELETE, UNLOCK, RESET_PASSWORD
Fields:
  - TENANT_ID: <tenant-specific>, SYSTEM (for system admins)
  - ROLE_LEVEL: BASIC, ELEVATED, ADMIN
```

### Example: INVENTORY_ADJ (Inventory Adjustment)
```
Object: INVENTORY_ADJ
Actions: CREATE, APPROVE, REVERSE
Fields:
  - WAREHOUSE: WH01, WH02, REGION-*, *
  - ADJUSTMENT_TYPE: PHYSICAL_COUNT, DAMAGE, OBSOLETE, THEFT
  - VALUE_RANGE: 0-500, 500-5000, 5000+, *
```

### Access Control Model
- **RBAC** for coarse-grained permissions (role -> authorization objects).
- **ABAC** for fine-grained constraints (attributes on user, tenant, org, and object instance).
- Default deny; explicit allow with least-privilege.

### Segregation of Duties (SoD)
- Define **SoD rules** prohibiting conflicting role combinations (e.g., create vendor + approve payment).
- Enforce **maker/checker** on sensitive actions (posting, approvals, reversals).
- Detect SoD conflicts at role assignment time and at transaction time.

### Standard SoD Rules

### High-Risk Conflicts (Auto-Block)

| Rule ID | Conflict Description | Risk | Example Scenario |
|---------|---------------------|------|------------------|
| SOD-001 | Create Vendor + Approve Payment | Fraud: fake vendors | User creates vendor "ABC Corp" and approves payment to same |
| SOD-002 | Post Journal Entry + Approve Journal Entry | Fraud: unauthorized GL postings | User posts $1M debit and self-approves |
| SOD-003 | Create User + Assign Admin Role | Privilege escalation | User creates account and grants self admin |
| SOD-004 | Modify Price + Approve Sales Order | Revenue manipulation | User lowers price and approves discounted order |
| SOD-005 | Receive Goods + Post Invoice | Collusion risk | User receives inventory and posts invoice without verification |
| SOD-006 | Create Budget + Approve Budget | Budget manipulation | User inflates budget and self-approves |
| SOD-007 | Write-off Invoice + Collect Payment | Theft | User writes off receivable and pockets payment |
| SOD-008 | Modify Bank Account + Approve Wire Transfer | Fraud: unauthorized transfers | User changes bank details and approves transfer to new account |
| SOD-009 | Create Purchase Order + Approve Goods Receipt | Procurement fraud | User orders goods from colluding vendor and approves receipt |
| SOD-010 | Adjust Inventory + Approve Adjustment | Inventory theft | User reduces physical count and self-approves shrinkage |

### Medium-Risk Conflicts (Require Justification + Approval)

| Rule ID | Conflict Description | Mitigation | 
|---------|---------------------|-----------|
| SOD-101 | Create Purchase Req + Approve Purchase Order | Compensating control: manager review |
| SOD-102 | Post Receipt + Release Payment | Compensating control: payment run audit |
| SOD-103 | Modify Customer Master + Create Invoice | Compensating control: quarterly master data review |
| SOD-104 | Create Sales Order + Approve Credit Limit | Compensating control: credit manager spot checks |
| SOD-105 | Enter Timesheet + Approve Payroll | Compensating control: HR validation |

### Financial-Specific SoD Rules

**Period Close Process:**
- SOD-201: User cannot execute close AND approve close
- SOD-202: User cannot post adjusting entries AND execute close
- SOD-203: User cannot reconcile accounts AND approve reconciliation

**Bank Reconciliation:**
- SOD-301: User cannot perform reconciliation AND approve discrepancies
- SOD-302: User cannot import bank statement AND post manual journal entries
- SOD-303: User cannot initiate wire transfer AND approve wire transfer

**Fixed Assets:**
- SOD-401: User cannot create asset AND approve acquisition
- SOD-402: User cannot adjust depreciation AND approve adjustment
- SOD-403: User cannot retire asset AND approve disposal

### Dynamic Authorization Rules

### Status-Based Authorization

Authorization checks that vary based on object lifecycle state:

```kotlin
// Example: Invoice Authorization based on Status
object InvoiceAuthorizationRules {
    fun canModify(invoice: Invoice, user: User): Boolean {
        return when (invoice.status) {
            InvoiceStatus.DRAFT -> 
                user.hasAction("AR_INVOICE", "MODIFY") && 
                user.hasFieldAccess("BUKRS", invoice.companyCode)
            
            InvoiceStatus.PENDING_APPROVAL -> 
                user.hasAction("AR_INVOICE", "APPROVE") &&
                user.id != invoice.createdBy // Maker-checker
            
            InvoiceStatus.APPROVED -> 
                user.hasRole("FINANCE_SUPERVISOR") && // Elevated privilege
                invoice.amount < user.getFieldConstraint("AMOUNT_RANGE")
            
            InvoiceStatus.POSTED, InvoiceStatus.PAID -> 
                false // Immutable
            
            InvoiceStatus.CANCELLED -> 
                false // Immutable
        }
    }
    
    fun canReverse(invoice: Invoice, user: User): Boolean {
        return invoice.status == InvoiceStatus.POSTED &&
               user.hasAction("AR_INVOICE", "REVERSE") &&
               invoice.postingDate.isInOpenPeriod() &&
               !invoice.hasSubsequentTransactions()
    }
}
```

### Hierarchical Authorization

Authorization that respects organizational structure:

```kotlin
// Example: Approval Authority based on Hierarchy
data class ApprovalAuthority(
    val userId: String,
    val costCenter: String,
    val amountLimit: BigDecimal,
    val subordinateCostCenters: List<String>
)

object HierarchicalAuthorizationRules {
    fun canApprove(requisition: PurchaseRequisition, user: User): Boolean {
        val authority = getUserApprovalAuthority(user.id)
        
        // Direct cost center match
        if (requisition.costCenter == authority.costCenter) {
            return requisition.amount <= authority.amountLimit
        }
        
        // Subordinate cost center match (manager can approve for team)
        if (requisition.costCenter in authority.subordinateCostCenters) {
            return requisition.amount <= authority.amountLimit * 0.5m // 50% for indirect
        }
        
        return false
    }
    
    fun getApprovalChain(amount: BigDecimal, costCenter: String): List<String> {
        // Example: $0-10K: Supervisor, $10K-50K: Manager, $50K+: Director
        return when {
            amount < 10000.bd -> listOf(getCostCenterSupervisor(costCenter))
            amount < 50000.bd -> listOf(
                getCostCenterSupervisor(costCenter),
                getCostCenterManager(costCenter)
            )
            else -> listOf(
                getCostCenterSupervisor(costCenter),
                getCostCenterManager(costCenter),
                getDivisionDirector(costCenter)
            )
        }
    }
}
```

### Time-Based Authorization

Authorization rules that vary by time:

```kotlin
// Example: Period-sensitive Authorization
object TimeBoundAuthorizationRules {
    fun canPostToFiscalPeriod(period: FiscalPeriod, user: User): Boolean {
        val periodStatus = getFiscalPeriodStatus(period)
        
        return when (periodStatus) {
            PeriodStatus.OPEN -> 
                user.hasAction("FI_POSTING", "CREATE")
            
            PeriodStatus.CLOSING -> 
                user.hasRole("PERIOD_CLOSE_TEAM") &&
                user.hasAction("FI_POSTING", "CREATE_ADJUSTING")
            
            PeriodStatus.CLOSED -> 
                user.hasRole("FINANCE_CONTROLLER") &&
                user.hasAction("FI_POSTING", "REOPEN_PERIOD")
            
            PeriodStatus.LOCKED -> 
                false // Hard lock, no one can post
        }
    }
    
    fun canExecuteOutsideBusinessHours(action: String, user: User): Boolean {
        if (isBusinessHours()) return true
        
        // Batch jobs and emergency access only
        return user.hasRole("SYSTEM_ADMIN") || 
               user.hasActiveBreakGlassSession()
    }
    
    fun canAccessDuringMaintenanceWindow(user: User): Boolean {
        if (!isMaintenanceWindow()) return true
        
        // Only infrastructure team during maintenance
        return user.hasRole("INFRASTRUCTURE_TEAM")
    }
}
```

### Data-Driven Authorization

Authorization based on data relationships:

```kotlin
// Example: Territory-based Sales Authorization
object TerritoryAuthorizationRules {
    fun canAccessCustomer(customer: Customer, user: User): Boolean {
        val userTerritories = getUserAssignedTerritories(user.id)
        
        // Direct territory match
        if (customer.territoryId in userTerritories) return true
        
        // Hierarchical territory (e.g., Regional Manager sees all territories)
        val territoryHierarchy = getTerritoryHierarchy(userTerritories)
        if (customer.territoryId in territoryHierarchy) return true
        
        // National account override
        if (customer.isNationalAccount && user.hasRole("NATIONAL_ACCOUNT_MANAGER")) {
            return true
        }
        
        return false
    }
    
    fun canModifyPricing(salesOrder: SalesOrder, user: User): Boolean {
        val discountAmount = salesOrder.listPrice - salesOrder.netPrice
        val discountPercent = (discountAmount / salesOrder.listPrice) * 100
        
        val userDiscountLimit = user.getFieldConstraint("DISCOUNT_PERCENT")
        
        return discountPercent <= userDiscountLimit &&
               canAccessCustomer(salesOrder.customer, user)
    }
}
```

### Performance & Caching Strategy

### Authorization Decision Cache

```kotlin
data class AuthorizationCacheKey(
    val userId: String,
    val tenantId: String,
    val objectType: String,
    val action: String,
    val resourceId: String?
)

object AuthorizationCache {
    private val cache = Caffeine.newBuilder()
        .maximumSize(100_000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build<AuthorizationCacheKey, Boolean>()
    
    fun get(key: AuthorizationCacheKey): Boolean? = cache.getIfPresent(key)
    
    fun put(key: AuthorizationCacheKey, decision: Boolean) {
        cache.put(key, decision)
    }
    
    fun invalidateUser(userId: String) {
        // Invalidate all entries for this user
        cache.asMap().keys.filter { it.userId == userId }.forEach {
            cache.invalidate(it)
        }
    }
    
    fun invalidateTenant(tenantId: String) {
        // Invalidate all entries for this tenant
        cache.asMap().keys.filter { it.tenantId == tenantId }.forEach {
            cache.invalidate(it)
        }
    }
}
```

### Policy Preloading

```kotlin
// Load user's complete authorization profile at login
data class UserAuthorizationProfile(
    val userId: String,
    val tenantId: String,
    val roles: Set<String>,
    val authorizationObjects: Map<String, Set<String>>, // object -> actions
    val fieldConstraints: Map<String, Map<String, Set<String>>>, // object -> field -> values
    val sodViolations: List<SoDConflict>,
    val validUntil: Instant
)

object AuthorizationProfileCache {
    fun loadProfile(userId: String, tenantId: String): UserAuthorizationProfile {
        // Load from database and cache for session duration
        return redisCache.get("auth:profile:$userId:$tenantId") 
            ?: loadFromDatabase(userId, tenantId).also {
                redisCache.setex("auth:profile:$userId:$tenantId", 3600, it)
            }
    }
    
    fun invalidateProfile(userId: String, tenantId: String) {
        redisCache.del("auth:profile:$userId:$tenantId")
    }
}
```

### Bulk Authorization Checks

```kotlin
// Efficient batch evaluation for list queries
object BulkAuthorizationService {
    fun filterAuthorizedResources(
        user: User,
        resources: List<Resource>,
        action: String
    ): List<Resource> {
        val profile = AuthorizationProfileCache.loadProfile(user.id, user.tenantId)
        
        return resources.filter { resource ->
            evaluateAuthorization(profile, resource, action)
        }
    }
    
    private fun evaluateAuthorization(
        profile: UserAuthorizationProfile,
        resource: Resource,
        action: String
    ): Boolean {
        // Fast-path checks without database
        val objectActions = profile.authorizationObjects[resource.objectType] ?: return false
        if (action !in objectActions) return false
        
        // Field constraint checks
        val constraints = profile.fieldConstraints[resource.objectType] ?: return true
        return constraints.all { (field, allowedValues) ->
            val resourceValue = resource.getFieldValue(field)
            resourceValue in allowedValues || "*" in allowedValues
        }
    }
}
```

### Emergency Access (Break-Glass)

### Break-Glass Workflow

```kotlin
data class BreakGlassRequest(
    val requestId: String,
    val userId: String,
    val tenantId: String,
    val reason: String,
    val requestedRole: String,
    val requestedDuration: Duration,
    val businessJustification: String,
    val requestedAt: Instant
)

data class BreakGlassSession(
    val sessionId: String,
    val userId: String,
    val grantedRole: String,
    val approvedBy: String,
    val startTime: Instant,
    val endTime: Instant,
    val reason: String,
    val isActive: Boolean
)

object BreakGlassService {
    fun requestEmergencyAccess(request: BreakGlassRequest): String {
        // Log request
        auditLog.log(AuditEvent.BREAK_GLASS_REQUESTED, request)
        
        // Notify approvers (security team, compliance)
        notifyApprovers(request)
        
        // Return request ID for tracking
        return request.requestId
    }
    
    fun approveEmergencyAccess(
        requestId: String,
        approverId: String,
        approvedDuration: Duration
    ): BreakGlassSession {
        val request = getRequest(requestId)
        
        // Verify approver authority
        require(hasApprovalAuthority(approverId)) {
            "User $approverId not authorized to approve break-glass"
        }
        
        // Create session
        val session = BreakGlassSession(
            sessionId = UUID.randomUUID().toString(),
            userId = request.userId,
            grantedRole = request.requestedRole,
            approvedBy = approverId,
            startTime = Instant.now(),
            endTime = Instant.now().plus(approvedDuration),
            reason = request.reason,
            isActive = true
        )
        
        // Grant temporary elevated permissions
        grantTemporaryRole(session)
        
        // Audit log
        auditLog.log(AuditEvent.BREAK_GLASS_APPROVED, session)
        
        // Enhanced monitoring
        enableEnhancedMonitoring(session)
        
        return session
    }
    
    fun endSession(sessionId: String) {
        val session = getSession(sessionId)
        
        // Revoke temporary permissions
        revokeTemporaryRole(session)
        
        // Generate activity report
        val activityReport = generateActivityReport(session)
        
        // Audit log
        auditLog.log(AuditEvent.BREAK_GLASS_ENDED, session, activityReport)
        
        // Notify stakeholders
        notifySessionEnded(session, activityReport)
    }
}
```

### Monitoring & Alerting

```kotlin
object BreakGlassMonitoring {
    fun enableEnhancedMonitoring(session: BreakGlassSession) {
        // Real-time activity logging
        activityMonitor.startSession(session.sessionId) {
            logEveryAction = true
            captureScreenshots = true // For compliance
            requireSecondaryApproval = session.grantedRole == "SYSTEM_ADMIN"
        }
        
        // Alert on suspicious activity
        securityMonitor.watchForAnomalies(session.sessionId) {
            alertOn { activity ->
                activity.isDataExport() ||
                activity.isPrivilegedOperation() ||
                activity.isOutsideNormalPattern(session.userId)
            }
        }
    }
    
    fun generateActivityReport(session: BreakGlassSession): BreakGlassReport {
        return BreakGlassReport(
            sessionId = session.sessionId,
            actionsPerformed = getActions(session.sessionId),
            dataAccessed = getDataAccess(session.sessionId),
            changesM = getChanges(session.sessionId),
            anomalyFlags = getAnomalies(session.sessionId)
        )
    }
}
```

### Authorization Testing Strategy

### Unit Tests for Authorization Logic

```kotlin
class AuthorizationServiceTest {
    @Test
    fun `should allow finance user to create invoice within amount limit`() {
        // Given
        val user = createUser {
            role = "ACCOUNTANT"
            authObject("AR_INVOICE", "CREATE")
            fieldConstraint("AMOUNT_RANGE", setOf("0-1000", "1000-10000"))
            fieldConstraint("BUKRS", setOf("1000", "2000"))
        }
        
        val invoice = Invoice(
            amount = 5000.bd,
            companyCode = "1000"
        )
        
        // When
        val canCreate = authService.canCreate(user, invoice)
        
        // Then
        assertTrue(canCreate)
    }
    
    @Test
    fun `should deny finance user creating invoice exceeding amount limit`() {
        // Given
        val user = createUser {
            role = "ACCOUNTANT"
            authObject("AR_INVOICE", "CREATE")
            fieldConstraint("AMOUNT_RANGE", setOf("0-1000", "1000-10000"))
        }
        
        val invoice = Invoice(amount = 50000.bd, companyCode = "1000")
        
        // When
        val canCreate = authService.canCreate(user, invoice)
        
        // Then
        assertFalse(canCreate)
    }
}
```

### SoD Violation Tests

```kotlin
class SoDEnforcementTest {
    @Test
    fun `should detect high-risk SoD conflict at role assignment`() {
        // Given
        val user = User(id = "user123")
        user.assignRole("VENDOR_CREATOR")
        
        // When & Then
        assertThrows<SoDViolationException> {
            user.assignRole("PAYMENT_APPROVER") // SOD-001 violation
        }
    }
    
    @Test
    fun `should block transaction when user has conflicting roles`() {
        // Given - user with vendor creation permission
        val user = createUser {
            role = "PROCUREMENT_CLERK"
            authObject("VENDOR_MASTER", "CREATE")
        }
        
        val vendor = user.createVendor("ABC Corp")
        
        // When - same user tries to approve payment
        val payment = Payment(vendor = vendor, amount = 10000.bd)
        
        // Then
        assertThrows<SoDViolationException> {
            paymentService.approve(payment, user) // SOD-001 enforcement
        }
    }
}
```

### Integration Tests

```kotlin
@QuarkusTest
class AuthorizationIntegrationTest {
    @Inject
    lateinit var authService: AuthorizationService
    
    @Test
    fun `should enforce authorization through full stack`() {
        // Given - authenticated request
        val token = generateJWT(userId = "user123", tenantId = "tenant1")
        
        // When - API call to create invoice
        given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "customerId": "CUST001",
                    "amount": 50000,
                    "companyCode": "1000"
                }
            """)
        .`when`()
            .post("/api/invoices")
        .then()
            .statusCode(403) // User lacks authorization for this amount
            .body("error.code", equalTo("AUTHORIZATION_DENIED"))
    }
}
```

### Load Tests for Authorization Performance

```kotlin
@LoadTest
class AuthorizationPerformanceTest {
    @Test
    fun `authorization check should complete under 10ms (p95)`() {
        // Simulate 1000 concurrent authorization checks
        val metrics = loadTest {
            concurrency = 1000
            duration = Duration.ofMinutes(5)
            operation = {
                authService.authorize(
                    user = randomUser(),
                    action = "CREATE",
                    resource = randomInvoice()
                )
            }
        }
        
        // Assert performance SLA
        assertTrue(metrics.p95Latency < Duration.ofMillis(10))
        assertTrue(metrics.cacheHitRate > 0.9) // 90%+ cache hit
    }
}
```

### Audit & Compliance

- Log all authorization decisions (allow/deny) with user, tenant, action, object, attributes.
- Immutable audit trail for SOX/GDPR.
- Periodic review reports for access and SoD conflicts.

## Alternatives Considered
### 1. Pure RBAC (Rejected)
**Approach**: Use only role-based access control with predefined roles (e.g., ACCOUNTANT, MANAGER, ADMIN).

**Pros**:
- Simple to implement and understand
- Low runtime overhead
- Easy role assignment

**Cons**:
- No fine-grained control (can't restrict by company code, cost center, amount)
- Can't enforce attribute-based constraints (e.g., "approve only < $10K")
- No built-in SoD enforcement
- Role explosion problem (need ACCOUNTANT_1000, ACCOUNTANT_2000 for each company code)

**Why Rejected**: Insufficient for SAP-grade ERP requiring object-level and attribute-level authorization.

### 2. Custom Policy Language (Rejected)
**Approach**: Design a proprietary policy definition language (DSL) for authorization rules.

**Pros**:
- Tailored to exact business needs
- Could be more expressive than standard approaches

**Cons**:
- High development and maintenance cost
- Team learning curve for custom DSL
- No ecosystem support or tooling
- Difficult to audit and validate
- Risk of security vulnerabilities in custom parser

**Why Rejected**: Standard ABAC patterns are well-proven; no need to reinvent. Focus resources on business logic.

### 3. External Authorization Service (e.g., OPA, Casbin) (Considered)
**Approach**: Use Open Policy Agent (OPA) or similar external policy engine for all authorization decisions.

**Pros**:
- Battle-tested policy engine
- Rego policy language is expressive
- Good performance with caching
- Decoupled policy management

**Cons**:
- Additional infrastructure dependency
- Network latency for every authorization check
- Complex integration with existing JWT/RBAC
- OPA doesn't natively support SoD conflict detection
- Requires learning Rego language

**Decision**: **Partially adopted** - Use OPA-inspired patterns (policy-as-code, declarative rules) but implement natively in Kotlin with domain-specific optimizations (e.g., SoD matrix evaluation, authorization object caching). May integrate OPA later for advanced use cases.

### 4. Database-Level Access Control (Rejected)
**Approach**: Use PostgreSQL Row-Level Security (RLS) and column-level permissions for authorization.

**Pros**:
- Authorization enforced at database layer (defense in depth)
- Performance optimized by PostgreSQL
- No application-layer bypass possible

**Cons**:
- Limited to SQL operations (can't control API actions like APPROVE, REVERSE)
- Difficult to implement complex SoD rules in SQL
- Policy scattered between application and database
- Hard to audit and report on access patterns
- Doesn't work across services in distributed architecture

**Why Rejected**: Too rigid for complex business workflows. Better as complementary control, not primary authorization mechanism.

### 5. Attribute Stores (e.g., LDAP, AD) (Complementary)
**Approach**: Store user attributes (department, cost center, region) in external directory service.

**Pros**:
- Centralized attribute management
- Integration with corporate directory
- Single source of truth for org structure

**Cons**:
- Additional external dependency
- Sync lag between directory and application
- Limited support for dynamic/contextual attributes (e.g., current transaction amount)

**Decision**: **Complementary approach** - Use LDAP/AD for static user attributes (department, manager), but maintain application-level attributes for dynamic business context (company code, cost center assignments, authorization limits).

## Consequences
### Positive
- SAP-grade security and compliance with fine-grained control
- Prevents fraud through SoD enforcement
- Clear governance and auditability of policy changes

### Negative / Risks
- Increased complexity in policy definitions and evaluation
- Requires strong governance and tooling for role management
- Potential performance overhead without caching

### Neutral
- Some legacy flows may need refactoring to pass object-level attributes

## Compliance
### Policy Management

- Policies stored as versioned, auditable artifacts.
- Approval workflow for policy changes with separation of duties.
- Policy evaluation service with caching and audit logging.

### Enforcement Points

- **API Gateway**: coarse-grained checks (roles/scopes).
- **Service Layer**: authorization object + ABAC evaluation per action.
- **Domain Layer**: critical invariants double-checked for defense in depth.

### Regulatory Requirements

#### SOX (Sarbanes-Oxley Act)
**Requirement**: Segregation of Duties for financial systems to prevent fraud and ensure accurate financial reporting.

**How We Comply**:
- **SoD Matrix**: 10 high-risk conflicts (SOD-001 through SOD-010) automatically blocked at role assignment and transaction time
- **Maker-Checker**: Enforced on sensitive financial actions (journal posting, payment approval, invoice write-off)
- **Audit Trail**: Immutable log of all authorization decisions with user, tenant, action, object, attributes, and decision (allow/deny)
- **Access Reviews**: Periodic review reports generated for auditors showing role assignments, authorization objects, and SoD conflicts
- **Change Control**: All authorization policy changes audited with approval workflow

**Evidence**:
- Authorization audit logs (7-year retention)
- SoD conflict reports (quarterly)
- Access review reports (annual)
- Policy change audit trail

#### GDPR (General Data Protection Regulation)
**Requirement**: Data access must be limited to authorized users based on legitimate need; access must be auditable.

**How We Comply**:
- **Least Privilege**: Default deny with explicit allow; users only get access to data they need for their job function
- **Attribute-Based Control**: Authorization objects constrain access by organizational unit, cost center, region (data minimization)
- **Audit Trail**: Complete audit log of data access with user identity, purpose, and legal basis
- **Right to Access**: Users can query their authorization profile and understand what data they can access
- **Data Residency**: Tenant-specific authorization enforces data residency requirements (see ADR-005)

**Evidence**:
- Authorization audit logs (GDPR Article 30)
- User consent and legal basis records
- Data access reports for data subjects
- Privacy impact assessment (DPIA) documentation

#### HIPAA (Health Insurance Portability and Accountability Act)
**Requirement**: Protected Health Information (PHI) access must be logged and restricted to minimum necessary.

**How We Comply**:
- **Role-Based Minimum Necessary**: Authorization objects define minimum data access for each role (e.g., BILLING can see PHI for billing, not clinical notes)
- **Break-Glass Audit**: Emergency access is logged with justification and reviewed within 24 hours
- **Automatic Logoff**: Session timeout enforced; re-authentication required for sensitive actions
- **Audit Controls**: 164.312(b) - All PHI access logged with user, timestamp, action, and data accessed

**Evidence**:
- PHI access audit logs (6-year retention)
- Break-glass access reports (daily review)
- Authorization policy documentation
- Access control risk assessment

#### PCI-DSS (Payment Card Industry Data Security Standard)
**Requirement**: Restrict access to cardholder data to only those with legitimate business need.

**How We Comply**:
- **Need-to-Know Authorization**: Authorization objects for payment data (e.g., `FI_PAYMENT`) restrict access by role and business function
- **SoD for Payment Processing**: SOD-001 (Create Vendor + Approve Payment) and SOD-008 (Modify Bank + Approve Wire) prevent payment fraud
- **Dual Control**: Sensitive payment actions require two-person approval (maker-checker)
- **Audit Logging**: Requirement 10 - All access to cardholder data logged with user, timestamp, action, and success/failure

**Evidence**:
- Payment data access logs (1-year online, 3-year archive)
- SoD enforcement reports
- Dual control approval logs
- Quarterly access reviews

#### ISO 27001 (Information Security Management)
**Requirement**: Access control policy must ensure users have access only to resources they are authorized for.

**How We Comply**:
- **Access Control Policy (A.9.1)**: Documented authorization object framework with role definitions
- **User Access Management (A.9.2)**: Automated role assignment with SoD conflict detection
- **User Responsibilities (A.9.3)**: Clear segregation of duties enforced by system
- **System and Application Access Control (A.9.4)**: Secure authentication (JWT), session management, and authorization enforcement

**Evidence**:
- Access control policy documentation
- Role assignment procedures
- SoD matrix and conflict reports
- Security audit logs

### Compliance Monitoring

| Control | Frequency | Report | Remediation SLA |
|---------|-----------|--------|-----------------|
| **SoD Conflict Detection** | Real-time | Blocked at assignment | Immediate (automatic) |
| **Access Reviews** | Quarterly | Role assignment report | 30 days |
| **Audit Log Review** | Daily | Authorization violations | 24 hours |
| **Break-Glass Access** | Real-time | Emergency access report | 24 hours (review) |
| **Policy Change Audit** | Real-time | Policy change log | 7 days (approval) |
| **Privileged Access Review** | Monthly | Admin role report | 15 days |

## Implementation Plan
### Migration & Rollout Plan

### Phase 1: Foundation (Months 1-2)
- Define all authorization objects per bounded context
- Create authorization object catalog with actions and fields
- Build policy evaluation engine with basic ABAC support
- Implement caching layer (in-memory + Redis)

**Deliverables:**
- Authorization object dictionary (100+ objects)
- Policy evaluation library
- Cache infrastructure

### Phase 2: SoD Rules (Month 3)
- Define high-risk SoD rules (auto-block)
- Define medium-risk SoD rules (compensating controls)
- Implement SoD conflict detection at role assignment
- Build SoD violation reporting dashboard

**Deliverables:**
- 50+ SoD rules documented and planned
- Conflict detection at assignment-time
- SoD violation report for existing users

### Phase 3: Integration (Months 4-5)
- Integrate authorization checks into all service layers
- Add enforcement points at API Gateway (coarse-grained)
- Add enforcement points at Domain Layer (fine-grained)
- Implement audit logging for all authorization decisions

**Deliverables:**
- Authorization checks in 12 bounded contexts
- Complete audit trail
- Performance benchmarks

### Phase 4: Dynamic Rules (Month 6)
- Implement status-based authorization
- Implement hierarchical authorization
- Implement time-based authorization
- Add break-glass emergency access workflow

**Deliverables:**
- Dynamic rule engine
- Break-glass request/approval workflow
- Enhanced monitoring for elevated access

### Phase 5: Production Rollout (Months 7-8)
- Pilot with 2-3 bounded contexts (Financial Accounting, AP, AR)
- Gradual rollout to remaining contexts
- Monitor performance and cache hit rates
- Conduct compliance audit

**Deliverables:**
- Production-ready authorization across all contexts
- SOX/GDPR compliance certification
- User training materials

### Post-Launch Support
- Continuous monitoring of authorization decisions
- Quarterly access review campaigns
- SoD conflict remediation workflows
- Policy refinement based on business feedback

### Implementation Plan (Not Started)

- Phase 1: Define authorization objects per context and action taxonomy.
- Phase 2: Build policy service + evaluation library with caching.
- Phase 3: Implement SoD conflict rules and assignment-time checks.
- Phase 4: Integrate enforcement across gateway/service/domain layers.
- Phase 5: Add compliance reporting and periodic access review workflows.

## References

### Related ADRs
- ADR-007 (AuthN/AuthZ Strategy), ADR-006 (Governance), ADR-011 (Saga Pattern)

### External References
- SAP analogs: PFCG roles, Authorization Objects, GRC SoD
