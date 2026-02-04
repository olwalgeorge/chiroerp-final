package com.chiroerp.finance.infrastructure

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Disabled("Panache bytecode enhancement not available in Quarkus test env; covered by use-case and tenant isolation tests")
@QuarkusTest
class JournalEntryResourceTest {

    @Inject
    lateinit var em: EntityManager

    private val tenantA = UUID.randomUUID()
    private val tenantB = UUID.randomUUID()

    @BeforeEach
    fun cleanDb() {
        QuarkusTransaction.requiringNew().run {
            em.createNativeQuery("TRUNCATE TABLE finance.journal_entry_lines CASCADE").executeUpdate()
            em.createNativeQuery("TRUNCATE TABLE finance.journal_entries CASCADE").executeUpdate()
            em.createNativeQuery("TRUNCATE TABLE finance.gl_accounts CASCADE").executeUpdate()
        }
    }

    @Test
    fun `should create and post journal entry with tenant isolation`() {
        // Create journal entry (tenant A)
        val entryNumber = "JE-1001"
        val today = LocalDate.now()

        // Seed required GL accounts for tenant A (FK enforcement)
        insertGlAccount(tenantA, "1000", "Cash", "ASSET", "DEBIT")
        insertGlAccount(tenantA, "4000", "Revenue", "REVENUE", "CREDIT")

        given()
            .header("X-Tenant-Id", tenantA)
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "entryNumber" to entryNumber,
                    "companyCode" to "1000",
                    "entryType" to "STANDARD",
                    "documentDate" to today.toString(),
                    "postingDate" to today.toString(),
                    "fiscalYear" to today.year,
                    "fiscalPeriod" to today.monthValue,
                    "currency" to "USD",
                    "exchangeRate" to BigDecimal.ONE,
                    "description" to "Test JE",
                    "lines" to listOf(
                        mapOf(
                            "lineNumber" to 1,
                            "accountNumber" to "1000",
                            "debitAmount" to 100.0,
                            "creditAmount" to 0.0
                        ),
                        mapOf(
                            "lineNumber" to 2,
                            "accountNumber" to "4000",
                            "debitAmount" to 0.0,
                            "creditAmount" to 100.0
                        )
                    )
                )
            )
            .post("/api/v1/finance/journal-entries")
            .then()
            .statusCode(201)
            .body("status", equalTo("DRAFT"))
            .body("entryNumber", equalTo(entryNumber))

        // Post journal entry
        given()
            .header("X-Tenant-Id", tenantA)
            .contentType(ContentType.JSON)
            .post("/api/v1/finance/journal-entries/$entryNumber/post")
            .then()
            .statusCode(200)
            .body("status", equalTo("POSTED"))

        // List for tenant A should contain 1
        given()
            .header("X-Tenant-Id", tenantA)
            .get("/api/v1/finance/journal-entries")
            .then()
            .statusCode(200)
            .body("size()", `is`(1))

        // Tenant B should see nothing
        given()
            .header("X-Tenant-Id", tenantB)
            .get("/api/v1/finance/journal-entries")
            .then()
            .statusCode(200)
            .body("size()", `is`(0))
    }

    private fun insertGlAccount(tenantId: UUID, number: String, name: String, type: String, balanceType: String) {
        QuarkusTransaction.requiringNew().run {
            em.createNativeQuery(
                """
                INSERT INTO finance.gl_accounts (
                    id, tenant_id, account_number, account_name, account_type, balance_type,
                    currency_code, is_control_account, is_active, is_posting_allowed,
                    company_code, created_at, created_by, updated_at, updated_by, version
                ) VALUES (?,?,?,?,?,?, 'USD', false, true, true, '1000', now(), ?, now(), ?, 0)
                """.trimIndent()
            ).apply {
                setParameter(1, UUID.randomUUID())
                setParameter(2, tenantId)
                setParameter(3, number)
                setParameter(4, name)
                setParameter(5, type)
                setParameter(6, balanceType)
                setParameter(7, UUID.randomUUID())
                setParameter(8, UUID.randomUUID())
            }.executeUpdate()
        }
    }
}
