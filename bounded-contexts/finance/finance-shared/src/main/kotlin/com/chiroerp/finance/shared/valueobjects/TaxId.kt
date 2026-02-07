package com.chiroerp.finance.shared.valueobjects

data class TaxId(val value: String) {
    init {
        require(value.isNotBlank()) { "Tax ID cannot be blank" }
        require(value.length in 5..32) { "Tax ID length must be between 5 and 32" }
    }
}
