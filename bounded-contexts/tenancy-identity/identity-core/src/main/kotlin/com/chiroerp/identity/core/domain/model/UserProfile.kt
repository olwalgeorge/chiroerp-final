package com.chiroerp.identity.core.domain.model

import java.time.ZoneId
import java.util.Locale

private val EMAIL_REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE)

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val locale: Locale,
    val timeZone: ZoneId,
) {
    init {
        require(firstName.isNotBlank()) { "First name is required" }
        require(lastName.isNotBlank()) { "Last name is required" }
        require(EMAIL_REGEX.matches(email.trim())) { "Invalid email address" }
    }

    val normalizedEmail: String = email.trim().lowercase(Locale.ROOT)

    val fullName: String = listOf(firstName.trim(), lastName.trim()).joinToString(" ").trim()

    fun updateContactDetails(
        newEmail: String = email,
        newPhoneNumber: String? = phoneNumber,
        newLocale: Locale = locale,
        newTimeZone: ZoneId = timeZone,
    ): UserProfile = copy(
        email = newEmail,
        phoneNumber = newPhoneNumber,
        locale = newLocale,
        timeZone = newTimeZone,
    )
}
