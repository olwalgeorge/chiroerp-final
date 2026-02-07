package com.chiroerp.tenancy.shared.valueobjects

private const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"

data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.matches(Regex(EMAIL_PATTERN))) { "Invalid email format" }
    }

    override fun toString(): String = value
}
