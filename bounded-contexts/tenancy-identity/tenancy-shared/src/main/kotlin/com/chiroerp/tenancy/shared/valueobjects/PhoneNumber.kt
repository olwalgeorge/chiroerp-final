package com.chiroerp.tenancy.shared.valueobjects

data class PhoneNumber(val value: String) {
    init {
        require(value.trim().length >= 7) { "Invalid phone number" }
    }
}
