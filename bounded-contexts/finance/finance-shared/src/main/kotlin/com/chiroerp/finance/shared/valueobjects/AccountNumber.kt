package com.chiroerp.finance.shared.valueobjects

data class AccountNumber(val value: String) {
    init {
        require(value.matches(Regex("^[0-9A-Za-z-]{6,34}$"))) {
            "Account number format is invalid"
        }
    }
}
