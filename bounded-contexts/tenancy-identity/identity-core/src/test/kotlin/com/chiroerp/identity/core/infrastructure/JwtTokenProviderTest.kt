package com.chiroerp.identity.core.infrastructure

import com.chiroerp.identity.core.infrastructure.security.JwtTokenProvider
import com.chiroerp.identity.core.infrastructure.security.JwtTokenRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class JwtTokenProviderTest {
    private val provider = JwtTokenProvider(
        objectMapper = ObjectMapper(),
        signingSecret = "test-signing-secret",
        issuer = "identity-core-test",
        defaultTtlSeconds = 900,
    )

    @Test
    fun `issue and parse returns expected claims`() {
        val now = Instant.parse("2026-02-12T10:00:00Z")
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val tenantId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333")

        val issued = provider.issue(
            JwtTokenRequest(
                subject = userId,
                tenantId = tenantId,
                sessionId = sessionId,
                tokenType = "ACCESS",
                roles = setOf("TENANT_ADMIN"),
                permissions = setOf("USER_ADMIN:READ"),
                ttl = Duration.ofMinutes(5),
            ),
            now = now,
        )

        val parsed = provider.parseAndValidate(issued.token, now = now.plusSeconds(10))

        assertThat(parsed).isNotNull
        assertThat(parsed!!.subject).isEqualTo(userId)
        assertThat(parsed.tenantId).isEqualTo(tenantId)
        assertThat(parsed.sessionId).isEqualTo(sessionId)
        assertThat(parsed.tokenType).isEqualTo("ACCESS")
        assertThat(parsed.roles).contains("TENANT_ADMIN")
        assertThat(parsed.permissions).contains("USER_ADMIN:READ")
    }

    @Test
    fun `parse rejects tampered token`() {
        val issued = provider.issue(
            JwtTokenRequest(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                tokenType = "ACCESS",
                ttl = Duration.ofMinutes(5),
            ),
        )

        val parts = issued.token.split('.')
        val tampered = "${parts[0]}.${parts[1]}.tampered-signature"

        val parsed = provider.parseAndValidate(tampered)

        assertThat(parsed).isNull()
    }

    @Test
    fun `parse rejects expired token`() {
        val now = Instant.parse("2026-02-12T10:00:00Z")
        val issued = provider.issue(
            JwtTokenRequest(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                tokenType = "ACCESS",
                ttl = Duration.ofSeconds(10),
            ),
            now = now,
        )

        val parsed = provider.parseAndValidate(issued.token, now = now.plusSeconds(11))

        assertThat(parsed).isNull()
    }
}
