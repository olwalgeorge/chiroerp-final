# Certification & Compliance Guide - ChiroERP

**Status**: Draft  
**Priority**: P2 (Medium - Compliance Critical)  
**Last Updated**: February 3, 2026  
**Scope**: Kenya KRA eTIMS, M-Pesa Daraja API, Tanzania TRA VFD, Security Audits

---

## 1. Executive Summary

### 1.1 Purpose
This guide provides step-by-step certification processes for:
1. **KRA eTIMS** (Kenya Revenue Authority Electronic Tax Invoice Management System) - **MANDATORY**
2. **M-Pesa Daraja API** (Safaricom mobile money integration)
3. **TRA VFD/EFD** (Tanzania Revenue Authority Virtual/Electronic Fiscal Device)
4. **Security & Compliance Audits** (ISO 27001, GDPR, PCI-DSS)

### 1.2 Certification Timeline

| Certification | Duration | Cost (USD) | Blocking? | Priority |
|---------------|----------|------------|-----------|----------|
| **KRA eTIMS** | 4-8 weeks | $2,000 | ✅ YES (Cannot invoice without it) | 🔴 P0 |
| **M-Pesa Daraja** | 2-4 weeks | Free (Safaricom) | ⚠️ Partial (80% payments) | 🟡 P1 |
| **TRA VFD** | 6-10 weeks | $1,500 | ✅ YES (Tanzania invoicing) | 🟡 P1 |
| **ISO 27001** | 6-12 months | $15,000 - $50,000 | ❌ NO (Enterprise sales) | 🟢 P2 |
| **PCI-DSS** | 3-6 months | $10,000 - $30,000 | ⚠️ Partial (Card payments) | 🟢 P2 |
| **SOC 2 Type II** | 6-12 months | $20,000 - $100,000 | ❌ NO (US customers) | 🟢 P3 |

**Critical Path**: KRA eTIMS → M-Pesa Daraja → TRA VFD → Go-Live

---

## 2. KRA eTIMS Certification (Kenya) 🇰🇪

### 2.1 Overview

**What is KRA eTIMS?**  
Kenya Revenue Authority's **Electronic Tax Invoice Management System** - mandatory since January 1, 2023 for all VAT-registered businesses.

**Why is it mandatory?**  
- Real-time invoice submission to KRA
- **Cu (Control Unit) number** issued for each invoice (proof of tax compliance)
- Prevents tax evasion (KRA tracks all B2B/B2C transactions)
- Non-compliance penalty: **KES 1,000,000 fine or 5 years imprisonment**

**Key Requirement**:  
Every invoice must have a **Cu number** from KRA before it can be issued to a customer.

---

### 2.2 Certification Process

#### Phase 1: Registration (Week 1-2)

**Step 1: Register on KRA iTax Portal**
1. Go to https://itax.kra.go.ke/
2. Log in with your KRA PIN
3. Navigate to: **eTIMS → Device Registration**
4. Fill application form:
   - Business name
   - KRA PIN
   - Physical address
   - Number of devices required (start with 5-10)
   - Contact person details

**Step 2: KRA Site Inspection (1-2 weeks wait)**
- KRA officer visits your office
- Verifies business operations
- Confirms device requirements
- Issues approval letter

**Documents Required**:
- ✅ Certificate of Registration (CR12)
- ✅ KRA PIN Certificate
- ✅ VAT Registration Certificate
- ✅ Business Permit (County Government)
- ✅ Director's ID copies
- ✅ Proof of physical address (lease agreement / ownership)

**Outcome**: KRA approval letter with device allocation

---

#### Phase 2: Device Purchase & Installation (Week 3-4)

**Step 1: Purchase eTIMS Devices**

**Option A: Physical Device (Hardware SDC)**
- **Vendor**: Huawei, ZTE (KRA-approved vendors)
- **Cost**: KES 30,000 - KES 50,000 per device
- **Specs**: Thermal printer, touchscreen, 4G connectivity
- **Use Case**: Retail (POS), restaurants, small businesses

