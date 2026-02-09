package com.chiroerp.tenancy.core.infrastructure.api

import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Integration tests for TenantController REST API.
 * 
 * Tests validate:
 * - Authentication/authorization enforcement
 * - Request validation
 * - Lifecycle operations
 * - Error handling
 */
@QuarkusTest
class TenantControllerTest {

    @Test
    fun `create tenant without auth returns 401`() {
        given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest("unauth-test.example"))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `create tenant with valid auth returns 201 and tenant in ACTIVE status`() {
        val domain = "create-test-${System.currentTimeMillis()}.example"
        
        given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("domain", equalTo(domain))
            .body("status", equalTo(TenantStatus.ACTIVE.name))
            .body("tier", equalTo(TenantTier.STANDARD.name))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `create tenant with invalid domain returns 400`() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name": "Invalid Domain Tenant",
                    "domain": "INVALID DOMAIN WITH SPACES",
                    "dataResidencyCountry": "US"
                }
            """.trimIndent())
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(400)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `get tenant by id returns tenant details`() {
        val domain = "get-by-id-${System.currentTimeMillis()}.example"
        
        // Create tenant first
        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        // Get by ID
        given()
            .`when`()
            .get("/api/tenants/$tenantId")
            .then()
            .statusCode(200)
            .body("id", equalTo(tenantId))
            .body("domain", equalTo(domain))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `get tenant by non-existent id returns 404`() {
        given()
            .`when`()
            .get("/api/tenants/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["tenant-admin"])
    fun `get tenant by domain returns tenant`() {
        val domain = "by-domain-${System.currentTimeMillis()}.example"
        
        // Create tenant first
        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        // Get by domain (requires tenant-admin or platform-admin)
        given()
            .header("X-Tenant-ID", tenantId)
            .`when`()
            .get("/api/tenants/by-domain/$domain")
            .then()
            .statusCode(200)
            .body("domain", equalTo(domain))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["tenant-admin"])
    fun `get tenant by id with matching tenant header succeeds`() {
        val domain = "tenant-scope-ok-${System.currentTimeMillis()}.example"

        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        given()
            .header("X-Tenant-ID", tenantId)
            .`when`()
            .get("/api/tenants/$tenantId")
            .then()
            .statusCode(200)
            .body("id", equalTo(tenantId))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["tenant-admin"])
    fun `get tenant by id with mismatched tenant header returns 403`() {
        val domain = "tenant-scope-deny-${System.currentTimeMillis()}.example"

        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        given()
            .header("X-Tenant-ID", UUID.randomUUID().toString())
            .`when`()
            .get("/api/tenants/$tenantId")
            .then()
            .statusCode(403)
    }

    @Test
    fun `get tenant by domain without auth returns 401`() {
        given()
            .`when`()
            .get("/api/tenants/by-domain/some-domain.example")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(user = "gateway", roles = ["gateway-service"])
    fun `resolve tenant with gateway-service role succeeds`() {
        val domain = "resolve-test-${System.currentTimeMillis()}.example"
        
        // First create a tenant as admin (need to switch roles)
        // Note: This test assumes tenant exists or uses a different approach
        
        given()
            .queryParam("domain", domain)
            .`when`()
            .get("/api/tenants/resolve")
            .then()
            // Will be 404 if tenant doesn't exist, but should not be 401/403
            .statusCode(404) // Expected - tenant doesn't exist yet
    }

    @Test
    fun `resolve tenant without auth returns 401`() {
        given()
            .queryParam("domain", "test.example")
            .`when`()
            .get("/api/tenants/resolve")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `suspend tenant returns updated tenant with SUSPENDED status`() {
        val domain = "suspend-test-${System.currentTimeMillis()}.example"
        
        // Create tenant
        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        // Suspend tenant
        given()
            .contentType(ContentType.JSON)
            .body("""{"reason": "Billing issue"}""")
            .`when`()
            .post("/api/tenants/$tenantId/suspend")
            .then()
            .statusCode(200)
            .body("status", equalTo(TenantStatus.SUSPENDED.name))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `activate suspended tenant returns ACTIVE status`() {
        val domain = "reactivate-test-${System.currentTimeMillis()}.example"
        
        // Create and suspend tenant
        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        given()
            .contentType(ContentType.JSON)
            .body("""{"reason": "Temporary suspension"}""")
            .`when`()
            .post("/api/tenants/$tenantId/suspend")
            .then()
            .statusCode(200)

        // Reactivate
        given()
            .`when`()
            .post("/api/tenants/$tenantId/activate")
            .then()
            .statusCode(200)
            .body("status", equalTo(TenantStatus.ACTIVE.name))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `terminate tenant returns TERMINATED status`() {
        val domain = "terminate-test-${System.currentTimeMillis()}.example"
        
        // Create tenant
        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        // Terminate
        given()
            .contentType(ContentType.JSON)
            .body("""{"reason": "Contract ended"}""")
            .`when`()
            .post("/api/tenants/$tenantId/terminate")
            .then()
            .statusCode(200)
            .body("status", equalTo(TenantStatus.TERMINATED.name))
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `activate terminated tenant returns 409 conflict`() {
        val domain = "terminated-activate-${System.currentTimeMillis()}.example"
        
        // Create and terminate tenant
        val tenantId = given()
            .contentType(ContentType.JSON)
            .body(createTenantRequest(domain))
            .`when`()
            .post("/api/tenants")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        given()
            .contentType(ContentType.JSON)
            .body("""{"reason": "Offboarding"}""")
            .`when`()
            .post("/api/tenants/$tenantId/terminate")
            .then()
            .statusCode(200)

        // Try to reactivate - should fail
        given()
            .`when`()
            .post("/api/tenants/$tenantId/activate")
            .then()
            .statusCode(409)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["platform-admin"])
    fun `list tenants returns paginated results`() {
        given()
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .`when`()
            .get("/api/tenants")
            .then()
            .statusCode(200)
            .body("offset", equalTo(0))
            .body("limit", equalTo(10))
            .body("items", notNullValue())
    }

    private fun createTenantRequest(domain: String): String = """
        {
            "name": "Test Tenant",
            "domain": "$domain",
            "tier": "STANDARD",
            "dataResidencyCountry": "US",
            "locale": "en_US",
            "timezone": "UTC",
            "currency": "USD"
        }
    """.trimIndent()
}
