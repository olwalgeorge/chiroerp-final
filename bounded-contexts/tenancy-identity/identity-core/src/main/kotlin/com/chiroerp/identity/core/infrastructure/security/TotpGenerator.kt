package com.chiroerp.identity.core.infrastructure.security

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@ApplicationScoped
class TotpGenerator(
    @param:ConfigProperty(name = "chiroerp.identity.mfa.totp.digits", defaultValue = "6")
    private val digits: Int,
    @param:ConfigProperty(name = "chiroerp.identity.mfa.totp.time-step-seconds", defaultValue = "30")
    private val timeStepSeconds: Long,
    @param:ConfigProperty(name = "chiroerp.identity.mfa.totp.secret-bytes", defaultValue = "20")
    private val secretBytes: Int,
) {
    private val secureRandom = SecureRandom()

    init {
        require(digits in 6..8) { "TOTP digits must be between 6 and 8" }
        require(timeStepSeconds > 0) { "TOTP time step must be positive" }
        require(secretBytes >= 10) { "TOTP secret bytes must be at least 10" }
    }

    fun generateSecret(): String {
        val bytes = ByteArray(secretBytes)
        secureRandom.nextBytes(bytes)
        return encodeBase32(bytes)
    }

    fun generateCode(secret: String, at: Instant = Instant.now()): String {
        val key = decodeBase32(secret)
        val counter = at.epochSecond / timeStepSeconds

        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(data)

        val offset = hash.last().toInt() and 0x0F
        val binary =
            ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val otp = binary % pow10(digits)
        return otp.toString().padStart(digits, '0')
    }

    fun verifyCode(
        secret: String,
        code: String,
        at: Instant = Instant.now(),
        allowedSkewWindows: Int = 1,
    ): Boolean {
        val normalized = code.trim()
        if (normalized.length != digits || normalized.any { !it.isDigit() }) {
            return false
        }

        for (offset in -allowedSkewWindows..allowedSkewWindows) {
            val candidateInstant = at.plusSeconds(offset.toLong() * timeStepSeconds)
            if (generateCode(secret, candidateInstant) == normalized) {
                return true
            }
        }

        return false
    }

    fun buildOtpAuthUri(
        secret: String,
        accountName: String,
        issuer: String,
    ): String {
        val normalizedAccount = accountName.trim().ifEmpty { "user" }
        val normalizedIssuer = issuer.trim().ifEmpty { "ChiroERP" }

        val encodedIssuer = urlEncode(normalizedIssuer)
        val encodedAccount = urlEncode(normalizedAccount)

        return "otpauth://totp/$encodedIssuer:$encodedAccount" +
            "?secret=${secret.trim()}" +
            "&issuer=$encodedIssuer" +
            "&algorithm=SHA1" +
            "&digits=$digits" +
            "&period=$timeStepSeconds"
    }

    private fun pow10(power: Int): Int {
        var result = 1
        repeat(power) { result *= 10 }
        return result
    }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun encodeBase32(bytes: ByteArray): String {
        if (bytes.isEmpty()) {
            return ""
        }

        val output = StringBuilder((bytes.size * 8 + 4) / 5)
        var buffer = bytes[0].toInt() and 0xFF
        var next = 1
        var bitsLeft = 8

        while (bitsLeft > 0 || next < bytes.size) {
            if (bitsLeft < 5) {
                if (next < bytes.size) {
                    buffer = (buffer shl 8) or (bytes[next++].toInt() and 0xFF)
                    bitsLeft += 8
                } else {
                    val pad = 5 - bitsLeft
                    buffer = buffer shl pad
                    bitsLeft += pad
                }
            }

            val index = (buffer shr (bitsLeft - 5)) and 0x1F
            bitsLeft -= 5
            output.append(BASE32_ALPHABET[index])
        }

        return output.toString()
    }

    private fun decodeBase32(secret: String): ByteArray {
        val normalized = secret.uppercase()
            .replace("=", "")
            .replace(" ", "")

        require(normalized.isNotEmpty()) { "TOTP secret is required" }

        val output = ByteArray(normalized.length * 5 / 8)
        var buffer = 0
        var bitsLeft = 0
        var index = 0

        normalized.forEach { char ->
            val value = BASE32_LOOKUP[char.code]
            require(value != -1) { "Invalid Base32 character: '$char'" }

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                output[index++] = ((buffer shr (bitsLeft - 8)) and 0xFF).toByte()
                bitsLeft -= 8
            }
        }

        return output.copyOf(index)
    }

    companion object {
        private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

        private val BASE32_LOOKUP = IntArray(128) { -1 }.apply {
            BASE32_ALPHABET.forEachIndexed { index, char ->
                this[char.code] = index
            }
        }
    }
}