**Option B: Virtual Device (Software SDC) - RECOMMENDED FOR CHIROERP**
- **Integration**: API-based (no physical device)
- **Cost**: Free (API access only)
- **Requirements**: 
  - Server with internet connectivity
  - HTTPS endpoint (SSL certificate)
  - JSON API integration
- **Use Case**: ERP systems, e-commerce, SaaS

**ChiroERP Approach**: **Virtual Device (Software SDC)**

**Step 2: Device Initialization**
1. Receive device credentials from KRA:
   - **Device Serial Number**: e.g., `KRA-ETIMS-001234`
   - **Device PIN**: e.g., `9876543210`
   - **Organization Code**: e.g., `ORG-KE-5678`
2. Initialize device via eTIMS API:

```bash
# Initialize Device
curl -X POST "https://etims.kra.go.ke/api/v1/device/init" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${KRA_API_KEY}" \
  -d '{
    "deviceSerialNumber": "KRA-ETIMS-001234",
    "devicePin": "9876543210",
    "organizationCode": "ORG-KE-5678",
    "businessName": "Acme Kenya Ltd",
    "kraPin": "A123456789X"
  }'

# Expected Response:
{
  "status": "SUCCESS",
  "deviceId": "DEV-001234",
  "activationDate": "2026-02-03T10:00:00Z",
  "expiryDate": "2027-02-03T10:00:00Z"
}
```

**Outcome**: Device activated and ready for testing

---

#### Phase 3: Testing (Week 5-6)

**Step 1: Configure ChiroERP Integration**

**Application Configuration** (`application.yml`):
```yaml
kra:
  etims:
    baseUrl: https://etims-sandbox.kra.go.ke/api/v1  # Test environment
    apiKey: ${KRA_ETIMS_API_KEY}
    deviceSerialNumber: KRA-ETIMS-001234
    devicePin: 9876543210
    organizationCode: ORG-KE-5678
    timeout: 5000  # 5 seconds
    retryAttempts: 3
    retryDelayMs: 1000
```

**Step 2: Test Invoice Submission**

**ChiroERP Invoice Payload**:
```kotlin
data class InvoiceSubmissionRequest(
    val deviceSerialNumber: String,
    val internalData: InternalData,
    val receiptSignature: String
)

data class InternalData(
    val rcptNo: String,           // Invoice number: INV-2026-001
    val rcptPbctDate: String,     // Publication date: 2026-02-03T10:00:00Z
    val trdrInvcNo: String,       // Trader invoice number: INV-2026-001
    val trdrNm: String,           // Trader name: Acme Kenya Ltd
    val trdrPin: String,          // Trader PIN: A123456789X
    val custPin: String,          // Customer PIN: P051234567X
    val custNm: String,           // Customer name: Safaricom Ltd
    val salesTyCd: String,        // Sales type: N (Normal)
    val rcptTyCd: String,         // Receipt type: S (Sale)
    val pmtTyCd: String,          // Payment type: 01 (Cash), 02 (Card), 03 (Mobile)
    val salesSttsCd: String,      // Sales status: 02 (Approved)
    val cfmDt: String,            // Confirmation date: 2026-02-03T10:00:00Z
    val totItemCnt: Int,          // Total items: 2
    val taxblAmtA: Double,        // Taxable amount (VAT 16%): 20000.00
    val taxblAmtB: Double,        // Taxable amount (VAT 0%): 0.00
    val taxblAmtC: Double,        // Exempt amount: 0.00
    val taxRtA: Double,           // Tax rate A: 16.0
    val taxAmtA: Double,          // Tax amount A: 3200.00
    val totTaxblAmt: Double,      // Total taxable: 20000.00
    val totTaxAmt: Double,        // Total tax: 3200.00
    val totAmt: Double,           // Total amount: 23200.00
    val remark: String?           // Remark: Payment via M-Pesa
)
```

