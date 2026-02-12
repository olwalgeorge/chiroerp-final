package com.chiroerp.identity.core.infrastructure.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val JWT_ALG = "HS256"

data class JwtTokenRequest(
    val subject: UUID,
    val tenantId: UUID,
    val tokenType: String,
    val sessionId: UUID? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val ttl: Duration? = null,
    val additionalClaims: Map<String, String> = emptyMap(),
)

data class JwtClaims(
    val subject: UUID,
    val tenantId: UUID,
    val tokenType: String,
    val sessionId: UUID?,
    val roles: Set<String>,
    val permissions: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val additionalClaims: Map<String, String>,
)

data class JwtIssuedToken(
    val token: String,
    val claims: JwtClaims,
)

@ApplicationScoped
class JwtTokenProvider(
    private val objectMapper: ObjectMapper,
    @param:ConfigProperty(name = "chiroerp.identity.jwt.secret", defaultValue = "change-me-identity-jwt-secret")
    private val signingSecret: String,
    @param:ConfigProperty(name = "chiroerp.identity.jwt.issuer", defaultValue = "identity-core")
    private val issuer: String,
    @param:ConfigProperty(name = "chiroerp.identity.jwt.default-ttl-seconds", defaultValue = "900")
    private val defaultTtlSeconds: Long,
) {
    private val base64UrlEncoder = Base64.getUrlEncoder().withoutPadding()
    private val base64UrlDecoder = Base64.getUrlDecoder()

    fun issue(request: JwtTokenRequest, now: Instant = Instant.now()): JwtIssuedToken {
        val ttl = request.ttl ?: Duration.ofSeconds(defaultTtlSeconds)
        require(!ttl.isNegative && !ttl.isZero) { "JWT TTL must be positive" }

        val issuedAt = now
        val expiresAt = now.plus(ttl)

        val claims = JwtClaims(
            subject = request.subject,
            tenantId = request.tenantId,
            tokenType = request.tokenType.trim().ifEmpty { "ACCESS" }.uppercase(),
            sessionId = request.sessionId,
            roles = request.roles.map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            permissions = request.permissions.map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            additionalClaims = request.additionalClaims.filterKeys { it.isNotBlank() },
        )

        val headerJson = objectMapper.writeValueAsBytes(mapOf("alg" to JWT_ALG, "typ" to "JWT"))
        val payloadJson = objectMapper.writeValueAsBytes(
            mutableMapOf<String, Any>(
                "iss" to issuer,
                "sub" to claims.subject.toString(),
                "tid" to claims.tenantId.toString(),
                "typ" to claims.tokenType,
                "iat" to claims.issuedAt.epochSecond,
                "exp" to claims.expiresAt.epochSecond,
                "roles" to claims.roles.toList(),
                "permissions" to claims.permissions.toList(),
            ).apply {
                claims.sessionId?.let { put("sid", it.toString()) }
                putAll(claims.additionalClaims)
            },
        )

        val encodedHeader = base64UrlEncoder.encodeToString(headerJson)
        val encodedPayload = base64UrlEncoder.encodeToString(payloadJson)
        val signingInput = "$encodedHeader.$encodedPayload"
        val signature = sign(signingInput)

        return JwtIssuedToken(
            token = "$signingInput.$signature",
            claims = claims,
        )
    }

    fun parseAndValidate(token: String, now: Instant = Instant.now()): JwtClaims? {
        val parts = token.trim().split('.')
        if (parts.size != 3) {
            return null
        }

        val signingInput = "${parts[0]}.${parts[1]}"
        if (!isValidSignature(signingInput, parts[2])) {
            return null
        }

        val claimsRaw = decodeClaims(parts[1]) ?: return null
        if (claimsRaw["iss"]?.toString() != issuer) {
            return null
        }

        val issuedAt = asLong(claimsRaw["iat"]) ?: return null
        val expiresAt = asLong(claimsRaw["exp"]) ?: return null
        if (expiresAt <= now.epochSecond || issuedAt > expiresAt) {
            return null
        }

        val subject = runCatching { UUID.fromString(claimsRaw["sub"]?.toString()) }.getOrNull() ?: return null
        val tenantId = runCatching { UUID.fromString(claimsRaw["tid"]?.toString()) }.getOrNull() ?: return null
        val sessionId = claimsRaw["sid"]?.toString()?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        val tokenType = claimsRaw["typ"]?.toString()?.trim().orEmpty().ifEmpty { "ACCESS" }.uppercase()
        val roles = asStringSet(claimsRaw["roles"])
        val permissions = asStringSet(claimsRaw["permissions"])

        val reservedKeys = setOf("iss", "sub", "tid", "sid", "typ", "iat", "exp", "roles", "permissions")
        val additionalClaims = claimsRaw
            .filterKeys { it !in reservedKeys }
            .mapValues { (_, value) -> value?.toString() ?: "" }

        return JwtClaims(
            subject = subject,
            tenantId = tenantId,
            tokenType = tokenType,
            sessionId = sessionId,
            roles = roles,
            permissions = permissions,
            issuedAt = Instant.ofEpochSecond(issuedAt),
            expiresAt = Instant.ofEpochSecond(expiresAt),
            additionalClaims = additionalClaims,
        )
    }

    private fun sign(input: String): String {
        val keySpec = SecretKeySpec(signingSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(keySpec)
        return base64UrlEncoder.encodeToString(mac.doFinal(input.toByteArray(StandardCharsets.UTF_8)))
    }

    private fun isValidSignature(input: String, signature: String): Boolean {
        val expected = sign(input)
        return MessageDigest.isEqual(
            expected.toByteArray(StandardCharsets.UTF_8),
            signature.toByteArray(StandardCharsets.UTF_8),
        )
    }

    private fun decodeClaims(payload: String): Map<String, Any?>? = runCatching {
        val payloadBytes = base64UrlDecoder.decode(payload)
        objectMapper.readValue(payloadBytes, CLAIMS_TYPE)
    }.getOrNull()

    private fun asLong(value: Any?): Long? = when (value) {
        null -> null
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }

    private fun asStringSet(value: Any?): Set<String> {
        if (value !is Collection<*>) {
            return emptySet()
        }
        return value.mapNotNull { it?.toString()?.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    companion object {
        private val CLAIMS_TYPE = object : TypeReference<Map<String, Any?>>() {}
    }
}
