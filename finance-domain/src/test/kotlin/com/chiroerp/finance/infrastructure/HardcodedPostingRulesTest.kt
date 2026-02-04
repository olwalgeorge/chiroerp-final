package com.chiroerp.finance.infrastructure

import com.chiroerp.finance.domain.HardcodedPostingRules
import com.chiroerp.shared.config.PostingContext
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class HardcodedPostingRulesTest {

    private lateinit var postingRules: HardcodedPostingRules

    @BeforeEach
    fun setup() {
        postingRules = HardcodedPostingRules()
    }

    // =========================================================================
    // Invoice Transaction Tests
    // =========================================================================

    @Test
    fun `should determine accounts for invoice transaction`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("1000.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("customerId" to "CUST-001")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping).isNotNull
        assertThat(mapping?.debitAccount).isEqualTo("1200")  // AR
        assertThat(mapping?.creditAccount).isEqualTo("4000")  // Revenue
        assertThat(mapping?.costCenter).isEqualTo("CC-100")
        assertThat(mapping?.explanation).contains("Customer Invoice")
    }

    // =========================================================================
    // Payment Transaction Tests
    // =========================================================================

    @Test
    fun `should determine accounts for cash payment`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "PAYMENT",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("500.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("paymentMethod" to "CASH")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("1000")  // Cash
        assertThat(mapping?.creditAccount).isEqualTo("1200")  // AR
        assertThat(mapping?.explanation).contains("Payment Received")
    }

    @Test
    fun `should determine accounts for check payment`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "PAYMENT",
            companyCode = "1000",
            costCenter = null,
            amount = BigDecimal("750.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("paymentMethod" to "CHECK")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("1010")  // Cash - Checks
        assertThat(mapping?.creditAccount).isEqualTo("1200")  // AR
    }

    @Test
    fun `should determine accounts for wire payment`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "PAYMENT",
            companyCode = "1000",
            costCenter = null,
            amount = BigDecimal("2000.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("paymentMethod" to "WIRE")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("1020")  // Cash - Wire
    }

    // =========================================================================
    // Expense Transaction Tests
    // =========================================================================

    @Test
    fun `should determine accounts for salary expense`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "EXPENSE",
            companyCode = "1000",
            costCenter = "CC-200",
            amount = BigDecimal("5000.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf(
                "expenseCategory" to "SALARY",
                "isPaid" to false
            )
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("5100")  // Salaries Expense
        assertThat(mapping?.creditAccount).isEqualTo("2000")  // AP (not paid)
        assertThat(mapping?.costCenter).isEqualTo("CC-200")
    }

    @Test
    fun `should determine accounts for paid expense`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "EXPENSE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("300.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf(
                "expenseCategory" to "UTILITIES",
                "isPaid" to true
            )
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("5300")  // Utilities Expense
        assertThat(mapping?.creditAccount).isEqualTo("1000")  // Cash (paid)
    }

    @Test
    fun `should use default cost center for expense without one`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "EXPENSE",
            companyCode = "1000",
            costCenter = null,
            amount = BigDecimal("100.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf(
                "expenseCategory" to "GENERAL",
                "isPaid" to false
            )
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.costCenter).isEqualTo("CC-100")  // Default
    }

    // =========================================================================
    // Revenue Transaction Tests
    // =========================================================================

    @Test
    fun `should determine accounts for product revenue`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "REVENUE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("1500.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("revenueType" to "PRODUCT")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("1000")  // Cash
        assertThat(mapping?.creditAccount).isEqualTo("4000")  // Product Revenue
    }

    @Test
    fun `should determine accounts for service revenue`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "REVENUE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("800.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("revenueType" to "SERVICE")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.creditAccount).isEqualTo("4100")  // Service Revenue
    }

    // =========================================================================
    // Asset Transaction Tests
    // =========================================================================

    @Test
    fun `should determine accounts for equipment purchase`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "ASSET_PURCHASE",
            companyCode = "1000",
            costCenter = "CC-300",
            amount = BigDecimal("10000.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf(
                "assetType" to "EQUIPMENT",
                "isPaid" to true
            )
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("1520")  // Equipment
        assertThat(mapping?.creditAccount).isEqualTo("1000")  // Cash (paid)
    }

    @Test
    fun `should determine accounts for building purchase on credit`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "ASSET_PURCHASE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("500000.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf(
                "assetType" to "BUILDING",
                "isPaid" to false
            )
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("1510")  // Building
        assertThat(mapping?.creditAccount).isEqualTo("2000")  // AP (not paid)
    }

    // =========================================================================
    // Depreciation Transaction Tests
    // =========================================================================

    @Test
    fun `should determine accounts for depreciation`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "DEPRECIATION",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("833.33"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.debitAccount).isEqualTo("5700")  // Depreciation Expense
        assertThat(mapping?.creditAccount).isEqualTo("1590")  // Accumulated Depreciation
    }

    // =========================================================================
    // Profit Center Derivation Tests
    // =========================================================================

    @Test
    fun `should derive profit center from sales cost center`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "1000",
            costCenter = "CC-1001",
            amount = BigDecimal("1000.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("customerId" to "CUST-001")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.profitCenter).isEqualTo("PC-1000")
    }

    @Test
    fun `should derive profit center from operations cost center`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "REVENUE",
            companyCode = "1000",
            costCenter = "CC-2001",
            amount = BigDecimal("500.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = mapOf("revenueType" to "SERVICE")
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isSuccess).isTrue()
        val mapping = result.getOrNull()
        assertThat(mapping?.profitCenter).isEqualTo("PC-2000")
    }

    // =========================================================================
    // Validation Tests
    // =========================================================================

    @Test
    fun `should validate correct posting context`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("100.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.validatePosting(context)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should reject posting with zero amount`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal.ZERO,
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.validatePosting(context)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("positive")
    }

    @Test
    fun `should reject posting with negative amount`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("-100.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.validatePosting(context)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject posting without company code`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "",
            costCenter = "CC-100",
            amount = BigDecimal("100.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.validatePosting(context)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Company code")
    }

    @Test
    fun `should reject posting without currency`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "INVOICE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("100.00"),
            currency = "",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.validatePosting(context)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Currency")
    }

    // =========================================================================
    // Error Handling Tests
    // =========================================================================

    @Test
    fun `should reject unknown transaction type`(): Unit = runBlocking {
        val context = PostingContext(
            transactionType = "UNKNOWN_TYPE",
            companyCode = "1000",
            costCenter = "CC-100",
            amount = BigDecimal("100.00"),
            currency = "USD",
            documentDate = LocalDate.now(),
            attributes = emptyMap()
        )

        val result = postingRules.determineAccounts(context)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Unknown transaction type")
    }
}