**Submit to KRA eTIMS**:
```kotlin
@Service
class KraEtimsService(
    private val restTemplate: RestTemplate,
    private val etimsConfig: EtimsConfig
) {
    fun submitInvoice(invoice: Invoice): CuNumber {
        val internalData = buildInternalData(invoice)
        val signature = generateSignature(internalData)
        
        val request = InvoiceSubmissionRequest(
            deviceSerialNumber = etimsConfig.deviceSerialNumber,
            internalData = internalData,
            receiptSignature = signature
        )
        
        val response = restTemplate.postForEntity(
            "${etimsConfig.baseUrl}/invoice/submit",
            request,
            EtimsResponse::class.java
        )
        
        if (response.statusCode == HttpStatus.OK) {
            val cuNumber = response.body?.cuNumber
            return cuNumber ?: throw EtimsException("Cu number not received")
        } else {
            throw EtimsException("eTIMS submission failed: ${response.statusCode}")
        }
    }
    
    private fun generateSignature(internalData: InternalData): String {
        // Signature = SHA256(deviceSerialNumber + internalData JSON + devicePin)
        val dataToSign = etimsConfig.deviceSerialNumber +
                         objectMapper.writeValueAsString(internalData) +
                         etimsConfig.devicePin
        
        return MessageDigest.getInstance("SHA-256")
            .digest(dataToSign.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
```

**Expected Response** (Cu Number Retrieval):
```json
{
  "status": "SUCCESS",
  "cuNumber": "CU-2026-02-03-001234",
  "rcptNo": "INV-2026-001",
  "intrlData": "...",
  "rcptSign": "...",
  "sdcDateTime": "2026-02-03T10:05:00Z"
}
```

**Step 3: Test Scenarios**

| Scenario | Expected Result | Status |
|----------|-----------------|--------|
| Normal Sale (VAT 16%) | Cu number received < 2s | ✅ |
| Zero-Rated Sale (VAT 0%) | Cu number received | ✅ |
| Exempt Sale | Cu number received | ✅ |
| Credit Note (Return) | Cu number received | ✅ |
| Invalid Customer PIN | Error: INVALID_CUSTOMER_PIN | ✅ |
| Duplicate Invoice Number | Error: DUPLICATE_INVOICE | ✅ |
| eTIMS Service Down | Retry with exponential backoff | ✅ |
| Network Timeout (> 5s) | Queue for retry | ✅ |

**Outcome**: 100 test invoices submitted successfully with Cu numbers

---

#### Phase 4: KRA Inspection & Approval (Week 7-8)

**Step 1: Submit Test Results to KRA**
1. Export test invoice log (100 invoices)
2. Include Cu numbers for all invoices
3. Submit via iTax portal: **eTIMS → Testing Results Upload**

**Step 2: KRA Technical Inspection**
- KRA IT officer reviews test results
- Validates Cu numbers match KRA database
- Checks signature generation algorithm
- Verifies error handling (retries, timeouts)

**Step 3: Production Approval**
- KRA issues **eTIMS Production Certificate**
- Provides production API credentials:
  - Production endpoint: `https://etims.kra.go.ke/api/v1`
  - Production API key
  - Production device serial numbers (5-10 devices)

**Documents Issued**:
- ✅ eTIMS Production Certificate
- ✅ Device Serial Numbers (production)
- ✅ API Key (production)

**Outcome**: **ChiroERP certified for KRA eTIMS production use** 🎉

---

### 2.3 Production Deployment

**Step 1: Update Configuration**
```yaml
kra:
  etims:
    baseUrl: https://etims.kra.go.ke/api/v1  # PRODUCTION
    apiKey: ${KRA_ETIMS_PROD_API_KEY}
    deviceSerialNumber: KRA-ETIMS-PROD-001
```

