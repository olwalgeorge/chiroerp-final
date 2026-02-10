package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class TenantAuditLoggerTest {
    @Test
    fun `buildLifecyclePayload emits structured json`() {
        val fixedTime = Instant.parse("2026-02-10T00:00:00Z")
        val logger = TenantAuditLogger(clock = Clock.fixed(fixedTime, ZoneOffset.UTC))
        val tenant = Tenant.rehydrate(
            id = TenantId.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            name = "Example",
            domain = "example.test",
            tier = TenantTier.STANDARD,
            status = TenantStatus.ACTIVE,
            dataResidency = DataResidency("US"),
            settings = TenantSettings(),
            createdAt = fixedTime,
            updatedAt = fixedTime,
        )

        val actor = TenantAuditActor(
            principalId = "admin@example.com",
            tenantId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            roles = setOf("platform-admin", "tenant-admin"),
        )

        val json = logger.buildLifecyclePayload(
            action = TenantLifecycleAction.SUSPEND,
            tenant = tenant,
            actor = actor,
            metadata = mapOf("reason" to "Manual suspension"),
        )

        assertThat(json).contains("\"action\":\"SUSPEND\"")
        assertThat(json).contains("\"tenantId\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\"")
        assertThat(json).contains("\"actorRoles\":[\"platform-admin\",\"tenant-admin\"]")
        assertThat(json).contains("\"reason\":\"Manual suspension\"")
        assertThat(json).contains("\"timestamp\":\"2026-02-10T00:00:00Z\"")
    }
}
