package com.chiroerp.tenancy.core.domain

import com.chiroerp.tenancy.core.domain.event.TenantActivatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantCreatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantSuspendedEvent
import com.chiroerp.tenancy.core.domain.event.TenantTerminatedEvent
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantTest {
    @Test
    fun `create normalizes domain and emits created event with PENDING status`() {
        val tenant = Tenant.create(
            id = TenantId(UUID.fromString("10000000-0000-0000-0000-000000000001")),
            name = " Acme Healthcare ",
            domain = "ACME-CLINIC.EXAMPLE",
            tier = TenantTier.STANDARD,
            dataResidency = DataResidency("US"),
            now = Instant.parse("2026-02-09T08:00:00Z"),
        )

        assertThat(tenant.name).isEqualTo("Acme Healthcare")
        assertThat(tenant.domain).isEqualTo("acme-clinic.example")
        assertThat(tenant.status).isEqualTo(TenantStatus.PENDING)

        val createdEvent = tenant.pullDomainEvents().single()
        assertThat(createdEvent).isInstanceOf(TenantCreatedEvent::class.java)
    }

    @Test
    fun `tenant can be activated from PENDING status`() {
        val tenant = tenant()
        tenant.pullDomainEvents()
        
        assertThat(tenant.status).isEqualTo(TenantStatus.PENDING)
        
        tenant.activate(Instant.parse("2026-02-09T08:00:00Z"))
        
        assertThat(tenant.status).isEqualTo(TenantStatus.ACTIVE)
        val events = tenant.pullDomainEvents()
        assertThat(events.filterIsInstance<TenantActivatedEvent>()).hasSize(1)
    }

    @Test
    fun `tenant cannot be re-activated after termination`() {
        val tenant = tenant()
        tenant.pullDomainEvents()
        tenant.activate() // Move to ACTIVE first
        tenant.pullDomainEvents()
        tenant.terminate("Contract ended", Instant.parse("2026-02-09T08:00:00Z"))
        tenant.pullDomainEvents()

        assertThatThrownBy {
            tenant.activate(Instant.parse("2026-02-09T09:00:00Z"))
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("cannot be activated")
    }

    @Test
    fun `suspend and terminate are idempotent once already applied`() {
        val tenant = tenant()
        tenant.pullDomainEvents()
        tenant.activate() // Move to ACTIVE first for suspend to work
        tenant.pullDomainEvents()

        tenant.suspend("Compliance review", Instant.parse("2026-02-09T08:00:00Z"))
        tenant.suspend("Repeated command", Instant.parse("2026-02-09T08:05:00Z"))
        tenant.terminate("Tenant offboarded", Instant.parse("2026-02-09T09:00:00Z"))
        tenant.terminate("Repeated terminate", Instant.parse("2026-02-09T09:05:00Z"))

        val events = tenant.pullDomainEvents()
        assertThat(events.filterIsInstance<TenantSuspendedEvent>()).hasSize(1)
        assertThat(events.filterIsInstance<TenantTerminatedEvent>()).hasSize(1)
    }

    private fun tenant(): Tenant = Tenant.create(
        id = TenantId(UUID.fromString("20000000-0000-0000-0000-000000000001")),
        name = "Test Tenant",
        domain = "tenant.example",
        tier = TenantTier.PREMIUM,
        dataResidency = DataResidency("US"),
        now = Instant.parse("2026-02-09T07:00:00Z"),
    )
}