**Step 2: Go-Live Checklist**
- [ ] Production API key configured
- [ ] All invoices generating Cu numbers
- [ ] Error handling tested (retry logic)
- [ ] Monitoring alerts configured (Cu retrieval > 2s)
- [ ] Finance team trained on eTIMS workflow
- [ ] Customer invoices display Cu number prominently

**Step 3: Ongoing Compliance**
- **Monthly Report**: Submit invoice statistics to KRA (via iTax portal)
- **Device Renewal**: Annual renewal (KES 10,000/device/year)
- **Audit Readiness**: KRA can request invoice audit anytime (must provide Cu numbers)

---

### 2.4 Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| **Cu number not received** | eTIMS service down | Retry with exponential backoff (1s, 2s, 4s) |
| **Error: INVALID_DEVICE_PIN** | Wrong device PIN | Verify device credentials in iTax portal |
| **Error: DUPLICATE_INVOICE** | Invoice number reused | Ensure unique invoice numbers per day |
| **Error: INVALID_CUSTOMER_PIN** | Customer KRA PIN invalid | Validate customer PIN before invoice |
| **Timeout (> 5s)** | Network latency | Queue invoice, retry later |
| **Error: DEVICE_EXPIRED** | Annual renewal missed | Renew device on iTax portal |

**Support Contact**:
- **KRA eTIMS Helpdesk**: +254 20 310 900 (Option 3)
- **Email**: etimssupport@kra.go.ke
- **Portal**: https://itax.kra.go.ke/KRA-Portal/helpDesk.htm

---

## 3. M-Pesa Daraja API Certification (Kenya) 🇰🇪

### 3.1 Overview

**What is M-Pesa Daraja API?**  
Safaricom's **mobile money API** for integrating M-Pesa payments into ChiroERP.

**Why is it critical?**  
- **80%+ of B2B/B2C payments in Kenya via M-Pesa**
- Real-time payment notifications (C2B callback)
- Automated invoice application
- Lower transaction fees vs banks (0.5% vs 3%)

**Use Cases in ChiroERP**:
- **C2B (Customer-to-Business)**: Customer pays invoice via M-Pesa Paybill/Till
- **B2C (Business-to-Customer)**: Refunds, salaries, commissions
- **B2B (Business-to-Business)**: Supplier payments, intercompany transfers

---

### 3.2 Certification Process

#### Phase 1: Sandbox Registration (Week 1)

**Step 1: Create Daraja Developer Account**
1. Go to https://developer.safaricom.co.ke/
2. Click **Sign Up**
3. Fill registration form:
   - Full name
   - Email address
   - Phone number (Safaricom line required)
4. Verify email (activation link)

**Step 2: Create Sandbox App**
1. Log in to Daraja Portal
2. Navigate to **My Apps**
3. Click **Add a New App**
4. Fill app details:
   - **App Name**: ChiroERP Kenya
   - **Description**: ERP system with M-Pesa payments
   - **APIs**: Select C2B, B2C, B2B
5. Click **Create App**

**Step 3: Get Sandbox Credentials**
- **Consumer Key**: e.g., `ABC123XYZ456`
- **Consumer Secret**: e.g., `SECRET789`
- **Shortcode (Paybill)**: `174379` (sandbox shortcode)

**Outcome**: Sandbox credentials received

---

#### Phase 2: Integration Development (Week 2-3)

**Step 1: OAuth2 Token Retrieval**

