package com.chiroerp.tenancy.shared.valueobjects

private const val PHONE_PATTERN = "^\\+?[0-9]{7,15}$"

data class PhoneNumber(val value: String) {
    init {
        require(value.matches(Regex(PHONE_PATTERN))) {
            "Phone number must be 7-15 digits and may start with '+'"
        }
    }

    override fun toString(): String = value
}
