package com.chiroerp.tenancy.shared.valueobjects

data class Email(val value: String) {
    init {
        require(value.contains('@')) { "Invalid email: $value" }
    }
}