```kotlin
@Service
class MPesaDarajaService(
    private val restTemplate: RestTemplate,
    private val mpesaConfig: MPesaConfig
) {
    private var accessToken: String? = null
    private var tokenExpiryTime: Instant? = null
    
    fun getAccessToken(): String {
        // Check if token is still valid (expires in 3600s)
        if (accessToken != null && tokenExpiryTime?.isAfter(Instant.now()) == true) {
            return accessToken!!
        }
        
        // Generate Basic Auth header
        val credentials = "${mpesaConfig.consumerKey}:${mpesaConfig.consumerSecret}"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
        
        val headers = HttpHeaders()
        headers["Authorization"] = "Basic $encodedCredentials"
        
        val response = restTemplate.exchange(
            "${mpesaConfig.baseUrl}/oauth/v1/generate?grant_type=client_credentials",
            HttpMethod.GET,
            HttpEntity<String>(headers),
            MPesaTokenResponse::class.java
        )
        
        accessToken = response.body?.access_token
        tokenExpiryTime = Instant.now().plusSeconds(3500) // 3500s (100s buffer)
        
        return accessToken!!
    }
}

data class MPesaTokenResponse(
    val access_token: String,
    val expires_in: Int  // 3600 seconds
)
```

**Step 2: C2B Registration (Register Callback URLs)**

```kotlin
fun registerC2BUrls() {
    val token = getAccessToken()
    
    val request = C2BRegistrationRequest(
        ShortCode = mpesaConfig.shortcode,  // 174379 (sandbox)
        ResponseType = "Completed",
        ConfirmationURL = "${mpesaConfig.callbackBaseUrl}/api/v1/treasury/payments/mpesa/confirmation",
        ValidationURL = "${mpesaConfig.callbackBaseUrl}/api/v1/treasury/payments/mpesa/validation"
    )
    
    val headers = HttpHeaders()
    headers["Authorization"] = "Bearer $token"
    
    val response = restTemplate.postForEntity(
        "${mpesaConfig.baseUrl}/mpesa/c2b/v1/registerurl",
        HttpEntity(request, headers),
        MPesaRegistrationResponse::class.java
    )
    
    logger.info("C2B registration: ${response.body}")
}
```

**Step 3: C2B Payment Simulation (Sandbox Only)**

```kotlin
fun simulateC2BPayment(invoiceId: String, amount: Double, phoneNumber: String) {
    val token = getAccessToken()
    
    val request = C2BSimulateRequest(
        ShortCode = mpesaConfig.shortcode,
        CommandID = "CustomerPayBillOnline",
        Amount = amount.toInt(),
        Msisdn = phoneNumber,  // e.g., 254712345678
        BillRefNumber = invoiceId  // Invoice ID: INV-2026-001
    )
    
    val headers = HttpHeaders()
    headers["Authorization"] = "Bearer $token"
    
    val response = restTemplate.postForEntity(
        "${mpesaConfig.baseUrl}/mpesa/c2b/v1/simulate",
        HttpEntity(request, headers),
        MPesaSimulateResponse::class.java
    )
    
    logger.info("Payment simulation: ${response.body}")
}
```

**Step 4: C2B Callback Handler**

```kotlin
@RestController
@RequestMapping("/api/v1/treasury/payments/mpesa")
class MPesaCallbackController(
    private val paymentService: PaymentService
) {
    @PostMapping("/confirmation")
    fun confirmationCallback(@RequestBody callback: MPesaC2BCallback): ResponseEntity<MPesaCallbackResponse> {
        logger.info("M-Pesa C2B confirmation: $callback")
        
        try {
            // Extract invoice ID from BillRefNumber
            val invoiceId = callback.BillRefNumber
            
            // Create payment record
            val payment = Payment(
                id = UUID.randomUUID(),
                invoiceId = invoiceId,
                amount = callback.TransAmount,
                paymentMethod = "MPESA",
                transactionId = callback.TransID,
                phoneNumber = callback.MSISDN,
                receivedAt = Instant.now()
            )
            
            // Apply payment to invoice
            paymentService.applyPayment(payment)
            
            // Respond to Safaricom (MUST respond within 30 seconds)
            return ResponseEntity.ok(MPesaCallbackResponse(
                ResultCode = "0",
                ResultDesc = "Accepted"
            ))
        } catch (e: Exception) {
            logger.error("M-Pesa callback error", e)
            return ResponseEntity.ok(MPesaCallbackResponse(
                ResultCode = "1",
                ResultDesc = "Rejected: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/validation")
    fun validationCallback(@RequestBody callback: MPesaC2BCallback): ResponseEntity<MPesaCallbackResponse> {
        // Validate invoice exists and amount matches
        val invoice = invoiceService.findById(callback.BillRefNumber)
        
        return if (invoice != null && invoice.totalAmount == callback.TransAmount) {
            ResponseEntity.ok(MPesaCallbackResponse(ResultCode = "0", ResultDesc = "Accepted"))
        } else {
            ResponseEntity.ok(MPesaCallbackResponse(ResultCode = "1", ResultDesc = "Invalid invoice"))
        }
    }
}

data class MPesaC2BCallback(
    val TransactionType: String,  // "Pay Bill"
    val TransID: String,          // "MPX123456789"
    val TransTime: String,        // "20260203100500"
    val TransAmount: Double,      // 23200.00
    val BusinessShortCode: String, // "174379"
    val BillRefNumber: String,    // "INV-2026-001"
    val InvoiceNumber: String?,   // Optional
    val OrgAccountBalance: String?, // "100000.00"
    val ThirdPartyTransID: String?, // Optional
    val MSISDN: String,           // "254712345678"
    val FirstName: String,        // "John"
    val MiddleName: String?,      // "Doe"
    val LastName: String?         // "Smith"
)
```

**Step 5: Testing**

| Test Case | Expected Result | Status |
|-----------|-----------------|--------|
| C2B Payment (Correct Amount) | Payment applied to invoice | ✅ |
| C2B Payment (Wrong Amount) | Payment rejected (validation) | ✅ |
| C2B Payment (Invalid Invoice) | Payment rejected | ✅ |
| Callback Timeout (> 30s) | Safaricom retries callback | ✅ |
| Duplicate Transaction | Idempotency check (TransID) | ✅ |

**Outcome**: Sandbox C2B payments working

---

#### Phase 3: Production Application (Week 4)

**Step 1: Prepare Production Application**

**Documents Required**:
- ✅ Certificate of Registration (CR12)
- ✅ KRA PIN Certificate
- ✅ Business Permit
- ✅ Safaricom Paybill Number (apply via Safaricom)
- ✅ Letter of Request (signed by Director)
- ✅ Test results from sandbox (100 successful transactions)

**Step 2: Submit Application**
1. Log in to Daraja Portal
2. Navigate to **My Apps → ChiroERP Kenya**
3. Click **Apply for Production**
4. Upload documents
5. Fill production form:
   - **Paybill Number**: (Your Safaricom Paybill)
   - **Till Number**: (If using Till instead)
   - **Expected Monthly Volume**: 10,000 transactions
   - **Go-Live Date**: March 1, 2026

**Step 3: Safaricom Review (2-3 weeks)**
- Safaricom technical team reviews integration
- May request demo or additional tests
- Issues production credentials

**Step 4: Production Credentials**
- **Consumer Key** (production)
- **Consumer Secret** (production)
- **Paybill Shortcode** (production): e.g., `987654`

**Outcome**: Production API credentials received

---

#### Phase 4: Production Deployment

**Step 1: Update Configuration**
```yaml
mpesa:
  daraja:
    baseUrl: https://api.safaricom.co.ke  # PRODUCTION
    consumerKey: ${MPESA_PROD_CONSUMER_KEY}
    consumerSecret: ${MPESA_PROD_CONSUMER_SECRET}
    shortcode: 987654  # PRODUCTION PAYBILL
    callbackBaseUrl: https://api.chiroerp.com
```

**Step 2: Go-Live Checklist**
- [ ] Production credentials configured
- [ ] Callback URLs registered (production)
- [ ] SSL certificate valid (HTTPS required)
- [ ] Firewall allows Safaricom IPs:
  - `196.201.214.0/24`
  - `196.201.213.0/24`
- [ ] Monitoring alerts configured (callback failures)
- [ ] Finance team trained on M-Pesa reconciliation

**Step 3: First Production Transaction**
1. Finance team tests with real payment (KES 10)
2. Verify callback received
3. Verify payment applied to invoice
4. Verify GL posting correct

**Outcome**: **M-Pesa Daraja API production certified** 🎉

---

### 3.3 Ongoing Operations

**Monthly Reconciliation**:
- Download M-Pesa statement from Safaricom portal
- Match M-Pesa transactions with ChiroERP payments
- Investigate discrepancies (missing callbacks)

**Rate Limits**:
- **Production**: 20 requests/second
- **Burst**: 100 requests/second (short burst allowed)

**Support Contact**:
- **Daraja Support**: darajaapi@safaricom.co.ke
- **Phone**: +254 722 000 000

---

## 4. TRA VFD/EFD Certification (Tanzania) 🇹🇿

### 4.1 Overview

**What is TRA VFD/EFD?**  
Tanzania Revenue Authority's **Virtual/Electronic Fiscal Device** - mandatory since 2019 for all VAT-registered businesses.

**Difference from Kenya eTIMS**:
- **VFD**: Virtual device (API-based, like ChiroERP approach)
- **EFD**: Electronic device (physical printer with fiscal memory)

**ChiroERP Approach**: **VFD (Virtual Fiscal Device)**

---

### 4.2 Certification Process (6-10 weeks)

#### Phase 1: Registration (Week 1-2)
1. Register on TRA iTax portal: https://itax.tra.go.tz/
2. Apply for VFD registration
3. Submit documents (CR, TIN, VAT Certificate)
4. TRA site inspection (2 weeks)

#### Phase 2: VFD Purchase (Week 3-4)
1. Purchase VFD software license from TRA-approved vendor
2. **Cost**: TZS 3,000,000 (~$1,500)
3. Receive VFD serial number and credentials

#### Phase 3: Integration (Week 5-6)
1. Integrate ChiroERP with TRA VFD API
2. Submit test invoices (100 invoices)
3. Retrieve **verification codes** for each invoice

#### Phase 4: Approval (Week 7-10)
1. TRA technical inspection
2. Production approval issued

**Outcome**: TRA VFD production certified

---

## 5. Security & Compliance Audits

### 5.1 ISO 27001 (Information Security Management)

**Purpose**: Enterprise customers require ISO 27001 certification

**Timeline**: 6-12 months  
**Cost**: $15,000 - $50,000  
**Audit Firm**: Bureau Veritas, BSI, SGS

**Certification Steps**:
1. **Gap Analysis** (Month 1): Identify missing controls
2. **ISMS Implementation** (Month 2-6): Implement ISO 27001 controls
3. **Internal Audit** (Month 7): Test controls
4. **Stage 1 Audit** (Month 8): Documentation review
5. **Stage 2 Audit** (Month 9-10): On-site audit
6. **Certification** (Month 11-12): ISO 27001 certificate issued

**Key Controls** (Annex A):
- **A.9**: Access Control (RBAC, MFA)
- **A.10**: Cryptography (TLS 1.3, AES-256)
- **A.12**: Operations Security (Logging, Monitoring)
- **A.14**: System Acquisition (Secure SDLC)
- **A.17**: Business Continuity (Disaster Recovery)

---

### 5.2 PCI-DSS (Payment Card Industry Data Security Standard)

**Purpose**: If ChiroERP processes credit card payments

**Timeline**: 3-6 months  
**Cost**: $10,000 - $30,000  
**Level**: Level 4 (< 1M transactions/year) - Self-Assessment Questionnaire (SAQ)

**Certification Steps**:
1. Complete SAQ D (350+ questions)
2. Quarterly vulnerability scan (Approved Scanning Vendor)
3. Submit Attestation of Compliance (AOC)

**Key Requirements**:
- Never store CVV/CVC2
- Encrypt cardholder data (AES-256)
- Restrict access to cardholder data (RBAC)
- Log all access to cardholder data

---

### 5.3 SOC 2 Type II (US Customers)

**Purpose**: US enterprise customers require SOC 2

**Timeline**: 6-12 months  
**Cost**: $20,000 - $100,000  
**Audit Firm**: Deloitte, PwC, EY, KPMG

**Trust Service Criteria**:
- **Security**: Firewall, IDS/IPS, MFA
- **Availability**: 99.9% uptime SLA
- **Processing Integrity**: Data accuracy (GL reconciliation)
- **Confidentiality**: Encryption (TLS, AES-256)
- **Privacy**: GDPR compliance

---

## 6. Production Readiness Checklist

### 6.1 Kenya Production
- [ ] **KRA eTIMS certified** (Cu numbers working)
- [ ] **M-Pesa Daraja API certified** (C2B callbacks working)
- [ ] PesaLink integration tested
- [ ] RTGS Kenya integration tested
- [ ] Finance team trained (eTIMS workflow)
- [ ] Customer invoices display Cu number
- [ ] Monthly KRA reporting automated

### 6.2 Tanzania Production
- [ ] **TRA VFD certified** (Verification codes working)
- [ ] M-Pesa Tanzania integration tested
- [ ] EAC customs documentation generated
- [ ] Finance team trained (VFD workflow)

### 6.3 Security & Compliance
- [ ] SSL certificate valid (HTTPS)
- [ ] MFA enabled for all users
- [ ] Audit logging enabled (all financial transactions)
- [ ] Data encryption at rest (AES-256)
- [ ] Data encryption in transit (TLS 1.3)
- [ ] Backup/restore tested (RPO 1 hour, RTO 4 hours)
- [ ] Penetration test completed (OWASP Top 10)
- [ ] ISO 27001 certification (if enterprise sales)
- [ ] PCI-DSS compliance (if card payments)
- [ ] SOC 2 Type II (if US customers)

---

## 7. Cost Summary

| Certification | Duration | One-Time Cost | Annual Cost | Blocking? |
|---------------|----------|---------------|-------------|-----------|
| **KRA eTIMS** | 8 weeks | $2,000 | $500 (renewal) | ✅ YES |
| **M-Pesa Daraja** | 4 weeks | Free | Free | ⚠️ Partial |
| **TRA VFD** | 10 weeks | $1,500 | $300 (renewal) | ✅ YES (Tanzania) |
| **ISO 27001** | 12 months | $30,000 | $5,000 (audit) | ❌ NO |
| **PCI-DSS** | 6 months | $15,000 | $5,000 (scan) | ⚠️ Partial |
| **SOC 2 Type II** | 12 months | $50,000 | $20,000 (audit) | ❌ NO |
| **TOTAL** | 24 months | **$98,500** | **$30,800/year** | |

**Critical Path (Go-Live)**:  
KRA eTIMS (8 weeks) → M-Pesa Daraja (4 weeks) → **Go-Live** (Month 3)

**Nice-to-Have (Enterprise Sales)**:  
ISO 27001 (12 months) → SOC 2 (12 months) → **Enterprise-Ready** (Month 24)

---

## 8. Support Contacts

### Kenya
- **KRA eTIMS**: +254 20 310 900, etimssupport@kra.go.ke
- **M-Pesa Daraja**: darajaapi@safaricom.co.ke
- **iTax Portal**: https://itax.kra.go.ke/

### Tanzania
- **TRA VFD**: +255 22 210 2242, vfd@tra.go.tz
- **iTax Portal**: https://itax.tra.go.tz/

### Security
- **ISO 27001**: info@bureauveritas.com
- **PCI-DSS**: compliance@pcisecuritystandards.org
- **SOC 2**: soc@aicpa.org

---

**Next Steps**: Start KRA eTIMS registration (Week 1), target production by Month 3.
